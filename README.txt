a distributed, Hazlecast-backed http k/v store that allows vertxbus or websocket clients to subscribe to update events on maps.
jar must be run with -cluster at the moment, and can be clustered on multipule machines (see hazelcast / vertx doc)


The implementation of distributed data is pluggable through the Vert.X cluster manager
this should enable the chouse of underlieing algorithms as the plugs are finished for the underlieing framework:
 - Hazelcast (native)
 - Zookeeper / Paxos (hopeful)
 - Atomix / Raft (new)
 
 
https://github.com/vert-x3/vertx-zookeeper
http://atomix.io/ 
https://github.com/atomix/atomix-vertx
