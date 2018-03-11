./gradlew clean util:jar util:publish;DISABLE_EXTENSIONS=true ./gradlew --info clean shadowJar publish test && echo built base &&
	./gradlew service-proxy:generateserviceproxy service-proxy:shadowJar service-proxy:publish && echo built proxy &&
	./gradlew service-proxy:generateserviceproxy service-proxy:shadowJar service-proxy:publish && echo built proxy &&
        ./gradlew  server:clean server:shadowJar server:publish &&
	./gradlew  extensions/mapdb:shadowJar publish
