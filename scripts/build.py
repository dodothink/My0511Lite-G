#!/usr/bin/env python3
# Edition: G
"""Build from Android SDK build-tools 35, platform 35, JDK 21, jsoup 1.18.3."""
import pathlib,os,subprocess,zipfile,hashlib,json,shutil
project=pathlib.Path(__file__).resolve().parents[1]
workspace=project.parent.parent if project.parent.name=='outputs' else project
tools=pathlib.Path(os.environ.get('MY0511_TOOLCHAIN',str(workspace/'work/toolchain')))
jdk=pathlib.Path(os.environ.get('MY0511_JDK',str(tools/'jdk/Contents/Home')))
java=jdk/'bin/java';bt=tools/'build-tools';android=tools/'platform/android.jar';jsoup=tools/'jsoup.jar'
build=workspace/'work/lite-build';build.mkdir(exist_ok=True)
for name in ['compiled','generated','classes','dex']:
 d=build/name
 if d.exists():shutil.rmtree(d)
 d.mkdir()
def run(*args):subprocess.run([str(a) for a in args],check=True)
run(bt/'aapt2','compile','--dir',project/'app/src/main/res','-o',build/'compiled')
run(bt/'aapt2','link','-o',build/'resources.apk','-I',android,'--manifest',project/'app/src/main/AndroidManifest.xml','--java',build/'generated','-A',project/'app/src/main/assets',*sorted((build/'compiled').glob('*.flat')))
source=list((project/'app/src/main/java').rglob('*.java'))+list((build/'generated').rglob('*.java'))
run(jdk/'bin/javac','--release','8','-encoding','UTF-8','-classpath',str(android)+os.pathsep+str(jsoup),'-d',build/'classes',*source)
with zipfile.ZipFile(build/'classes.jar','w') as jar:
 for f in (build/'classes').rglob('*.class'):jar.write(f,str(f.relative_to(build/'classes')))
run(java,'-cp',bt/'lib/d8.jar','com.android.tools.r8.D8','--min-api','26','--lib',android,'--output',build/'dex',build/'classes.jar',jsoup)
shutil.copy2(build/'resources.apk',build/'unsigned.apk')
with zipfile.ZipFile(build/'unsigned.apk','a',zipfile.ZIP_DEFLATED) as apk:
 for f in (build/'dex').glob('*.dex'):apk.write(f,f.name)
run(bt/'zipalign','-f','4',build/'unsigned.apk',build/'aligned.apk')
keys=workspace/'work/signing';keys.mkdir(exist_ok=True);key=keys/'my0511-lite-test.jks'
if not key.exists():run(jdk/'bin/keytool','-genkeypair','-keystore',key,'-storepass','android','-keypass','android','-alias','lite-test','-keyalg','RSA','-keysize','2048','-validity','3650','-dname','CN=My0511Lite Development, O=Local Personal Build, C=IE')
out=workspace/'outputs/My0511Lite-G-0.2.2-test.apk'
run(java,'-jar',bt/'lib/apksigner.jar','sign','--ks',key,'--ks-key-alias','lite-test','--ks-pass','pass:android','--key-pass','pass:android','--v1-signing-enabled','true','--v2-signing-enabled','true','--v3-signing-enabled','true','--out',out,build/'aligned.apk')
verified=subprocess.run([str(java),'-jar',str(bt/'lib/apksigner.jar'),'verify','--verbose','--print-certs',str(out)],check=True,text=True,capture_output=True).stdout
(workspace/'outputs/My0511Lite-G-0.2.2-signature.txt').write_text('Edition: G\n'+verified)
b=out.read_bytes();summary={'edition':'G','filename':out.name,'bytes':len(b),'hashes':{h:hashlib.new(h,b).hexdigest() for h in ['sha256','sha1','md5']},'signing':'local development key; not an official MY0511 certificate','jdk':str(jdk),'toolchain':json.loads((tools/'provenance.json').read_text())}
(workspace/'outputs/My0511Lite-G-0.2.2-build.json').write_text(json.dumps(summary,ensure_ascii=False,indent=2))
(workspace/'outputs/My0511Lite-G-0.2.2-hashes.txt').write_text('Edition: G\n'+out.name+'\n'+''.join(k.upper()+': '+v+'\n' for k,v in summary['hashes'].items()))
print(verified);print('Built',out,'bytes',len(b))
