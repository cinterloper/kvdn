FROM shadowsystem/saltmaster
ADD example/master /etc/salt/master
ADD example/project.json /
ADD example/kvdn.yml /srv/salt/kvdn.yml
ADD example/client_override.sh /opt/
RUN mkdir -p /etc/salt/modules/
ADD pillar /etc/salt/modules/
RUN cd /etc/salt/modules/pillar; wget https://raw.githubusercontent.com/cinterloper/kvdn/master/Client/python/kvdn_client.py
RUN echo '/opt/client_override.sh' >> /opt/lash/lib/chain/config/include.list
RUN rm -rf /srv/salt/master.d
