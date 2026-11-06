// Edition: G
(function(){
 if(location.origin!=='https://passport.my0511.com'||!document.getElementById('login-form'))return;
 var meta=document.querySelector('meta[name="viewport"]');if(!meta){meta=document.createElement('meta');meta.name='viewport';document.head.appendChild(meta);}meta.content='width=device-width,initial-scale=1';
 if(document.getElementById('lite-login-style'))return;
 var s=document.createElement('style');s.id='lite-login-style';s.textContent='html,body{min-width:0!important;width:100%!important;background:#f5f4ef!important} .login-2018-topbar,#login-left,.footcpr{display:none!important}.login-2018-bg{background:none!important}.login-2018-body{padding:16px 0!important}.login-2018-body-box{width:100%!important;height:auto!important}#login-module{position:static!important;width:auto!important;height:auto!important;padding:20px!important;box-sizing:border-box}#login-module:before{content:"论坛账号登录";display:block;font-size:22px;color:#182c29;margin-bottom:16px}.login-tab{display:none!important}#login-form{padding-top:8px!important}.fm-text,#login_btn{width:100%!important;box-sizing:border-box;font-size:18px!important;min-height:48px}#login_btn{background:#12756a!important;color:white!important}#login-error{box-sizing:border-box;text-align:left}#autoLoginDiv{position:static!important;width:auto!important;box-sizing:border-box}';document.head.appendChild(s);
})()
