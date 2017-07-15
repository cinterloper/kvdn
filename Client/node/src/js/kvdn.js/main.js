module.exports = (function() {

    'use strict'
    var CONF = {
        'baseurl': 'https://localhost:6500',
        'token': None,
        'prefix': '',
        'set':'raw',
        'headers':{}
    }
    var kvdnjs = {}


    var EventBus = require('vertx3-eventbus-client');
    var eb = new EventBus("http://localhost:9090/eb/")
    var axios = require('axios');



    kvdnjs.get = function(key,straddr,cb){
        axios
    }


    eb.onopen = function () {
        console.log("opened");
        eb.registerHandler('_KVDN_+this:that', function (e, mes) {

            console.log('incoming');

            if (e) console.log(e);


            else {

                var m = mes.body;
                var request = require("request");
                console.log("http://localhost:9090/X/this/that/" + m)
                request("http://localhost:9090/X/this/that/" + m, function (error, response, body) {
                    console.log(body);
                });

                console.log(JSON.stringify(m));

            }

        });
    }


    function onerrorEventBus(error) {
        console.log("Problem calling event bus " + error)
    }

    eb.onerror = onerrorEventBus;





}