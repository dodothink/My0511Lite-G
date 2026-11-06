// Edition: G
package local.my0511.lite;
import java.util.*;
/** Explicit paste only. A delimiter must occupy its own line. */
public final class BulkPaste {
 public static List<Task> parse(String text,boolean reply,String target){
  if(text==null||text.length()>1000000)throw new IllegalArgumentException("粘贴内容最多100万字符");
  List<Task> result=new ArrayList<>();Set<String> fingerprints=new HashSet<>();
  for(String part:text.replace("\r\n","\n").replace('\r','\n').replaceFirst("^\uFEFF","").split("(?m)^[ \t]*===[ \t]*$",-1)){
   String value=part.trim();if(value.isEmpty())continue;
   Task t=new Task();t.reply=reply;t.source="复制粘贴 · G";
   if(reply){t.tid=ForumPolicy.digits(target)?target:ForumPolicy.threadId(target);t.message=value;}
   else{String[] lines=value.split("\n",2);t.title=lines[0].trim();t.message=lines.length==2?lines[1].trim():"";}
   t.validate();if(!fingerprints.add(t.fingerprint()))throw new IllegalArgumentException("粘贴中包含重复条目");result.add(t);
   if(result.size()>100)throw new IllegalArgumentException("一次最多100条");
  }
  if(result.isEmpty())throw new IllegalArgumentException("请先粘贴内容");return result;
 }
}
