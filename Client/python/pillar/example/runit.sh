docker run -t -i -e JCEKS_KEYSTORE_PASS -v $(pwd)/keystore.jceks:/keystore.jceks -p 6500:6500 -v /run/docker.sock:/run/docker.sock -v $(pwd)/project.json:/project.json cinterloper/cornerstone-base bash -c "jar uf /opt/cornerstone.jar project.json; java -Dvertx.logger-delegate-factory-class-name=io.vertx.core.logging.SLF4JLogDelegateFactory -jar /opt/cornerstone.jar -d -l TRACE /project.json" 

