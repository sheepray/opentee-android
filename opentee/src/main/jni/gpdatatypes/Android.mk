LOCAL_PATH := $(call my-dir)
include $(CLEAR_VARS)

local_shared_libraries := libprotobuf
local_cflags := -DANDROID_NDK

ifeq ($(TARGET_ARCH),arm)
local_ldflags := -Wl,--hash-style=sysv
else
local_ldflags :=
endif

local_ldlibs := -L$(SYSROOT)/usr/lib -lz -llog

LOCAL_CPP_EXTENSION := .cc
local_src_files :=  GPDataTypes.pb.cc

local_c_includes := $(LOCAL_PATH)/../protobuf/google/protobuf \
                    $(LOCAL_PATH) \
                    external/zlib

#################################################
# Target dynamic library

include $(CLEAR_VARS)
LOCAL_SRC_FILES := $(local_src_files)
LOCAL_C_INCLUDES += $(local_c_includes)
LOCAL_CFLAGS += $(local_cflags)
LOCAL_LDFLAGS += $(local_ldflags)
LOCAL_LDLIBS += $(local_ldlibs)
LOCAL_SHARED_LIBRARIES += libc $(local_shared_libraries)
LOCAL_MODULE := gpdatatypes-jni
LOCAL_MODULE_TAGS := optional
include $(BUILD_SHARED_LIBRARY)

###############################################
# Target static library

include $(CLEAR_VARS)
LOCAL_SRC_FILES := $(local_src_files)
LOCAL_C_INCLUDES += $(local_c_includes)
LOCAL_CFLAGS += $(local_cflags)
LOCAL_LDLIBS += $(local_ldlibs)
LOCAL_STATIC_LIBRARIES += libc
LOCAL_SHARED_LIBRARIES += $(local_shared_libraries)
LOCAL_MODULE := gpdatatypes-jni_static
LOCAL_MODULE_TAGS := optional
include $(BUILD_STATIC_LIBRARY)
