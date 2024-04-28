#include <jni.h>
#include <string>


extern "C"
JNIEXPORT jstring JNICALL
Java_com_obb_backuprestore_utils_AppConfig_adsappopen(JNIEnv *env, jclass thiz) {
    std::string hello = "ca-app-pub-6823301895984878/2507002661";
    return env->NewStringUTF(hello.c_str());
}
extern "C"
JNIEXPORT jstring JNICALL
Java_com_obb_backuprestore_utils_AppConfig_adsnative(JNIEnv *env, jclass thiz) {
    std::string hello = "ca-app-pub-6823301895984878/8661100056";
    return env->NewStringUTF(hello.c_str());
}
extern "C"
JNIEXPORT jstring JNICALL
Java_com_obb_backuprestore_utils_AppConfig_adsintrestial(JNIEnv *env, jclass thiz) {
    std::string hello = "ca-app-pub-6823301895984878/5419859046";
    return env->NewStringUTF(hello.c_str());
}
extern "C"
JNIEXPORT jstring JNICALL
Java_com_obb_backuprestore_utils_AppConfig_adsbanner(JNIEnv *env, jclass thiz) {
    std::string hello = "ca-app-pub-3940256099942544/6300978111";
    return env->NewStringUTF(hello.c_str());
}
