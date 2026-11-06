// Edition: G
package local.my0511.lite;
import android.app.Instrumentation;
import android.os.Bundle;
import java.io.*;
import java.util.*;
/** Test APK only. Tests the actual Android org.json and AtomicFile on isolated device. */
public final class AndroidSmoke extends Instrumentation {
 private int checks=0;private void check(boolean condition,String msg){if(!condition)throw new AssertionError(msg);checks++;}
 @Override public void onCreate(Bundle b){super.onCreate(b);start();}
 @Override public void onStart(){Bundle result=new Bundle();File file=new File(getTargetContext().getFilesDir(),"tasks.json");byte[] backup=null;try{
  if(file.exists()){try(FileInputStream in=new FileInputStream(file)){ByteArrayOutputStream out=new ByteArrayOutputStream();byte[] buf=new byte[4096];int n;while((n=in.read(buf))!=-1)out.write(buf,0,n);backup=out.toByteArray();}}
  TaskStore s=new TaskStore(getTargetContext());s.tasks.clear();s.save();
  List<Task> imported=s.parseImport("# 手机中文素材\n本地测试正文，不得发布","设备测试.md");check(imported.size()==1,"markdown import");check(imported.get(0).state==Task.State.DRAFT,"import draft only");s.addAll(imported);Task t=s.tasks.get(0);check(new TaskStore(getTargetContext()).tasks.get(0).message.equals(t.message),"Android AtomicFile Unicode roundtrip");
  t.approve();s.save();check(new TaskStore(getTargetContext()).tasks.get(0).state==Task.State.READY,"approved persisted");t.preparing();s.save();check(new TaskStore(getTargetContext()).tasks.get(0).state==Task.State.WAITING_HUMAN,"before submit recovery");
  t.state=Task.State.SUBMIT_INTENT;s.save();check(new TaskStore(getTargetContext()).tasks.get(0).state==Task.State.NEEDS_REVIEW,"after intent recovery");check(new TaskStore(getTargetContext()).tasks.get(0).state==Task.State.NEEDS_REVIEW,"recovery stable");
  boolean blocked=false;try{s.parseImport("[{\"kind\":\"reply\",\"fid\":\"153\",\"message\":\"x\",\"tid\":\"123\"}]","x.json");}catch(Exception expected){blocked=true;}check(blocked,"cross board import blocked");
  blocked=false;try{s.parseImport("[{\"subject\":\"x\",\"message\":\"y\",\"status\":\"needs_review\"}]","x.json");}catch(Exception expected){blocked=true;}check(blocked,"uncertain import blocked");
  s.tasks.clear();s.save();List<Task> j=s.parseImport("{\"queue\":[{\"subject\":\"JSON中文\",\"message\":\"正文\",\"at\":\"2026-11-05T10:00:00Z\"}]}","x.json");check(j.size()==1&&j.get(0).at>0,"Android JSON and Instant import");
  result.putString("stream","PASS "+checks+" Android JSON/AtomicFile checks. No network operations or posting.\n");result.putInt("passed",checks);
 }catch(Throwable error){result.putString("stream","FAIL "+error.toString()+"\n");result.putString("error",error.toString());}finally{try{if(backup!=null){try(FileOutputStream out=new FileOutputStream(file)){out.write(backup);}}else file.delete();}catch(Exception ex){result.putString("restore_error",ex.toString());}}
 finish(result.containsKey("error")?0:-1,result);
 }
}
