"""Local-only reward demo. Uses public HTTP forms; never reads DB configuration or stores cookies."""
import argparse, http.cookiejar, json, re, sys, time
from html import unescape
from pathlib import Path
from urllib import request, parse, error

BASE = 'http://localhost:8080'
USERNAME = 'demo_rewards_20261019'
PASSWORD = 'DemoRewards_2026!'

class LocalRedirect(request.HTTPRedirectHandler):
    def redirect_request(self, req, fp, code, msg, headers, newurl):
        if parse.urlsplit(newurl).netloc != parse.urlsplit(BASE).netloc:
            raise RuntimeError('Blocked redirect outside local demo server')
        return super().redirect_request(req, fp, code, msg, headers, newurl)

client = request.build_opener(request.ProxyHandler({}), request.HTTPCookieProcessor(http.cookiejar.CookieJar()), LocalRedirect())
def http(path, params=None, ajax=False):
    headers = {'Accept': 'application/json' if ajax else 'text/html'}
    if ajax: headers['X-Requested-With'] = 'XMLHttpRequest'
    req = request.Request(BASE + path, data=None if params is None else parse.urlencode(params).encode(), headers=headers)
    try:
        with client.open(req, timeout=30) as res: return res.status, res.read().decode('utf-8'), res.url
    except error.HTTPError as ex:
        # Do not print server error bodies: they may contain internal diagnostics.
        raise RuntimeError('Local HTTP ' + str(ex.code) + ' at ' + parse.urlsplit(path).path) from None

def csrf(html):
    match = re.search(r'name="_csrf"\s+value="([^"]+)"', html)
    if not match: raise RuntimeError('CSRF form field not found')
    return unescape(match.group(1))

def login(register=False):
    html = http('/auth?action=' + ('register' if register else 'login'))[1]
    values = {'action': 'register' if register else 'login', 'username': USERNAME, 'password': PASSWORD, '_csrf':csrf(html)}
    if register: values.update(email=USERNAME+'@example.invalid', confirm=PASSWORD, agree='yes')
    _, page, url = http('/auth', values)
    if 'action=login' in url or 'id="form-register"' in page:
        raise RuntimeError('Demo authentication failed; no credentials or response body printed')
    return page

def summary(html):
    result = {}
    for key in ['wallet-balance','today-earned','total-earned','claimable-count','claimable-coins']:
        match = re.search(r'data-'+key+r'>([0-9.,]+)<', html)
        if match: result[key] = int(re.sub('[^0-9]', '', match.group(1)))
    date = re.search(r'data-reward-date>([^<]+)<', html)
    if date: result['date']=date.group(1)
    result['pending-achievements'] = re.findall(r'<article data-achievement="([^"]+)"[^>]*data-earned="true" data-claimed="false"[^>]*data-coins="[1-9][0-9]*"',html)
    return result

def links(html, pathname):
    result=[]
    for href in re.findall(r'href="([^"]+)"',html):
        href=unescape(href);url=parse.urlsplit(href)
        if url.path==pathname and not url.netloc and href not in result: result.append(href)
    return result

def emit(label,data):
    print(json.dumps({'step':label,**data},ensure_ascii=False),flush=True)

def main():
    parser=argparse.ArgumentParser();parser.add_argument('phase',choices=['register','prepare','checkin','milestone','daily','verify']);args=parser.parse_args()
    login(args.phase=='register')
    html=http('/user?action=achievements')[1]
    before=summary(html)
    if 'date' not in before: raise RuntimeError('Reward summary missing: current app did not render the updated view')
    emit('before',before)
    if args.phase=='register': return
    if args.phase=='prepare':
        home=http('/')[1]
        stories=[href for href in links(home,'/story') if parse.parse_qs(parse.urlsplit(href).query).get('action')==['detail']]
        stories=list(dict.fromkeys(stories))
        if len(stories)<5: raise RuntimeError('Need five public stories for collector demo')
        token=csrf(html)
        for href in stories[:5]:
            story_id=parse.parse_qs(parse.urlsplit(href).query)['id'][0]
            result=json.loads(http('/bookmark',{'action':'add','storyId':story_id,'_csrf':token},True)[1])
            if not result.get('success'): raise RuntimeError('Could not bookmark demo story')
        chapter_id=None
        last_unit=0
        for href in stories[:10]:
            detail=http(href)[1]
            for chapter_link in links(detail,'/chapter')[:3]:
                chapter=http(chapter_link)[1]
                if 'vipUnlockForm-' in chapter or 'data-para=' not in chapter: continue
                chapter_id=parse.parse_qs(parse.urlsplit(chapter_link).query).get('id',[None])[0]
                if chapter_id:
                    last_unit=max(map(int,re.findall(r'data-para="([0-9]+)"',chapter)))
                    break
            if chapter_id: break
        if not chapter_id: raise RuntimeError('No readable text chapter found for verified reading demo')
        emit('reading-timer',{'wait-seconds':31,'chapter':chapter_id})
        time.sleep(31)
        http('/bookmark',{'action':'progress','chapterId':chapter_id,'paraNo':str(last_unit),'_csrf':token},True)
    elif args.phase in ['checkin','milestone','daily']:
        values={'action':{'checkin':'checkin','milestone':'claim-achievement','daily':'claim-quest'}[args.phase],'_csrf':csrf(html)}
        if args.phase=='milestone': values['key']='first_read'
        if args.phase=='daily': values['key']='READ_CHAPTER'
        result=json.loads(http('/user',values,True)[1]);emit('claim',result)
        if not (result.get('success') or result.get('claimed')) and args.phase!='checkin': raise RuntimeError('Claim was rejected')
        # Re-submit through server, then GET again: persisted result must not double-credit.
        duplicate=json.loads(http('/user',values,True)[1]);emit('repeat',duplicate)
        balance_key='balance' if args.phase=='milestone' else 'newBalance'
        if duplicate.get(balance_key)!=result.get(balance_key): raise RuntimeError('Repeated request changed wallet balance')
        if duplicate.get('todayEarned')!=result.get('todayEarned') or duplicate.get('totalEarned')!=result.get('totalEarned'):
            raise RuntimeError('Repeated request changed reward totals')
        delta=result[balance_key]-before['wallet-balance']
        if result['todayEarned']-before['today-earned']!=delta or result['totalEarned']-before['total-earned']!=delta:
            raise RuntimeError('Wallet delta differs from earned reward delta')
    after_html=http('/user?action=achievements')[1];after=summary(after_html)
    emit('reloaded',after)
    if args.phase in ['checkin','milestone','daily']:
        if after['wallet-balance']!=result[balance_key] or after['today-earned']!=result['todayEarned'] or after['total-earned']!=result['totalEarned']:
            raise RuntimeError('Reloaded totals do not match committed response')
    me=re.sub(r'<script\b[^>]*>[\s\S]*?</script>', '', http('/user?action=me')[1], flags=re.I)
    emit('profile',{'checkin-done':'id="checkin-completed-badge"' in me,'daily-read-claim-ready':'id="btn-claim-quest-READ_CHAPTER"' in me,'same-totals':all(summary(me).get(k)==after[k] for k in ['wallet-balance','today-earned','total-earned'])})
    wallet=http('/wallet')[1]
    emit('wallet-history',{'checkin-record':'Thưởng điểm danh' in wallet,'achievement-record':'Thưởng thành tựu' in wallet,'quest-record':'Thưởng nhiệm vụ' in wallet})
    report=Path('build/rewards-demo')/(args.phase+'.json')
    report.parent.mkdir(parents=True,exist_ok=True)
    report.write_text(json.dumps({'account':USERNAME,'before':before,'after':after},ensure_ascii=False,indent=2),encoding='utf-8')

if __name__=='__main__':
    sys.stdout.reconfigure(encoding='utf-8')
    try: main()
    except Exception as ex: print(type(ex).__name__+': '+str(ex));sys.exit(1)
