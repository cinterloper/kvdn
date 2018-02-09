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

    
    void set(const string &options,const string &value,const string &key,const string &straddr,  VertxBus::ReplyHandler handler);
    
    void get(const string &options,const string &key,const string &straddr,  VertxBus::ReplyHandler handler);
    
    void submit(const string &options,const string &value,const string &straddr,  VertxBus::ReplyHandler handler);
    
    void getKeys(const string &options,const string &straddr,  VertxBus::ReplyHandler handler);
    
    void query(const string &query,const string &options,const string &straddr,  VertxBus::ReplyHandler handler);
    
    void size(const string &options,const string &straddr,  VertxBus::ReplyHandler handler);
    
    void delete(const string &options,const string &key,const string &straddr,  VertxBus::ReplyHandler handler);
    

private:
    VertxBus *eb;
    void kvdnOP(const string &straddr, Json::Value opdata, VertxBus::ReplyHandler handler);

};


#endif //BASICFILETRAVERSE_KVDN_H
