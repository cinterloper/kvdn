# -*- coding: utf-8 -*-
"""
Use KVDN secrets as a Pillar source


based on https://github.com/ripple/salt-pillar-vault/blob/master/pillar/vault.py


Example Configuration
---------------------

The KVDN server should be defined in the master config file with the
following options:

.. code-block:: yaml

    ext_pillar:
      - KVDN:
          url: https://KVDN:8200
          config: Local path or salt:// URL to secret configuration file
          token: Explicit token for token authentication
          app_id: Application ID for app-id authentication
          user_id: Explicit User ID for app-id authentication
          user_file: File to read for user-id value
          unset_if_missing: Leave pillar key unset if KVDN secret not found

The ``url`` parameter is the full URL to the KVDN API endpoint.

The ``config`` parameter is the path or salt:// URL to the secret map YML file.

The ``token`` parameter is an explicit token to use for authentication, and it
overrides all other authentication methods.

The ``app_id`` parameter is an Application ID to use for app-id authentication.

The ``user_id`` parameter is an explicit User ID to pair with ``app_id`` for
app-id authentication.

The ``user_file`` parameter is the path to a file on the master to read for a
``user-id`` value if ``user_id`` is not specified.

The ``unset_if_missing`` parameter determins behavior when the KVDN secret is
missing or otherwise inaccessible. If set to ``True``, the pillar key is left
unset. If set to ``False``, the pillar key is set to ``None``. Default is
``False``

Mapping KVDN Secrets to Minions
--------------------------------

The ``config`` parameter, above, is a path to the YML file which will be
used for mapping secrets to minions. The map uses syntax similar to the
top file:

.. code-block:: yaml

    'filter':
      'variable': 'path'
      'variable': 'path?key'
    'filter':
      'variable': 'path?key'


Each ``filter`` is a compound matcher:
    https://docs.saltstack.com/en/latest/topics/targeting/compound.html

``variable`` is the name of the variable which will be injected into the
pillar data.

``path`` is the path the desired secret on the KVDN server.

``key`` is optional. If specified, only this specific key will be returned
for the secret at ``path``. If unspecified, the entire secret json structure
will be returned.


.. code-block:: yaml

    'web*':
      'ssl_cert': '/secret/certs/domain?certificate'
      'ssl_key': '/secret/certs/domain?private_key'
    'db* and G@os.Ubuntu':
      'db_pass': '/secret/passwords/database

"""

# Import stock modules
from __future__ import absolute_import
import base64
import logging
import os
import yaml

# Import salt modules
import salt.loader
import salt.minion
import salt.template
import salt.utils.minions
import kvdn_client

# Set up logging
LOG = logging.getLogger(__name__)

# Default config values
CONF = {
    'url': 'https://KVDN:8200',
    'config': '/srv/salt/kvdn.yml',
    'token': None,
    'app_id': None,
    'user_id': None,
    'user_file': None,
    'unset_if_missing': False
}

def __virtual__():
    return True




def _authenticate(conn):
    """ Determine the appropriate authentication method and authenticate
        for a token, if necesssary.
    """

    # Check for explicit token, first
    if CONF["token"]:
        conn["token"] = CONF["token"]
    # Check for token in ENV
    elif os.environ.get('KVDN_TOKEN'):
        conn["token"] = os.environ.get('KVDN_TOKEN')


def ext_pillar(minion_id, pillar, *args, **kwargs):
    """ Main handler. Compile pillar data for the specified minion ID
    """
    kvdn_pillar = {}

    # Load configuration values
    for key in CONF:
        if kwargs.get(key, None):
            CONF[key] = kwargs.get(key)

    # Resolve salt:// fileserver path, if necessary
    if CONF["config"].startswith("salt://"):
        local_opts = __opts__.copy()
        local_opts["file_client"] = "local"
        minion = salt.minion.MasterMinion(local_opts)
        CONF["config"] = minion.functions["cp.cache_file"](CONF["config"])

    # Read the kvdn_value map
    renderers = salt.loader.render(__opts__, __salt__)
    raw_yml = salt.template.compile_template(CONF["config"], renderers, 'jinja')
    if raw_yml:
        config_map = yaml.safe_load(raw_yml.getvalue()) or {}
    else:
        LOG.error("Unable to read configuration file '%s'", CONF["config"])
        return kvdn_pillar

    if not CONF["url"]:
        LOG.error("'url' must be specified for KVDN configuration");
        return kvdn_pillar

    #  KVDN
    kvdn = kvdn_client.kvdn_client()
    
    _authenticate(kvdn)
    
    # Apply the compound filters to determine which mappings to expose for this minion
    ckminions = salt.utils.minions.CkMinions(__opts__)

    for filter, mappings in config_map.items():
        if minion_id in ckminions.check_minions(filter, "compound"):
            for variable, location in mappings.items():

                # Determine if a specific key was requested
                try:
                    (path, key) = location.split('?' , 1)
                except ValueError:
                    (path, key) = (location, None)

                # Return only the key value, if requested, otherwise return
                # the entire kvdn_value json structure
                kvdn_value = kvdn.read(path)
                if key:
                    kvdn_value = kvdn_value["data"].get(key, None)
                    # Decode base64 data, if detected
                    prefix = "base64:"
                    if kvdn_value.startswith(prefix):
                        kvdn_value = base64.b64decode(kvdn_value[len(prefix):]).rstrip()

                if kvdn_value or not CONF["unset_if_missing"]:
                    kvdn_pillar[variable] = kvdn_value

    return kvdn_pillar

