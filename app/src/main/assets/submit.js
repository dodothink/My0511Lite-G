// Edition: G
(function(t){
 'use strict';
 var u=new URL(location.href),q=u.searchParams;
 if(['fid','tid','action'].some(function(k){return q.getAll(k).length>1;}))return false;
 if(u.origin!=='https://bbs.my0511.com'||u.pathname!=='/post.php'||q.get('fid')!=='152'||q.get('action')!==(t.reply?'reply':'newthread')||(t.reply&&q.get('tid')!==t.tid))return false;
 var f=document.getElementById('postform');if(!f)return false;
 var a=new URL(f.getAttribute('action')||location.href,location.href);
 if(['fid','tid','action'].some(function(k){return a.searchParams.getAll(k).length>1;}))return false;
 if(a.origin!==u.origin||a.pathname!=='/post.php'||a.searchParams.get('fid')!=='152'||a.searchParams.get('action')!==q.get('action')||(t.reply&&a.searchParams.get('tid')!==t.tid)||f.method.toLowerCase()!=='post')return false;
 var login=document.querySelector('#userlogin');if(!login||!Array.from(login.querySelectorAll('a[href*="viewpro"]')).some(function(x){return x.textContent.trim()===t.author;}))return false;
 if(t.uid&&!Array.from(login.querySelectorAll('a[href*="viewpro"]')).some(function(x){try{var p=new URL(x.getAttribute('href'),location.href);return p.origin===u.origin&&p.pathname==='/viewpro.php'&&p.searchParams.getAll('uid').length===1&&p.searchParams.get('uid')===t.uid&&x.textContent.trim()===t.author;}catch(e){return false;}}))return false;
 if(f.querySelector('input[name*="seccode"],input[name*="secqaa"],iframe[src*="captcha"]'))return false;
 var ta=f.querySelector('textarea[name="message"]'),s=f.querySelector('input[name="subject"]');
 if(!ta||!window.My0511GEditor||!window.My0511GEditor.matches(ta,t.message)||(!t.reply&&(!s||s.value!==t.title)))return false;
 if(!t.reply&&t.typeId){var category=f.querySelector('select[name="typeid"]');if(!category||category.value!==t.typeId||!Array.from(category.options).some(function(o){return o.value===t.typeId&&!o.disabled;}))return false;}
 var b=f.querySelector('[name="topicsubmit"],[name="replysubmit"],#postsubmit');if(!b||b.disabled)return false;
 b.click();return true;
})
