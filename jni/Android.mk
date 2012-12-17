LOCAL_PATH:= $(call my-dir)

include $(CLEAR_VARS)

LOCAL_CFLAGS += -DEGL_EGLEXT_PROTOTYPES

LOCAL_SRC_FILES := jni_egl_fence.cpp

LOCAL_SHARED_LIBRARIES :=  liblog libEGL

LOCAL_MODULE_TAGS := optional

LOCAL_MODULE := libjni_eglfence


include $(BUILD_SHARED_LIBRARY)

# Filtershow

include $(CLEAR_VARS)

LOCAL_CPP_EXTENSION := .cc
LOCAL_SHARED_LIBRARIES :=  liblog libjnigraphics
LOCAL_MODULE    := libjni_filtershow_filters
LOCAL_SRC_FILES := filters/bw.c \
                   filters/gradient.c \
                   filters/saturated.c \
                   filters/exposure.c \
                   filters/contrast.c \
                   filters/hue.c \
                   filters/shadows.c \
                   filters/hsv.c \
                   filters/vibrance.c \
                   filters/geometry.c \
                   filters/vignette.c \
                   filters/redEyeMath.c \
                   filters/fx.c \
                   filters/wbalance.c \
                   filters/redeye.c \
                   filters/bwfilter.c \
                   filters/tinyplanet.cc

LOCAL_CFLAGS    += -ffast-math -O3 -funroll-loops
LOCAL_ARM_MODE := arm

include $(BUILD_SHARED_LIBRARY)
