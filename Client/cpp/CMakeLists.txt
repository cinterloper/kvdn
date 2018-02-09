#add_definitions(-DBACKWARD_HAS_BFD=1)

cmake_minimum_required(VERSION 3.7)
project(KvdnClient VERSION 1.0.1)
set(CMAKE_MODULE_PATH ${CMAKE_MODULE_PATH} "${CMAKE_SOURCE_DIR}/cmake/Modules/")
set(CMAKE_CXX_STANDARD 11)
include_directories(./ /usr/include/uuid/ /usr/include/ /core/software/libs/cxx_args/include asio/include websocketpp json)
set(SOURCE_FILES test.cpp VertxBus.cpp json/jsoncpp.cpp KvdnClient.cpp KvdnClient.h)
#add_subdirectory(backward-cpp)

add_library(Kvdn SHARED KvdnClient.cpp)
add_executable(KvdnClient ${SOURCE_FILES} )# ${BACKWARD_ENABLE})
#add_backward(KvdnClient)
set_target_properties(Kvdn PROPERTIES
        VERSION ${PROJECT_VERSION}
        SOVERSION 1
        PUBLIC_HEADER KvdnClient.h)

find_package(PkgConfig REQUIRED)
include(GNUInstallDirs)

pkg_search_module(UUID REQUIRED uuid)
target_include_directories(KvdnClient PUBLIC ${UUID_INCLUDE_DIRS})
find_package(Threads)
#find_package(UUID)
pkg_search_module(UUID REQUIRED uuid)
#target_link_libraries(BACKWARD)
find_package(LibUuid REQUIRED)
include_directories(${LIBUUID_INCLUDE_DIRS})
#list(APPEND CMAKE_MODULE_PATH ./backward-cpp)
#find_package(Backward)
target_link_libraries(KvdnClient ${CMAKE_THREAD_LIBS_INIT} ${LIBUUID_LIBRARIES})

link_directories(${UUID_LIBRARY_DIRS})
set(CMAKE_CXX_FLAGS " ${CMAKE_CXX_FLAGS} -g -O0 -luuid -pthread -lbfd -ldl  ")
configure_file(kvdn.pc.in kvdn.pc @ONLY)
target_include_directories(Kvdn PRIVATE .)
install(TARGETS Kvdn
        LIBRARY DESTINATION ${CMAKE_INSTALL_LIBDIR}
        PUBLIC_HEADER DESTINATION ${CMAKE_INSTALL_INCLUDEDIR})
install(FILES ${CMAKE_BINARY_DIR}/kvdn.pc
        DESTINATION ${CMAKE_INSTALL_DATAROOTDIR}/pkgconfig)