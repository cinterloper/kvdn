preexec() { echo $1 | base64 | clip.sh -u=shell/cmds >/dev/null; }

