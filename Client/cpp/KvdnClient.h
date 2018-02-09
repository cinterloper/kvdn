//
// Created by g on 2/6/18.
//

#ifndef BASICFILETRAVERSE_KVDN_H
#define BASICFILETRAVERSE_KVDN_H

#include <VertxBus.h>
#include <json/json.h>

using namespace std;

class KvdnClient {

public:
    KvdnClient(VertxBus *eb);
    ~KvdnClient();
    void Set(const string &straddr, const string &key, const string &value, VertxBus::ReplyHandler handler);

    void Submit(const string &straddr, string value, VertxBus::ReplyHandler handler);

    void GetKeys(const string &straddr, VertxBus::ReplyHandler handler);


    void Get(const string &straddr, string key, VertxBus::ReplyHandler handler);

    void Query(const string &straddr, string query, VertxBus::ReplyHandler handler);

    void Delete(const string &straddr, string key, VertxBus::ReplyHandler handler);

    void Size(const string &straddr, VertxBus::ReplyHandler handler);
private:
    VertxBus *eb;


    void kvdnOP(const string &straddr, Json::Value opdata, VertxBus::ReplyHandler handler);


};


#endif //BASICFILETRAVERSE_KVDN_H
