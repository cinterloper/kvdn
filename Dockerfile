FROM nfnty/arch-mini
RUN pacman -Syu --noconfirm
RUN pacman -S --noconfirm extra/jre8-openjdk extra/jdk8-openjdk gradle jq dnsutils which awk nano
ENV TERM vt100
ADD project/build/libs/project-0.1-fat.jar /opt/kvdn.jar
ADD project/Client /opt/Client
ENV JAVA_HOME=/usr/lib/jvm/java-8-openjdk
CMD java -jar /opt/kvdn.jar -cluster
