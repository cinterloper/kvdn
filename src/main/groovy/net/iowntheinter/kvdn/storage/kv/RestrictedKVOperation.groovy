package net.iowntheinter.kvdn.storage.kv

abstract class RestrictedKVOperation implements KVOperation {
    RestrictedKVOperation(Set<String> allowedKeys ){

    }
}
