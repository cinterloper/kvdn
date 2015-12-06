a distributed, Hazlecast-backed http k/v store that allows vertxbus or websocket clients to subscribe to update events on maps.
jar must be run with -cluster at themoment, and can be clustered on multipule machines (see hazelcast / vertx doc)


The implementation of distributed data is pluggable through the Vert.X cluster manager
this should enable the chouse of underlieing algorithms as the plugs are finished for the underlieing framework:
 - Hazelcast (native)
 - Zookeeper / Paxos (hopeful)
 - Atomix / Raft (new)
 
 
https://github.com/vert-x3/vertx-zookeeper
http://atomix.io/ 
https://github.com/atomix/atomix-vertx

grant@nighthawk ~/sandbox/vertx/kvdn (git)-[master] % bats/bin/bats  kvdn.bats
 ✓ build kvdn
 ✓ build kvdn docker image
 ✓ start a group of kv servers
 ✓ are they still running after 10 seconds?
 ✓ insert a value
 ✓ get keys
 ✓ get the value
 ✓ cleanup instances

8 tests, 0 failures

this is tinker-in-the garage stage software
 - keys may contain false positives
 - no snapshots (yet)
 - ... needs more testing
