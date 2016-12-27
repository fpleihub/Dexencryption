#include <string.h>
#include <string>
#include <stdio.h>
#include <sys/types.h>
#include <sys/stat.h>
#include <dirent.h>
#include "XJniHook.h"
#include "Logger.h"
#include "core/FastLoadDex.h"
using namespace std;
char* jstringTostring(JNIEnv* env, jstring jstr)
{
    char* rtn = NULL;
    jclass clsstring = env->FindClass("java/lang/String");
    jstring strencode =env->NewStringUTF("utf-8");
    jmethodID mid = env->GetMethodID(clsstring, "getBytes", "(Ljava/lang/String;)[B");
    jbyteArray barr = (jbyteArray)env->CallObjectMethod(jstr, mid, strencode);
    jsize alen = env->GetArrayLength(barr);
    jbyte* ba = env->GetByteArrayElements(barr, JNI_FALSE);
    if (alen > 0)
    {
        rtn = (char*)malloc(alen + 1);
        memcpy(rtn, ba, alen);
        rtn[alen] = 0;
    }
    env->ReleaseByteArrayElements(barr, ba, 0);
    return rtn;
}
jstring stoJstring(JNIEnv* env, char* pat)
{
    jclass strClass = env->FindClass("java/lang/String");
    jmethodID ctorID = env->GetMethodID(strClass, "<init>", "([BLjava/lang/String;)V");
    jbyteArray bytes = env->NewByteArray(strlen(pat));
    env->SetByteArrayRegion(bytes, 0, strlen(pat), (jbyte*)pat);
    jstring encoding = env->NewStringUTF("utf-8");
    return (jstring)env->NewObject(strClass, ctorID, bytes, encoding);
}
const string rootworkdir="/data/data/";
const string dirlibs="/libs";
const string dirdex="/dexs";
const string dirplugs="/plugs";
int createdir(JNIEnv* env,char* path){
	if (NULL == opendir(path)) { //目录不存在
			if (0 == mkdir(path, 0777)) {
				return 0;
			} else {
				return -1;
			}
	} else {
			return 1;
	}
}

jobjectArray Java_com_handpay_shell_DexOperation_initEnvriment(JNIEnv* env,
		jobject thiz,jstring pkg_name) {
	LOGI("_initEnvriment");
	if(NULL==pkg_name){
		LOGI("NULL==pkg_name");
	}
	jobjectArray args = 0;
	const char* temp=jstringTostring(env,pkg_name);
	string pkname=temp;
	string dirlibs1=rootworkdir+pkname+dirlibs;
	char* _dirlibs=dirlibs1.c_str();
	int flag=createdir(env,_dirlibs);
	string dirdex1=rootworkdir+pkname+dirdex;
	char* _dirdex=dirdex1.c_str();
	int flag1=createdir(env,_dirdex);
	string dirplugs1=rootworkdir+pkname+dirplugs;
	char* _dirplugs=dirplugs1.c_str();
	int flag2=createdir(env,_dirplugs);
	char* sa[]={_dirlibs,_dirdex,_dirplugs};
	jsize len = sizeof(sa)/sizeof(sa[0]);
	int i=0;
	args = env->NewObjectArray(len,env->FindClass("java/lang/String"),0);
	jstring str;
	for( i=0; i < len; i++ ){
		 str = env->NewStringUTF(sa[i] );
		 env->SetObjectArrayElement(args, i, str);
	}
	return args;
}

void Java_com_handpay_shell_DexOperation_nativeEnableTurboDex(JNIEnv* env,
		jobject thiz) {
	LOGI("Java_com_handpay_shell_DexOperation_nativeEnableTurboDex");
	enableFastLoadDex();
}

void Java_com_handpay_shell_DexOperation_nativeDisableTurboDex(JNIEnv* env,
		jobject thiz) {
	closeFastLoadDex();
}



