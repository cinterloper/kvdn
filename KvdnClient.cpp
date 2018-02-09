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


void KvdnClient::set(const string &options, const string &value, const string &key, const string &straddr,   VertxBus::ReplyHandler handler) {
    Json::Value hdrs;
    hdrs["action"] = "set";

    Json::Value jval;
    
    jval["options"] = options
    
    jval["value"] = value
    
    jval["key"] = key
    
    jval["straddr"] = straddr
    
    Json::Value opts;
    opts["NOT"] = "EMPTY";
    jval["options"] = opts;

    Json::Value opdata;
    opdata["hdrs"] = hdrs;
    opdata["jval"] = jval;


    this->kvdnOP(straddr, opdata, handler);
}

void KvdnClient::get(const string &options, const string &key, const string &straddr,   VertxBus::ReplyHandler handler) {
    Json::Value hdrs;
    hdrs["action"] = "get";

    Json::Value jval;
    
    jval["options"] = options
    
    jval["key"] = key
    
    jval["straddr"] = straddr
    
    Json::Value opts;
    opts["NOT"] = "EMPTY";
    jval["options"] = opts;

    Json::Value opdata;
    opdata["hdrs"] = hdrs;
    opdata["jval"] = jval;


    this->kvdnOP(straddr, opdata, handler);
}

void KvdnClient::submit(const string &options, const string &value, const string &straddr,   VertxBus::ReplyHandler handler) {
    Json::Value hdrs;
    hdrs["action"] = "submit";

    Json::Value jval;
    
    jval["options"] = options
    
    jval["value"] = value
    
    jval["straddr"] = straddr
    
    Json::Value opts;
    opts["NOT"] = "EMPTY";
    jval["options"] = opts;

    Json::Value opdata;
    opdata["hdrs"] = hdrs;
    opdata["jval"] = jval;


    this->kvdnOP(straddr, opdata, handler);
}

void KvdnClient::getKeys(const string &options, const string &straddr,   VertxBus::ReplyHandler handler) {
    Json::Value hdrs;
    hdrs["action"] = "getKeys";

    Json::Value jval;
    
    jval["options"] = options
    
    jval["straddr"] = straddr
    
    Json::Value opts;
    opts["NOT"] = "EMPTY";
    jval["options"] = opts;

    Json::Value opdata;
    opdata["hdrs"] = hdrs;
    opdata["jval"] = jval;


    this->kvdnOP(straddr, opdata, handler);
}

void KvdnClient::query(const string &query, const string &options, const string &straddr,   VertxBus::ReplyHandler handler) {
    Json::Value hdrs;
    hdrs["action"] = "query";

    Json::Value jval;
    
    jval["query"] = query
    
    jval["options"] = options
    
    jval["straddr"] = straddr
    
    Json::Value opts;
    opts["NOT"] = "EMPTY";
    jval["options"] = opts;

    Json::Value opdata;
    opdata["hdrs"] = hdrs;
    opdata["jval"] = jval;


    this->kvdnOP(straddr, opdata, handler);
}

void KvdnClient::size(const string &options, const string &straddr,   VertxBus::ReplyHandler handler) {
    Json::Value hdrs;
    hdrs["action"] = "size";

    Json::Value jval;
    
    jval["options"] = options
    
    jval["straddr"] = straddr
    
    Json::Value opts;
    opts["NOT"] = "EMPTY";
    jval["options"] = opts;

    Json::Value opdata;
    opdata["hdrs"] = hdrs;
    opdata["jval"] = jval;


    this->kvdnOP(straddr, opdata, handler);
}

void KvdnClient::delete(const string &options, const string &key, const string &straddr,   VertxBus::ReplyHandler handler) {
    Json::Value hdrs;
    hdrs["action"] = "delete";

    Json::Value jval;
    
    jval["options"] = options
    
    jval["key"] = key
    
    jval["straddr"] = straddr
    
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
