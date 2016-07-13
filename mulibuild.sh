for ver in 3.3.2 3.2.1; do VERTX_VERSION=$ver ./gradlew clean shadowJar publish; done
