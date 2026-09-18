const { chromium } = require('playwright');
const assert = require('node:assert/strict');
const path = require('node:path');
(async () => {
  const browser = await chromium.launch({executablePath: process.env.TEST_BROWSER, headless:true, args:['--no-sandbox']});
  try {
    const page = await browser.newPage({viewport:{width:412,height:800}});
    page.setDefaultTimeout(5000);
    const errors=[];
    page.on('pageerror', e=>errors.push(e.message));
    await page.addInitScript(()=>{ window.messages=[]; window.AndroidNotes={postMessage:s=>window.messages.push(JSON.parse(s))}; });
    await page.route('https://images.example.test/**', route=>route.fulfill({contentType:'image/svg+xml',body:'<svg xmlns="http://www.w3.org/2000/svg" width="160" height="90"><rect width="160" height="90" fill="teal"/></svg>'}));
    await page.goto('file://'+(process.env.EDITOR_PAGE || path.resolve(__dirname,'../app/src/main/assets/editor/index.html')));
    const markdown='# Image test\n\n![Photo](https://images.example.test/photo.png)\n\n**Texte en gras**';
    for(let revision=1;revision<=3;revision++) {
      await page.evaluate(({markdown,revision})=>window.notesEditor.setDocument(markdown,revision),{markdown,revision});
      const img=page.locator('.toastui-editor-ww-container img[alt=Photo]');
      await img.waitFor({state:'visible'});
      await page.waitForFunction(()=>[...document.querySelectorAll('.toastui-editor-ww-container img')].some(i=>i.naturalWidth>0), null, {timeout:5000});
      assert.equal(await page.evaluate(()=>window.notesEditor.snapshot()),markdown);
      assert(await page.locator('.toastui-editor-ww-container strong').isVisible());
    }
    // Editing must preserve the image in the Markdown sent to Android.
    await page.locator('.toastui-editor-ww-container [contenteditable=true]').click();
    await page.keyboard.press('Control+End');
    await page.keyboard.type(' modification');
    assert.match(await page.evaluate(()=>window.notesEditor.snapshot()), /!\[Photo\]\(https:\/\/images.example.test\/photo.png\)/);
    const sanitized=await page.evaluate(()=>DOMPurify.sanitize('<img src="file:///etc/passwd" onerror="alert(1)"><img src="https://images.example.test/photo.png">',{USE_PROFILES:{html:true}}));
    assert(!sanitized.includes('file:')); assert(!sanitized.includes('onerror')); assert(sanitized.includes('https:'));
    assert.deepEqual(errors,[]);
    assert(await page.evaluate(()=>window.messages.some(m=>m.type==='loaded'&&m.epoch===3)));
    await page.screenshot({path:path.resolve(__dirname,'editor-verified.png')});
    console.log('PASS: HTTPS image rendered, formatted text, repeated loading, exact Markdown round-trip, image preserved after editing, unsafe URL removed, bridge acknowledgement, no JavaScript errors.');
  } finally { await browser.close(); }
})().catch(e=>{console.error(e);process.exitCode=1});
