FROM cinterloper/bash-json
ARG PROJVER
ENV PROJVER ${PROJVER}
RUN apt-get update && apt-get upgrade -y && apt-get install -y openjdk-8-jdk axel maven jq nodejs gradle groovy2 && apt clean
ENV TERM xterm
RUN mkdir -p /opt/kvdn/{bin,lib}
ADD build/libs/kvdn-$PROJVER-fat.jar /opt/kvdn.jar
ADD Client /opt/kvdn/lib/Client
ENV JAVA_HOME=/usr/lib/jvm/java-8-openjdk
CMD java -Dvertx.logger-delegate-factory-class-name=io.vertx.core.logging.SLF4JLogDelegateFactory  -jar /opt/kvdn.jar
