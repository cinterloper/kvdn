FROM shadowsystem/saltmaster
ADD master /etc/salt/master.d/example.conf
ADD project.json /
ADD kvdn.yml /srv/salt/kvdn.yml
ADD client_override.sh /opt/
RUN echo '/opt/client_override.sh' >> /opt/lash/lib/chain/config/include.list
