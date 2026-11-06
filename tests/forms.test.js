// Edition: G
'use strict';
const fs=require('fs'),path=require('path'),vm=require('vm'),assert=require('assert');
const editorScript=fs.readFileSync(path.join(__dirname,'../app/src/main/assets/editor.js'),'utf8');
const scripts={prepare:fs.readFileSync(path.join(__dirname,'../app/src/main/assets/prepare.js'),'utf8'),submit:fs.readFileSync(path.join(__dirname,'../app/src/main/assets/submit.js'),'utf8')};let checks=0;
function fixture(opts={}){
 let clicked=0;const message={value:'',dispatchEvent(){}},subject={value:'',dispatchEvent(){}},mode={value:'0'},button={disabled:!!opts.disabled,click(){clicked++;}},login={querySelectorAll(){return [{textContent:opts.account||'测试昵称',getAttribute(){return opts.profile||'/viewpro.php?uid=991521';}}];}};
 const form={method:opts.method||'post',getAttribute(){return opts.action||'/post.php?action=newthread&fid=152&topicsubmit=yes';},querySelector(sel){if(sel==='textarea[name="message"]')return message;if(sel==='input[name="subject"]')return opts.noSubject?null:subject;if(sel==='input[name="formhash"]')return opts.noHash?null:{};if(sel.startsWith('input[name*="seccode"]'))return opts.captcha?{}:null;if(sel.startsWith('[name="topicsubmit"]'))return button;return null;},querySelectorAll(){return [];}};
 const context={URL,Event:class Event{},location:{href:opts.url||'https://bbs.my0511.com/post.php?action=newthread&fid=152'},document:{getElementById(id){return id==='postform'?(opts.noForm?null:form):id==='e_mode'?mode:null;},querySelector(sel){return sel==='#userlogin'?login:null;}},window:{}};
 return {context,message,subject,clicked:()=>clicked};
}
function run(script,f,t){if(script==='prepare')vm.runInNewContext(editorScript,f.context);return vm.runInNewContext(scripts[script]+'('+JSON.stringify(t)+')',f.context);}
function check(b,msg){checks++;assert(b,msg);}
const task={reply:false,tid:'',title:'标题',message:'中文\n正文 </script> " \\ \u2028',author:'测试昵称'};
let f=fixture();check(run('prepare',f,task).status==='ready','valid prepares');check(f.message.value===task.message,'exact body preserved');check(f.subject.value===task.title,'exact title preserved');check(f.clicked()===0,'prepare never submits');check(run('submit',f,task)===true,'valid submits');check(f.clicked()===1,'exactly one click');
for(const [opts,label] of [[{url:'https://bbs.my0511.com/post.php?action=newthread&fid=153'},'wrong board'],[{url:'https://evil.test/post.php?action=newthread&fid=152'},'wrong host'],[{account:'另一个账号'},'wrong account'],[{captcha:true},'captcha'],[{noHash:true},'no CSRF token'],[{url:'https://bbs.my0511.com/post.php?action=newthread&fid=152&fid=153'},'duplicate page target'],[{action:'/post.php?action=newthread&fid=152&fid=153'},'duplicate form target'],[{noForm:true},'no form'],[{action:'https://evil.test/post.php?action=newthread&fid=152'},'foreign action'],[{method:'get'},'GET form'],[{disabled:true},'disabled button'],[{noSubject:true},'no subject']]){f=fixture(opts);check(run('prepare',f,task).status==='wait',label);check(f.clicked()===0,label+' no click');}
f=fixture();run('prepare',f,task);f.message.value='被修改';check(run('submit',f,task)===false,'body mutation blocks submit');check(f.clicked()===0,'no click after mutation');
f=fixture();run('prepare',f,task);f.context.location.href='https://bbs.my0511.com/post.php?action=newthread&fid=153';check(run('submit',f,task)===false,'navigation mutation blocks submit');
const reply={...task,reply:true,tid:'12345',title:''};f=fixture({url:'https://bbs.my0511.com/post.php?action=reply&fid=152&tid=12345',action:'/post.php?action=reply&fid=152&tid=12345&replysubmit=yes'});check(run('prepare',f,reply).status==='ready','reply prepares');check(run('submit',f,reply)===true,'reply submits');
f=fixture({url:'https://bbs.my0511.com/post.php?action=reply&fid=152&tid=99999',action:'/post.php?action=reply&fid=152&tid=99999'});check(run('prepare',f,reply).status==='wait','wrong reply target blocked');
const bound={...task,uid:'991521'};f=fixture();check(run('prepare',f,bound).status==='ready','UID matches');check(run('submit',f,bound)===true,'UID checked at click');
for(const profile of ['/viewpro.php?uid=2','https://evil.test/viewpro.php?uid=991521','/viewpro.php?uid=991521&uid=2']){f=fixture({profile});check(run('prepare',f,bound).status==='wait','wrong UID profile rejects');check(run('submit',f,bound)===false,'wrong UID cannot click');}
f=fixture();check(run('prepare',f,{...bound,typeId:'99'}).status==='wait','absent category never fabricated');
console.log('PASS '+checks+' form checks; synthetic DOM only, no real submission');
