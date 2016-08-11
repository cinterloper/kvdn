preexec()
{
  echo $1 | base64 | kvdn-cli.py --submit shell/cmds > /dev/null;
}

