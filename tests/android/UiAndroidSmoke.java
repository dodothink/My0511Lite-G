// Edition: G
package local.my0511.lite;
import android.app.*;
import android.os.*;
import android.content.*;
import android.graphics.Bitmap;
import java.io.*;
import java.lang.reflect.Method;
/** Native UI screenshots; leaves no synthetic publishing tasks. */
public final class UiAndroidSmoke extends Instrumentation {
 MainActivity activity;
 @Override public void onCreate(Bundle b){super.onCreate(b);start();}
 void page(String name,String image)throws Exception{runOnMainSync(()->{try{Method m=MainActivity.class.getDeclaredMethod(name);m.setAccessible(true);m.invoke(activity);}catch(Exception e){throw new RuntimeException(e);}});waitForIdleSync();Thread.sleep(300);Bitmap bitmap=getUiAutomation().takeScreenshot();try(FileOutputStream out=new FileOutputStream(new File(getTargetContext().getExternalFilesDir(null),image))){bitmap.compress(Bitmap.CompressFormat.PNG,100,out);}bitmap.recycle();}
 @Override public void onStart(){Bundle result=new Bundle();String previous=getTargetContext().getSharedPreferences("settings",0).getString("appearance","system");try{getTargetContext().getSharedPreferences("settings",0).edit().putString("appearance","light").commit();activity=(MainActivity)startActivitySync(new Intent(getTargetContext(),MainActivity.class).addFlags(Intent.FLAG_ACTIVITY_NEW_TASK));page("pastePage","G-粘贴分隔.png");page("rulePage","G-时间与限额.png");runOnMainSync(()->activity.finish());waitForIdleSync();getTargetContext().getSharedPreferences("settings",0).edit().putString("appearance","dark").commit();activity=(MainActivity)startActivitySync(new Intent(getTargetContext(),MainActivity.class).addFlags(Intent.FLAG_ACTIVITY_NEW_TASK));page("compose","G-深色写帖.png");result.putString("stream","PASS G native light/dark UI rendered; no submissions\n");finish(-1,result);}catch(Exception e){result.putString("stream","FAIL "+e+"\n");finish(0,result);}finally{getTargetContext().getSharedPreferences("settings",0).edit().putString("appearance",previous).commit();if(activity!=null)runOnMainSync(()->activity.finish());}}
}
