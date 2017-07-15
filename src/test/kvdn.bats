#!/usr/bin/env bats


@test "build kvdn" {
  cd project/; ./gradlew clean shadowJar
  result=$?
  if [ $result -eq 0 ]; then
    run rm kvdn.jar
    ln build/libs/*fat.jar kvdn.jar
  fi
  [ $result -eq 0 ]
}

@test "remove old containers" {
  docker-compose rm -f
  result=$?
  [ "$result" -eq 0 ]
}

@test "build kvdn docker $DKRGS image" {
  docker $DKRGS build --no-cache -t kvdn .
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
  result=$(docker $DKRGS ps   | grep -i _t | wc -l)
  [ "$result" -ge 3 ]
}
@test "insert a value" {
  result=$(docker $DKRGS exec -t -i kvdn_test /bin/bash -c "cd /opt/Client/cli; cp alpha.sh config.sh; echo hello | bash clip.sh -s=test/str/firstkey")
  echo $result > /tmp/result.res
  [ $(echo "$result" | grep -c firstkey ) -eq 1 ]
}
@test "get keys" {
  result=$(docker $DKRGS exec -t -i kvdn_test /bin/bash -c "cd /opt/Client/cli; cp beta.sh config.sh; bash clip.sh -k=test/str/")
  echo $result >> /tmp/res
  [ $(echo "$result" | grep -c firstkey ) -eq 1 ]
}

@test "get the value" {
  result=$(docker $DKRGS exec -t -i kvdn_test /bin/bash -c "cd /opt/Client/cli; cp gamma.sh config.sh; bash clip.sh -g=test/str/firstkey")
  [ $(echo "$result" | grep -c hello ) -eq 1 ]
}

@test "cleanup instances" {
  # docker-compose kill
  result=$?
  [ "$result" -eq 0 ]
}
