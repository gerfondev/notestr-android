'use strict';(() => {
  let muted=false, loaded=false, epoch=0, original='', baseline='';
  const send=(m)=>{if(window.AndroidNotes) window.AndroidNotes.postMessage(JSON.stringify(m));};
  // Keep image URLs, but never allow local files or active content.
  DOMPurify.addHook('uponSanitizeAttribute', (node, data) => {
    if (data.attrName === 'src' && (node.nodeName !== 'IMG' || !/^https:\/\//i.test(data.attrValue))) data.keepAttr = false;
  });
  const make=()=>{const e=new toastui.Editor({el:document.getElementById('editor'),height:'100%',initialEditType:'wysiwyg',initialValue:'',hideModeSwitch:true,autofocus:false,language:'fr-FR',usageStatistics:false,customHTMLSanitizer:(html)=>DOMPurify.sanitize(html,{USE_PROFILES:{html:true},FORBID_TAGS:['iframe','audio','video','style','form'],FORBID_ATTR:['style','srcset']}),toolbarItems:[['heading','bold','italic','strike'],['hr','quote'],['ul','ol','task'],['table','link'],['code','codeblock']],hooks:{addImageBlobHook:()=>false}});e.on('change',()=>{if(!muted&&loaded)send({type:'change',epoch,markdown:value()});});return e;};
  let editor=make();
  const value=()=>{const v=editor.getMarkdown();return v===baseline?original:v;};
  document.addEventListener('click',e=>{if(e.target.closest('a'))e.preventDefault();},true);
  document.addEventListener('drop',e=>{e.preventDefault();e.stopImmediatePropagation();},true);
  document.addEventListener('dragover',e=>e.preventDefault(),true);
  document.addEventListener('paste',e=>{e.preventDefault();e.stopImmediatePropagation();const t=e.clipboardData?.getData('text/plain');if(t)editor.insertText(t);},true);
  window.notesEditor=Object.freeze({setDocument(markdown,revision){muted=true;loaded=false;editor.destroy();document.getElementById('editor').textContent='';editor=make();epoch=revision;original=markdown;editor.setMarkdown(markdown,false);baseline=editor.getMarkdown();loaded=true;muted=false;send({type:'loaded',epoch});return value();},snapshot(){return value();}});
  send({type:'ready'});
})();
