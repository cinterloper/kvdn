 clip.sh -k=shell/cmds/ | jq -r '.[]' | while read key; do clip.sh -g=shell/cmds/$key  | base64 -d ; done
