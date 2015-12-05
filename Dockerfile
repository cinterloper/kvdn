FROM nfnty/arch-mini
RUN pacman -Syu --noconfirm
RUN pacman -S --noconfirm extra/jre8-openjdk extra/jdk8-openjdk gradle jq 
ADD . /opt/kvdn
ENV JAVA_HOME=/usr/lib/jvm/java-8-openjdk
RUN cd /opt/kvdn; ./gradlew clean; ./gradlew shadowJar
CMD java -jar /opt/kvdn/build/libs/kvdn-0.1-fat.jar -cluster
