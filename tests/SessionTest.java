// Edition: G
package local.my0511.lite;
public final class SessionTest {
 static int n=0;static void ok(boolean b,String s){n++;if(!b)throw new AssertionError(s);}
 public static void main(String[] args){
  String base=ForumPolicy.BASE;
  ok(ForumPolicy.authenticationAllowed("https://www.my0511.com/"),"observed login portal return allowed");
  ok(ForumPolicy.authenticationAllowed("https://my0511.com/"),"portal alias allowed");
  ok(ForumPolicy.authenticationAllowed(base+"/index.php"),"forum home login landing allowed");
  ok(ForumPolicy.authenticationAllowed(base+"/logging.php?action=login"),"login callback allowed");
  ok(!ForumPolicy.webMainAllowed("https://www.my0511.com/"),"portal is auth context only");
  ok(!ForumPolicy.readAllowed("https://www.my0511.com/"),"native reader remains board only");
  ok(!ForumPolicy.authenticationAllowed(base+"/f153b-1"),"other board blocked even during auth");
  ok(!ForumPolicy.authenticationAllowed(base+"/post.php?action=newthread&fid=153"),"posting boundary unchanged");
  ok(!ForumPolicy.authenticationAllowed(base+"/goto.php?fid=153&tid=123"),"goto cannot expand board");
  for(String u:new String[]{"https://www.my0511.com.evil.test/","https://evil@www.my0511.com/","https://www.my0511.com:444/","file:///index.php","javascript:alert(1)"})ok(!ForumPolicy.authenticationAllowed(u),"foreign auth blocked: "+u);
  ok(ForumPolicy.secureAuthenticationUrl("http://www.my0511.com/").equals("https://www.my0511.com/"),"known http landing upgraded before request");
  ok(ForumPolicy.secureAuthenticationUrl("http://passport.my0511.com/lphysignin.php?goto=a%26b").endsWith("goto=a%26b"),"upgrade preserves encoded callback parameters");
  ok(ForumPolicy.secureAuthenticationUrl("http://evil.test/").isEmpty(),"no general http bypass");
  ok(ForumPolicy.secureAuthenticationUrl("http://bbs.my0511.com/f153b-1").isEmpty(),"upgrade cannot bypass board");
  ok(!ForumPolicy.safeAddress("https://passport.my0511.com/login.php?token=secret").contains("secret"),"diagnostics exclude query tokens");
  String logged="<ul id=userlogin><li><a href='/viewpro.php?uid=123'>测试昵称</a> <a href='https://passport.my0511.com/lphysignin.php?action=logout'>退出</a></li></ul>";
  SessionInfo s=SessionInfo.parseForum(logged);ok(s.state==SessionInfo.State.LOGGED_IN,"forum header establishes session");ok(s.nickname.equals("测试昵称")&&s.uid.equals("123"),"actual nickname and uid parsed");
  String anon="<ul id=userlogin><a href='https://passport.my0511.com/lphysignin.php?action=login'>登录</a></ul><div class=post-content><a href='/viewpro.php?uid=123'>帖子作者</a></div>";
  ok(SessionInfo.parseForum(anon).state==SessionInfo.State.LOGGED_OUT,"authors outside header not mistaken for account");
  ok(SessionInfo.parseForum("<html><title>503 Service Temporarily Unavailable</title></html>").state==SessionInfo.State.UNKNOWN,"error HTML cannot establish login or logout");
  ok(SessionInfo.parseForum("<title>安全验证</title>").state==SessionInfo.State.CHALLENGE,"challenge is distinct state");
  ok(SessionInfo.parseForum("<ul id=userlogin><a href='https://evil.test/viewpro.php?uid=123'>假身份</a></ul>").state==SessionInfo.State.UNKNOWN,"external profile cannot establish identity");
  String portal="<div id=usrloginfo>镇江网友之家通行证： 示例账户 <a href='https://passport.my0511.com/lphysignin.php?action=logout'>[退出]</a><a href='https://bbs.my0511.com/'>进入论坛</a></div>";
  ok(SessionInfo.parsePortalName(portal).equals("示例账户"),"observed portal header shape parsed");
  ok(SessionInfo.parsePortalName("<div id=usrloginfo>镇江网友之家通行证： 示例账户</div>").isEmpty(),"text alone without signed-in controls not accepted");
  ok(SessionInfo.parsePortalName("<div>欢迎您回来，示例账户</div>").isEmpty(),"message text not confused with forum login proof");
  ok(SessionInfo.parsePortalName("<div id=usrloginfo>镇江网友之家通行证： 示例账户<a href='https://evil.test/logout'>退出</a></div>").isEmpty(),"foreign logout not accepted");
  System.out.println("PASS "+n+" login redirect / session identity checks; no passwords or real posting.");
 }
}
