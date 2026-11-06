// Edition: G
package local.my0511.lite;
import java.net.*;
import java.io.*;
import java.util.regex.*;
import org.jsoup.Jsoup;
public final class ForumClient {
 public static final String UA="Mozilla/5.0 (X11; Linux x86_64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/120.0.0.0 Safari/537.36 My0511Lite-G/0.2";
 public String get(String initial,String cookies)throws Exception {return get(initial,cookies,null);}
 public String get(String initial,String cookies,java.util.function.BiConsumer<String,String> receivedCookie)throws Exception {
  String url=initial;
  for(int n=0;n<5;n++){
   if(!ForumPolicy.readAllowed(url))throw new IOException("请求超出百姓话题范围，未发送");
   HttpURLConnection c=(HttpURLConnection)new URL(url).openConnection();c.setInstanceFollowRedirects(false);c.setRequestMethod("GET");c.setConnectTimeout(15000);c.setReadTimeout(20000);c.setUseCaches(false);c.setRequestProperty("User-Agent",UA);c.setRequestProperty("Accept","text/html");if(cookies!=null&&!cookies.isEmpty())c.setRequestProperty("Cookie",cookies);
   try {
    int code=c.getResponseCode();if(receivedCookie!=null)for(java.util.Map.Entry<String,java.util.List<String>> h:c.getHeaderFields().entrySet())if(h.getKey()!=null&&h.getKey().equalsIgnoreCase("Set-Cookie"))for(String value:h.getValue())receivedCookie.accept(url,value);if(code>=300&&code<400){String dest=c.getHeaderField("Location");if(dest==null)throw new IOException("服务器跳转缺少地址");url=new URL(new URL(url),dest).toString();if(!ForumPolicy.readAllowed(url))throw new IOException("论坛要求登录或验证，请打开登录页面处理");continue;}
    if(code!=200)throw new IOException("论坛返回 HTTP "+code+"，本次未继续请求");
    if(c.getContentLengthLong()>3*1024*1024)throw new IOException("页面超过大小限制");ByteArrayOutputStream b=new ByteArrayOutputStream();try(InputStream in=c.getInputStream()){byte[] buf=new byte[8192];int len;while((len=in.read(buf))!=-1){if(b.size()+len>3*1024*1024)throw new IOException("页面超过大小限制");b.write(buf,0,len);}}
    String ct=c.getContentType(),charset=null;if(ct!=null){Matcher m=Pattern.compile("charset=([^;\\s]+)",Pattern.CASE_INSENSITIVE).matcher(ct);if(m.find())charset=m.group(1).replace("\"","");}
    return Jsoup.parse(new ByteArrayInputStream(b.toByteArray()),charset,url).outerHtml();
   }finally{c.disconnect();}
  }
  throw new IOException("跳转次数过多");
 }
}
