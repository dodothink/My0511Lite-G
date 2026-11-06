// Edition: G
package local.my0511.lite;
import android.app.job.*;
import android.content.*;
/** Android chooses the actual run time; periodic work is never an exact-time guarantee. */
public final class QueueJob extends JobService {
 private static final int ID=15202;
 public static boolean configure(Context c,boolean enabled){JobScheduler jobs=(JobScheduler)c.getSystemService(JOB_SCHEDULER_SERVICE);if(!enabled){jobs.cancel(ID);return true;}return jobs.schedule(new JobInfo.Builder(ID,new ComponentName(c,QueueJob.class)).setRequiredNetworkType(JobInfo.NETWORK_TYPE_ANY).setPeriodic(15*60*1000L).setPersisted(true).build())==JobScheduler.RESULT_SUCCESS;}
 @Override public boolean onStartJob(JobParameters params){SharedPreferences prefs=getSharedPreferences("settings",0);if(!prefs.getBoolean("background",false)||!prefs.getBoolean("queueEnabled",false))return false;try{AppState app=AppState.get(this);if(app.engine.busy())return false;String uid=prefs.getString("verifiedUid","");long now=System.currentTimeMillis();for(Task t:app.store.tasks)if(t.accountId.equals(uid)&&(t.state==Task.State.WAITING_HUMAN||t.state==Task.State.NEEDS_REVIEW)){prefs.edit().putBoolean("queueEnabled",false).commit();return false;}for(Task t:app.store.tasks){if(t.state!=Task.State.READY||!t.accountId.equals(uid))continue;if(t.plannedAt==0){t.plannedAt=app.engine.rules().planned(t,now,prefs.getLong("G.account."+uid+".lastAttempt",0));app.store.save();}if(app.engine.start(t,params,()->jobFinished(params,false)))return true;}return false;}catch(Exception e){prefs.edit().putBoolean("queueEnabled",false).commit();return false;}}
 @Override public boolean onStopJob(JobParameters params){try{AppState.get(this).engine.cancel(params,"系统中止后台任务，请检查队列");}catch(Exception ignored){}return false;}
}
