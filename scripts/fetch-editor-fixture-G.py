# Edition: G
"""Fetch public licensed website scripts for local tests only; never ship them in the app."""
import pathlib,json,hashlib,subprocess,os
p=pathlib.Path(__file__).resolve().parents[1];w=p.parent.parent if p.parent.name=='outputs' else p
folder=w/'work';folder.mkdir(exist_ok=True)
for source in json.loads((p/'tests/editor-sources-G.json').read_text())['sources']:
 target=folder/source['file'];temp=target.with_suffix('.download')
 subprocess.run(['curl','-fLsS','--max-time','45',source['url'],'-o',str(temp)],check=True)
 if hashlib.sha256(temp.read_bytes()).hexdigest()!=source['sha256']:
  temp.unlink();raise SystemExit('G: upstream editor changed; inspect before accepting new fixture')
 os.replace(temp,target)
print('G: three public editor scripts verified; test-only files saved under work/')
