// Edition: G
(function(t){
 'use strict';
 function result(status,note){return {status:status,note:note};}
 var u=new URL(location.href),q=u.searchParams;
 if(['fid','tid','action'].some(function(k){return q.getAll(k).length>1;}))return result('wait','页面参数重复，停止提交');
 if(u.origin!=='https://bbs.my0511.com'||u.pathname!=='/post.php'||q.get('fid')!=='152'||q.get('action')!==(t.reply?'reply':'newthread')||(t.reply&&q.get('tid')!==t.tid))return result('wait','当前不是预期的百姓话题发帖页');
 var login=document.querySelector('#userlogin');
 var accounts=login?Array.from(login.querySelectorAll('a[href*="viewpro"]')).map(function(a){return a.textContent.trim();}):[];
 if(accounts.indexOf(t.author)<0)return result('wait','没有确认当前登录昵称与设置一致；请登录后核对昵称');
 if(t.uid&&(!login||!Array.from(login.querySelectorAll('a[href*="viewpro"]')).some(function(a){try{var p=new URL(a.getAttribute('href'),location.href);return p.origin===u.origin&&p.pathname==='/viewpro.php'&&p.searchParams.getAll('uid').length===1&&p.searchParams.get('uid')===t.uid&&a.textContent.trim()===t.author;}catch(e){return false;}})))return result('wait','实际账户UID不一致');
 var f=document.getElementById('postform');
 if(!f||!f.querySelector('textarea[name="message"]'))return result('wait','未找到标准发帖表单，请先登录或处理验证');
 var action=new URL(f.getAttribute('action')||location.href,location.href);
 if(['fid','tid','action'].some(function(k){return action.searchParams.getAll(k).length>1;}))return result('wait','表单参数重复，停止提交');
 if(action.origin!==u.origin||action.pathname!=='/post.php'||action.searchParams.get('fid')!=='152'||action.searchParams.get('action')!==q.get('action')||(t.reply&&action.searchParams.get('tid')!==t.tid)||f.method.toLowerCase()!=='post')return result('wait','表单目标或提交方式与预期不符');
 if(f.querySelector('input[name*="seccode"],input[name*="secqaa"],iframe[src*="captcha"]'))return result('wait','检测到验证码，自动提交已暂停');
 if(!f.querySelector('input[name="formhash"]'))return result('wait','缺少论坛校验令牌');
 var ta=f.querySelector('textarea[name="message"]');
 if(!window.My0511GEditor)return result('wait','编辑器适配器未加载，未提交');
 var synced=window.My0511GEditor.fill(ta,t.message);if(!synced.ok)return result('wait',synced.note);
 if(!t.reply){var subject=f.querySelector('input[name="subject"]');if(!subject)return result('wait','没有找到标题字段');subject.value=t.title;subject.dispatchEvent(new Event('input',{bubbles:true}));}
 if(f.querySelector('input[type="file"]')&&Array.from(f.querySelectorAll('input[type="file"]')).some(function(x){return x.files&&x.files.length;}))return result('wait','表单已有附件，第一版仅自动发布纯文本');
 if(!t.reply&&t.typeId){var category=f.querySelector('select[name="typeid"]');if(!category||!Array.from(category.options).some(function(o){return o.value===t.typeId&&!o.disabled;}))return result('wait','分类编号不是官方表单可用选项');category.value=t.typeId;category.dispatchEvent(new Event('change',{bubbles:true}));}
 var button=f.querySelector('[name="topicsubmit"],[name="replysubmit"],#postsubmit');if(!button||button.disabled)return result('wait','没有可用的发表按钮');
 return result('ready','表单已准备；等待提交意图落盘');
})
