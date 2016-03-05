LOCAL_PATH := $(call my-dir)
include $(CLEAR_VARS)

IGNORED_WARNINGS := -Wno-sign-compare -Wno-unused-parameter -Wno-sign-promo

CC_LITE_SRC_FILES := \
    src/google/protobuf/stubs/common.cc                              \
    src/google/protobuf/stubs/once.cc                                \
    src/google/protobuf/stubs/hash.cc                                \
    src/google/protobuf/stubs/hash.h                                 \
    src/google/protobuf/stubs/map-util.h                             \
    src/google/protobuf/stubs/stl_util-inl.h                         \
    src/google/protobuf/extension_set.cc                             \
    src/google/protobuf/generated_message_util.cc                    \
    src/google/protobuf/message_lite.cc                              \
    src/google/protobuf/repeated_field.cc                            \
    src/google/protobuf/wire_format_lite.cc                          \
    src/google/protobuf/io/coded_stream.cc                           \
    src/google/protobuf/io/coded_stream_inl.h                        \
    src/google/protobuf/io/zero_copy_stream.cc                       \
    src/google/protobuf/io/zero_copy_stream_impl_lite.cc
    
# C++ full library
# =======================================================
protobuf_cc_full_src_files := \
    $(CC_LITE_SRC_FILES)                                             \
    src/google/protobuf/stubs/strutil.cc                             \
    src/google/protobuf/stubs/strutil.h                              \
    src/google/protobuf/stubs/substitute.cc                          \
    src/google/protobuf/stubs/substitute.h                           \
    src/google/protobuf/stubs/structurally_valid.cc                  \
    src/google/protobuf/descriptor.cc                                \
    src/google/protobuf/descriptor.pb.cc                             \
    src/google/protobuf/descriptor_database.cc                       \
    src/google/protobuf/dynamic_message.cc                           \
    src/google/protobuf/extension_set_heavy.cc                       \
    src/google/protobuf/generated_message_reflection.cc              \
    src/google/protobuf/message.cc                                   \
    src/google/protobuf/reflection_ops.cc                            \
    src/google/protobuf/service.cc                                   \
    src/google/protobuf/text_format.cc                               \
    src/google/protobuf/unknown_field_set.cc                         \
    src/google/protobuf/wire_format.cc                               \
    src/google/protobuf/io/gzip_stream.cc                            \
    src/google/protobuf/io/printer.cc                                \
    src/google/protobuf/io/tokenizer.cc                              \
    src/google/protobuf/io/zero_copy_stream_impl.cc                  \
    src/google/protobuf/compiler/importer.cc                         \
    src/google/protobuf/compiler/parser.cc
# C++ full library - stlport version
# =======================================================
LOCAL_MODULE := libprotobuf
LOCAL_MODULE_TAGS := optional
LOCAL_CPP_EXTENSION := .cc
LOCAL_SRC_FILES := $(protobuf_cc_full_src_files)
LOCAL_C_INCLUDES := \
    $(LOCAL_PATH)/android \
    external/zlib \
    $(LOCAL_PATH)/src
# Define the header files to be copied
#LOCAL_COPY_HEADERS := \
#    src/google/protobuf/stubs/once.h \
#    src/google/protobuf/stubs/common.h \
#    src/google/protobuf/io/coded_stream.h \
#    src/google/protobuf/generated_message_util.h \
#    src/google/protobuf/repeated_field.h \
#    src/google/protobuf/extension_set.h \
#    src/google/protobuf/wire_format_lite_inl.h
#
#LOCAL_COPY_HEADERS_TO := $(LOCAL_MODULE)
LOCAL_CFLAGS := -DGOOGLE_PROTOBUF_NO_RTTI $(IGNORED_WARNINGS)
# These are the minimum versions and don't need to be update.
ifeq ($(TARGET_ARCH),arm)
LOCAL_SDK_VERSION := 8
else
# x86/mips support only available from API 9.
LOCAL_SDK_VERSION := 9
endif
LOCAL_NDK_STL_VARIANT := stlport_static
include $(BUILD_STATIC_LIBRARY)