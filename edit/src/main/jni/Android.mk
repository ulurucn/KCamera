LOCAL_PATH := $(call my-dir)
include $(CLEAR_VARS)

LOCAL_MODULE := picwarp
LOCAL_LDFLAGS := -Wl,--build-id
LOCAL_LDLIBS += -llog

LOCAL_SRC_FILES := Picwarp.cpp

include $(BUILD_SHARED_LIBRARY)
