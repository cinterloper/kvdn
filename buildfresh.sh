./gradlew clean util:jar util:publish;DISABLE_EXTENSIONS=true ./gradlew clean shadowJar publish test && ./gradlew service-proxy:generateserviceproxy service-proxy:shadowJar
