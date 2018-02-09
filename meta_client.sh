INTERFACE=$(groovy -cp service-proxy/build/libs/service-proxy-3.5.0-2.0.0-a1-fat.jar service-proxy/src/main/resources/JsonClass.groovy net.iowntheinter.kvdn.service.kvsvc | jq -c .)
envtpl < Client/cpp/KvdnClient.cpp.jinja2 > Client/cpp/KvdnClient.cpp
envtpl < Client/cpp/KvdnClient.hpp.jinja2 > Client/cpp/KvdnClient.hpp
