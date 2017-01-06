# KVDN


KVDN is an extensible distributed data framework and key-value storage protocol server.
It builds on the Vert.X SharedData interface with features such as:

  - keySet() on distribued maps
  - pre and post action hooks
  - utilities such as distributedWaitGroup and cluster wide exclusiveTask 

You can also:
  - use kvdn as a stand alone http or tcp server
  - persist your data to JDBC or Cassandra
  - embed kvdn in your vertx-enabled application

Some of these fetures are achived through cluster-manager specific `provider` implementations. 
right now KVDN supports Hazelcast and Apache Ignite


### Drivers

KVDN has client drivers for 

* Golang
* Python
* Javascript
* (vert.x languages supporting codegen)

KVDN also has a saltstack integration where it can be a `pillar` data provider

* [https://github.com/cinterloper/salt-pillar-kvdn] [PlDb]


#### Building for source
For production release:
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
[![N|Vertx](http://vertx.io/assets/logo-sm.png)](https://vertx.io)



[//]: # (These are reference links used in the body of this note and get stripped out when the markdown processor does its job. There is no need to format nicely because it shouldn't be seen. Thanks SO - http://stackoverflow.com/questions/4823468/store-comments-in-markdown-syntax)


   [dill]: <https://github.com/joemccann/dillinger>
   [git-repo-url]: <https://github.com/joemccann/dillinger.git>
   [john gruber]: <http://daringfireball.net>
   [@thomasfuchs]: <http://twitter.com/thomasfuchs>
   [df1]: <http://daringfireball.net/projects/markdown/>
   [markdown-it]: <https://github.com/markdown-it/markdown-it>
   [Ace Editor]: <http://ace.ajax.org>
   [node.js]: <http://nodejs.org>
   [Twitter Bootstrap]: <http://twitter.github.com/bootstrap/>
   [keymaster.js]: <https://github.com/madrobby/keymaster>
   [jQuery]: <http://jquery.com>
   [@tjholowaychuk]: <http://twitter.com/tjholowaychuk>
   [express]: <http://expressjs.com>
   [AngularJS]: <http://angularjs.org>
   [Gulp]: <http://gulpjs.com>

   [PlDb]: <https://github.com/joemccann/dillinger/tree/master/plugins/dropbox/README.md>
   [PlGh]:  <https://github.com/joemccann/dillinger/tree/master/plugins/github/README.md>
   [PlGd]: <https://github.com/joemccann/dillinger/tree/master/plugins/googledrive/README.md>
   [PlOd]: <https://github.com/joemccann/dillinger/tree/master/plugins/onedrive/README.md>
