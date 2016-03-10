LOCAL_PATH := $(call my-dir)
include $(CLEAR_VARS)

LOCAL_CPPFLAGS := -std=c++11

LOCAL_MODULE    := GPDataTypes

# To test compilation, generate a .pb.cc file and add it to local_src_files,
# this will make the linker complain about everything that's missing because
# we're building a shared library
LOCAL_SRC_FILES := GPDataTypes.pb.cc 

LOCAL_STATIC_LIBRARIES += libprotobuf
LOCAL_LDLIBS := -lz

include $(BUILD_SHARED_LIBRARY)

# This assumes protobuf-android is checked out in a directory named
# protobuf-android
$(call import-module,protobuf-android)
