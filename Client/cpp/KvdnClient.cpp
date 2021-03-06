//
// Created by g on 2/6/18.
//

#include <iostream>
#include <args.hxx>
#include <fstream>
#include <VertxBus.h>
#include <json/json.h>
#include "KvdnClient.hpp"
#include <functional>

using namespace std;


KvdnClient::KvdnClient(VertxBus *eb)
        : eb{eb} {}


KvdnClient::~KvdnClient() {
    delete eb;
};


void KvdnClient::set(const string &straddr, const string &key, const string &value, const string &options,   VertxBus::ReplyHandler handler) {
    Json::Value hdrs;
    hdrs["action"] = "set";

    Json::Value jval;
    
    jval["straddr"] = straddr;
    
    jval["key"] = key;
    
    jval["value"] = value;
    
    jval["options"] = options;
    
    Json::Value opts;
    opts["NOT"] = "EMPTY";
    jval["options"] = opts;

    Json::Value opdata;
    opdata["hdrs"] = hdrs;
    opdata["jval"] = jval;


    this->kvdnOP(straddr, opdata, handler);
}

void KvdnClient::get(const string &straddr, const string &key, const string &options,   VertxBus::ReplyHandler handler) {
    Json::Value hdrs;
    hdrs["action"] = "get";

    Json::Value jval;
    
    jval["straddr"] = straddr;
    
    jval["key"] = key;
    
    jval["options"] = options;
    
    Json::Value opts;
    opts["NOT"] = "EMPTY";
    jval["options"] = opts;

    Json::Value opdata;
    opdata["hdrs"] = hdrs;
    opdata["jval"] = jval;


    this->kvdnOP(straddr, opdata, handler);
}

void KvdnClient::submit(const string &straddr, const string &value, const string &options,   VertxBus::ReplyHandler handler) {
    Json::Value hdrs;
    hdrs["action"] = "submit";

    Json::Value jval;
    
    jval["straddr"] = straddr;
    
    jval["value"] = value;
    
    jval["options"] = options;
    
    Json::Value opts;
    opts["NOT"] = "EMPTY";
    jval["options"] = opts;

    Json::Value opdata;
    opdata["hdrs"] = hdrs;
    opdata["jval"] = jval;


    this->kvdnOP(straddr, opdata, handler);
}

void KvdnClient::getKeys(const string &straddr, const string &options,   VertxBus::ReplyHandler handler) {
    Json::Value hdrs;
    hdrs["action"] = "getKeys";

    Json::Value jval;
    
    jval["straddr"] = straddr;
    
    jval["options"] = options;
    
    Json::Value opts;
    opts["NOT"] = "EMPTY";
    jval["options"] = opts;

    Json::Value opdata;
    opdata["hdrs"] = hdrs;
    opdata["jval"] = jval;


    this->kvdnOP(straddr, opdata, handler);
}

void KvdnClient::del(const string &straddr, const string &key, const string &options,   VertxBus::ReplyHandler handler) {
    Json::Value hdrs;
    hdrs["action"] = "del";

    Json::Value jval;
    
    jval["straddr"] = straddr;
    
    jval["key"] = key;
    
    jval["options"] = options;
    
    Json::Value opts;
    opts["NOT"] = "EMPTY";
    jval["options"] = opts;

    Json::Value opdata;
    opdata["hdrs"] = hdrs;
    opdata["jval"] = jval;


    this->kvdnOP(straddr, opdata, handler);
}

void KvdnClient::query(const string &straddr, const string &query, const string &options,   VertxBus::ReplyHandler handler) {
    Json::Value hdrs;
    hdrs["action"] = "query";

    Json::Value jval;
    
    jval["straddr"] = straddr;
    
    jval["query"] = query;
    
    jval["options"] = options;
    
    Json::Value opts;
    opts["NOT"] = "EMPTY";
    jval["options"] = opts;

    Json::Value opdata;
    opdata["hdrs"] = hdrs;
    opdata["jval"] = jval;


    this->kvdnOP(straddr, opdata, handler);
}

void KvdnClient::size(const string &straddr, const string &options,   VertxBus::ReplyHandler handler) {
    Json::Value hdrs;
    hdrs["action"] = "size";

    Json::Value jval;
    
    jval["straddr"] = straddr;
    
    jval["options"] = options;
    
    Json::Value opts;
    opts["NOT"] = "EMPTY";
    jval["options"] = opts;

    Json::Value opdata;
    opdata["hdrs"] = hdrs;
    opdata["jval"] = jval;


    this->kvdnOP(straddr, opdata, handler);
}



void KvdnClient::kvdnOP(const string &straddr, Json::Value opdata, VertxBus::ReplyHandler handler) {
    Json::FastWriter fastWriter;

    //std::cout << "fireing kvdn op with data " << fastWriter.write(opdata.asString()) << std::endl;

    std::cout << "issuing kvdn operation";
    std::string data = opdata.toStyledString();
    std::cout << data;

    eb->Send("kvdnsvc", opdata["jval"], opdata["hdrs"], handler);
}
