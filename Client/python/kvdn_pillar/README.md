The following configuration

```

'*':
  test_value1: this/that?test_key1
  test_value2: this/that?test_key2
  complex_pillar_value:
    key_1: this/that?test_key1
    key_2: this/that?test_key2
    key_4: this/other
```

maps the pillar key test_value1 to kvdn storage address 'this/that' and the key test_key1
complex_pillar_value has several subkeys mapped, and key_4 is a special key

key_4 refrences the map 'this/other'. the keys for that map are looked up, and the map is unrolled onto key4

if the kvdn value is valid json, it will be decoded and overlaid on the pillar, retaining its structure
```
$ KEY_SET='UID PWD RANDOM' encodeJson
{"PWD":"/","RANDOM":"16239","UID":"0"}

$ export KVDN_BASE_URL='http://172.17.0.1:6500'                                                                                                                                                                   

$ KEY_SET='UID PWD RANDOM' encodeJson | kvdn-cli --submit this/other
other:f528d75e20ab60b3cb8ae9f2ff454b29

$ KEY_SET='UID PWD RANDOM' encodeJson | kvdn-cli --submit this/other
other:8e2ddb28ce45502a3d69728e6f8a8db9

$ KEY_SET='UID PWD RANDOM' encodeJson | kvdn-cli --set --key test_key1 this/that   
that:test_key1

$ KEY_SET='UID PWD RANDOM' encodeJson | kvdn-cli --set --key test_key2 this/that 
that:test_key2

$ salt '*' test.ping
saltmaster:
    True
$ salt '*' pillar.items                                                                                                                                                                                                                        
saltmaster:
    ----------
    complex_pillar_value:
        ----------
        key_1:
            ----------
            PWD:
                /
            RANDOM:
                25148
            UID:
                0
        key_2:
            ----------
            PWD:
                /
            RANDOM:
                21061
            UID:
                0
        key_4:
            ----------
            546c197ef3aeb3a1bfe4c3e1b1835477:
                ----------
                PWD:
                    /
                RANDOM:
                    27214
                UID:
                    0
            75cd27626ef9610dc1de53e923e9f50a:
                ----------
                PWD:
                    /
                RANDOM:
                    25795
                UID:
                    0
    test_value1:
        ----------
        PWD:
            /
        RANDOM:
            25148
        UID:
            0
    test_value2:
        ----------
        PWD:
            /
        RANDOM:
            21061
        UID:
            0

```
