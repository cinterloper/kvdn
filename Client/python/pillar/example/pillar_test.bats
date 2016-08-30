source /pillar_test.sh

@test "start salt-master" {
  start_master
  pgrep salt-master
  [ "$?" -eq 0 ]
}
