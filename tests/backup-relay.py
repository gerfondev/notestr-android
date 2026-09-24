import asyncio,json
from pathlib import Path
from websockets.asyncio.server import serve
store={}; traffic=[]
async def relay(ws):
    path=ws.request.path
    async for raw in ws:
        msg=json.loads(raw)
        if msg[0]=='EVENT':
            e=msg[1];kind=e['kind'];accepted=not(path=='/reject' and kind==30078)
            traffic.append({'relay':path,'kind':kind,'accepted':accepted,'id':e['id'],'tags':e['tags']})
            Path('/tmp/notestr-backup-relay-traffic.json').write_text(json.dumps(traffic))
            if accepted:store[e['id']]=e
            await ws.send(json.dumps(['OK',e['id'],accepted,'' if accepted else 'blocked: synthetic backup rejection']))
        elif msg[0]=='REQ':
            sub=msg[1];f=msg[2]
            for e in list(store.values()):
                if path != '/empty' and e['pubkey'] in f.get('authors',[e['pubkey']]) and e['kind'] in f.get('kinds',[e['kind']]):
                    await ws.send(json.dumps(['EVENT',sub,e]))
            await ws.send(json.dumps(['EOSE',sub]))
async def main():
    async with serve(relay,'127.0.0.1',18765):
        print('Synthetic local relay ready',flush=True)
        await asyncio.Future()
asyncio.run(main())
