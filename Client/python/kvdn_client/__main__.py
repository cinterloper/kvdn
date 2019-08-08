from __future__ import print_function
from kvdn_client import kvdn_client
import fileinput
import argparse
import sys, os
import json


def main(args=None):
    parser = argparse.ArgumentParser(description='KVDN PYCLIENT')
    parser.add_argument('--verify', type=str, default=True, help='True, False, or path to ca')
    parser.add_argument('--noverify', action='store_true', help='do not verify host certificate')
    parser.add_argument('--version', action='store_true', default=False, help='get server version')
    parser.add_argument('--cert', type=str, default='', help='pa./th to certificate')
    parser.add_argument('--type', type=str, default='text/plain', help='MIME type')
    parser.add_argument('--debug', action='store_true', help='debug')
    parser.add_argument('--set', action='store_true', help='set a value', )
    parser.add_argument('--submit', action='store_true', help='submit a value', )
    parser.add_argument('--submit_bulk', action='store_true', help='submit a bulk set', )
    parser.add_argument('--delete', action='store_true', help='delete a value', )
    parser.add_argument('--localdb', type=str, default=False, help='use localdb mode')
    parser.add_argument('straddr', type=str, help='the kvdn map address to work with')
    parser.add_argument('--key', type=str, default='', help='the kvdn key to work with')

    # if(args.localdb):
    #     from kvdn_client import sqlite_client_emulation as kvdn_client
    # else:
    #     from kvdn_client import kvdn_client
    s = ''
    args = parser.parse_args()
    if (args.set or args.submit or args.submit_bulk):
        s = sys.stdin.read()
    _baseurl = ''
    _token = ''
    varargs = vars(args)
    if varargs['verify'] is '':
        varargs['verify'] = True

    try:
        _baseurl = os.environ["KVDN_BASE_URL"]
    except KeyError:
        _baseurl = 'http://localhost:6500'
        sys.stderr.write("using default url : " + _baseurl + '\n')

    try:
        _token = os.environ["CACERT"]
        varargs["verify"] = _token
    except Exception:
        pass
    try:
        _token = os.environ["CERT"]
        try:
            _token = json.load(_token)
            varargs["verify"] = _token
        except Exception:
            varargs["cert"] = _token
    except Exception:
        pass

    # hack, for python3, @fixme ?
    if sys.version_info[0] == 3:
        if (args.localdb):
            kc = kvdn_client.local_storage
        else:
            kc = kvdn_client.kvdn_client
    else:  # python2
        # @fixme need local kvdn client cleanup here
        kc = kvdn_client
    if (args.localdb):
        k = kc(dbpath=args.localdb)
    else:
        try:
            _token = os.environ["JWT_TOKEN"]
            k = kc(baseurl=_baseurl, token=_token, **varargs)
        except KeyError:
            sys.stderr.write('JWT_TOKEN not set \n')
            k = kc(baseurl=_baseurl)

    if (args.debug):
        sys.stderr.write("KVDN VERSION: " + k.version() + "\n")

    if (args.set):
        print(k.set(args.straddr, args.key, s))
    elif (args.version):
        print(k.version())
    elif (args.submit):
        print(k.submit_cas(args.straddr, s))
    elif (args.submit_bulk):
        print(k.submit_bulk(args.straddr, s))
    else:
        if (args.delete):
            print(k.delete(args.straddr, args.key))
        elif (args.key):
            print(k.get(args.straddr, args.key))
        else:
            print(k.getKeys(args.straddr))

    k._close_hook()


if __name__ == "__main__":
    main()
