const {chromium}=require('playwright');
const assert=require('node:assert/strict');
const path=require('node:path');
(async()=>{
 const browser=await chromium.launch({executablePath:process.env.TEST_BROWSER,headless:true,args:['--no-sandbox']});
 try {
  for(const width of [320,412]){
   const page=await browser.newPage({viewport:{width,height:600},isMobile:true,hasTouch:true});
   const errors=[];page.on('pageerror',e=>errors.push(e.message));
   await page.goto('file://'+path.resolve(__dirname,'../app/src/main/assets/editor/index.html'));
   for(let level=1;level<=6;level++){
    await page.evaluate(()=>notesEditor.setDocument('Titre test',1));
    await page.locator('.toastui-editor-ww-container p').tap();
    await page.locator('button.heading').tap();
    const menu=page.locator('.toastui-editor-popup');
    const rect=await menu.boundingBox();
    assert(rect && rect.x>=0 && rect.x+rect.width<=width,`Menu outside viewport: ${JSON.stringify(rect)}`);
    for(let n=1;n<=6;n++) assert(await menu.locator(`[data-level="${n}"]`).isVisible());
    if(width===412 && level===2) await page.screenshot({path:path.resolve(__dirname,'headings-verified.png')});
    await menu.locator(`[data-level="${level}"]`).tap();
    assert.equal(await page.evaluate(()=>notesEditor.snapshot()),'#'.repeat(level)+' Titre test');
    assert(await page.locator(`.toastui-editor-ww-container h${level}`).isVisible());
   }
   await page.locator('button.heading').tap();
   await page.locator('.toastui-editor-popup [data-type="Paragraph"]').tap();
   assert.equal(await page.evaluate(()=>notesEditor.snapshot()),'Titre test');
   assert.deepEqual(errors,[]);
   await page.close();
  }
  console.log('PASS: touch menu fully on-screen at 320/412px; H1-H6 and paragraph commands change visible content and Markdown.');
 } finally {await browser.close();}
})().catch(e=>{console.error(e);process.exitCode=1});
