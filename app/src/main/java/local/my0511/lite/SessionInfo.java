// Edition: G
package local.my0511.lite;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/** Account identity is established from a trusted account header, never a cookie name or configured nickname. */
public final class SessionInfo {
 public enum State { UNKNOWN, CHECKING, LOGGED_IN, LOGGED_OUT, CHALLENGE, ERROR }
 public final State state;
 public final String nickname, uid, detail;
 public SessionInfo(State state,String nickname,String uid,String detail){this.state=state;this.nickname=nickname;this.uid=uid;this.detail=detail;}
 public static SessionInfo unknown(){return new SessionInfo(State.UNKNOWN,"","","尚未检查论坛登录状态");}
 public static SessionInfo parseForum(String html){
  Document d=Jsoup.parse(html,ForumPolicy.BASE);
  Element header=d.selectFirst("#userlogin");
  if(header!=null){
   for(Element a:header.select("a[href]")){
    String href=a.absUrl("href");
    if(!ForumPolicy.forum(href)||!"/viewpro.php".equals(ForumPolicy.uri(href).getPath()))continue;
    Map<String,String> q=ForumPolicy.query(href);String name=a.text().trim();
    if(!q.containsKey("!invalid")&&(ForumPolicy.digits(q.get("uid"))||q.containsKey("username"))&&!name.isEmpty()&&name.length()<=80&&!name.equals("个人资料")&&!name.equals("我的资料"))
     return new SessionInfo(State.LOGGED_IN,name,q.getOrDefault("uid",""),"论坛账户栏已确认");
   }
   for(Element a:header.select("a[href]")){
    String href=a.absUrl("href");
    if((ForumPolicy.authenticationAllowed(href))&&("登录".equals(a.text().trim())||href.contains("action=login")))
     return new SessionInfo(State.LOGGED_OUT,"","","论坛页面显示尚未登录");
   }
  }
  String title=d.title();
  if(title.matches(".*(安全验证|人机验证|访问验证|验证码).*"))return new SessionInfo(State.CHALLENGE,"","","论坛要求完成验证");
  return new SessionInfo(State.UNKNOWN,"","","未能识别论坛账户栏，暂时无法确认登录状态");
 }
 public static String parsePortalName(String headerHtml){
  Element header=Jsoup.parse(headerHtml,"https://www.my0511.com/").selectFirst("#usrloginfo");
  if(header==null)return "";
  boolean logout=false;
  for(Element a:header.select("a[href]"))if(ForumPolicy.httpsHost(a.absUrl("href"),"passport.my0511.com")&&a.text().contains("退出"))logout=true;
  if(!logout)return "";
  Element clone=header.clone();clone.select("a,script,style").remove();
  Matcher m=Pattern.compile("通行证[：:]\\s*(.+?)\\s*$").matcher(clone.text());
  if(!m.find())return "";String name=m.group(1).trim();return name.length()<=80?name:"";
 }
}
