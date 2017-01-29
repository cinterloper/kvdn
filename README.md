# KVDN

KVDN is an extensible distributed data framework and key-value storage protocol server.

It builds on the Vert.X SharedData interface with features such as:

  - getKeys() on distribued maps
  - pre and post action hooks
  - utilities such as distributedWaitGroup and cluster wide exclusiveTask 

You can also:
  - use kvdn as a stand alone http or tcp server
  - persist your data to JDBC or Cassandra
  - embed kvdn in your vertx-enabled application

Some of these fetures are achived through cluster-manager specific `provider` implementations. 

right now KVDN supports MapDB for local persistance, with support for Hazelcast and Apache Ignite for distributed persistance

A JDBC mapstore is included for Hazelcast, Apache Ignite users should follow the Ignite docs for Cassandra or JDBC persistance


### Drivers

KVDN has client drivers for 

* Golang
* Python
  - CLI
* Javascript
* (vert.x languages supporting codegen)

out of these, only the python driver is mature, and even it is still under development

KVDN also has a saltstack integration where it can be a `pillar` data provider

* [https://github.com/cinterloper/salt-pillar-kvdn] [PlDb]


#### Building from source
```sh
$ ./gradlew clean shadowJar publish
$ export BUILD_EXTENSIONS=true
$ ./gradlew clean shadowJar publish
```

### Docker

```sh
docker run -p6500:6500 cinterloper/kvdn
```

License
----

Apache

Powered by
----
[![N|Vertx](http://vertx.io/assets/logo-sm.png)](http://vertx.io)


