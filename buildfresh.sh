JAVA_HOME=/core/software/g/graalvm/
set -e
 ./gradlew -DDISABLE_EXTENSIONS=true --info clean util:jar util:publish; ./gradlew -DDISABLE_EXTENSIONS=true --info clean shadowJar publish test && echo built base &&
	./gradlew --info service-proxy:generateserviceproxy service-proxy:shadowJar service-proxy:publish && echo built proxy &&
        ./gradlew --info server:clean server:shadowJar server:publish && echo built server &&
        for entry in $(ls extensions/storage/)
        do
        set -x
         ./gradlew --info extensions/storage/$entry:shadowJar extensions/storage/$entry:publish && echo build extensions/storage/$entry
        set +x
        done
