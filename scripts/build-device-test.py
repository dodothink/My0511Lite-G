# Edition: G
import pathlib,subprocess,zipfile,shutil,os
p=pathlib.Path(__file__).resolve().parents[1];w=p.parent.parent if p.parent.name=='outputs' else p;t=pathlib.Path(os.environ.get('MY0511_TOOLCHAIN',str(w/'work/toolchain')));jdk=pathlib.Path(os.environ.get('MY0511_JDK',str(t/'jdk/Contents/Home')));bt=t/'build-tools';b=w/'work/device-tests';b.mkdir(exist_ok=True)
for n in ['classes','dex']:(b/n).mkdir(exist_ok=True)
def run(*a):subprocess.run([str(x) for x in a],check=True)
run(bt/'aapt2','link','-o',b/'test.apk','-I',t/'platform/android.jar','--manifest',p/'tests/android/AndroidManifest.xml')
run(jdk/'bin/javac','--release','8','-encoding','UTF-8','-cp',str(t/'platform/android.jar')+os.pathsep+str(w/'work/lite-build/classes'),'-d',b/'classes',p/'tests/android/AndroidSmoke.java',p/'tests/android/SessionAndroidSmoke.java',p/'tests/android/UnifiedAndroidSmoke.java',p/'tests/android/UiAndroidSmoke.java',p/'tests/android/EditorAndroidSmoke.java',p/'tests/android/StatusAndroidSmoke.java',p/'tests/android/QuickAddAndroidSmoke.java')
with zipfile.ZipFile(b/'test-classes.jar','w') as z:
 for f in (b/'classes').rglob('*.class'):z.write(f,str(f.relative_to(b/'classes')))
run(jdk/'bin/java','-cp',bt/'lib/d8.jar','com.android.tools.r8.D8','--min-api','26','--lib',t/'platform/android.jar','--classpath',w/'work/lite-build/classes.jar','--output',b/'dex',b/'test-classes.jar')
with zipfile.ZipFile(b/'test.apk','a',zipfile.ZIP_DEFLATED) as z:
 for f in (b/'dex').glob('*.dex'):z.write(f,f.name)
 for key,name in [('common','common'),('post','editor-post'),('bbcode','editor-bbcode')]:
  public=w/('work/G-official-'+name+'.js')
  if public.exists():z.write(public,'assets/G-public-'+key+'.js')
run(bt/'zipalign','-f','4',b/'test.apk',b/'aligned.apk')
run(jdk/'bin/java','-jar',bt/'lib/apksigner.jar','sign','--ks',w/'work/signing/my0511-lite-test.jks','--ks-key-alias','lite-test','--ks-pass','pass:android','--out',b/'test-signed.apk',b/'aligned.apk')
print(b/'test-signed.apk')
