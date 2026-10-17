import os, json, urllib.request, sys

TOKEN = os.environ.get('GITHUB_TOKEN')
if not TOKEN:
    sys.exit('GITHUB_TOKEN missing')
REPO = 'Xposed-Modules-Repo/io.github.kelebao.colorosclockseconds'
API = f'https://api.github.com/repos/{REPO}'
H = {
    'Authorization': f'Bearer {TOKEN}',
    'Accept': 'application/vnd.github+json',
    'User-Agent': 'wb-release',
    'X-GitHub-Api-Version': '2022-11-28',
}

body = open(r'C:/Users/HSG/Documents/AIKF/deliverables/lsposed-repo/release-body.md', encoding='utf-8').read()
payload = json.dumps({
    'tag_name': '4-1.0.3',
    'name': '4-1.0.3',
    'body': body,
    'prerelease': False,
}).encode()

req = urllib.request.Request(f'{API}/releases', data=payload, headers=H, method='POST')
resp = urllib.request.urlopen(req)
rel = json.loads(resp.read())
rid = rel['id']
upload_url = rel['upload_url'].split('{')[0]
print('RELEASE_ID', rid, flush=True)

apk = r'C:/Users/HSG/Documents/AIKF/deliverables/ColorOSClockSeconds_1.0.3.apk'
with open(apk, 'rb') as f:
    data = f.read()
up = f'{upload_url}?name=io.github.kelebao.colorosclockseconds_1.0.3.apk'
req2 = urllib.request.Request(
    up, data=data,
    headers={**H, 'Content-Type': 'application/vnd.android.package-archive'},
    method='POST',
)
resp2 = urllib.request.urlopen(req2)
asset = json.loads(resp2.read())
print('ASSET', asset['name'], asset['size'], flush=True)
print('DONE')
