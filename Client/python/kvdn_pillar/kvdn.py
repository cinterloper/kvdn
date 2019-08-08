# -*- coding: utf-8 -*-
"""
Use KVDN as a SaltStack Pillar source

"""

# Import stock modules
from __future__ import absolute_import
import traceback
import logging
import os
import sys
import salt.loader
import salt.minion
import salt.template
import salt.utils.minions
import yaml
import json
from kvdn_client import kvdn_client
kvdn_py_ver = "1.6.8"
# Set up logging
log = logging.getLogger(__name__)
# Default config values
CONF = {
    'baseurl': 'https://KVDN:6500',
    'config': '/srv/salt/kvdn.yml',
    'token': None,
    'token_path': None,
    'unset_if_missing': False,
    'dynamic_config_map': 'salt/pillar_mapping',
    'dynamic_config_key': 'dynamic_config',
    'dynamic_config_enabled': False,
    'verify':False,
    'timeout':15,
    'cert':None,
    'prefix':'',
    'set':'raw',
    'debug':False
}

__virtualname__ = 'kvdn'

def _kvlog(data):
    if CONF['debug']:
        f = open('/tmp/kvdn_pillar_log_file','a')
        f.write(str(data) + '\n')
        f.close()
_kvlog('hello there')
_kvlog("loaded salt-kvdn_pillar-kvdn")

def merge(x, y):
    z = x.copy()
    z.update(y)
    return z


def __virtual__():
    _kvlog("initalized KVDN kvdn_pillar")
    return __virtualname__


def couple(location, kvdnc):
    coupled_data = {}
    if isinstance(location, basestring):
        try:
            (path, key) = location.split('?', 1)
        except ValueError:
            (path, key) = (location, json.loads(kvdnc.getKeys(location)))
            _kvlog("loaded keys for mapmap ")

        if isinstance(key, basestring):  # real value gets set here
            kvdn_value = kvdnc.get(path, key)
            try:
                kvdn_value = json.loads(kvdn_value)
            except:
                _kvlog("kvdn value not json " + str(kvdn_value))
            if kvdn_value or not CONF["unset_if_missing"]:
                _kvlog(location + ":" + str(kvdn_value))
                return kvdn_value

        elif isinstance(key, list):
            for i, ikey in enumerate(key):
                coupled_data[ikey] = couple(location + '?' + ikey, kvdnc)

    elif isinstance(location, dict):
        for return_key, real_location in location.items():
            coupled_data[return_key] = couple(real_location, kvdnc)
    else:
        _kvlog("strange kvdn config type: " + str(type(location).__name__))

    if coupled_data or not CONF["unset_if_missing"]:
        return coupled_data


def ext_pillar(minion_id, pillar, *args, **kwargs):
    """ Main handler. Compile kvdn_pillar data for the specified minion ID
    """
    kvdn_pillar = {}
    _kvlog("called KVDN kvdn_pillar")

    # Load configuration values
    for key in CONF:
        if kwargs.get(key, None):
            CONF[key] = kwargs.get(key, None)
            _kvlog("set config key " + key + " to value " + str(kwargs.get(key, None)))
    _kvlog("KVDN_CONFIGURATION:")
    _kvlog(CONF)
    if CONF["token_path"]:
        CONF["token"] = open(CONF["token_path"]).read().strip()
    if os.environ.get('KVDN_TOKEN'):
        CONF["token"] = os.environ.get('KVDN_TOKEN')

    # KVDN
    try:
        kvdnc = kvdn_client.kvdn_client(logger=log, **CONF)
    except :
        _kvlog("Error getting kvdn connection \n" + str(traceback.format_exc()))
        return kvdn_pillar

    # Resolve salt:// fileserver path, if necessary
    if CONF["config"].startswith("salt://"):
        local_opts = __opts__.copy()
        local_opts["file_client"] = "local"
        minion = salt.minion.MasterMinion(local_opts)
        CONF["config"] = minion.functions["cp.cache_file"](CONF["config"])

    # Read the kvdn_value map
    renderers = salt.loader.render(__opts__, __salt__)

    try:
        raw_yml = salt.template.compile_template(CONF["config"], renderers, 'jinja', whitelist=[], blacklist=[])
    except:
        log.error("error while rendering the config kvdn config template")
        return kvdn_pillar
    if raw_yml:
        config_map = yaml.safe_load(raw_yml.getvalue()) or {}
    else:
        log.error("Unable to read configuration file '%s'", CONF["config"])
        return kvdn_pillar
    if CONF["dynamic_config_enabled"]:
        try:
            dynamic_config = json.loads(kvdnc.get(CONF["dynamic_config_map"], CONF["dynamic_config_key"]))
            config_map = merge(config_map, dynamic_config)
            log.info("loaded dynamic configration")
        except:
            log.error("unable to load dynamic config")

    if not CONF["baseurl"]:
        log.error("'baseurl' must be specified for KVDN configuration")
        return kvdn_pillar

    # Apply the compound filters to determine which mappings to expose for this minion
    ckminions = salt.utils.minions.CkMinions(__opts__)

    for filter, mappings in config_map.items():
        if minion_id in ckminions.check_minions(filter, "compound"):
            for variable, location in mappings.items():
              return_data = couple(location,kvdnc)
              if return_data:
                kvdn_pillar[variable] = return_data
    _kvlog(str(kvdn_pillar))
    return kvdn_pillar
