// Edition: G
package local.my0511.lite;
import java.net.URI;
import java.net.URLDecoder;
import java.util.*;
import java.util.regex.*;
public final class ForumPolicy {
 public static final String BASE="https://bbs.my0511.com", LOGIN="https://passport.my0511.com/lphysignin.php?action=login";
 private static final Pattern THREAD=Pattern.compile("^/f152b-t([1-9][0-9]*)z(?:-([1-9][0-9]*))?/?$");
 public static URI uri(String value){try{return new URI(value);}catch(Exception e){return null;}}
 public static boolean httpsHost(String url,String host){URI u=uri(url);return u!=null&&"https".equalsIgnoreCase(u.getScheme())&&host.equalsIgnoreCase(u.getHost())&&u.getRawUserInfo()==null&&(u.getPort()==-1||u.getPort()==443);}
 public static boolean forum(String u){return httpsHost(u,"bbs.my0511.com");}
 public static Map<String,String> query(String url){Map<String,String> q=new LinkedHashMap<>();try{String s=uri(url).getRawQuery();if(s!=null)for(String part:s.split("&")){String[] a=part.split("=",2);String key=URLDecoder.decode(a[0],"UTF-8");if(q.containsKey(key))throw new IllegalArgumentException("duplicate key");q.put(key,a.length>1?URLDecoder.decode(a[1],"UTF-8"):"");}}catch(Exception e){q.clear();q.put("!invalid","1");}return q;}
 public static String threadId(String url){if(!forum(url))return "";URI u=uri(url);Matcher m=THREAD.matcher(u.getPath());if(m.matches())return m.group(1);Map<String,String> q=query(url);if("/viewthread.php".equals(u.getPath())&&"152".equals(q.get("fid"))&&digits(q.get("tid")))return q.get("tid");return "";}
 public static boolean digits(String s){return s!=null&&s.matches("[1-9][0-9]{0,14}");}
 public static boolean readAllowed(String url){if(!forum(url))return false;URI u=uri(url);if(u.getRawPath().contains("%"))return false;String path=u.getPath();if(path.matches("/f152b(?:-[1-9][0-9]*)?/?")||THREAD.matcher(path).matches())return true;Map<String,String> q=query(url);return (path.equals("/forumdisplay.php")&&"152".equals(q.get("fid")))||(!threadId(url).isEmpty());}
 public static boolean postingPage(String url,Task t){if(!forum(url)||!"/post.php".equals(uri(url).getPath()))return false;Map<String,String> q=query(url);return "152".equals(q.get("fid"))&&(t.reply?"reply":"newthread").equals(q.get("action"))&&(!t.reply||t.tid.equals(q.get("tid")))&&!q.containsKey("!invalid");}
 public static boolean webMainAllowed(String url){if(readAllowed(url))return true;if(httpsHost(url,"passport.my0511.com"))return true;if(!forum(url))return false;String path=uri(url).getPath();if(path.equals("/")||path.equals("/captcha")||path.equals("/logging.php")||path.equals("/member.php")||path.equals("/lphysignin.php"))return true;if(path.equals("/post.php")){Map<String,String> q=query(url);return "152".equals(q.get("fid"))&&Arrays.asList("newthread","reply").contains(q.get("action"))&&!q.containsKey("!invalid");}return false;}
 public static boolean resourceAllowed(String url){return forum(url)||httpsHost(url,"passport.my0511.com")||httpsHost(url,"fs.zhenjiang365.cn");}
 /** Login returns through the portal and forum home; these are not board-reading permissions. */
 public static boolean authenticationAllowed(String url){
  if(webMainAllowed(url))return true;
  if(httpsHost(url,"www.my0511.com")||httpsHost(url,"my0511.com")){
   String path=uri(url).getPath();return Arrays.asList("","/","/index.php","/index.html","/login.php","/lphysignin.php","/passport.php").contains(path);
  }
  if(!forum(url))return false;
  String path=uri(url).getPath();
  if(Arrays.asList("/index.php","/login.php","/crossdomain.php","/passport.php","/sso.php","/viewpro.php").contains(path))return true;
  if(path.equals("/goto.php")){Map<String,String> q=query(url);return !q.containsKey("!invalid")&&(!q.containsKey("fid")||"152".equals(q.get("fid")));}
  return false;
 }
 public static boolean authenticationResourceAllowed(String url){return resourceAllowed(url)||httpsHost(url,"www.my0511.com")||httpsHost(url,"my0511.com");}
 public static String secureAuthenticationUrl(String url){
  if(authenticationAllowed(url))return url;
  URI u=uri(url);if(u==null||!"http".equalsIgnoreCase(u.getScheme())||u.getHost()==null||u.getRawUserInfo()!=null||(u.getPort()!=-1&&u.getPort()!=80))return "";
  String host=u.getHost().toLowerCase(Locale.ROOT);if(!Arrays.asList("bbs.my0511.com","passport.my0511.com","www.my0511.com","my0511.com").contains(host))return "";
  try{String safe="https://"+host+(u.getRawPath()==null?"":u.getRawPath())+(u.getRawQuery()==null?"":"?"+u.getRawQuery())+(u.getRawFragment()==null?"":"#"+u.getRawFragment());return authenticationAllowed(safe)?safe:"";}catch(Exception ex){return "";}
 }
 public static String safeAddress(String url){URI u=uri(url);if(u==null||u.getHost()==null)return "未知地址";return u.getHost()+(u.getPath()==null?"":u.getPath());}
 public static String board(int page){return BASE+"/f152b-"+Math.max(1,page);}
 public static String thread(String tid,int page){if(!digits(tid))throw new IllegalArgumentException("帖子编号无效");return BASE+"/f152b-t"+tid+"z-"+Math.max(1,page);}
 public static String post(Task t){return BASE+"/post.php?action="+(t.reply?"reply&fid=152&tid="+t.tid:"newthread&fid=152");}
}
