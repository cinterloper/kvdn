FROM shadowsystem/saltmaster
ADD example/master /etc/salt/master.d/example.conf
ADD example/project.json /
ADD example/kvdn.yml /srv/salt/kvdn.yml
ADD example/client_override.sh /opt/
RUN mkdir -p /etc/salt/modules/pillar
ADD kvdn.py /etc/salt/modules/pillar
ADD kvdn_client.py /etc/salt/modules/pillar
RUN echo '/opt/client_override.sh' >> /opt/lash/lib/chain/config/include.list
