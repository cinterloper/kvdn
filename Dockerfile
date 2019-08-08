FROM ubuntu:bionic
ARG JAR
ENV JAR ${JAR}
ARG GRAAL
ENV GRAAL "https://github.com/oracle/graal/releases/download/vm-19.1.1/graalvm-ce-linux-amd64-19.1.1.tar.gz"
SHELL ["/bin/bash","-c"]
RUN apt-get update && apt-get install -y openjdk-8-jre-headless iproute2 dnsutils wget
ENV TERM xterm
RUN echo JAR: $JAR
RUN mkdir -p /opt/kvdn/{bin,lib}
RUN cd /opt && wget $GRAAL && tar -zxvf graalvm-ce-*.tar.gz && ln -sf $(echo $GRAAL | rev | cut -d '/' -f 1 | cut -d '.' -f 3- | rev ) graal
ADD $JAR /opt/kvdn/bin
ADD Client /opt/kvdn/lib/Client
ADD examples/config /opt/kvdn/config

ENV JAVA_HOME=/usr/lib/jvm/java-8-openjdk
RUN cd /opt/kvdn/bin/extensions/storage/xodus/build/libs/extensions/storage/ && ln *fat.jar /opt/kvdn/bin/kvdn.jar

CMD java -Dvertx.logger-delegate-factory-class-name=io.vertx.core.logging.Log4j2LogDelegateFactory   -jar /opt/kvdn/bin/kvdn.jar

