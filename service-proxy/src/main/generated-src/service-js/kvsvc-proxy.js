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

/** @module service-js/kvsvc */
!function (factory) {
  if (typeof require === 'function' && typeof module !== 'undefined') {
    factory();
  } else if (typeof define === 'function' && define.amd) {
    // AMD loader
    define('service-js/kvsvc-proxy', [], factory);
  } else {
    // plain old include
    kvsvc = factory();
  }
}(function () {

  /**
 Created by g on 9/15/16.

 @class
  */
  var kvsvc = function(eb, address) {

    var j_eb = eb;
    var j_address = address;
    var closed = false;
    var that = this;
    var convCharCollection = function(coll) {
      var ret = [];
      for (var i = 0;i < coll.length;i++) {
        ret.push(String.fromCharCode(coll[i]));
      }
      return ret;
    };

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
        if (closed) {
          throw new Error('Proxy is closed');
        }
        j_eb.send(j_address, {"straddr":__args[0], "key":__args[1], "value":__args[2], "options":__args[3]}, {"action":"set"}, function(err, result) { __args[4](err, result &&result.body); });
        return;
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
        if (closed) {
          throw new Error('Proxy is closed');
        }
        j_eb.send(j_address, {"straddr":__args[0], "value":__args[1], "options":__args[2]}, {"action":"submit"}, function(err, result) { __args[3](err, result &&result.body); });
        return;
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
        if (closed) {
          throw new Error('Proxy is closed');
        }
        j_eb.send(j_address, {"straddr":__args[0], "key":__args[1], "options":__args[2]}, {"action":"get"}, function(err, result) { __args[3](err, result &&result.body); });
        return;
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
        if (closed) {
          throw new Error('Proxy is closed');
        }
        j_eb.send(j_address, {"straddr":__args[0], "options":__args[1]}, {"action":"size"}, function(err, result) { __args[2](err, result &&result.body); });
        return;
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
        if (closed) {
          throw new Error('Proxy is closed');
        }
        j_eb.send(j_address, {"straddr":__args[0], "options":__args[1]}, {"action":"getKeys"}, function(err, result) { __args[2](err, result &&result.body); });
        return;
      } else throw new TypeError('function invoked with invalid arguments');
    };

    /**

     @public
     @param straddr {string} 
     @param key {string} 
     @param options {Object} 
     @param resultHandler {function} 
     */
    this.delete = function(straddr, key, options, resultHandler) {
      var __args = arguments;
      if (__args.length === 4 && typeof __args[0] === 'string' && typeof __args[1] === 'string' && (typeof __args[2] === 'object' && __args[2] != null) && typeof __args[3] === 'function') {
        if (closed) {
          throw new Error('Proxy is closed');
        }
        j_eb.send(j_address, {"straddr":__args[0], "key":__args[1], "options":__args[2]}, {"action":"delete"}, function(err, result) { __args[3](err, result &&result.body); });
        return;
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
        if (closed) {
          throw new Error('Proxy is closed');
        }
        j_eb.send(j_address, {"straddr":__args[0], "query":__args[1], "options":__args[2]}, {"action":"query"}, function(err, result) { __args[3](err, result &&result.body); });
        return;
      } else throw new TypeError('function invoked with invalid arguments');
    };

  };

  /**

   @memberof module:service-js/kvsvc
   @param vertx {Vertx} 
   @return {kvsvc}
   */
  kvsvc.create = function(vertx) {
    var __args = arguments;
    if (__args.length === 1 && typeof __args[0] === 'object' && __args[0]._jdel) {
      if (closed) {
        throw new Error('Proxy is closed');
      }
      j_eb.send(j_address, {"vertx":__args[0]}, {"action":"create"});
      return;
    } else throw new TypeError('function invoked with invalid arguments');
  };

  /**

   @memberof module:service-js/kvsvc
   @param vertx {Vertx} 
   @param address {string} 
   @return {kvsvc}
   */
  kvsvc.createProxy = function(vertx, address) {
    var __args = arguments;
    if (__args.length === 2 && typeof __args[0] === 'object' && __args[0]._jdel && typeof __args[1] === 'string') {
      if (closed) {
        throw new Error('Proxy is closed');
      }
      j_eb.send(j_address, {"vertx":__args[0], "address":__args[1]}, {"action":"createProxy"});
      return;
    } else throw new TypeError('function invoked with invalid arguments');
  };

  if (typeof exports !== 'undefined') {
    if (typeof module !== 'undefined' && module.exports) {
      exports = module.exports = kvsvc;
    } else {
      exports.kvsvc = kvsvc;
    }
  } else {
    return kvsvc;
  }
});