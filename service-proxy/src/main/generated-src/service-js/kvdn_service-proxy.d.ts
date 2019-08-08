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


/**
 Created by g on 9/15/16.

 @class
*/
export default class KvdnService {

  constructor (eb: any, address: string);

  set(straddr: string, key: string, value: string, options: Object, resultHandler: (err: any, result: string) => any) : void;

  submit(straddr: string, value: string, options: Object, resultHandler: (err: any, result: string) => any) : void;

  get(straddr: string, key: string, options: Object, resultHandler: (err: any, result: string) => any) : void;

  size(straddr: string, options: Object, resultHandler: (err: any, result: any) => any) : void;

  getKeys(straddr: string, options: Object, resultHandler: (err: any, result: Array) => any) : void;

  del(straddr: string, key: string, options: Object, resultHandler: (err: any, result: string) => any) : void;

  query(straddr: string, query: Object, options: Object, resultHandler: (err: any, result: Object) => any) : void;

  clear(straddr: string, options: Object, resultHandler: (err: any, result: any) => any) : void;

  ctrGet(straddr: string, options: Object, cb: (err: any, result: any) => any) : void;

  addAndGet(straddr: string, value: number, options: Object, cb: (err: any, result: any) => any) : void;

  getAndAdd(straddr: string, value: number, options: Object, cb: (err: any, result: any) => any) : void;

  ctrCompareAndSet(straddr: string, oldv: number, newv: number, options: Object, cb: (err: any, result: any) => any) : void;

  enqueue(straddr: string, options: Object, value: string, cb: (err: any, result: any) => any) : void;

  dequeue(straddr: string, options: Object, cb: (err: any, result: string) => any) : void;

  qPeek(straddr: string, options: Object, cb: (err: any, result: string) => any) : void;

  qArrayView(straddr: string, options: Object, cb: (err: any, result: Object) => any) : void;
}