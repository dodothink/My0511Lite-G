// Edition: G
package local.my0511.lite;
import android.content.*;
/** One repository and one submission owner in the app's single process. */
public final class AppState {
 private static AppState instance;
 public final TaskStore store;public final PublishEngine engine;
 private AppState(Context c)throws Exception{store=new TaskStore(c);engine=new PublishEngine(c.getApplicationContext(),store);}
 public static synchronized AppState get(Context c)throws Exception{if(instance==null)instance=new AppState(c);return instance;}
}
