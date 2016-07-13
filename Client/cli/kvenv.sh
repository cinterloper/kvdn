#!/usr/bin/env bash

if [ "$MAP" == "" ]; then
  MAP='example/path2'
fi
keys_j=$(clip.sh -k=$MAP/)
for k in $(echo $keys_j | jq -r '.[]' )
do
  export $k="$(clip.sh -g=$MAP/$k)"
done
