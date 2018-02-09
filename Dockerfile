FROM ubuntu:artful
ARG JAR
ENV JAR ${JAR}
RUN apt-get update && apt-get install -y openjdk-8-jre-headless
ENV TERM xterm
RUN mkdir -p /opt/kvdn/{bin,lib}
ADD $JAR /opt/kvdn.jar
ADD Client /opt/kvdn/lib/Client
ADD examples/config /opt/kvdn/config
ENV JAVA_HOME=/usr/lib/jvm/java-8-openjdk
ENTRYPOINT java -Dvertx.logger-delegate-factory-class-name=io.vertx.core.logging.SLF4JLogDelegateFactory  -jar /opt/kvdn.jar
