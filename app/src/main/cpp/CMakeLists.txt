cmake_minimum_required(VERSION 3.22.1)
project(edgeproc)

set(CMAKE_CXX_STANDARD 17)
set(CMAKE_CXX_STANDARD_REQUIRED ON)

add_library(edgeproc SHARED
    native-lib.cpp
)

find_library(log-lib log)
find_library(android-lib android)
find_library(gles-lib GLESv2)

# Expect OpenCV for Android SDK. Set OpenCV_DIR in local.properties as:
#   opencvsdk.dir=/absolute/path/to/OpenCV-android-sdk
# And pass -DOpenCV_DIR=${opencvsdk.dir}/sdk/native/jni when configuring, handled by Gradle.

if(DEFINED OpenCV_DIR)
    message(STATUS "OpenCV_DIR is set to: ${OpenCV_DIR}")
endif()

find_package(OpenCV REQUIRED)

message(STATUS "OpenCV libraries: ${OpenCV_LIBS}")

include_directories(${OpenCV_INCLUDE_DIRS})

target_link_libraries(edgeproc
    ${OpenCV_LIBS}
    ${log-lib}
    ${gles-lib}
    ${android-lib}
)
