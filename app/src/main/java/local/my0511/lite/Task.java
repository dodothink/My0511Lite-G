// Edition: G
package local.my0511.lite;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.util.UUID;
public final class Task {
 public enum State {DRAFT, READY, PREPARING, SUBMIT_INTENT, WAITING_HUMAN, NEEDS_REVIEW, DONE, FAILED}
 public String id=UUID.randomUUID().toString(),title="",message="",tid="",note="",receipt="",source="手动编写",accountId="",accountName="",typeId="";
 public boolean reply=false;
 public long created=System.currentTimeMillis(),at=0,latest=0,plannedAt=0,holdUntil=0;
 public State state=State.DRAFT;
 public String fingerprint(){try{String raw=accountId+"\n"+(reply?"reply:"+tid:"thread:152")+"\n"+(reply?"":title)+"\n"+message;byte[] b=MessageDigest.getInstance("SHA-256").digest(raw.getBytes(StandardCharsets.UTF_8));StringBuilder s=new StringBuilder();for(byte x:b)s.append(String.format("%02x",x&255));return s.toString();}catch(Exception e){throw new IllegalStateException(e);}}
 public void validate(){if(message.trim().isEmpty()||message.length()>5000)throw new IllegalArgumentException("正文须为 1–5000 字");if(reply&&!ForumPolicy.digits(tid))throw new IllegalArgumentException("回复须指定百姓话题帖子");if(!reply&&(title.trim().isEmpty()||title.length()>80))throw new IllegalArgumentException("标题须为 1–80 字");if(latest<0||(latest>0&&latest<at))throw new IllegalArgumentException("时间范围无效");if(!typeId.isEmpty()&&!ForumPolicy.digits(typeId))throw new IllegalArgumentException("分类编号无效");if(at<0)throw new IllegalArgumentException("发布时间无效");}
 public void approve(){validate();if(state!=State.DRAFT&&state!=State.WAITING_HUMAN)throw new IllegalStateException("该任务不能直接排队，请先核实结果");state=State.READY;note="已加入队列，等待执行";}
 public void preparing(){if(state!=State.READY)throw new IllegalStateException("任务不在等待队列");state=State.PREPARING;}
 public void intent(){if(state!=State.PREPARING)throw new IllegalStateException("尚未准备表单");state=State.SUBMIT_INTENT;note="已记录提交意图；异常时不得自动重发";}
 public void interrupted(){if(state==State.SUBMIT_INTENT){state=State.NEEDS_REVIEW;note="提交可能已发生，请到论坛核实；不会自动重发";}else if(state==State.PREPARING){state=State.WAITING_HUMAN;note="准备中断，尚未触发应用提交，确认后可重新排队";}}
 public boolean locked(){return state==State.PREPARING||state==State.SUBMIT_INTENT||state==State.NEEDS_REVIEW;}
 public String label(){switch(state){case DRAFT:return "尚未加入队列";case READY:return "已排队";case PREPARING:return "准备表单";case SUBMIT_INTENT:return "正在核实";case WAITING_HUMAN:return "等待处理";case NEEDS_REVIEW:return "结果待核实";case DONE:return "已完成";default:return "已停止";}}
}
