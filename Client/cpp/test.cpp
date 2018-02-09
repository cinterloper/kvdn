#include <iostream>
#include <args.hxx>
#include <fstream>
#include <VertxBus.h>
#include <json/json.h>
#include "KvdnClient.hpp"
#define BACKWARD_HAS_BFD 1

//#include "backward.hpp"

using namespace std;

vector<long> InitalizeVector(long content, unsigned long len);

vector<long> ModifyVector(vector<long> &v);


class resource {
public:
    int x = 42;

    void assign(int y);
};

void resource::assign(int y) {
    x = y;
}

int main(int argc, char **argv) {
    VertxBus vertxeb;
    KvdnClient *k = new KvdnClient(&vertxeb);
    KvdnClient kvc = *k;


    args::ArgumentParser parser("this is a test program", "this goes after the options");
    args::HelpFlag help(parser, "help", "display this help menu", {'h', "help"});
    try {
        parser.ParseCLI(argc, argv);
    } catch (args::Help) {
        std::cout << parser;
        return 0;
    } catch (args::ParseError e) {
        std::cerr << e.what() << std::endl;
        std::cerr << parser;
        return 1;
    }
    vector<long> testVec = InitalizeVector(3, 13ul);
    for (long &i : testVec) {
        cout << i << " ";
    }
    cout << "\n";
    testVec = ModifyVector(testVec);
    for (long &i : testVec) {
        cout << i << " ";
    }
    ofstream myfileOut;
    myfileOut.open("/tmp/example");

    myfileOut << "Hello, World!" << std::endl;




    std::cout<<"\nbefore connect\n";
    vertxeb.Connect("http://172.17.0.1:6501/eb",
                    [&] {
                        // OnOpen
                        std::cout << "\non connection\n";

                        vertxeb.RegisterHandler("_KVDN_+this_that", VertxBus::ReplyHandler(
                                [&](const Json::Value &jmsg, const VertxBus::VertxBusReplier &vbr) {
                                    Json::StyledWriter writer;
                                    std::cout << "Received:\n" << writer.write(jmsg["body"]) << std::endl;
                                    // jval["value"] = "some_text_example"; //want assignment here
                                    std::cout << "after assignment" << std::endl;
                                    Json::Value hdrs;
                                    std::cout << "after creation of heaers" << std::endl;

                                    hdrs["action"] = "Set";
                                    std::cout << "after assignment of heaers" << std::endl;
                                    Json::FastWriter fastWriter;

                                    kvc.set("something_else","{}", (jmsg["body"]["key"].asString()), fastWriter.write(jmsg),
                                            VertxBus::ReplyHandler(


                                                    [&](const Json::Value &rjmsg,
                                                        const VertxBus::VertxBusReplier &rvbr) {
                                                        std::cout << "in  kvc Set resp" << std::endl;
                                                        //std::cout << "Received:\n" << writer.write(rjmsg["body"]) << std::endl;
                                                        // vertxeb.Close();
                                                    }));
                                    kvc.getKeys("something_else","{}", VertxBus::ReplyHandler(


                                            [&](const Json::Value &rjmsg,
                                                const VertxBus::VertxBusReplier &rvbr) {
                                                std::cout << "KEYS: " << rjmsg.toStyledString();
                                            })
                                    );


                                    std::cout << "after send body" << std::endl;


                                }));

                    },
                    [&](const std::error_code &ec) {
                        // OnClose
                        std::cout << "Connection closed." << std::endl;
                    },
                    [&](const std::error_code &ec, const Json::Value &jmsg_fail) {
                        // OnFail
                        std::cerr << "Connection failed: " << ec.message() << std::endl;

                        if (!jmsg_fail.empty()) {
                            Json::StyledWriter writer;
                            std::cerr << writer.write(jmsg_fail) << std::endl;
                        }
                    });

    vertxeb.WaitClosed();
    return 0;
}

vector<long> InitalizeVector(long content, unsigned long len) {
    vector<long> v(len);
    for (unsigned long i = 0; i < len; i++) {
        v[i] = content;
    }
    return v;
}

vector<long> ModifyVector(vector<long> &v) {

    for (long &i : v) {
        i = random() % 100;
    }
    return v;
}


