#include <jniWrapper.hpp>

#ifdef __cplusplus
extern "C" {
#endif

JNIEXPORT bool JNICALL
Java_com_example_jaycee_pomdpobjectsearch_JNIBridge_initSound(JNIEnv* env, jobject obj)
{
    soundGenerator->init();
    soundGenerator->startSound();

    return true;
}

JNIEXPORT bool JNICALL
Java_com_example_jaycee_pomdpobjectsearch_JNIBridge_killSound(JNIEnv* env, jobject obj)
{
    soundGenerator->endSound();
    soundGenerator->kill();

    return true;
}

JNIEXPORT void JNICALL
Java_com_example_jaycee_pomdpobjectsearch_JNIBridge_playSound(JNIEnv* env, jobject obj, jfloatArray src, jfloatArray list, jfloat gain, jfloat pitch)
{
    soundGenerator->play(env, src, list, gain, pitch);
}

JNIEXPORT bool JNICALL
Java_com_example_jaycee_pomdpobjectsearch_JNIBridge_initDetector(JNIEnv* env, jobject obj)
{
    return markerDetector->init();
}

JNIEXPORT bool JNICALL
Java_com_example_jaycee_pomdpobjectsearch_JNIBridge_killDetector(JNIEnv* env, jobject obj)
{
    return markerDetector->kill();
}

JNIEXPORT void JNICALL
Java_com_example_jaycee_pomdpobjectsearch_JNIBridge_processImage(JNIEnv* env, jobject obj, jintArray data)
{
    jboolean isCopy;
    jint* imageData = env->GetIntArrayElements(data, &isCopy);
    markerDetector->processImage((unsigned char*)imageData);

    env->ReleaseIntArrayElements(data, imageData, JNI_ABORT);
}

#ifdef __cplusplus
}
#endif
