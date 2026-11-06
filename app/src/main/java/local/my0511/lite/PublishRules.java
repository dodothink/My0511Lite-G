// Edition: G
package local.my0511.lite;
import java.time.*;
/** All calendar limits use Beijing time; planning is persisted once per task. */
public final class PublishRules {
 public static final ZoneId ZONE=ZoneId.of("Asia/Shanghai");
 public int start=540,end=1350,minMinutes=10,meanMinutes=10,daily=3,perThread=2,quietDays=0;
 public void validate(){if(start<0||start>1439||end<0||end>1439||minMinutes<1||minMinutes>1440||meanMinutes<minMinutes||meanMinutes>10080||daily<1||daily>100||perThread<1||perThread>100||quietDays<0||quietDays>3)throw new IllegalArgumentException("规则范围无效：间隔1–1440、平均不小于最短、每日/每帖1–100、安静日0–3");}
 public static String day(long now){return Instant.ofEpochMilli(now).atZone(ZONE).toLocalDate().toString();}
 public boolean active(long now){ZonedDateTime z=Instant.ofEpochMilli(now).atZone(ZONE);if(quietDays>0&&z.getDayOfWeek().getValue()>7-quietDays)return false;int m=z.getHour()*60+z.getMinute();return start==end||(start<end?m>=start&&m<end:m>=start||m<end);}
 public long planned(Task t,long now,long last){if(t.plannedAt>0)return t.plannedAt;long earliest=Math.max(now,Math.max(t.at,last+minMinutes*60000L));if(t.at>0&&t.latest==0)return earliest;long spread=(meanMinutes-minMinutes)*120000L;double unit=(t.id.hashCode()&0x7fffffff)/(double)Integer.MAX_VALUE;long candidate=earliest+(long)(unit*spread);return t.latest>0&&t.latest>=earliest?Math.min(candidate,t.latest):candidate;}
 public String blocked(Task t,String uid,long now,long last,int today,int threadToday){if(t.accountId.isEmpty()||!t.accountId.equals(uid))return "任务绑定的账户与当前账户不同";if(t.latest>0&&now>t.latest)return "已超过最晚发布时间，请调整后重审";if(!active(now))return "当前为安静日或不在发布时段";if(today>=daily)return "当前账户已达到每日上限";if(t.reply&&threadToday>=perThread)return "当前账户已达到该帖每日上限";if(now<t.holdUntil)return "防连帖暂缓，等待再次检查";if(now<Math.max(t.at,t.plannedAt))return "等待任务计划时间";if(last>0&&now-last<minMinutes*60000L)return "等待最短发布间隔";return "";}
 public static int minute(String text){try{String[] p=text.trim().split(":",-1);if(p.length!=2)throw new Exception();int h=Integer.parseInt(p[0]),m=Integer.parseInt(p[1]);if(h<0||h>23||m<0||m>59)throw new Exception();return h*60+m;}catch(Exception e){throw new IllegalArgumentException("时段使用HH:mm，例如09:00");}}
 public static String clock(int m){return String.format(java.util.Locale.ROOT,"%02d:%02d",m/60,m%60);}
}
