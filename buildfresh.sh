./gradlew clean util:jar util:publish;DISABLE_EXTENSIONS=true ./gradlew clean shadowJar publish test && echo built base &&
	./gradlew service-proxy:generateserviceproxy service-proxy:shadowJar service-proxy:publish && echo built proxy &&
	./gradlew  extensions/mapdb:shadowJar publish
