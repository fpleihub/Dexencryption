NDK_TOOLCHAIN_VERSION=4.9
APP_ABI :=armeabi# armeabi-v7a x86 x86_64 arm64-v8a 
APP_STL=gnustl_static
APP_CPPFLAGS := -std=c++11 -fexceptions -frtti
APP_CPPFLAGS +=-fpermissive