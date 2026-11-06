# Edition: G
import pathlib,subprocess,os
p=pathlib.Path(__file__).resolve().parents[1];w=p.parent.parent if p.parent.name=='outputs' else p;tools=pathlib.Path(os.environ.get('MY0511_TOOLCHAIN',str(w/'work/toolchain')));jdk=pathlib.Path(os.environ.get('MY0511_JDK',str(tools/'jdk/Contents/Home')));out=w/'work/core-tests';out.mkdir(exist_ok=True)
src=p/'app/src/main/java/local/my0511/lite';files=[src/(n+'.java') for n in ['Task','ForumPolicy','ForumParser','SessionInfo','BulkPaste','PublishRules']]+[p/'tests/CoreTest.java',p/'tests/SessionTest.java',p/'tests/RulesTest.java']
subprocess.run([str(jdk/'bin/javac'),'--release','8','-encoding','UTF-8','-cp',str(tools/'jsoup.jar'),'-d',str(out),*map(str,files)],check=True)
cmd=[str(jdk/'bin/java'),'-ea','-cp',str(out)+os.pathsep+str(tools/'jsoup.jar'),'local.my0511.lite.CoreTest',str(p/'tests/fixtures')]
if (w/'work/board-utf8.html').exists():cmd.append(str(w/'work'))
subprocess.run(cmd,check=True)
subprocess.run([str(jdk/'bin/java'),'-ea','-cp',str(out)+os.pathsep+str(tools/'jsoup.jar'),'local.my0511.lite.SessionTest'],check=True)
subprocess.run(['node',str(p/'tests/forms.test.js')],check=True)

cp=str(tools/'jsoup.jar')+os.pathsep+str(tools/'test-json.jar')
storefiles=files[:4]+[src/'TaskStore.java',p/'tests/StoreTest.java']+list((p/'tests/stubs').rglob('*.java'))
subprocess.run([str(jdk/'bin/javac'),'--release','8','-encoding','UTF-8','-cp',cp,'-d',str(out),*map(str,storefiles)],check=True)
subprocess.run([str(jdk/'bin/java'),'-ea','-cp',str(out)+os.pathsep+cp,'local.my0511.lite.StoreTest'],check=True)

subprocess.run([str(jdk/'bin/java'),'-ea','-cp',str(out)+os.pathsep+str(tools/'jsoup.jar'),'local.my0511.lite.RulesTest'],check=True)
