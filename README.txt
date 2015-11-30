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

1 grant@nighthawk ~/sandbox/vertx/kvdn (git)-[master] % ./gradlew clean; ./gradlew shadowJar                                                      :(
:clean UP-TO-DATE

BUILD SUCCESSFUL

Total time: 3.782 secs
./gradlew clean  18.72s user 0.92s system 464% cpu 4.227 total
:compileJava
:compileGroovy
:processResources
:classes
:shadowJar

BUILD SUCCESSFUL

Total time: 20.676 secs
./gradlew shadowJar  49.37s user 9.12s system 276% cpu 21.170 total
grant@nighthawk ~/sandbox/vertx/kvdn (git)-[master] % java -jar build/libs/kvdn-0.1-fat.jar -cluster --config config.json
Nov 30, 2015 11:01:06 AM io.vertx.core.Starter
INFO: Starting clustering...
Nov 30, 2015 11:01:06 AM io.vertx.core.Starter
INFO: No cluster-host specified so using address 10.12.1.221
Nov 30, 2015 11:01:07 AM com.hazelcast.config.AbstractXmlConfigHelper
WARNING: Name of the hazelcast schema location incorrect using default
Nov 30, 2015 11:01:07 AM com.hazelcast.instance.DefaultAddressPicker
INFO: [LOCAL] [dev] [3.5] Prefer IPv4 stack is true.
Nov 30, 2015 11:01:07 AM com.hazelcast.instance.DefaultAddressPicker
INFO: [LOCAL] [dev] [3.5] Picked Address[10.12.1.221]:5701, using socket ServerSocket[addr=/0:0:0:0:0:0:0:0,localport=5701], bind any local is true
Nov 30, 2015 11:01:08 AM com.hazelcast.spi.OperationService
INFO: [10.12.1.221]:5701 [dev] [3.5] Backpressure is disabled
Nov 30, 2015 11:01:08 AM com.hazelcast.spi.impl.operationexecutor.classic.ClassicOperationExecutor
INFO: [10.12.1.221]:5701 [dev] [3.5] Starting with 12 generic operation threads and 24 partition operation threads.
Nov 30, 2015 11:01:08 AM com.hazelcast.system
INFO: [10.12.1.221]:5701 [dev] [3.5] Hazelcast 3.5 (20150617 - 4270dc6) starting at Address[10.12.1.221]:5701
Nov 30, 2015 11:01:08 AM com.hazelcast.system
INFO: [10.12.1.221]:5701 [dev] [3.5] Copyright (c) 2008-2015, Hazelcast, Inc. All Rights Reserved.
Nov 30, 2015 11:01:08 AM com.hazelcast.instance.Node
INFO: [10.12.1.221]:5701 [dev] [3.5] Creating MulticastJoiner
Nov 30, 2015 11:01:08 AM com.hazelcast.core.LifecycleService
INFO: [10.12.1.221]:5701 [dev] [3.5] Address[10.12.1.221]:5701 is STARTING
Nov 30, 2015 11:01:12 AM com.hazelcast.cluster.impl.MulticastJoiner
INFO: [10.12.1.221]:5701 [dev] [3.5]


Members [1] {
        Member [10.12.1.221]:5701 this
}

Nov 30, 2015 11:01:12 AM com.hazelcast.core.LifecycleService
INFO: [10.12.1.221]:5701 [dev] [3.5] Address[10.12.1.221]:5701 is STARTED
Nov 30, 2015 11:01:13 AM com.hazelcast.partition.InternalPartitionService
INFO: [10.12.1.221]:5701 [dev] [3.5] Initializing cluster partition table first arrangement...
Nov 30, 2015 11:01:14 AM io.vertx.core.Starter
INFO: Succeeded in deploying verticle


....


grant@nighthawk ~/sandbox/vertx/kvdn (git)-[master] % cd Client/cli/
grant@nighthawk ~/sandbox/vertx/kvdn/Client/cli (git)-[master] % echo "hello" | bash clip.sh -s=this/that/izbifdgasdd
that:izbifdgasdd%                                                                                                                        grant@nighthawk ~/sandbox/vertx/kvdn/Client/cli (git)-[master] % echo "hello" | bash clip.sh -s=this/that/anotherkey
that:anotherkey%                                                                                                                         grant@nighthawk ~/sandbox/vertx/kvdn/Client/cli (git)-[master] % curl http://192.168.7.101:9090/KEYS/this/that/
[result:[izbifdgasdd, anotherkey], error:null]
