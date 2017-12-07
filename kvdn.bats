#!/usr/bin/env bats



#@test "build kvdn" {
#  cd project/; ./gradlew clean; ./gradlew shadowJar
#  result=$?
#  [ "$result" -eq 0 ]
#}

@test "build kvdn docker image" {
  docker build -t kvdn .
  result=$?
  [ "$result" -eq 0 ]
}

@test "start a group of kv servers" {
  docker-compose up -d
  result=$?
  [ "$result" -eq 0 ]
}

@test "are they still running after 10 seconds?" {
  sleep 10 #should verify we can talk to them here 
  result=$(docker ps   | grep -i _t | wc -l)
  [ "$result" -ge 3 ]
}
@test "insert a value" {
  result=$(docker exec -t -i kvdn_test /bin/bash -c "cd /opt/Client/cli; cp alpha.sh config.sh; echo hello | bash clip.sh -s=test/str/firstkey")
  [ $(echo "$result" | grep -c str:firstkey ) -eq 1 ]
}
@test "get keys" {
  result=$(docker exec -t -i kvdn_test /bin/bash -c "cd /opt/Client/cli; cp beta.sh config.sh; bash clip.sh -k=test/str/")
  echo $result >> /tmp/res
  [ $(echo "$result" | grep -c firstkey ) -eq 1 ]
}

@test "get the value" {
  result=$(docker exec -t -i kvdn_test /bin/bash -c "cd /opt/Client/cli; cp gamma.sh config.sh; bash clip.sh -g=test/str/firstkey")
  [ $(echo "$result" | grep -c hello ) -eq 1 ]
}

@test "cleanup instances" {
  docker-compose kill
  result=$?
  [ "$result" -eq 0 ]
}
