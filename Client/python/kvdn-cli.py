import kvdn_client
import fileinput
import argparse
import sys,os
parser = argparse.ArgumentParser(description='KVDN PYCLIENT')
parser.add_argument('--set', action='store_true',  help='set a value', )
parser.add_argument('--delete', action='store_true',  help='delete a value', )
parser.add_argument('straddr', type=str,  help='the kvdn map address to work with')
parser.add_argument('--key', type=str, default='', help='the kvdn key to work with')

s=''
args = parser.parse_args()
if(args.set):
    s = sys.stdin.read()
_baseurl=''
_token=''
try:
    _baseurl=os.environ["KVDN_BASE_URL"]
except KeyError:
    _baseurl='http://localhost:6500'
    sys.stderr.write( "using default url of" + _baseurl)

try:
    _token=os.environ["JWT_TOKEN"]
    k = kvdn_client.kvdn_client(baseurl=_baseurl, token=_token)
except KeyError:
    sys.stderr.write( 'JWT_TOKEN not set')
    k = kvdn_client.kvdn_client(baseurl=_baseurl)




if(args.set):
    print k.set(args.straddr,args.key,s)
else:
    if(args.delete):
        print k.delete(args.straddr, args.key)
    elif(args.key):
        print k.get(args.straddr, args.key)
    else:
        print k.getKeys(args.straddr)