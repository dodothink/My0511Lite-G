// Edition: G
package local.my0511.lite;
import android.content.Context;
import android.security.keystore.*;
import android.util.AtomicFile;
import android.webkit.CookieManager;
import org.json.*;
import java.io.*;
import java.nio.charset.StandardCharsets;
import java.security.KeyStore;
import java.util.*;
import javax.crypto.*;
import javax.crypto.spec.GCMParameterSpec;
/** Session snapshots stay in private storage, authenticated and encrypted with AndroidKeyStore. */
public final class AccountVault {
 private static final String ALIAS="my0511-G-accounts-v1";
 private static final String[] ORIGINS={"https://bbs.my0511.com","https://passport.my0511.com","https://www.my0511.com","https://my0511.com"};
 public static final class Account {public String uid,name,alias;public long saved;private JSONObject cookies;}
 private final AtomicFile file;private final List<Account> accounts=new ArrayList<>();
 public AccountVault(Context c)throws Exception {file=new AtomicFile(new File(c.getFilesDir(),"accounts-G.enc"));if(!file.getBaseFile().exists())return;byte[] bytes=file.readFully();if(bytes.length<29||bytes[0]!=1)throw new IOException("账户会话文件格式无效");Cipher cipher=Cipher.getInstance("AES/GCM/NoPadding");cipher.init(Cipher.DECRYPT_MODE,key(),new GCMParameterSpec(128,Arrays.copyOfRange(bytes,1,13)));JSONArray arr=new JSONArray(new String(cipher.doFinal(Arrays.copyOfRange(bytes,13,bytes.length)),StandardCharsets.UTF_8));for(int i=0;i<arr.length();i++){JSONObject j=arr.getJSONObject(i);Account a=new Account();a.uid=j.getString("uid");a.name=j.getString("name");a.alias=j.optString("alias",a.name);a.saved=j.getLong("saved");a.cookies=j.getJSONObject("cookies");accounts.add(a);}}
 private javax.crypto.SecretKey key()throws Exception {KeyStore ks=KeyStore.getInstance("AndroidKeyStore");ks.load(null);if(ks.containsAlias(ALIAS))return (javax.crypto.SecretKey)ks.getKey(ALIAS,null);KeyGenerator g=KeyGenerator.getInstance(KeyProperties.KEY_ALGORITHM_AES,"AndroidKeyStore");g.init(new KeyGenParameterSpec.Builder(ALIAS,KeyProperties.PURPOSE_ENCRYPT|KeyProperties.PURPOSE_DECRYPT).setBlockModes(KeyProperties.BLOCK_MODE_GCM).setEncryptionPaddings(KeyProperties.ENCRYPTION_PADDING_NONE).build());return g.generateKey();}
 private void save()throws Exception {JSONArray arr=new JSONArray();for(Account a:accounts)arr.put(new JSONObject().put("uid",a.uid).put("name",a.name).put("alias",a.alias).put("saved",a.saved).put("cookies",a.cookies));Cipher cipher=Cipher.getInstance("AES/GCM/NoPadding");cipher.init(Cipher.ENCRYPT_MODE,key());byte[] encrypted=cipher.doFinal(arr.toString().getBytes(StandardCharsets.UTF_8));FileOutputStream out=null;try{out=file.startWrite();out.write(1);out.write(cipher.getIV());out.write(encrypted);file.finishWrite(out);}catch(Exception e){if(out!=null)file.failWrite(out);throw e;}}
 public List<Account> list(){return new ArrayList<>(accounts);}
 public void capture(SessionInfo s,String alias)throws Exception {if(s.state!=SessionInfo.State.LOGGED_IN||s.uid.isEmpty())throw new IllegalArgumentException("须先确认真实论坛账户及UID");Account a=new Account();a.uid=s.uid;a.name=s.nickname;a.alias=alias.trim().isEmpty()?s.nickname:alias.trim();a.saved=System.currentTimeMillis();a.cookies=new JSONObject();for(String origin:ORIGINS){String value=CookieManager.getInstance().getCookie(origin);if(value!=null&&!value.isEmpty())a.cookies.put(origin,value);}if(!a.cookies.has(ORIGINS[0]))throw new IOException("论坛会话为空，未保存");List<Account> prior=new ArrayList<>(accounts);accounts.removeIf(x->x.uid.equals(a.uid));accounts.add(a);try{save();}catch(Exception e){accounts.clear();accounts.addAll(prior);throw e;}}
 public void remove(Account a)throws Exception {int i=accounts.indexOf(a);if(i<0)return;accounts.remove(i);try{save();}catch(Exception e){accounts.add(i,a);throw e;}}
 public void restore(Account a){CookieManager cm=CookieManager.getInstance();for(String origin:ORIGINS){String raw=a.cookies.optString(origin);for(String pair:raw.split(";")){String v=pair.trim();if(v.contains("=")&&v.indexOf('\n')<0&&v.indexOf('\r')<0)cm.setCookie(origin,v+"; Path=/; Secure; HttpOnly; SameSite=Lax");}}cm.flush();}
}
