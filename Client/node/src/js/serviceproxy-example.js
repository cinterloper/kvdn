EventBus=require("vertx3-eventbus-client")
kvdn=require("./kvsvc-proxy")

var eb = new EventBus("http://iowntheinter.net:6500/eb/")

    eb.onopen = function () {
        console.log("opened");
        k = new kvdn(eb, "kvdnsvc")
        k.set({"straddr":"me_you","key":"skooba","value":"booba"},function(res,err) {
          console.log(res)
          console.log(err)
        })
        k.getKeys({"straddr":"this_that"}, function(res,err) {
          console.log(res)
          console.log(err)
        })
   }
