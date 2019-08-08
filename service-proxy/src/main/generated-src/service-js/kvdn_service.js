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

  var __super_create = this.create;
  var __super_createProxy = this.createProxy;
  var __super_set = this.set;
  var __super_submit = this.submit;
  var __super_get = this.get;
  var __super_size = this.size;
  var __super_getKeys = this.getKeys;
  var __super_del = this.del;
  var __super_query = this.query;
  var __super_clear = this.clear;
  var __super_ctrGet = this.ctrGet;
  var __super_addAndGet = this.addAndGet;
  var __super_getAndAdd = this.getAndAdd;
  var __super_ctrCompareAndSet = this.ctrCompareAndSet;
  var __super_enqueue = this.enqueue;
  var __super_dequeue = this.dequeue;
  var __super_qPeek = this.qPeek;
  var __super_qArrayView = this.qArrayView;
  /**

   @public
   @param straddr {string} 
   @param key {string} 
   @param value {string} 
   @param options {Object} 
   @param resultHandler {function} 
   */
  this.set =  function(straddr, key, value, options, resultHandler) {
    var __args = arguments;
    if (__args.length === 5 && typeof __args[0] === 'string' && typeof __args[1] === 'string' && typeof __args[2] === 'string' && (typeof __args[3] === 'object' && __args[3] != null) && typeof __args[4] === 'function') {
      j_kvdnService["set(java.lang.String,java.lang.String,java.lang.String,io.vertx.core.json.JsonObject,io.vertx.core.Handler)"](__args[0], __args[1], __args[2], utils.convParamJsonObject(__args[3]), function(ar) {
        if (ar.succeeded()) {
          __args[4](ar.result(), null);
        } else {
          __args[4](null, ar.cause());
        }
      });
    } else if (typeof __super_set != 'undefined') {
      return __super_set.apply(this, __args);
    }
    else throw new TypeError('function invoked with invalid arguments');
  };

  /**

   @public
   @param straddr {string} 
   @param value {string} 
   @param options {Object} 
   @param resultHandler {function} 
   */
  this.submit =  function(straddr, value, options, resultHandler) {
    var __args = arguments;
    if (__args.length === 4 && typeof __args[0] === 'string' && typeof __args[1] === 'string' && (typeof __args[2] === 'object' && __args[2] != null) && typeof __args[3] === 'function') {
      j_kvdnService["submit(java.lang.String,java.lang.String,io.vertx.core.json.JsonObject,io.vertx.core.Handler)"](__args[0], __args[1], utils.convParamJsonObject(__args[2]), function(ar) {
        if (ar.succeeded()) {
          __args[3](ar.result(), null);
        } else {
          __args[3](null, ar.cause());
        }
      });
    } else if (typeof __super_submit != 'undefined') {
      return __super_submit.apply(this, __args);
    }
    else throw new TypeError('function invoked with invalid arguments');
  };

  /**

   @public
   @param straddr {string} 
   @param key {string} 
   @param options {Object} 
   @param resultHandler {function} 
   */
  this.get =  function(straddr, key, options, resultHandler) {
    var __args = arguments;
    if (__args.length === 4 && typeof __args[0] === 'string' && typeof __args[1] === 'string' && (typeof __args[2] === 'object' && __args[2] != null) && typeof __args[3] === 'function') {
      j_kvdnService["get(java.lang.String,java.lang.String,io.vertx.core.json.JsonObject,io.vertx.core.Handler)"](__args[0], __args[1], utils.convParamJsonObject(__args[2]), function(ar) {
        if (ar.succeeded()) {
          __args[3](ar.result(), null);
        } else {
          __args[3](null, ar.cause());
        }
      });
    } else if (typeof __super_get != 'undefined') {
      return __super_get.apply(this, __args);
    }
    else throw new TypeError('function invoked with invalid arguments');
  };

  /**

   @public
   @param straddr {string} 
   @param options {Object} 
   @param resultHandler {function} 
   */
  this.size =  function(straddr, options, resultHandler) {
    var __args = arguments;
    if (__args.length === 3 && typeof __args[0] === 'string' && (typeof __args[1] === 'object' && __args[1] != null) && typeof __args[2] === 'function') {
      j_kvdnService["size(java.lang.String,io.vertx.core.json.JsonObject,io.vertx.core.Handler)"](__args[0], utils.convParamJsonObject(__args[1]), function(ar) {
        if (ar.succeeded()) {
          __args[2](ar.result(), null);
        } else {
          __args[2](null, ar.cause());
        }
      });
    } else if (typeof __super_size != 'undefined') {
      return __super_size.apply(this, __args);
    }
    else throw new TypeError('function invoked with invalid arguments');
  };

  /**

   @public
   @param straddr {string} 
   @param options {Object} 
   @param resultHandler {function} 
   */
  this.getKeys =  function(straddr, options, resultHandler) {
    var __args = arguments;
    if (__args.length === 3 && typeof __args[0] === 'string' && (typeof __args[1] === 'object' && __args[1] != null) && typeof __args[2] === 'function') {
      j_kvdnService["getKeys(java.lang.String,io.vertx.core.json.JsonObject,io.vertx.core.Handler)"](__args[0], utils.convParamJsonObject(__args[1]), function(ar) {
        if (ar.succeeded()) {
          __args[2](utils.convReturnJson(ar.result()), null);
        } else {
          __args[2](null, ar.cause());
        }
      });
    } else if (typeof __super_getKeys != 'undefined') {
      return __super_getKeys.apply(this, __args);
    }
    else throw new TypeError('function invoked with invalid arguments');
  };

  /**

   @public
   @param straddr {string} 
   @param key {string} 
   @param options {Object} 
   @param resultHandler {function} 
   */
  this.del =  function(straddr, key, options, resultHandler) {
    var __args = arguments;
    if (__args.length === 4 && typeof __args[0] === 'string' && typeof __args[1] === 'string' && (typeof __args[2] === 'object' && __args[2] != null) && typeof __args[3] === 'function') {
      j_kvdnService["del(java.lang.String,java.lang.String,io.vertx.core.json.JsonObject,io.vertx.core.Handler)"](__args[0], __args[1], utils.convParamJsonObject(__args[2]), function(ar) {
        if (ar.succeeded()) {
          __args[3](ar.result(), null);
        } else {
          __args[3](null, ar.cause());
        }
      });
    } else if (typeof __super_del != 'undefined') {
      return __super_del.apply(this, __args);
    }
    else throw new TypeError('function invoked with invalid arguments');
  };

  /**

   @public
   @param straddr {string} 
   @param query {Object} 
   @param options {Object} 
   @param resultHandler {function} 
   */
  this.query =  function(straddr, query, options, resultHandler) {
    var __args = arguments;
    if (__args.length === 4 && typeof __args[0] === 'string' && (typeof __args[1] === 'object' && __args[1] != null) && (typeof __args[2] === 'object' && __args[2] != null) && typeof __args[3] === 'function') {
      j_kvdnService["query(java.lang.String,io.vertx.core.json.JsonObject,io.vertx.core.json.JsonObject,io.vertx.core.Handler)"](__args[0], utils.convParamJsonObject(__args[1]), utils.convParamJsonObject(__args[2]), function(ar) {
        if (ar.succeeded()) {
          __args[3](utils.convReturnJson(ar.result()), null);
        } else {
          __args[3](null, ar.cause());
        }
      });
    } else if (typeof __super_query != 'undefined') {
      return __super_query.apply(this, __args);
    }
    else throw new TypeError('function invoked with invalid arguments');
  };

  /**

   @public
   @param straddr {string} 
   @param options {Object} 
   @param resultHandler {function} 
   */
  this.clear =  function(straddr, options, resultHandler) {
    var __args = arguments;
    if (__args.length === 3 && typeof __args[0] === 'string' && (typeof __args[1] === 'object' && __args[1] != null) && typeof __args[2] === 'function') {
      j_kvdnService["clear(java.lang.String,io.vertx.core.json.JsonObject,io.vertx.core.Handler)"](__args[0], utils.convParamJsonObject(__args[1]), function(ar) {
        if (ar.succeeded()) {
          __args[2](ar.result(), null);
        } else {
          __args[2](null, ar.cause());
        }
      });
    } else if (typeof __super_clear != 'undefined') {
      return __super_clear.apply(this, __args);
    }
    else throw new TypeError('function invoked with invalid arguments');
  };

  /**

   @public
   @param straddr {string} 
   @param options {Object} 
   @param cb {function} 
   */
  this.ctrGet =  function(straddr, options, cb) {
    var __args = arguments;
    if (__args.length === 3 && typeof __args[0] === 'string' && (typeof __args[1] === 'object' && __args[1] != null) && typeof __args[2] === 'function') {
      j_kvdnService["ctrGet(java.lang.String,io.vertx.core.json.JsonObject,io.vertx.core.Handler)"](__args[0], utils.convParamJsonObject(__args[1]), function(ar) {
        if (ar.succeeded()) {
          __args[2](utils.convReturnLong(ar.result()), null);
        } else {
          __args[2](null, ar.cause());
        }
      });
    } else if (typeof __super_ctrGet != 'undefined') {
      return __super_ctrGet.apply(this, __args);
    }
    else throw new TypeError('function invoked with invalid arguments');
  };

  /**

   @public
   @param straddr {string} 
   @param value {number} 
   @param options {Object} 
   @param cb {function} 
   */
  this.addAndGet =  function(straddr, value, options, cb) {
    var __args = arguments;
    if (__args.length === 4 && typeof __args[0] === 'string' && typeof __args[1] ==='number' && (typeof __args[2] === 'object' && __args[2] != null) && typeof __args[3] === 'function') {
      j_kvdnService["addAndGet(java.lang.String,long,io.vertx.core.json.JsonObject,io.vertx.core.Handler)"](__args[0], __args[1], utils.convParamJsonObject(__args[2]), function(ar) {
        if (ar.succeeded()) {
          __args[3](utils.convReturnLong(ar.result()), null);
        } else {
          __args[3](null, ar.cause());
        }
      });
    } else if (typeof __super_addAndGet != 'undefined') {
      return __super_addAndGet.apply(this, __args);
    }
    else throw new TypeError('function invoked with invalid arguments');
  };

  /**

   @public
   @param straddr {string} 
   @param value {number} 
   @param options {Object} 
   @param cb {function} 
   */
  this.getAndAdd =  function(straddr, value, options, cb) {
    var __args = arguments;
    if (__args.length === 4 && typeof __args[0] === 'string' && typeof __args[1] ==='number' && (typeof __args[2] === 'object' && __args[2] != null) && typeof __args[3] === 'function') {
      j_kvdnService["getAndAdd(java.lang.String,long,io.vertx.core.json.JsonObject,io.vertx.core.Handler)"](__args[0], __args[1], utils.convParamJsonObject(__args[2]), function(ar) {
        if (ar.succeeded()) {
          __args[3](utils.convReturnLong(ar.result()), null);
        } else {
          __args[3](null, ar.cause());
        }
      });
    } else if (typeof __super_getAndAdd != 'undefined') {
      return __super_getAndAdd.apply(this, __args);
    }
    else throw new TypeError('function invoked with invalid arguments');
  };

  /**

   @public
   @param straddr {string} 
   @param oldv {number} 
   @param newv {number} 
   @param options {Object} 
   @param cb {function} 
   */
  this.ctrCompareAndSet =  function(straddr, oldv, newv, options, cb) {
    var __args = arguments;
    if (__args.length === 5 && typeof __args[0] === 'string' && typeof __args[1] ==='number' && typeof __args[2] ==='number' && (typeof __args[3] === 'object' && __args[3] != null) && typeof __args[4] === 'function') {
      j_kvdnService["ctrCompareAndSet(java.lang.String,long,long,io.vertx.core.json.JsonObject,io.vertx.core.Handler)"](__args[0], __args[1], __args[2], utils.convParamJsonObject(__args[3]), function(ar) {
        if (ar.succeeded()) {
          __args[4](ar.result(), null);
        } else {
          __args[4](null, ar.cause());
        }
      });
    } else if (typeof __super_ctrCompareAndSet != 'undefined') {
      return __super_ctrCompareAndSet.apply(this, __args);
    }
    else throw new TypeError('function invoked with invalid arguments');
  };

  /**

   @public
   @param straddr {string} 
   @param options {Object} 
   @param value {string} 
   @param cb {function} 
   */
  this.enqueue =  function(straddr, options, value, cb) {
    var __args = arguments;
    if (__args.length === 4 && typeof __args[0] === 'string' && (typeof __args[1] === 'object' && __args[1] != null) && typeof __args[2] === 'string' && typeof __args[3] === 'function') {
      j_kvdnService["enqueue(java.lang.String,io.vertx.core.json.JsonObject,java.lang.String,io.vertx.core.Handler)"](__args[0], utils.convParamJsonObject(__args[1]), __args[2], function(ar) {
        if (ar.succeeded()) {
          __args[3](utils.convReturnLong(ar.result()), null);
        } else {
          __args[3](null, ar.cause());
        }
      });
    } else if (typeof __super_enqueue != 'undefined') {
      return __super_enqueue.apply(this, __args);
    }
    else throw new TypeError('function invoked with invalid arguments');
  };

  /**

   @public
   @param straddr {string} 
   @param options {Object} 
   @param cb {function} 
   */
  this.dequeue =  function(straddr, options, cb) {
    var __args = arguments;
    if (__args.length === 3 && typeof __args[0] === 'string' && (typeof __args[1] === 'object' && __args[1] != null) && typeof __args[2] === 'function') {
      j_kvdnService["dequeue(java.lang.String,io.vertx.core.json.JsonObject,io.vertx.core.Handler)"](__args[0], utils.convParamJsonObject(__args[1]), function(ar) {
        if (ar.succeeded()) {
          __args[2](ar.result(), null);
        } else {
          __args[2](null, ar.cause());
        }
      });
    } else if (typeof __super_dequeue != 'undefined') {
      return __super_dequeue.apply(this, __args);
    }
    else throw new TypeError('function invoked with invalid arguments');
  };

  /**

   @public
   @param straddr {string} 
   @param options {Object} 
   @param cb {function} 
   */
  this.qPeek =  function(straddr, options, cb) {
    var __args = arguments;
    if (__args.length === 3 && typeof __args[0] === 'string' && (typeof __args[1] === 'object' && __args[1] != null) && typeof __args[2] === 'function') {
      j_kvdnService["qPeek(java.lang.String,io.vertx.core.json.JsonObject,io.vertx.core.Handler)"](__args[0], utils.convParamJsonObject(__args[1]), function(ar) {
        if (ar.succeeded()) {
          __args[2](ar.result(), null);
        } else {
          __args[2](null, ar.cause());
        }
      });
    } else if (typeof __super_qPeek != 'undefined') {
      return __super_qPeek.apply(this, __args);
    }
    else throw new TypeError('function invoked with invalid arguments');
  };

  /**

   @public
   @param straddr {string} 
   @param options {Object} 
   @param cb {function} 
   */
  this.qArrayView =  function(straddr, options, cb) {
    var __args = arguments;
    if (__args.length === 3 && typeof __args[0] === 'string' && (typeof __args[1] === 'object' && __args[1] != null) && typeof __args[2] === 'function') {
      j_kvdnService["qArrayView(java.lang.String,io.vertx.core.json.JsonObject,io.vertx.core.Handler)"](__args[0], utils.convParamJsonObject(__args[1]), function(ar) {
        if (ar.succeeded()) {
          __args[2](utils.convReturnJson(ar.result()), null);
        } else {
          __args[2](null, ar.cause());
        }
      });
    } else if (typeof __super_qArrayView != 'undefined') {
      return __super_qArrayView.apply(this, __args);
    }
    else throw new TypeError('function invoked with invalid arguments');
  };

  // A reference to the underlying Java delegate
  // NOTE! This is an internal API and must not be used in user code.
  // If you rely on this property your code is likely to break if we change it / remove it without warning.
  this._jdel = j_kvdnService;
};

KvdnService._jclass = utils.getJavaClass("net.iowntheinter.kvdn.service.KvdnService");
KvdnService._jtype = {accept: function(obj) {
    return KvdnService._jclass.isInstance(obj._jdel);
  },wrap: function(jdel) {
    var obj = Object.create(KvdnService.prototype, {});
    KvdnService.apply(obj, arguments);
    return obj;
  },
  unwrap: function(obj) {
    return obj._jdel;
  }
};
KvdnService._create = function(jdel) {var obj = Object.create(KvdnService.prototype, {});
  KvdnService.apply(obj, arguments);
  return obj;
}
/**

 @memberof module:service-js/kvdn_service
 @param vertx {Vertx} 
 @return {KvdnService}
 */
KvdnService.create =  function(vertx) {
  var __args = arguments;
  if (__args.length === 1 && typeof __args[0] === 'object' && __args[0]._jdel) {
    return utils.convReturnVertxGen(KvdnService, JKvdnService["create(io.vertx.core.Vertx)"](__args[0]._jdel)) ;
  }else throw new TypeError('function invoked with invalid arguments');
};

/**

 @memberof module:service-js/kvdn_service
 @param vertx {Vertx} 
 @param address {string} 
 @return {KvdnService}
 */
KvdnService.createProxy =  function(vertx, address) {
  var __args = arguments;
  if (__args.length === 2 && typeof __args[0] === 'object' && __args[0]._jdel && typeof __args[1] === 'string') {
    return utils.convReturnVertxGen(KvdnService, JKvdnService["createProxy(io.vertx.core.Vertx,java.lang.String)"](__args[0]._jdel, __args[1])) ;
  }else throw new TypeError('function invoked with invalid arguments');
};

module.exports = KvdnService;