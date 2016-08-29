import httplib2

CONF = {
    'baseurl': 'https://localhost:6500',
    'token': None,
    'prefix': '',
    'set':'raw',
    'headers':{}
}

class kvdn_client:
    def __init__(self, **kwargs):
        for key,value in kwargs.iteritems():
            CONF[key]=value
        if CONF['token'] is not None:
            CONF['headers'].update({'Authorization': 'Bearer %s' % CONF['token']})
    def set(self, straddr, key, data, **kwargs):
        if CONF['set'] == 'raw':
            return self.setRaw(straddr,key,data,**kwargs)
        elif CONF['set'] == 'json':
            return self.setJson(straddr,key,data,**kwargs)

    def get(self, straddr, key, **kwargs):
        h = httplib2.Http()
        resp, content = h.request(CONF['baseurl'] +CONF['prefix'] +'/X/'+ straddr +'/' +key, headers=CONF['headers'])
        return content

    def setRaw(self, straddr, key, data, **kwargs):
        h = httplib2.Http()
        resp, content = h.request(CONF['baseurl'] +CONF['prefix'] +'/R/'+ straddr +'/' +key, 'PUT', body=data, headers=CONF['headers'])
        return content
    def setJson(self, straddr, key, data, **kwargs):
        h = httplib2.Http()
        resp, content = h.request(CONF['baseurl'] +CONF['prefix'] +'/X/'+ straddr +'/' +key, 'PUT', body=data, headers=CONF['headers'])
        return content

    def getKeys(self, straddr, **kwargs):
        h = httplib2.Http()
        resp, content = h.request(CONF['baseurl'] +CONF['prefix'] +'/KEYS/'+ straddr +'/' ,  headers=CONF['headers'])
        return content

    def delete(self, straddr, key, **kwargs):
        h = httplib2.Http()
        resp, content = h.request(CONF['baseurl'] +CONF['prefix'] +'/X/'+ straddr +'/' +key, 'DELETE', body='', headers=CONF['headers'])
        return content

    def submit_cas(self, straddr, data, **kwargs):
        h = httplib2.Http()
        resp, content = h.request(CONF['baseurl'] +CONF['prefix'] +'/X/'+ straddr , 'POST', body=data, headers=CONF['headers'])
        return content #the returned content should be the hash of data as a key

    def submit_uuid(self, straddr, data, **kwargs):
        h = httplib2.Http()
        resp, content = h.request(CONF['baseurl'] +CONF['prefix'] +'/U/'+ straddr , 'POST', body=data, headers=CONF['headers'])
        return content # the returned content should be a uuid key
