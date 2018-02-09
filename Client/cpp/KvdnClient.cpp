//
// Created by g on 2/6/18.
//

#include <iostream>
#include <args.hxx>
#include <fstream>
#include <VertxBus.h>
#include <json/json.h>
#include "KvdnClient.h"
#include <functional>

using namespace std;


KvdnClient::KvdnClient(VertxBus *eb)
        : eb{eb} {}


KvdnClient::~KvdnClient() {
    delete eb;
};


void KvdnClient::Set(const string &straddr, const string &key, const string &value, VertxBus::ReplyHandler handler) {
    Json::Value hdrs;
    hdrs["action"] = "set";

    Json::Value jval;
    jval["straddr"] = straddr;
    jval["key"] = key;
    jval["value"] = value;
    Json::Value opts;
    opts["NOT"] = "EMPTY";
    jval["options"] = opts;

    Json::Value opdata;
    opdata["hdrs"] = hdrs;
    opdata["jval"] = jval;


    this->kvdnOP(straddr, opdata, handler);
    /*VertxBus::ReplyHandler(

            [&](const Json::Value &rjmsg, const VertxBus::VertxBusReplier &rvbr) {


                std::cout << "in resp" << std::endl;
                handler(rjmsg, VertxBus::VertxBusReplier(eb, "questionable reply address?"));
                //do we even need a reployer? maybe we need a special type of handler, like an
                //kvtxn result handler .... generic Handler<AsyncResult>?
                //std::cout << "Received:\n" << writer.write(rjmsg["body"]) << std::endl;
                // vertxeb.Close();
            }));*/
}

void KvdnClient::Submit(const string &straddr, string value, VertxBus::ReplyHandler handler) {
    Json::Value hdrs;
    hdrs["action"] = "submit";

    Json::Value jval;
    jval["straddr"] = straddr;
    jval["value"] = value;
    Json::Value opts;
    opts["NOT"] = "EMPTY";
    jval["options"] = opts;

    Json::Value opdata;
    opdata["hdrs"] = hdrs;
    opdata["jval"] = jval;
    this->kvdnOP(straddr, opdata, std::move(handler));

}

void KvdnClient::GetKeys(const string &straddr, VertxBus::ReplyHandler handler) {
    Json::Value hdrs;
    hdrs["action"] = "getKeys";

    Json::Value jval;
    jval["straddr"] = straddr;
    Json::Value opts;
    opts["NOT"] = "EMPTY";
    jval["options"] = opts;

    Json::Value opdata;
    opdata["hdrs"] = hdrs;
    opdata["jval"] = jval;
    this->kvdnOP(straddr, opdata, std::move(handler));

}


void KvdnClient::Get(const string &straddr, string key, VertxBus::ReplyHandler handler) {
    Json::Value hdrs;
    hdrs["action"] = "get";

    Json::Value jval;
    jval["straddr"] = straddr;
    jval["key"] = key;

    Json::Value opts;
    opts["NOT"] = "EMPTY";
    jval["options"] = opts;

    Json::Value opdata;
    opdata["hdrs"] = hdrs;
    opdata["jval"] = jval;
    this->kvdnOP(straddr, opdata, std::move(handler));

}

void KvdnClient::Query(const string &straddr, string query, VertxBus::ReplyHandler handler) {
    Json::Value hdrs;
    hdrs["action"] = "query";

    Json::Value jval;
    jval["straddr"] = straddr;
    jval["query"] = query;

    Json::Value opts;
    opts["NOT"] = "EMPTY";
    jval["options"] = opts;

    Json::Value opdata;
    opdata["hdrs"] = hdrs;
    opdata["jval"] = jval;
    this->kvdnOP(straddr, opdata, std::move(handler));

}

void KvdnClient::kvdnOP(const string &straddr, Json::Value opdata, VertxBus::ReplyHandler handler) {
    Json::FastWriter fastWriter;

    //std::cout << "fireing kvdn op with data " << fastWriter.write(opdata.asString()) << std::endl;

    std::cout << "issuing kvdn operation";
    std::string data = opdata.toStyledString();
    std::cout << data;

    eb->Send("kvdnsvc", opdata["jval"], opdata["hdrs"], handler);
}

void KvdnClient::Delete(const string &straddr, string key, VertxBus::ReplyHandler handler) {
    Json::Value hdrs;
    hdrs["action"] = "delete"; // reserved keyword in c++

    Json::Value jval;
    jval["straddr"] = straddr;
    jval["key"] = key;

    Json::Value opts;
    opts["NOT"] = "EMPTY";
    jval["options"] = opts;

    Json::Value opdata;
    opdata["hdrs"] = hdrs;
    opdata["jval"] = jval;
    this->kvdnOP(straddr, opdata, std::move(handler));

}

void KvdnClient::Size(const string &straddr, VertxBus::ReplyHandler handler) {
    Json::Value hdrs;
    hdrs["action"] = "size"; // reserved keyword in c++

    Json::Value jval;
    jval["straddr"] = straddr;

    Json::Value opts;
    opts["NOT"] = "EMPTY";
    jval["options"] = opts;

    Json::Value opdata;
    opdata["hdrs"] = hdrs;
    opdata["jval"] = jval;
    this->kvdnOP(straddr, opdata, std::move(handler));

}

