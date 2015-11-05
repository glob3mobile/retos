LOCAL_PATH := $(call my-dir)

include $(CLEAR_VARS)

include ../../../../../ovr_sdk_mobile_0.6.2.0/cflags.mk

LOCAL_MODULE			:= ovrapp
LOCAL_SRC_FILES			:= ../../../Src/OvrApp.cpp
LOCAL_STATIC_LIBRARIES	:= vrsound vrmodel vrlocale vrgui vrappframework libovrkernel
LOCAL_SHARED_LIBRARIES	:= vrapi

include $(BUILD_SHARED_LIBRARY)

$(call import-module,LibOVRKernel/Projects/AndroidPrebuilt/jni)
$(call import-module,VrApi/Projects/AndroidPrebuilt/jni)
$(call import-module,VrAppFramework/Projects/AndroidPrebuilt/jni)
$(call import-module,VrAppSupport/VrGui/Projects/Android/jni)
$(call import-module,VrAppSupport/VrLocale/Projects/Android/jni)
$(call import-module,VrAppSupport/VrModel/Projects/Android/jni)
$(call import-module,VrAppSupport/VrSound/Projects/Android/jni)