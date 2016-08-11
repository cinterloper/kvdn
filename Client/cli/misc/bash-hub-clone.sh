preexec()
{
  echo $1 | base64 | kvdn-cli.py --set -u=shell/cmds >/dev/null;
}

