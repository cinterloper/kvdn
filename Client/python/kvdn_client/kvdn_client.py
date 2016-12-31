
import traceback
import logging
import requests
import sys

ch = logging.StreamHandler()
ch.setLevel(logging.ERROR)
dl = logging.getLogger(__name__)
dl.addHandler(ch)

def merge(x, y):
    z = x.copy()
    z.update(y)
    return z


CONF = {
    'baseurl': 'https://localhost:6500',
    'token': None,
    'prefix': '',
    'set':'raw',
    'headers':{},
    'logger': dl,
    'verify': True, # this can be False or a cacert path
    'timeout':15,
    'cert':None # pem or ('/path/client.cert', '/path/client.key')
}

class kvdn_client:
    kvdn_py_version = '1.6.8'
    def __init__(self, **kwargs):
        for key,value in kwargs.iteritems():
            CONF[key]=value
        if CONF['token'] is not None:
            CONF['headers'].update({'Authorization': 'Bearer %s' % CONF['token']})
        self.session=requests.Session()
        self.SVER=self.session.get(CONF['baseurl'] +CONF['prefix'] + '/__VERSION', verify=CONF['verify'],timeout=CONF['timeout'],cert=CONF['cert'],headers=CONF['headers']).text

    def set(self, straddr, key, data, **kwargs):
        if CONF['set'] == 'raw':
            return self.setRaw(straddr,key,data,**kwargs)
        elif CONF['set'] == 'json':
            return self.setJson(straddr,key,data,**kwargs)

    def version(self):
        return self.SVER

    def get(self, straddr, key, **kwargs):
        resp, content = kvdn_req(self.session,CONF['baseurl'] +CONF['prefix'] +'/X/'+ straddr +'/' +key, headers=CONF['headers'])
        return content

    def setRaw(self, straddr, key, data, **kwargs):
        resp, content = kvdn_req(self.session,CONF['baseurl'] +CONF['prefix'] +'/R/'+ straddr +'/' +key, 'PUT', data=data, headers=CONF['headers'])
        return content
    def setJson(self, straddr, key, data, **kwargs):
        resp, content = kvdn_req(self.session,CONF['baseurl'] +CONF['prefix'] +'/X/'+ straddr +'/' +key, 'PUT', data=data, headers=CONF['headers'])
        return content

    def getKeys(self, straddr, **kwargs):
        resp, content = kvdn_req(self.session,CONF['baseurl'] +CONF['prefix'] +'/KEYS/'+ straddr +'/' ,  headers=CONF['headers'])
        return content

    def delete(self, straddr, key, **kwargs):
        resp, content = kvdn_req(self.session,CONF['baseurl'] +CONF['prefix'] +'/X/'+ straddr +'/' +key, 'DELETE', data='', headers=CONF['headers'])
        return content

    def submit_cas(self, straddr, data, **kwargs):
        resp, content = kvdn_req(self.session,CONF['baseurl'] +CONF['prefix'] +'/X/'+ straddr , 'POST', data=data, headers=CONF['headers'])
        return content #the returned content should be the hash of data as a key

    def submit_uuid(self, straddr, data, **kwargs):
        resp, content=kvdn_req(self.session,CONF['baseurl'] +CONF['prefix'] +'/U/'+ straddr , 'POST', data=data, headers=CONF['headers'])
        return content # the returned content should be a uuid key

def kvdn_req(session, url, method=None, data=None, **kwargs):
    if CONF['debug'] is not None or False:
        import pprint
        pprint.PrettyPrinter(indent=4).pprint(kwargs)
        pprint.PrettyPrinter(indent=4).pprint(CONF)
    if method is None:
        method = 'GET'
    req = requests.Request(method,url,data=data,headers=CONF['headers'])
    try:
        p = req.prepare()
        s=session
        resp=s.send(p,verify=CONF['verify'],timeout=CONF['timeout'],cert=CONF['cert'] )
        return resp, resp.text
    except :
        CONF['logger'].error("could not make kvdn request " +  traceback.format_exc() )
        return "", ""


