JNIPATH := $(call my-dir)
LOCAL_PATH := $(JNIPATH)

include $(call all-subdir-makefiles)

LOCAL_PATH := $(JNIPATH)

include $(CLEAR_VARS)

local_shared_libraries := libtee libprotobuf
local_cflags :=

ifeq ($(TARGET_ARCH),arm)
local_ldflags := -Wl,--hash-style=sysv
else
local_ldflags :=
endif

local_ldlibs :=

local_src_files := LibteeWrapper.c

local_c_includes := $(LOCAL_PATH)/libee/include \
                    $(LOCAL_PATH)/ \
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
LOCAL_MODULE := libteewrapper-jni
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
LOCAL_MODULE := libteewrapper-jni_static
LOCAL_MODULE_TAGS := optional
include $(BUILD_STATIC_LIBRARY)
