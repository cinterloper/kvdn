/*
 * Copyright 2014 Red Hat, Inc.
 *
 * Red Hat licenses this file to you under the Apache License, version 2.0
 * (the "License"); you may not use this file except in compliance with the
 * License.  You may obtain a copy of the License at:
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
 * WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.  See the
 * License for the specific language governing permissions and limitations
 * under the License.
 */

/** @module service-js/kvdn_service */
var utils = require('vertx-js/util/utils');
var Vertx = require('vertx-js/vertx');

var io = Packages.io;
var JsonObject = io.vertx.core.json.JsonObject;
var JKvdnService = Java.type('net.iowntheinter.kvdn.service.KvdnService');

/**
 Created by g on 9/15/16.

 @class
*/
var KvdnService = function(j_val) {

  var j_kvdnService = j_val;
  var that = this;

  /**

   @public
   @param straddr {string} 
   @param key {string} 
   @param value {string} 
   @param options {Object} 
   @param resultHandler {function} 
   */
  this.set = function(straddr, key, value, options, resultHandler) {
    var __args = arguments;
    if (__args.length === 5 && typeof __args[0] === 'string' && typeof __args[1] === 'string' && typeof __args[2] === 'string' && (typeof __args[3] === 'object' && __args[3] != null) && typeof __args[4] === 'function') {
      j_kvdnService["set(java.lang.String,java.lang.String,java.lang.String,io.vertx.core.json.JsonObject,io.vertx.core.Handler)"](straddr, key, value, utils.convParamJsonObject(options), function(ar) {
      if (ar.succeeded()) {
        resultHandler(utils.convReturnJson(ar.result()), null);
      } else {
        resultHandler(null, ar.cause());
      }
    });
    } else throw new TypeError('function invoked with invalid arguments');
  };

  /**

   @public
   @param straddr {string} 
   @param value {string} 
   @param options {Object} 
   @param resultHandler {function} 
   */
  this.submit = function(straddr, value, options, resultHandler) {
    var __args = arguments;
    if (__args.length === 4 && typeof __args[0] === 'string' && typeof __args[1] === 'string' && (typeof __args[2] === 'object' && __args[2] != null) && typeof __args[3] === 'function') {
      j_kvdnService["submit(java.lang.String,java.lang.String,io.vertx.core.json.JsonObject,io.vertx.core.Handler)"](straddr, value, utils.convParamJsonObject(options), function(ar) {
      if (ar.succeeded()) {
        resultHandler(utils.convReturnJson(ar.result()), null);
      } else {
        resultHandler(null, ar.cause());
      }
    });
    } else throw new TypeError('function invoked with invalid arguments');
  };

  /**

   @public
   @param straddr {string} 
   @param key {string} 
   @param options {Object} 
   @param resultHandler {function} 
   */
  this.get = function(straddr, key, options, resultHandler) {
    var __args = arguments;
    if (__args.length === 4 && typeof __args[0] === 'string' && typeof __args[1] === 'string' && (typeof __args[2] === 'object' && __args[2] != null) && typeof __args[3] === 'function') {
      j_kvdnService["get(java.lang.String,java.lang.String,io.vertx.core.json.JsonObject,io.vertx.core.Handler)"](straddr, key, utils.convParamJsonObject(options), function(ar) {
      if (ar.succeeded()) {
        resultHandler(ar.result(), null);
      } else {
        resultHandler(null, ar.cause());
      }
    });
    } else throw new TypeError('function invoked with invalid arguments');
  };

  /**

   @public
   @param straddr {string} 
   @param options {Object} 
   @param resultHandler {function} 
   */
  this.size = function(straddr, options, resultHandler) {
    var __args = arguments;
    if (__args.length === 3 && typeof __args[0] === 'string' && (typeof __args[1] === 'object' && __args[1] != null) && typeof __args[2] === 'function') {
      j_kvdnService["size(java.lang.String,io.vertx.core.json.JsonObject,io.vertx.core.Handler)"](straddr, utils.convParamJsonObject(options), function(ar) {
      if (ar.succeeded()) {
        resultHandler(ar.result(), null);
      } else {
        resultHandler(null, ar.cause());
      }
    });
    } else throw new TypeError('function invoked with invalid arguments');
  };

  /**

   @public
   @param straddr {string} 
   @param options {Object} 
   @param resultHandler {function} 
   */
  this.getKeys = function(straddr, options, resultHandler) {
    var __args = arguments;
    if (__args.length === 3 && typeof __args[0] === 'string' && (typeof __args[1] === 'object' && __args[1] != null) && typeof __args[2] === 'function') {
      j_kvdnService["getKeys(java.lang.String,io.vertx.core.json.JsonObject,io.vertx.core.Handler)"](straddr, utils.convParamJsonObject(options), function(ar) {
      if (ar.succeeded()) {
        resultHandler(utils.convReturnJson(ar.result()), null);
      } else {
        resultHandler(null, ar.cause());
      }
    });
    } else throw new TypeError('function invoked with invalid arguments');
  };

  /**

   @public
   @param straddr {string} 
   @param key {string} 
   @param options {Object} 
   @param resultHandler {function} 
   */
  this.del = function(straddr, key, options, resultHandler) {
    var __args = arguments;
    if (__args.length === 4 && typeof __args[0] === 'string' && typeof __args[1] === 'string' && (typeof __args[2] === 'object' && __args[2] != null) && typeof __args[3] === 'function') {
      j_kvdnService["del(java.lang.String,java.lang.String,io.vertx.core.json.JsonObject,io.vertx.core.Handler)"](straddr, key, utils.convParamJsonObject(options), function(ar) {
      if (ar.succeeded()) {
        resultHandler(utils.convReturnJson(ar.result()), null);
      } else {
        resultHandler(null, ar.cause());
      }
    });
    } else throw new TypeError('function invoked with invalid arguments');
  };

  /**

   @public
   @param straddr {string} 
   @param query {Object} 
   @param options {Object} 
   @param resultHandler {function} 
   */
  this.query = function(straddr, query, options, resultHandler) {
    var __args = arguments;
    if (__args.length === 4 && typeof __args[0] === 'string' && (typeof __args[1] === 'object' && __args[1] != null) && (typeof __args[2] === 'object' && __args[2] != null) && typeof __args[3] === 'function') {
      j_kvdnService["query(java.lang.String,io.vertx.core.json.JsonObject,io.vertx.core.json.JsonObject,io.vertx.core.Handler)"](straddr, utils.convParamJsonObject(query), utils.convParamJsonObject(options), function(ar) {
      if (ar.succeeded()) {
        resultHandler(utils.convReturnJson(ar.result()), null);
      } else {
        resultHandler(null, ar.cause());
      }
    });
    } else throw new TypeError('function invoked with invalid arguments');
  };

  // A reference to the underlying Java delegate
  // NOTE! This is an internal API and must not be used in user code.
  // If you rely on this property your code is likely to break if we change it / remove it without warning.
  this._jdel = j_kvdnService;
};

KvdnService._jclass = utils.getJavaClass("net.iowntheinter.kvdn.service.KvdnService");
KvdnService._jtype = {
  accept: function(obj) {
    return KvdnService._jclass.isInstance(obj._jdel);
  },
  wrap: function(jdel) {
    var obj = Object.create(KvdnService.prototype, {});
    KvdnService.apply(obj, arguments);
    return obj;
  },
  unwrap: function(obj) {
    return obj._jdel;
  }
};
KvdnService._create = function(jdel) {
  var obj = Object.create(KvdnService.prototype, {});
  KvdnService.apply(obj, arguments);
  return obj;
}
/**

 @memberof module:service-js/kvdn_service
 @param vertx {Vertx} 
 @return {KvdnService}
 */
KvdnService.create = function(vertx) {
  var __args = arguments;
  if (__args.length === 1 && typeof __args[0] === 'object' && __args[0]._jdel) {
    return utils.convReturnVertxGen(KvdnService, JKvdnService["create(io.vertx.core.Vertx)"](vertx._jdel));
  } else throw new TypeError('function invoked with invalid arguments');
};

/**

 @memberof module:service-js/kvdn_service
 @param vertx {Vertx} 
 @param address {string} 
 @return {KvdnService}
 */
KvdnService.createProxy = function(vertx, address) {
  var __args = arguments;
  if (__args.length === 2 && typeof __args[0] === 'object' && __args[0]._jdel && typeof __args[1] === 'string') {
    return utils.convReturnVertxGen(KvdnService, JKvdnService["createProxy(io.vertx.core.Vertx,java.lang.String)"](vertx._jdel, address));
  } else throw new TypeError('function invoked with invalid arguments');
};

module.exports = KvdnService;