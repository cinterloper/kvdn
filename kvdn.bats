#!/usr/bin/env bats

@test "build kvdn docker image" {
  docker build -t kvdn .
  result=$?
  [ "$result" -eq 0 ]
}

@test "start a group of 3" {
  docker-compose up -d
  result=$?
  [ "$result" -eq 0 ]
}

@test "are they still running after 5 seconds?" {
  sleep 5
  result=$(docker ps   | grep -i kvdn_peer | wc -l)
  [ "$result" -ge 3 ]
}
@test "insert a value" {
  result=$(docker exec -t -i kvdn_test_1 /bin/bash -c "cd /opt/kvdn/Client/cli; echo hello | bash clip.sh -s=test/str/firstkey")
  [ $(echo "$result" | grep -c str:firstkey ) -eq 1 ]
}

@test "get keys from a peer" {
  result=$(docker exec -t -i kvdn_test_1 /bin/bash -c "curl -s http://kvdn_alpha_1:9090/KEYS/test/str/ | jq -r")
  [ $(echo "$result" | grep -c firstkey  ) -eq 1 ]
}
@test "get the value" {
  result=$(docker exec -t -i kvdn_test_1 /bin/bash -c "cd /opt/kvdn/Client/cli;bash clip.sh -g=test/str/firstkey")
  [ $(echo "$result" | grep -c hello ) -eq 1 ]
}

