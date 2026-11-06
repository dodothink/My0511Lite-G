# Edition: G
import urllib.request,json,pathlib,hashlib,zipfile,tarfile,xml.etree.ElementTree as E
project=pathlib.Path(__file__).resolve().parents[1]
workspace=project.parent.parent if project.parent.name=='outputs' else project
root=workspace/'work/toolchain';root.mkdir(parents=True,exist_ok=True)
def get(url):
 with urllib.request.urlopen(url,timeout=120) as f:return f.read()
def download(url,dest,algo=None,expected=None):
 if not dest.exists():
  print('Downloading',url,flush=True);tmp=dest.with_suffix('.part')
  with urllib.request.urlopen(url,timeout=120) as src,tmp.open('wb') as out:
   while True:
    chunk=src.read(1024*1024)
    if not chunk:break
    out.write(chunk)
  tmp.rename(dest)
 if expected and hashlib.new(algo,dest.read_bytes()).hexdigest()!=expected:raise ValueError('checksum mismatch: '+str(dest))
 return dest
pkg={'link':'https://aka.ms/download-jdk/microsoft-jdk-21-macos-aarch64.tar.gz'}
archive=download(pkg['link'],root/'jdk.tar.gz')
pkg['sha256_observed']=hashlib.sha256(archive.read_bytes()).hexdigest()
if not (root/'jdk').exists():
 with tarfile.open(archive) as t:
  folder=next(m.name.lstrip('./').split('/')[0] for m in t.getmembers() if m.name.lstrip('./'))
  if not (root/folder).exists():t.extractall(root)
 (root/folder).rename(root/'jdk')
repo=E.fromstring(get('https://dl.google.com/android/repository/repository2-1.xml'))
records=[]
for path,label in [('platforms;android-35','platform'),('build-tools;35.0.0','build-tools')]:
 found=None
 for rp in repo.iter('remotePackage'):
  if rp.attrib['path']!=path:continue
  for ar in rp.findall('./archives/archive'):
   host=ar.findtext('host-os')
   if host is None or host=='macosx':found=ar.find('complete');break
  if found is not None:break
 if found is None:raise ValueError(path)
 url='https://dl.google.com/android/repository/'+found.findtext('url');cs=found.find('checksum');algo=cs.attrib.get('type','sha1');expected=cs.text
 arc=download(url,root/(label+'.zip'),algo,expected)
 target=root/label
 if not target.exists():
  tmp=root/(label+'-extract');tmp.mkdir(exist_ok=True)
  with zipfile.ZipFile(arc) as z:z.extractall(tmp)
  folders=list(tmp.iterdir()); assert len(folders)==1
  folders[0].rename(target)
 records.append({'package':path,'url':url,'checksum':expected,'algorithm':algo})
jar=download('https://repo.maven.apache.org/maven2/org/jsoup/jsoup/1.18.3/jsoup-1.18.3.jar',root/'jsoup.jar')
sha=get('https://repo.maven.apache.org/maven2/org/jsoup/jsoup/1.18.3/jsoup-1.18.3.jar.sha1').decode().strip();assert hashlib.sha1(jar.read_bytes()).hexdigest()==sha
records.append({'package':'org.jsoup:jsoup:1.18.3','sha1':sha})
for f in (root/'build-tools').iterdir():
 if f.name in ['aapt2','zipalign','apksigner','d8','aapt']: f.chmod(f.stat().st_mode|0o111)
(root/'provenance.json').write_text(json.dumps({'jdk':pkg,'artifacts':records},indent=2))
print('Toolchain ready',flush=True)

if __name__=='__main__':
 testjar=download('https://repo.maven.apache.org/maven2/org/json/json/20240303/json-20240303.jar',root/'test-json.jar')
 expected=get('https://repo.maven.apache.org/maven2/org/json/json/20240303/json-20240303.jar.sha1').decode().strip()
 assert hashlib.sha1(testjar.read_bytes()).hexdigest()==expected
