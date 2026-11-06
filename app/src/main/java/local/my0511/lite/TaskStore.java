// Edition: G
package local.my0511.lite;
import android.content.Context;
import android.util.AtomicFile;
import org.json.*;
import java.io.*;
import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.util.*;
public final class TaskStore {
 private final AtomicFile file;
 public final List<Task> tasks=new ArrayList<>();
 public TaskStore(Context c) throws Exception {file=new AtomicFile(new File(c.getFilesDir(),"tasks.json"));if(!file.getBaseFile().exists())return;byte[] b=file.readFully();JSONArray arr=new JSONArray(new String(b,StandardCharsets.UTF_8));for(int i=0;i<arr.length();i++)tasks.add(decode(arr.getJSONObject(i)));boolean changed=false;for(Task t:tasks)if(t.state==Task.State.SUBMIT_INTENT||t.state==Task.State.PREPARING){t.interrupted();changed=true;}if(changed)save();}
 private Task decode(JSONObject j)throws Exception{Task t=new Task();t.id=j.getString("id");t.title=j.optString("title");t.message=j.getString("message");t.tid=j.optString("tid");t.reply=j.optBoolean("reply");t.state=Task.State.valueOf(j.getString("state"));t.note=j.optString("note");t.receipt=j.optString("receipt");t.source=j.optString("source","本地素材");t.accountId=j.optString("accountId");t.accountName=j.optString("accountName");t.typeId=j.optString("typeId");t.holdUntil=j.optLong("holdUntil");t.latest=j.optLong("latest");t.plannedAt=j.optLong("plannedAt");t.at=j.optLong("at");t.created=j.optLong("created");t.validate();return t;}
 private JSONObject encode(Task t)throws Exception{return new JSONObject().put("id",t.id).put("title",t.title).put("message",t.message).put("tid",t.tid).put("reply",t.reply).put("state",t.state.name()).put("note",t.note).put("receipt",t.receipt).put("source",t.source).put("at",t.at).put("created",t.created).put("accountId",t.accountId).put("accountName",t.accountName).put("typeId",t.typeId).put("holdUntil",t.holdUntil).put("latest",t.latest).put("plannedAt",t.plannedAt);}
 public synchronized void save() throws Exception {JSONArray a=new JSONArray();for(Task t:tasks)a.put(encode(t));FileOutputStream out=null;try{out=file.startWrite();out.write(a.toString(2).getBytes(StandardCharsets.UTF_8));file.finishWrite(out);}catch(Exception e){if(out!=null)file.failWrite(out);throw e;}}
 public boolean duplicate(Task t){String fp=t.fingerprint();for(Task x:tasks)if(x.fingerprint().equals(fp))return true;return false;}
 public void add(Task t)throws Exception {t.validate();if(tasks.size()>=500)throw new IllegalArgumentException("最多保留 500 条任务，请先清理已完成任务");if(duplicate(t))throw new IllegalArgumentException("已有相同内容和目标，未重复加入");tasks.add(t);try{save();}catch(Exception e){tasks.remove(t);throw e;}}
 public List<Task> parseImport(String text,String name)throws Exception {
  List<Task> out=new ArrayList<>();String trimmed=text.trim();
  if(name.toLowerCase(Locale.ROOT).endsWith(".json")){
   Object raw=new JSONTokener(trimmed).nextValue();JSONArray arr=raw instanceof JSONArray?(JSONArray)raw:((JSONObject)raw).getJSONArray("queue");if(arr.length()>100)throw new IllegalArgumentException("一次最多导入 100 条");
   for(int i=0;i<arr.length();i++){JSONObject j=arr.getJSONObject(i);if("done".equals(j.optString("status")))continue;if(!"152".equals(j.optString("fid","152")))throw new IllegalArgumentException("第 "+(i+1)+" 条不是百姓话题");if(Arrays.asList("posting","preparing","submit_intent","needs_review","waiting_human").contains(j.optString("status")))throw new IllegalArgumentException("有尚未核实的任务，请先在原队列核实，不能通过导入重置");Task t=new Task();String kind=j.optString("kind","thread");if(!kind.equals("thread")&&!kind.equals("reply"))throw new IllegalArgumentException("未知任务类型");t.reply=kind.equals("reply");t.title=j.optString("subject",j.optString("title","")).trim();t.message=j.optString("message",j.optString("body","")).trim();
    if(t.reply){String target=j.optString("target",j.optString("url",j.optString("tid","")));t.tid=ForumPolicy.digits(target)?target:ForumPolicy.threadId(target);}
    Object at=j.opt("at");if(at!=null&&at!=JSONObject.NULL&&!String.valueOf(at).isEmpty())t.at=parseAt(at);t.source=name;t.note="从本地文件导入，须逐条审核";t.validate();out.add(t);
   }
  }else {Task t=new Task();String[] lines=trimmed.split("\n",2);t.title=lines[0].replaceFirst("^#+\\s*","").trim();t.message=lines.length>1?lines[1].trim():"";t.source=name;t.validate();out.add(t);}
  if(out.isEmpty())throw new IllegalArgumentException("没有可导入的草稿");if(tasks.size()+out.size()>500)throw new IllegalArgumentException("任务总数超过 500");Set<String> fps=new HashSet<>();for(Task t:out)if(duplicate(t)||!fps.add(t.fingerprint()))throw new IllegalArgumentException("文件中有重复任务或与现有任务重复；未导入任何内容");return out;
 }
 private static long parseAt(Object raw){if(raw instanceof Number)return ((Number)raw).longValue();String s=String.valueOf(raw);try{return Long.parseLong(s);}catch(Exception e){try{return Instant.parse(s).toEpochMilli();}catch(Exception ex){throw new IllegalArgumentException("at 须为毫秒时间戳或带时区的 ISO 时间");}}}
 public void addAll(List<Task> list)throws Exception {if(tasks.size()+list.size()>500)throw new IllegalArgumentException("最多500条任务");Set<String> seen=new HashSet<>();for(Task t:list){t.validate();if(duplicate(t)||!seen.add(t.fingerprint()))throw new IllegalArgumentException("有重复内容，未加入任何任务");}tasks.addAll(list);try{save();}catch(Exception e){tasks.removeAll(list);throw e;}}
}
