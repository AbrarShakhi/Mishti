#include <jni.h>
#include <vector>

#include "cactus.h"

extern "C" JNIEXPORT jint JNICALL
Java_com_abrarshakhi_mishti_core_speech_WhistleNative_load(JNIEnv *env, jobject, jbyteArray model) {
    const jsize size = env->GetArrayLength(model);
    jbyte *data = env->GetByteArrayElements(model, nullptr);
    if (!data) return -1;
    const int result = cactus_load(reinterpret_cast<unsigned char *>(data), size);
    env->ReleaseByteArrayElements(model, data, JNI_ABORT);
    return result;
}

extern "C" JNIEXPORT jint JNICALL
Java_com_abrarshakhi_mishti_core_speech_WhistleNative_process(
        JNIEnv *env, jobject, jfloatArray audio, jint count, jbyteArray output) {
    std::vector<float> samples(count);
    env->GetFloatArrayRegion(audio, 0, count, samples.data());
    if (env->ExceptionCheck()) return -1;
    char *json = reinterpret_cast<char *>(env->GetByteArrayElements(output, nullptr));
    if (!json) return -1;
    const int result = cactus_stream_transcribe_process(
            samples.data(), count, nullptr, nullptr, json, env->GetArrayLength(output));
    env->ReleaseByteArrayElements(output, reinterpret_cast<jbyte *>(json), 0);
    return result;
}

extern "C" JNIEXPORT jint JNICALL
Java_com_abrarshakhi_mishti_core_speech_WhistleNative_stop(JNIEnv *env, jobject, jbyteArray output) {
    char *json = reinterpret_cast<char *>(env->GetByteArrayElements(output, nullptr));
    if (!json) return -1;
    const int result = cactus_stream_transcribe_stop(json, env->GetArrayLength(output));
    env->ReleaseByteArrayElements(output, reinterpret_cast<jbyte *>(json), 0);
    return result;
}
