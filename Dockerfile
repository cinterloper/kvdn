FROM env/jvmdev
ARG JAR
ENV JAR ${JAR}
RUN apt-get update && apt-get install -y openjdk-8-jdk axel maven jq nodejs npm gradle groovy2 && apt clean
ENV TERM xterm
RUN mkdir -p /opt/kvdn/{bin,lib}
ADD $JAR /opt/kvdn.jar
ADD Client /opt/kvdn/lib/Client
ENV JAVA_HOME=/usr/lib/jvm/java-8-openjdk
CMD java -Dvertx.logger-delegate-factory-class-name=io.vertx.core.logging.SLF4JLogDelegateFactory  -jar /opt/kvdn.jar
