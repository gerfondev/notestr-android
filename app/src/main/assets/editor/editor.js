'use strict';(() => {
  let uiLanguage='fr';
  const label=(fr,en)=>uiLanguage==='en'?en:fr;
  let readOnly=false;
  let muted=false, loaded=false, epoch=0, original='', baseline='';
  const embeddedAssets=new Map(), imageSources=new Map(), requestedAssets=new Set();
  const send=(m)=>{if(window.AndroidNotes) window.AndroidNotes.postMessage(JSON.stringify(m));};
  // Only app-generated encrypted-image references and in-memory JPEGs may be image sources.
  DOMPurify.addHook('uponSanitizeAttribute', (node, data) => {
    if (data.attrName !== 'src') return;
    let custom=false;
    try { const u=new URL(data.attrValue); custom=node.nodeName==='IMG' && u.protocol==='notestr-image:' && /^[0-9a-f]{64}$/.test(u.hostname) && /^https:\/\/[a-z0-9.-]+(?::[0-9]+)?$/i.test(u.searchParams.get('server')||'') && /^[A-Za-z0-9_-]+$/.test(u.searchParams.get('key')||'') && /^[A-Za-z0-9_-]+$/.test(u.searchParams.get('nonce')||'') && u.searchParams.get('mime')==='image/jpeg'; } catch (_) {}
    const remote=node.nodeName==='IMG' && /^https:\/\//i.test(data.attrValue);
    const inline=node.nodeName==='IMG' && /^data:image\/(?:jpeg|png);base64,[A-Za-z0-9+/=]+(?:#notestr-[a-z0-9]+)?$/i.test(data.attrValue);
    data.keepAttr=custom||remote||inline;
  });
  const validImageReference=(reference)=>{try{const u=new URL(reference);return u.protocol==='notestr-image:'&&/^[0-9a-f]{64}$/.test(u.hostname)&&/^https:\/\/[a-z0-9.-]+(?::[0-9]+)?$/i.test(u.searchParams.get('server')||'')&&u.searchParams.getAll('server').length===1&&/^[A-Za-z0-9_-]{43}$/.test(u.searchParams.get('key')||'')&&u.searchParams.getAll('key').length===1&&/^[A-Za-z0-9_-]{16}$/.test(u.searchParams.get('nonce')||'')&&u.searchParams.getAll('nonce').length===1&&u.searchParams.get('mime')==='image/jpeg'&&u.searchParams.getAll('mime').length===1;}catch(_){return false;}};
  const placeholder='data:image/png;base64,iVBORw0KGgoAAAANSUhEUgAAAAEAAAABCAQAAAC1HAwCAAAAC0lEQVR42mP8/x8AAwMCAO+/xioAAAAASUVORK5CYII=';
  const resolveImages=()=>{for(const img of document.querySelectorAll('#editor img')){const reference=imageSources.get(img.getAttribute('src'));if(reference&&!requestedAssets.has(reference)){requestedAssets.add(reference);send({type:'loadImage',reference,epoch});}}};
  new MutationObserver(resolveImages).observe(document.getElementById('editor'),{childList:true,subtree:true});
  const make=()=>{const e=toastui.Editor.factory({viewer:readOnly,el:document.getElementById('editor'),height:'100%',initialEditType:'wysiwyg',initialValue:'',hideModeSwitch:true,autofocus:false,language:uiLanguage==='en'?'en-US':'fr-FR',usageStatistics:false,customHTMLSanitizer:(html)=>DOMPurify.sanitize(html,{USE_PROFILES:{html:true},FORBID_TAGS:['iframe','audio','video','style','form'],FORBID_ATTR:['style','srcset']}),toolbarItems:[['heading','bold','italic','strike'],['hr','quote'],['ul','ol','task'],['table','link'],['code','codeblock']],hooks:{addImageBlobHook:()=>false}});e.on('change',()=>{if(!readOnly&&!muted&&loaded)send({type:'change',epoch,markdown:value()});});return e;};
  let editor=make();
  // Controls live outside ProseMirror so they cannot become part of the note.
  const copyLayer=document.createElement('div');
  copyLayer.className='notes-code-actions';document.body.appendChild(copyLayer);
  let copySequence=0;
  const copyRequests=new Map();
  const finishCopy=(id,ok)=>{
    const button=copyRequests.get(id);copyRequests.delete(id);
    if(button?.isConnected){button.textContent=ok?label('Copié !','Copied!'):label('Échec','Failed');setTimeout(()=>{button.textContent=label('Copier','Copy');},1600);}
  };
  const buttons=new Map();
  const updateCopyButtons=()=>{
    const blocks=new Set(document.querySelectorAll('.toastui-editor-ww-container .toastui-editor-ww-code-block'));
    for(const [block,button] of buttons){if(!blocks.has(block)){button.remove();buttons.delete(block);}}
    for(const block of blocks){
      let button=buttons.get(block);
      if(!button){
        button=document.createElement('button');button.type='button';button.textContent=label('Copier','Copy');
        button.className='notes-copy-code';button.setAttribute('aria-label',label('Copier le contenu du bloc de code','Copy code block contents'));
        button.addEventListener('pointerdown',e=>e.preventDefault());
        button.addEventListener('click',async()=>{
          const text=Array.from(block.children).map(line=>line.textContent).join('\n');
          const id=++copySequence;copyRequests.set(id,button);
          if(window.AndroidNotes){send({type:'copyCode',epoch,id,text});setTimeout(()=>copyRequests.delete(id),10000);}
          else {try{await navigator.clipboard.writeText(text);finishCopy(id,true);}catch(_){finishCopy(id,false);}}
        });
        copyLayer.appendChild(button);buttons.set(block,button);
      }
      const rect=block.getBoundingClientRect(),container=block.closest('.toastui-editor-ww-container').getBoundingClientRect();
      const top=rect.top+6,right=Math.min(rect.right,container.right,innerWidth)-8;
      button.hidden=top<Math.max(container.top,0)||top+40>Math.min(container.bottom,innerHeight)||right<80;
      button.style.top=top+'px';button.style.left=(right-76)+'px';
    }
  };
  let copyFrame=0;
  const scheduleCopyButtons=()=>{if(!copyFrame)copyFrame=requestAnimationFrame(()=>{copyFrame=0;updateCopyButtons();});};
  new MutationObserver(scheduleCopyButtons).observe(document.getElementById('editor'),{subtree:true,childList:true,characterData:true});
  new ResizeObserver(scheduleCopyButtons).observe(document.getElementById('editor'));
  document.addEventListener('scroll',scheduleCopyButtons,true);window.addEventListener('resize',scheduleCopyButtons);
  const restoreAssets=(value)=>{for(const [dataUrl,reference] of embeddedAssets)value=value.replaceAll(dataUrl,reference);return value;};
  const explicitParagraphBreaks=(markdown)=>{const doc=editor.wwEditor?.view?.state?.doc;if(!doc||doc.childCount<2)return markdown;let paragraphs=true;doc.forEach(node=>{if(node.type.name!=='paragraph')paragraphs=false;});const lines=markdown.split('\n');return paragraphs&&lines.length===doc.childCount?lines.join('  \n'):markdown;};
  const value=()=>{if(readOnly)return original;const raw=restoreAssets(editor.getMarkdown());if(raw===baseline)return original;return explicitParagraphBreaks(raw);};
  let imageLightbox=null;
  const closeImageLightbox=()=>{if(!imageLightbox)return;imageLightbox.remove();imageLightbox=null;document.removeEventListener('keydown',onLightboxKeydown,true);send({type:'imageViewer',open:false,epoch});};
  const onLightboxKeydown=e=>{if(!imageLightbox)return;if(e.key==='Escape'){e.preventDefault();e.stopPropagation();closeImageLightbox();return;}const image=imageLightbox.querySelector('img');if(!image)return;const value=Number((image.style.transform.match(/scale\(([^)]+)\)/)||[])[1])||1;if(e.key==='+'||e.key==='='){e.preventDefault();imageLightbox.querySelector('.notes-image-lightbox-zoom-in')?.click();}else if(e.key==='-'){e.preventDefault();imageLightbox.querySelector('.notes-image-lightbox-zoom-out')?.click();}else if(e.key==='Home'){e.preventDefault();imageLightbox.querySelector('.notes-image-lightbox-zoom-reset')?.click();}else if(value>1&&['ArrowLeft','ArrowRight','ArrowUp','ArrowDown'].includes(e.key)){e.preventDefault();const match=image.style.transform.match(/translate3d\(([-\d.]+)px,\s*([-\d.]+)px/),maxX=Math.max(0,(image.clientWidth*value-(imageLightbox.clientWidth-48))/2),maxY=Math.max(0,(image.clientHeight*value-(imageLightbox.clientHeight-48))/2),x=parseFloat(match?.[1]||'0'),y=parseFloat(match?.[2]||'0'),d=e.shiftKey?120:40;image.style.transform=`translate3d(${Math.max(-maxX,Math.min(maxX,x+(e.key==='ArrowRight'?d:e.key==='ArrowLeft'?-d:0)))}px,${Math.max(-maxY,Math.min(maxY,y+(e.key==='ArrowDown'?d:e.key==='ArrowUp'?-d:0)))}px,0) scale(${value})`;}};
  const showImageLightbox=image=>{
    const src=image.getAttribute('src')||'';
    if(!/^data:image\/(?:jpeg|png);base64,[A-Za-z0-9+/=]+(?:#notestr-[a-z0-9]+)?$/i.test(src))return false;
    closeImageLightbox();
    const overlay=document.createElement('div');overlay.className='notes-image-lightbox';overlay.setAttribute('role','dialog');overlay.setAttribute('aria-modal','true');overlay.setAttribute('aria-label',label('Image en plein écran','Full-screen image'));
    const enlarged=document.createElement('img');enlarged.alt=image.alt||label('Image agrandie','Enlarged image');enlarged.src=src.split('#')[0];
    let zoom=1,panX=0,panY=0,gesture=null,moved=false,suppressBackdropClick=false;const pointers=new Map();
    const clampZoom=value=>Math.max(1,Math.min(8,value));
    const clampPan=()=>{const width=Math.max(0,overlay.clientWidth-48),height=Math.max(0,overlay.clientHeight-48);panX=Math.max(-Math.max(0,(enlarged.clientWidth*zoom-width)/2),Math.min(Math.max(0,(enlarged.clientWidth*zoom-width)/2),panX));panY=Math.max(-Math.max(0,(enlarged.clientHeight*zoom-height)/2),Math.min(Math.max(0,(enlarged.clientHeight*zoom-height)/2),panY));};
    const render=()=>{clampPan();enlarged.style.transform=`translate3d(${panX}px,${panY}px,0) scale(${zoom})`;level.textContent=Math.round(zoom*100)+'%';out.disabled=zoom<=1;};
    const zoomAt=(next,x=overlay.getBoundingClientRect().left+overlay.clientWidth/2,y=overlay.getBoundingClientRect().top+overlay.clientHeight/2)=>{const rect=overlay.getBoundingClientRect(),px=x-rect.left-overlay.clientWidth/2,py=y-rect.top-overlay.clientHeight/2,worldX=(px-panX)/zoom,worldY=(py-panY)/zoom;zoom=clampZoom(next);panX=px-worldX*zoom;panY=py-worldY*zoom;render();};
    const button=(cls,text,aria)=>{const b=document.createElement('button');b.type='button';b.className=cls;b.textContent=text;b.setAttribute('aria-label',aria);return b;};
    const controls=document.createElement('div');controls.className='notes-image-lightbox-controls';
    const out=button('notes-image-lightbox-zoom-out','−',label('Dézoomer','Zoom out'));
    const level=document.createElement('span');level.className='notes-image-lightbox-zoom-level';level.setAttribute('aria-live','polite');level.textContent='100%';
    const reset=button('notes-image-lightbox-zoom-reset','↺',label('Réinitialiser le zoom','Reset zoom'));
    const into=button('notes-image-lightbox-zoom-in','+',label('Zoomer','Zoom in'));
    out.addEventListener('click',()=>zoomAt(zoom/1.5));into.addEventListener('click',()=>zoomAt(zoom*1.5));reset.addEventListener('click',()=>{zoom=1;panX=0;panY=0;render();});controls.append(out,level,reset,into);
    const close=document.createElement('button');close.type='button';close.className='notes-image-lightbox-close';close.setAttribute('aria-label',label('Fermer l’image','Close image'));close.textContent='×';
    overlay.append(enlarged,controls,close);
    overlay.addEventListener('pointerdown',e=>{if(e.target.closest('button'))return;pointers.set(e.pointerId,{x:e.clientX,y:e.clientY});try{overlay.setPointerCapture(e.pointerId);}catch(_){}if(pointers.size>=2){const p=[...pointers.values()].slice(0,2),mx=(p[0].x+p[1].x)/2,my=(p[0].y+p[1].y)/2,r=overlay.getBoundingClientRect();gesture={pinch:true,distance:Math.hypot(p[0].x-p[1].x,p[0].y-p[1].y)||1,zoom,x:mx,y:my,worldX:(mx-r.left-overlay.clientWidth/2-panX)/zoom,worldY:(my-r.top-overlay.clientHeight/2-panY)/zoom};}else gesture={pinch:false,x:e.clientX,y:e.clientY,panX,panY};moved=false;if(zoom>1)e.preventDefault();});
    overlay.addEventListener('pointermove',e=>{if(!pointers.has(e.pointerId))return;pointers.set(e.pointerId,{x:e.clientX,y:e.clientY});if(pointers.size>=2&&gesture?.pinch){const p=[...pointers.values()].slice(0,2),mx=(p[0].x+p[1].x)/2,my=(p[0].y+p[1].y)/2,r=overlay.getBoundingClientRect();zoom=clampZoom(gesture.zoom*(Math.hypot(p[0].x-p[1].x,p[0].y-p[1].y)/gesture.distance));panX=mx-r.left-overlay.clientWidth/2-gesture.worldX*zoom;panY=my-r.top-overlay.clientHeight/2-gesture.worldY*zoom;moved=true;render();e.preventDefault();}else if(gesture&&!gesture.pinch){const dx=e.clientX-gesture.x,dy=e.clientY-gesture.y;if(Math.abs(dx)+Math.abs(dy)>3)moved=true;if(zoom>1){panX=gesture.panX+dx;panY=gesture.panY+dy;render();e.preventDefault();}}});
    const endPointer=e=>{if(!pointers.has(e.pointerId))return;pointers.delete(e.pointerId);if(pointers.size===1){const p=[...pointers.values()][0];gesture={pinch:false,x:p.x,y:p.y,panX,panY};}else gesture=null;if(moved){suppressBackdropClick=true;setTimeout(()=>{suppressBackdropClick=false;},350);}};
    overlay.addEventListener('pointerup',endPointer);overlay.addEventListener('pointercancel',endPointer);
    overlay.addEventListener('wheel',e=>{e.preventDefault();zoomAt(zoom*(e.deltaY<0?1.15:1/1.15),e.clientX,e.clientY);},{passive:false});
    overlay.addEventListener('dblclick',e=>{e.preventDefault();zoomAt(zoom>1?1:2,e.clientX,e.clientY);});
    overlay.addEventListener('click',e=>{if(e.target===overlay&&!suppressBackdropClick)closeImageLightbox();});
    close.addEventListener('click',e=>{e.preventDefault();e.stopPropagation();closeImageLightbox();});
    document.body.appendChild(overlay);imageLightbox=overlay;document.addEventListener('keydown',onLightboxKeydown,true);close.focus();send({type:'imageViewer',open:true,epoch});return true;
  };
  document.addEventListener('click',e=>{
    if(!(e.target instanceof Element))return;
    const image=e.target.closest('.toastui-editor-contents img');if(image){if(showImageLightbox(image)){e.preventDefault();e.stopImmediatePropagation();}return;}
    const link=e.target.closest('a');if(!link)return;
    e.preventDefault();
    const url=link.getAttribute('href') || '';
    if(e.isTrusted && loaded && /^https?:\/\//i.test(url))
      send({type:'openLink',epoch,url,markdown:value()});
  },true);
  document.addEventListener('drop',e=>{e.preventDefault();e.stopImmediatePropagation();},true);
  document.addEventListener('dragover',e=>e.preventDefault(),true);
  document.addEventListener('mousedown',e=>{if(readOnly)e.stopImmediatePropagation();},true);
  document.addEventListener('paste',e=>{if(readOnly){e.preventDefault();e.stopImmediatePropagation();return;}if(e.target instanceof Element && e.target.closest('input, textarea'))return;e.preventDefault();e.stopImmediatePropagation();const t=e.clipboardData?.getData('text/plain');if(t)editor.insertText(t);},true);
  let insertionSelection=null;
  window.notesEditor=Object.freeze({copyResult:finishCopy,setDocument(markdown,revision,language='fr',readonly=false){closeImageLightbox();readOnly=readonly===true;document.body.classList.toggle('notes-readonly',readOnly);uiLanguage=language==='en'?'en':'fr';document.documentElement.lang=uiLanguage;muted=true;loaded=false;editor.destroy();document.getElementById('editor').textContent='';editor=make();epoch=revision;original=markdown;embeddedAssets.clear();imageSources.clear();requestedAssets.clear();insertionSelection=null;let serial=0;const displayMarkdown=markdown.replace(/notestr-image:\/\/[^\s)>'"]+/g,reference=>{if(!validImageReference(reference))return reference;const preview=placeholder+'#notestr-'+(serial++).toString(36);imageSources.set(preview,reference);embeddedAssets.set(preview,reference);return preview;});editor.setMarkdown(displayMarkdown,false);baseline=restoreAssets(editor.getMarkdown());loaded=true;muted=false;send({type:'loaded',epoch});resolveImages();return value();},rememberInsertionPoint(){try{insertionSelection=editor.getSelection();return Array.isArray(insertionSelection)&&insertionSelection.length===2;}catch(_){insertionSelection=null;return false;}},insertImageReference(reference){const markdown=typeof reference==='string'&&reference.match(/^!\[[^\]]*\]\((notestr-image:\/\/[^)\s]+)\)$/);if(markdown)reference=markdown[1];if(!validImageReference(reference))return false;try{if(insertionSelection)editor.setSelection(...insertionSelection);const preview=placeholder+'#notestr-insert-'+Math.random().toString(36).slice(2);imageSources.set(preview,reference);embeddedAssets.set(preview,reference);editor.exec('addImage',{imageUrl:preview,altText:'Image'});insertionSelection=null;return true;}catch(_){return false;}},resolveImages(reset=false){if(reset)requestedAssets.clear();resolveImages();},imageResult(reference,base64){if(typeof reference!=='string'||typeof base64!=='string'||base64.length>28*1024*1024)return false;let found=false;for(const [preview,originalReference] of imageSources){if(originalReference!==reference)continue;const marker=preview.slice(preview.indexOf('#notestr-'));const dataUrl='data:image/jpeg;base64,'+base64+marker;embeddedAssets.set(dataUrl,reference);for(const img of document.querySelectorAll('#editor img'))if(img.getAttribute('src')===preview)img.setAttribute('src',dataUrl);imageSources.delete(preview);found=true;}return found;},closeImageLightbox,snapshot(){return value();}});
  send({type:'ready'});
})();
