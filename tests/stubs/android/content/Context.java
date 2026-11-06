// Edition: G
package android.content;
import java.io.File;
/** Host-only test double. Not included in the APK. */
public class Context {private final File dir;public Context(File d){dir=d;}public File getFilesDir(){return dir;}}
