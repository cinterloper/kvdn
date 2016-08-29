function send_message() {
  /opt/nxc/nxc.js -c http://$CORNERSTONE_HOST:6500/eb/ -n $RETURN_ADDR
}
function listen_channels() {
  /opt/nxc/nxc.js -c http://$CORNERSTONE_HOST:6500/eb/ -l -n $LAUNCHID
}

