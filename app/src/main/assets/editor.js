// Edition: G
// Preserve the website's own validator, CSRF fields and submit handler.
(function(){
 'use strict';
 function norm(s){return String(s).replace(/\r\n?/g,'\n');}
 function rich(mode){return window.wysiwyg===1||window.wysiwyg===true||!!(mode&&mode.value==='1');}
 function readRich(){var frame=document.getElementById('e_iframe');if(!frame||!frame.contentDocument||!frame.contentDocument.body)throw new Error('missing editor');var doc=frame.contentDocument;if(window.editdoc!==doc||typeof window.getEditorContents!=='function'||typeof window.html2bbcode!=='function')throw new Error('unknown editor');if(window.getEditorContents()!==doc.body.innerHTML)throw new Error('editor mismatch');return {doc:doc,text:window.html2bbcode(window.getEditorContents())};}
 function sourceReady(ta,mode){if(typeof window.wysiwyg!=='undefined'&&(window.wysiwyg!==0||window.editdoc!==ta))return false;return !mode||mode.value!=='1';}
 function matches(ta,message){try{if(norm(ta.value)!==norm(message))return false;var mode=document.getElementById('e_mode');if(rich(mode))return norm(readRich().text)===norm(message);if(!sourceReady(ta,mode))return false;return typeof window.getEditorContents!=='function'||norm(window.getEditorContents())===norm(message);}catch(e){return false;}}
 function fill(ta,message){
  var mode=document.getElementById('e_mode');
  if(rich(mode)&&typeof window.switchEditor==='function'){
   // Some old editors throw in the final focus/caret step after switching succeeds.
   // Inspect the resulting editor state instead of treating every exception as failure.
   try{window.switchEditor(0);}catch(e){}
  }
  try{
   if(rich(mode)){
    var current=readRich(),body=current.doc.body;
    while(body.firstChild)body.removeChild(body.firstChild);
    norm(message).split('\n').forEach(function(line,index){if(index)body.appendChild(current.doc.createElement('br'));body.appendChild(current.doc.createTextNode(line));});
    // No raw HTML insertion; verify the official HTML-to-forum-text conversion.
    if(norm(window.html2bbcode(window.getEditorContents()))!==norm(message))return {ok:false,note:'富文本转换改变了正文，未提交；请使用源码模式或调整内容'};
   }else{
    if(typeof window.wysiwyg!=='undefined'&&(window.wysiwyg!==0||window.editdoc!==ta))return {ok:false,note:'编辑器初始化未完成，未提交；可稍后重新准备'};
    if(mode&&typeof window.wysiwyg!=='undefined')mode.value='0';
   }
   ta.value=message;ta.dispatchEvent(new Event('input',{bubbles:true}));ta.dispatchEvent(new Event('change',{bubbles:true}));
   return matches(ta,message)?{ok:true,note:'正文与编辑器同步已核对'}:{ok:false,note:'编辑器正文与待发内容不一致，未提交'};
  }catch(e){return {ok:false,note:'当前编辑器未能安全同步正文，未提交'};}
 }
 window.My0511GEditor={fill:fill,matches:matches};
})();
