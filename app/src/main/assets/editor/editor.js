'use strict';(() => {
  let muted=false, loaded=false, epoch=0, original='', baseline='';
  const send=(m)=>{if(window.AndroidNotes) window.AndroidNotes.postMessage(JSON.stringify(m));};
  // Keep image URLs, but never allow local files or active content.
  DOMPurify.addHook('uponSanitizeAttribute', (node, data) => {
    if (data.attrName === 'src' && (node.nodeName !== 'IMG' || !/^https:\/\//i.test(data.attrValue))) data.keepAttr = false;
  });
  const make=()=>{const e=new toastui.Editor({el:document.getElementById('editor'),height:'100%',initialEditType:'wysiwyg',initialValue:'',hideModeSwitch:true,autofocus:false,language:'fr-FR',usageStatistics:false,customHTMLSanitizer:(html)=>DOMPurify.sanitize(html,{USE_PROFILES:{html:true},FORBID_TAGS:['iframe','audio','video','style','form'],FORBID_ATTR:['style','srcset']}),toolbarItems:[['heading','bold','italic','strike'],['hr','quote'],['ul','ol','task'],['table','link'],['code','codeblock']],hooks:{addImageBlobHook:()=>false}});e.on('change',()=>{if(!muted&&loaded)send({type:'change',epoch,markdown:value()});});return e;};
  let editor=make();
  // Controls live outside ProseMirror so they cannot become part of the note.
  const copyLayer=document.createElement('div');
  copyLayer.className='notes-code-actions';document.body.appendChild(copyLayer);
  let copySequence=0;
  const copyRequests=new Map();
  const finishCopy=(id,ok)=>{
    const button=copyRequests.get(id);copyRequests.delete(id);
    if(button?.isConnected){button.textContent=ok?'Copié !':'Échec';setTimeout(()=>{button.textContent='Copier';},1600);}
  };
  const buttons=new Map();
  const updateCopyButtons=()=>{
    const blocks=new Set(document.querySelectorAll('.toastui-editor-ww-container .toastui-editor-ww-code-block'));
    for(const [block,button] of buttons){if(!blocks.has(block)){button.remove();buttons.delete(block);}}
    for(const block of blocks){
      let button=buttons.get(block);
      if(!button){
        button=document.createElement('button');button.type='button';button.textContent='Copier';
        button.className='notes-copy-code';button.setAttribute('aria-label','Copier le contenu du bloc de code');
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
  const value=()=>{const v=editor.getMarkdown();return v===baseline?original:v;};
  document.addEventListener('click',e=>{if(e.target.closest('a'))e.preventDefault();},true);
  document.addEventListener('drop',e=>{e.preventDefault();e.stopImmediatePropagation();},true);
  document.addEventListener('dragover',e=>e.preventDefault(),true);
  document.addEventListener('paste',e=>{e.preventDefault();e.stopImmediatePropagation();const t=e.clipboardData?.getData('text/plain');if(t)editor.insertText(t);},true);
  window.notesEditor=Object.freeze({copyResult:finishCopy,setDocument(markdown,revision){muted=true;loaded=false;editor.destroy();document.getElementById('editor').textContent='';editor=make();epoch=revision;original=markdown;editor.setMarkdown(markdown,false);baseline=editor.getMarkdown();loaded=true;muted=false;send({type:'loaded',epoch});return value();},snapshot(){return value();}});
  send({type:'ready'});
})();
