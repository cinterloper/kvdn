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


export class KvdnService {

  private closed = false;

  private readonly convCharCollection = coll => {
    const ret = [];
    for (let i = 0; i < coll.length; i++) {
      ret.push(String.fromCharCode(coll[i]));
    }
    return ret;
  }

  constructor (private eb: any, private address: string) {
  }

  set(straddr: string, key: string, value: string, options: Object, resultHandler: (err: any, result: any) => any) : void {
    if (closed) {
      throw new Error('Proxy is closed');
    }
    this.eb.send(this.address, {"straddr": straddr, "key": key, "value": value, "options": options}, {"action":"set"}, function(err, result) { resultHandler(err, result &&result.body); });
  }

  submit(straddr: string, value: string, options: Object, resultHandler: (err: any, result: any) => any) : void {
    if (closed) {
      throw new Error('Proxy is closed');
    }
    this.eb.send(this.address, {"straddr": straddr, "value": value, "options": options}, {"action":"submit"}, function(err, result) { resultHandler(err, result &&result.body); });
  }

  get(straddr: string, key: string, options: Object, resultHandler: (err: any, result: any) => any) : void {
    if (closed) {
      throw new Error('Proxy is closed');
    }
    this.eb.send(this.address, {"straddr": straddr, "key": key, "options": options}, {"action":"get"}, function(err, result) { resultHandler(err, result &&result.body); });
  }

  size(straddr: string, options: Object, resultHandler: (err: any, result: any) => any) : void {
    if (closed) {
      throw new Error('Proxy is closed');
    }
    this.eb.send(this.address, {"straddr": straddr, "options": options}, {"action":"size"}, function(err, result) { resultHandler(err, result &&result.body); });
  }

  getKeys(straddr: string, options: Object, resultHandler: (err: any, result: any) => any) : void {
    if (closed) {
      throw new Error('Proxy is closed');
    }
    this.eb.send(this.address, {"straddr": straddr, "options": options}, {"action":"getKeys"}, function(err, result) { resultHandler(err, result &&result.body); });
  }

  del(straddr: string, key: string, options: Object, resultHandler: (err: any, result: any) => any) : void {
    if (closed) {
      throw new Error('Proxy is closed');
    }
    this.eb.send(this.address, {"straddr": straddr, "key": key, "options": options}, {"action":"del"}, function(err, result) { resultHandler(err, result &&result.body); });
  }

  query(straddr: string, query: Object, options: Object, resultHandler: (err: any, result: any) => any) : void {
    if (closed) {
      throw new Error('Proxy is closed');
    }
    this.eb.send(this.address, {"straddr": straddr, "query": query, "options": options}, {"action":"query"}, function(err, result) { resultHandler(err, result &&result.body); });
  }

  clear(straddr: string, options: Object, resultHandler: (err: any, result: any) => any) : void {
    if (closed) {
      throw new Error('Proxy is closed');
    }
    this.eb.send(this.address, {"straddr": straddr, "options": options}, {"action":"clear"}, function(err, result) { resultHandler(err, result &&result.body); });
  }

}