# MAKEFILE_LIST specifies the current used Makefiles, of which this is the last
# one. I use that to obtain the Application.mk dir then import the root
# Application.mk.
ROOT_DIR := $(dir $(lastword $(MAKEFILE_LIST)))../../../../../../ovr_sdk_mobile_0.6.2.0
include $(ROOT_DIR)/Application.mk
