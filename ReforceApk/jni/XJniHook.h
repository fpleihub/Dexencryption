#ifndef __PLUGIN_HOOK_H_
#define __PLUGIN_HOOK_H_
#include <jni.h>


#ifdef __cplusplus
extern "C" {
#endif
	
void Java_com_handpay_shell_DexOperation_nativeEnableTurboDex(JNIEnv* env,jobject thiz);

void Java_com_handpay_shell_DexOperation_nativeDisableTurboDex(JNIEnv* env,jobject thiz);

jobjectArray Java_com_handpay_shell_DexOperation_initEnvriment(JNIEnv* env,jobject thiz,jstring path);

#ifdef __cplusplus
}
#endif

#endif //__PLUGIN_HOOK_H_
