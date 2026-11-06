// Edition: G
package local.my0511.lite;
import java.io.*;import java.net.*;import android.graphics.*;
/** Only called following the user's per-image confirmation. No disk cache. */
public final class ImageClient {
 public static Bitmap load(String url,String cookies)throws Exception{
  URI uri=ForumPolicy.uri(url);if(uri==null||!"https".equalsIgnoreCase(uri.getScheme())||uri.getHost()==null||uri.getRawUserInfo()!=null||(uri.getPort()!=-1&&uri.getPort()!=443))throw new IOException("只读取标准 HTTPS 图片地址");
  for(InetAddress ip:InetAddress.getAllByName(uri.getHost())){byte[] bytes=ip.getAddress();boolean private6=bytes.length==16&&((bytes[0]&0xfe)==0xfc);if(ip.isAnyLocalAddress()||ip.isLoopbackAddress()||ip.isLinkLocalAddress()||ip.isSiteLocalAddress()||ip.isMulticastAddress()||private6)throw new IOException("不读取本地或私有网络地址");}
  HttpURLConnection c=(HttpURLConnection)new URL(url).openConnection();c.setInstanceFollowRedirects(false);c.setConnectTimeout(15000);c.setReadTimeout(20000);c.setUseCaches(false);c.setRequestProperty("User-Agent",ForumClient.UA);if(ForumPolicy.forum(url)&&cookies!=null)c.setRequestProperty("Cookie",cookies);
  try{if(c.getResponseCode()!=200)throw new IOException("图片服务器返回 "+c.getResponseCode()+"；不跟随跳转");String type=c.getContentType();if(type==null||!type.toLowerCase(java.util.Locale.ROOT).startsWith("image/"))throw new IOException("返回内容不是图片");ByteArrayOutputStream out=new ByteArrayOutputStream();try(InputStream in=c.getInputStream()){byte[] buf=new byte[8192];int n;while((n=in.read(buf))!=-1){if(out.size()+n>8*1024*1024)throw new IOException("图片大于 8 MB");out.write(buf,0,n);}}byte[] data=out.toByteArray();BitmapFactory.Options opts=new BitmapFactory.Options();opts.inJustDecodeBounds=true;BitmapFactory.decodeByteArray(data,0,data.length,opts);if(opts.outWidth<=0||opts.outHeight<=0||opts.outWidth>30000||opts.outHeight>30000)throw new IOException("图片格式或尺寸不支持");int sample=1;while(opts.outWidth/sample>2048||opts.outHeight/sample>2048)sample*=2;opts.inJustDecodeBounds=false;opts.inSampleSize=sample;Bitmap result=BitmapFactory.decodeByteArray(data,0,data.length,opts);if(result==null)throw new IOException("无法解码图片");return result;}finally{c.disconnect();}
 }
}
