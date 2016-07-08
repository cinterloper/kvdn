for ver in 3.3.1 3.2.1 3.3.0; do VERTX_VERSION=$ver ./gradlew clean shadowJar publish; done
