a distributed, http k/v store that allows vertxbus/websocket clients to subscribe to update events on maps.
jar must be run with -cluster, and can be clustered on multipule machines (see hazelcast / vertx doc)

The implementation of distributed data is pluggable through the Vert.X cluster manager

I have only tested with Hazelcast

 bash test/bats/bin/bats test/kvdn.bats                                                                               
 ✓ build kvdn
 ✓ remove old docker image
 ✓ build kvdn
 ✓ build kvdn docker image
 ✓ start a group of kv servers
 ✓ are they still running after 10 seconds?
 ✓ insert a value
 ✓ get keys
 ✓ get the value
 ✓ cleanup instances

10 tests, 0 failures


this is tinker-in-the garage stage software
 - keys may contain false positives
 - no snapshots (yet)
 - ... needs more testing
