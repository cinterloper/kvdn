import httplib2

CONF = {
    'baseurl': 'https://localhost:6500',
    'token': None,
    'prefix': '',
    'set':'raw'
}

class kvdn_client:
    def __init__(self, **kwargs):
        for key,value in kwargs.iteritems():
            CONF[key]=value
    def set(self, straddr, key, data, **kwargs):
        if CONF['set'] == 'raw':
            return self.setRaw(straddr,key,data,**kwargs)
        elif CONF['set'] == 'json':
            return self.setJson(straddr,key,data,**kwargs)

    def get(self, straddr, key, **kwargs):
        h = httplib2.Http()
        resp, content = h.request(CONF['baseurl'] +CONF['prefix'] +'/X/'+ straddr +'/' +key)
        return content

    def setRaw(self, straddr, key, data, **kwargs):
        h = httplib2.Http()
        resp, content = h.request(CONF['baseurl'] +CONF['prefix'] +'/R/'+ straddr +'/' +key, 'PUT', body=data)
        return content
    def setJson(self, straddr, key, data, **kwargs):
        h = httplib2.Http()
        resp, content = h.request(CONF['baseurl'] +CONF['prefix'] +'/X/'+ straddr +'/' +key, 'PUT', body=data)
        return content

    def getKeys(self, straddr, **kwargs):
        h = httplib2.Http()
        resp, content = h.request(CONF['baseurl'] +CONF['prefix'] +'/KEYS/'+ straddr +'/' )
        return content

    def delete(self, straddr, key, **kwargs):
        h = httplib2.Http()
        resp, content = h.request(CONF['baseurl'] +CONF['prefix'] +'/X/'+ straddr +'/' +key, 'DELETE', body='')
        return content

    def submit_cas(self, straddr, data, **kwargs):
        h = httplib2.Http()
        resp, content = h.request(CONF['baseurl'] +CONF['prefix'] +'/X/'+ straddr +'/' , 'POST', body=data)
        return content #the returned content should be the hash of data as a key

    def submit_uuid(self, straddr, data, **kwargs):
        h = httplib2.Http()
        resp, content = h.request(CONF['baseurl'] +CONF['prefix'] +'/U/'+ straddr +'/' , 'POST', body=data)
        return content # the returned content should be a uuid key
