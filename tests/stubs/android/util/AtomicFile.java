// Edition: G
package android.util;
import java.io.*;import java.nio.file.*;
/** Host-only persistence test double. Device AtomicFile behavior is tested separately. */
public class AtomicFile {
 public static boolean fail=false;
 private final File base;public AtomicFile(File f){base=f;}public File getBaseFile(){return base;}
 public byte[] readFully()throws IOException{return Files.readAllBytes(base.toPath());}
 public FileOutputStream startWrite()throws IOException{if(fail)throw new IOException("simulated disk failure");return new FileOutputStream(base.toString()+".new");}
 public void finishWrite(FileOutputStream f)throws IOException{f.close();Files.move(Paths.get(base.toString()+".new"),base.toPath(),StandardCopyOption.REPLACE_EXISTING);}
 public void failWrite(FileOutputStream f){try{f.close();}catch(Exception e){}new File(base.toString()+".new").delete();}
}
