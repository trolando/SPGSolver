cmake_minimum_required(VERSION 3.2)
project(CPPsolver)

set(CMAKE_CXX_FLAGS "${CMAKE_CXX_FLAGS} -std=c++11 -pthread")

include_directories("/usr/local/include")

set(SOURCE_FILES main.cpp)
add_executable(CPPsolver ${SOURCE_FILES})
