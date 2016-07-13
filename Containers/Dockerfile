FROM nfnty/arch-mini
RUN pacman -Syu --noconfirm
RUN pacman -S --noconfirm extra/jre8-openjdk extra/jdk8-openjdk gradle jq dnsutils which awk
ENV TERM vt100
ADD project/kvdn.jar /opt/kvdn.jar
ADD project/Client /opt/Client
ENV JAVA_HOME=/usr/lib/jvm/java-8-openjdk
CMD java -jar /opt/kvdn.jar -cluster
