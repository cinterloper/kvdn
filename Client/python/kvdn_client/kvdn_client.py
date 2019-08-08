from abc import ABC, abstractmethod

import six
import traceback
import logging
import requests
import sys
import json
import shelve

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
    'debug': False,
    'prefix': '',
    'set_mode': 'raw',
    'headers': {},
    'logger': dl,
    'verify': True,  # this can be False or a cacert path
    'timeout': 15,
    'cert': None  # pem or ('/path/client.cert', '/path/client.key')
}


class AbstractClient(ABC):
    kvdn_py_version = '2.8.0'

    @abstractmethod
    def set(self, straddr, key, data, **kwargs):
        pass

    @abstractmethod
    def version(self):
        pass

    @abstractmethod
    def get(self, straddr, key, **kwargs):
        pass

    @abstractmethod
    def setRaw(self, straddr, key, data, type='text/plain', **kwargs):
        pass

    @abstractmethod
    def setJson(self, straddr, key, data, type='application/json', **kwargs):
        pass

    @abstractmethod
    def getKeys(self, straddr, **kwargs):
        pass

    @abstractmethod
    def delete(self, straddr, key, **kwargs):
        pass

    @abstractmethod
    def submit_cas(self, straddr, data, type='text/plain', **kwargs):
        pass

    @abstractmethod
    def submit_bulk(self, straddr, data, type='text/plain', **kwargs):
        pass

    @abstractmethod
    def submit_uuid(self, straddr, data, type='text/plain', **kwargs):
        pass

    def _close_hook(self):
        pass


class local_storage(AbstractClient):
    kvdn_py_version = '2.8.0'
    CONF = {}
    db: shelve.Shelf = None

    def __init__(self, **kwargs):
        for key, value in six.iteritems(kwargs):
            CONF[key] = value
        print(json.dumps(list(CONF.keys())))
        self.db = shelve.open(CONF["dbpath"])

    def _close_hook(self):
        # self.db.close()
        pass

    def _getmap(self, straddr) -> dict:
        map: dict = {}
        try:
            map: dict = self.db[straddr]
            return map
        except(KeyError):
            return {}

    def set(self, straddr, key, data, **kwargs):
        map: dict = self._getmap(straddr)

        map[key] = data
        self.db[straddr] = map
        return key

    def version(self):
        return "2.8.0"

    def get(self, straddr, key, **kwargs):
        try:
            map: dict = self._getmap(straddr)
            return map[key]
        except(KeyError):
            return None

    def setRaw(self, straddr, key, data, type='text/plain', **kwargs):
        map: dict = self._getmap(straddr)

        map[key] = data
        self.db[straddr] = map

        return key

    def setJson(self, straddr, key, data, type='application/json', **kwargs):
        map: dict = self._getmap(straddr)

        map[key] = data
        self.db[straddr] = map

        return key

    def getKeys(self, straddr, **kwargs):

        map: dict = self._getmap(straddr)
        if straddr == '_/METADATA_MAP':
            return json.dumps(list(self.db.keys()))
        return json.dumps(list(map.keys()))

    def delete(self, straddr, key, **kwargs):
        map: dict = self._getmap(straddr)

        map[key] = None
        self.db[straddr] = map

        return key

    def submit_cas(self, straddr, data: str, type='text/plain', **kwargs):
        import hashlib
        key = hashlib.sha256(data.encode("UTF-8")).hexdigest()

        map: dict = self._getmap(straddr)

        map[key] = data
        self.db[straddr] = map

        return key

    def submit_bulk(self, straddr, data, type='text/plain', **kwargs):
        map: dict = self._getmap(straddr)

        d: dict = json.loads(data)
        for k, v in d.items():
            map[k] = v
        self.db[straddr] = map

        return True

    def submit_uuid(self, straddr, data, type='text/plain', **kwargs):
        import uuid
        u = str(uuid.uuid4())
        map: dict = self._getmap(straddr)

        map[u] = data
        self.db[straddr] = map

        return u

    def __del__(self):
        self.db.close()


class kvdn_client(AbstractClient):
    kvdn_py_version = '2.8.0'

    def __init__(self, **kwargs):
        for key, value in six.iteritems(kwargs):
            CONF[key] = value
        if CONF['token'] is not None:
            CONF['headers'].update({'Authorization': 'Bearer %s' % CONF['token']})
        self.session = requests.Session()
        self.SVER = self.session.get(CONF['baseurl'] + CONF['prefix'] + '/__VERSION', verify=CONF['verify'],
                                     timeout=CONF['timeout'], cert=CONF['cert'], headers=CONF['headers'])

    def set(self, straddr, key, data, **kwargs):
        if CONF['set_mode'] == 'raw':
            return self.setRaw(straddr, key, data, **kwargs)
        elif CONF['set_mode'] == 'json':
            return self.setJson(straddr, key, data, **kwargs)

    def version(self):
        return self.SVER.text

    def get(self, straddr, key, **kwargs):
        resp, content = kvdn_req(self.session, CONF['baseurl'] + CONF['prefix'] + '/X/' + straddr + '/' + key,
                                 headers=CONF['headers'])
        return content

    def setRaw(self, straddr, key, data, type='text/plain', **kwargs):
        resp, content = kvdn_req(self.session, CONF['baseurl'] + CONF['prefix'] + '/R/' + straddr + '/' + key,
                                 method='PUT', data=data, headers=CONF['headers'].update({'Content-Type': type}))
        return content

    def setJson(self, straddr, key, data, type='application/json', **kwargs):
        resp, content = kvdn_req(self.session, CONF['baseurl'] + CONF['prefix'] + '/X/' + straddr + '/' + key,
                                 method='PUT', data=data, headers=CONF['headers'].update({'Content-Type': type}))
        return content

    def getKeys(self, straddr, **kwargs):
        resp, content = kvdn_req(self.session, CONF['baseurl'] + CONF['prefix'] + '/KEYS/' + straddr + '/',
                                 headers=CONF['headers'])
        return content

    def delete(self, straddr, key, **kwargs):
        resp, content = kvdn_req(self.session, CONF['baseurl'] + CONF['prefix'] + '/X/' + straddr + '/' + key,
                                 method='DELETE', data='', headers=CONF['headers'])
        return content

    def submit_cas(self, straddr, data, type='text/plain', **kwargs):
        resp, content = kvdn_req(self.session, CONF['baseurl'] + CONF['prefix'] + '/X/' + straddr, method='POST',
                                 data=data, headers=CONF['headers'].update({'Content-Type': type}))
        return content  # the returned content should be the hash of data as a key

    def submit_bulk(self, straddr, data, type='text/plain', **kwargs):
        resp, content = kvdn_req(self.session, CONF['baseurl'] + CONF['prefix'] + '/X/' + straddr + '/', method='POST',
                                 data=data, headers=CONF['headers'].update({'Content-Type': type}))
        return content  # the returned content should be the hash of data as a key

    def submit_uuid(self, straddr, data, type='text/plain', **kwargs):
        resp, content = kvdn_req(self.session, CONF['baseurl'] + CONF['prefix'] + '/U/' + straddr, method='POST',
                                 data=data, headers=CONF['headers'].update({'Content-Type': type}))
        return content  # the returned content should be a uuid key


def kvdn_req(session, url, method=None, data=None, **kwargs):
    if CONF['debug'] is True:
        import pprint
        pprint.PrettyPrinter(indent=4).pprint(kwargs)
        pprint.PrettyPrinter(indent=4).pprint(CONF)
    if method is None:
        method = 'GET'
    req = requests.Request(method, url, data=data, headers=CONF['headers'])
    try:
        p = req.prepare()
        s = session
        resp = s.send(p, verify=CONF['verify'], timeout=CONF['timeout'], cert=CONF['cert'])
        return resp, resp.text
    except:
        CONF['logger'].error("could not make kvdn request " + traceback.format_exc())
        return "", ""
