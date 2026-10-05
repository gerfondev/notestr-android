const fs=require('node:fs');const vm=require('node:vm');const assert=require('node:assert/strict');
for(const file of process.argv.slice(2)){
 const source=fs.readFileSync(file,'utf8');
 const match=source.match(/document\.addEventListener\('paste',[\s\S]*?\},\s*true\);/);assert(match,'paste listener');
 let handler;const inserted=[];
 class Element {constructor(tag){this.tag=tag;}closest(){return ['input','textarea'].includes(this.tag)?this:null;}}
 vm.runInNewContext(match[0],{readOnly:false,Element,document:{addEventListener:(name,fn)=>handler=fn},editor:{insertText:text=>inserted.push(text)}});
 function paste(tag,text){const event={target:new Element(tag),prevented:false,stopped:false,preventDefault(){this.prevented=true;},stopImmediatePropagation(){this.stopped=true;},clipboardData:{getData:type=>type==='text/plain'?text:'<b>untrusted</b>'}};handler(event);return event;}
 for(const tag of ['input','textarea']){const e=paste(tag,'https://example.org/path?q=1&lang=fr');assert.equal(e.prevented,false);assert.equal(e.stopped,false);assert.deepEqual(inserted,[]);}
 const e=paste('div','Texte <b>brut</b>');assert(e.prevented&&e.stopped);assert.deepEqual(inserted,['Texte <b>brut</b>']);
 paste('div','');assert.equal(inserted.length,1);
 console.log('PASS',file,': dialog fields keep native paste; note uses plain text; empty paste leaves note intact');
}
