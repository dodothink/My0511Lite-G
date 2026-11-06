// Edition: G
package local.my0511.lite;
import android.content.Context;import android.util.AtomicFile;import java.nio.file.*;import java.util.*;
public final class StoreTest {
 static int count=0;static void check(boolean b,String msg){count++;if(!b)throw new AssertionError(msg);}interface Checked{void run()throws Exception;}static void rejects(Checked fn,String msg)throws Exception{boolean bad=false;try{fn.run();}catch(Exception e){bad=true;}check(bad,msg);}
 public static void main(String[] a)throws Exception{Path temp=Files.createTempDirectory("lite-store-test");try{
  Context c=new Context(temp.toFile());TaskStore s=new TaskStore(c);check(s.tasks.isEmpty(),"new store empty");
  List<Task> list=s.parseImport("# 中文标题\n中文正文\n第二行","素材.md");check(list.size()==1&&list.get(0).title.equals("中文标题"),"markdown title");check(list.get(0).state==Task.State.DRAFT,"import never auto approves");s.addAll(list);check(new TaskStore(c).tasks.get(0).message.contains("第二行"),"Chinese disk roundtrip");
  rejects(()->s.parseImport("中文标题\n中文正文\n第二行","same.txt"),"duplicate import blocked");
  rejects(()->s.parseImport("[{\"fid\":\"153\",\"subject\":\"标题\",\"message\":\"正文\"}]","file.json"),"other board blocked");
  rejects(()->s.parseImport("[{\"kind\":\"reply\",\"target\":\"https://evil.example/f152b-t123z-1\",\"message\":\"正文\"}]","file.json"),"external reply blocked");
  List<Task> reply=s.parseImport("{\"queue\":[{\"kind\":\"reply\",\"fid\":\"152\",\"target\":\"https://bbs.my0511.com/f152b-t123z-1\",\"message\":\"回复\",\"at\":\"2026-11-05T10:00:00Z\"}]}","q.json");check(reply.get(0).tid.equals("123")&&reply.get(0).at>0,"legacy JSON target and time");
  rejects(()->s.parseImport("[{\"subject\":\"A\",\"message\":\"B\",\"at\":\"bad\"}]","q.json"),"invalid time blocked");
  rejects(()->s.parseImport("[{\"subject\":\"A\",\"message\":\"B\"},{\"subject\":\"A\",\"message\":\"B\"}]","q.json"),"batch internal duplicates blocked");check(s.tasks.size()==1,"failed batches do not modify store");rejects(()->s.parseImport("[{\"subject\":\"A\",\"message\":\"B\",\"status\":\"needs_review\"}]","q.json"),"uncertain import cannot reset state");
  Task t=s.tasks.get(0);t.approve();t.preparing();s.save();TaskStore recovered=new TaskStore(c);check(recovered.tasks.get(0).state==Task.State.WAITING_HUMAN,"prepare crash persisted recovery");t.state=Task.State.SUBMIT_INTENT;s.save();recovered=new TaskStore(c);check(recovered.tasks.get(0).state==Task.State.NEEDS_REVIEW,"intent crash persisted recovery");check(new TaskStore(c).tasks.get(0).state==Task.State.NEEDS_REVIEW,"recovery state remains on second load");
  AtomicFile.fail=true;Task extra=new Task();extra.title="新标题";extra.message="新正文";rejects(()->s.add(extra),"disk failure reported");check(s.tasks.size()==1,"failed add rolled back");AtomicFile.fail=false;
  Files.write(temp.resolve("tasks.json"),"corrupted".getBytes("UTF-8"));rejects(()->new TaskStore(c),"corrupt store fails closed instead of clearing");
  System.out.println("PASS "+count+" storage/import checks; host AtomicFile double, not Android filesystem validation");
 }finally{AtomicFile.fail=false;try(java.util.stream.Stream<Path> paths=Files.walk(temp)){paths.sorted(Comparator.reverseOrder()).forEach(p->{try{Files.delete(p);}catch(Exception e){}});}}}
}
