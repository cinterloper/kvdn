FROM kvdn
RUN apt update && apt install -y salt-master supervisor
ADD master_include.yaml /tmp/master_include.yaml
RUN cat /tmp/master_include.yaml >> /etc/salt/master
ADD kvdn.yml /srv/salt/kvdn.yml
