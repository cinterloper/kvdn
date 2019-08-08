class KVDNImpl( object):
   # straddr = None

    def __init__(self, straddr, OBJKEYS):
        assert straddr is not None
        self.straddr = straddr
        keys = json.loads(kv.getKeys(straddr=straddr))
        data = {}
        for key in keys:
            data[key] = kv.get(straddr=straddr, key=key)
        for k in OBJKEYS:
            if k not in keys:
                data[k] = "null"  # None ...

        super().__init__(data)

    def set(self, **kwargs):
        for key, value in six.iteritems(kwargs):
            assert self.straddr is not None
            assert key is not None
            assert value is not None
            kv.set(self.straddr, key, value)





o = dm.getObject().apply(fn).commit()