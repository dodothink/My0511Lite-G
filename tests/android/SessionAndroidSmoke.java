// Edition: G
package local.my0511.lite;
import android.app.Instrumentation;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.widget.TextView;
import android.webkit.*;
import android.net.Uri;
import java.util.Collections;
import java.lang.reflect.*;
import java.util.Map;

/** Isolated device checks use synthetic identities; no real credentials or posting. */
public final class SessionAndroidSmoke extends Instrumentation {
 private int passed=0;
 private static final class RecordingView extends WebView {
  String loaded;
  RecordingView(android.content.Context context){super(context);}
  @Override public void loadUrl(String url){loaded=url;}
 }
 private static WebResourceRequest request(final String method,final String url){return request(method,url,true);}
 private static WebResourceRequest request(final String method,final String url,final boolean main){return new WebResourceRequest(){public Uri getUrl(){return Uri.parse(url);}public boolean isForMainFrame(){return main;}public boolean hasGesture(){return false;}public boolean isRedirect(){return true;}public String getMethod(){return method;}public Map<String,String> getRequestHeaders(){return Collections.emptyMap();}};}

 private void ok(boolean b,String name){if(!b)throw new AssertionError(name);passed++;}
 private Object field(Object owner,String name)throws Exception{Field f=owner.getClass().getDeclaredField(name);f.setAccessible(true);return f.get(owner);}
 private void invoke(Object owner,String name,Class<?>[] types,Object... args)throws Exception{Method m=owner.getClass().getDeclaredMethod(name,types);m.setAccessible(true);m.invoke(owner,args);}
 @Override public void onCreate(Bundle b){super.onCreate(b);start();}
 @Override public void onStart(){Bundle result=new Bundle();final MainActivity activity=(MainActivity)startActivitySync(new Intent(getTargetContext(),MainActivity.class).addFlags(Intent.FLAG_ACTIVITY_NEW_TASK));SharedPreferences prefs=getTargetContext().getSharedPreferences("settings",0);String oldAuthor=prefs.getString("author",""),oldLast=prefs.getString("last_verified_account","");long oldAt=prefs.getLong("last_verified_at",0);
 try{
  runOnMainSync(()->{try{
   TextView line=(TextView)field(activity,"accountLine");ok(line!=null,"persistent account status view exists");
   prefs.edit().putString("author","用户手填昵称").commit();
   invoke(activity,"applyForumSession",new Class[]{SessionInfo.class},new SessionInfo(SessionInfo.State.LOGGED_OUT,"","","论坛未登录"));
   ok(line.getText().toString().contains("未登录")&&!line.getText().toString().contains("用户手填昵称"),"configured nickname never masquerades as authenticated account");
   invoke(activity,"applyForumSession",new Class[]{SessionInfo.class},new SessionInfo(SessionInfo.State.LOGGED_IN,"合成测试账户","123","fixture"));
   ok(line.getText().toString().contains("合成测试账户")&&line.getText().toString().contains("论坛已登录"),"verified nickname shown in header");
   ok(prefs.getString("author","").equals("用户手填昵称"),"existing publish nickname not silently replaced");
   invoke(activity,"accountPage",new Class[]{});TextView detail=(TextView)field(activity,"accountDetails");ok(detail.getText().toString().contains("UID：123"),"account page exposes verified UID");
   Field portal=MainActivity.class.getDeclaredField("portalAccount");portal.setAccessible(true);portal.set(activity,"合成通行证账户");
   invoke(activity,"applyForumSession",new Class[]{SessionInfo.class},new SessionInfo(SessionInfo.State.ERROR,"","","HTTP 503"));
   String t=line.getText().toString();ok(t.contains("合成通行证账户")&&t.contains("暂时无法确认"),"portal success separate from forum server error");ok(detail.getText().toString().contains("503"),"server error preserved in account details");
   invoke(activity,"applyForumSession",new Class[]{SessionInfo.class},new SessionInfo(SessionInfo.State.CHALLENGE,"","","人工验证"));ok(line.getText().toString().contains("人工验证"),"verification state distinct");
   portal.set(activity,"");invoke(activity,"applyForumSession",new Class[]{SessionInfo.class},new SessionInfo(SessionInfo.State.LOGGED_OUT,"","","未登录"));
   ok(!line.getText().toString().contains("合成通行证账户"),"logged out UI does not retain current identity");
   ok(ForumPolicy.authenticationAllowed("https://www.my0511.com/"),"observed website login destination allowed");ok(!ForumPolicy.readAllowed("https://www.my0511.com/"),"reader not expanded with login policy");
   invoke(activity,"initBrowser",new Class[]{});WebView actual=(WebView)field(activity,"browser");WebViewClient policy=actual.getWebViewClient();
   Field flow=MainActivity.class.getDeclaredField("authenticationFlow");flow.setAccessible(true);flow.setBoolean(activity,true);
   RecordingView record=new RecordingView(activity);
   ok(policy.shouldOverrideUrlLoading(record,request("POST","http://www.my0511.com/")),"plaintext POST is cancelled");
   ok(record.loaded==null,"plaintext POST is not transformed into another request");
   ok(policy.shouldOverrideUrlLoading(record,request("GET","http://www.my0511.com/")),"official HTTP GET intercepted before loading");
   ok("https://www.my0511.com/".equals(record.loaded),"official landing GET upgraded to HTTPS");record.loaded=null;
   ok(!policy.shouldOverrideUrlLoading(record,request("GET","https://bbs.my0511.com/api/uc.php",false)),"same-site HTTPS iframe auth callback remains allowed");
   ok(policy.shouldOverrideUrlLoading(record,request("GET","https://evil.test/")),"foreign login redirect blocked");
   ok(record.loaded==null,"foreign login redirect never loaded");record.destroy();flow.setBoolean(activity,false);

  }catch(Exception e){throw new RuntimeException(e);}});
  result.putString("stream","PASS "+passed+" Android account-state UI checks; synthetic identities only; no credentials or posts.\n");
 }catch(Throwable e){result.putString("error",e.toString());result.putString("stream","FAIL "+e+"\n");}
 finally{prefs.edit().putString("author",oldAuthor).putString("last_verified_account",oldLast).putLong("last_verified_at",oldAt).commit();runOnMainSync(activity::finish);}
 finish(result.containsKey("error")?0:-1,result);
 }
}
