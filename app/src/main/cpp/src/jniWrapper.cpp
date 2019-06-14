#include <jniWrapper.hpp>

#ifdef __cplusplus
extern "C" {
#endif

JNIEXPORT bool JNICALL
Java_com_example_jaycee_mdpobjectsearch_JNIBridge_initSound(JNIEnv* env, jobject obj)
{
    soundGenerator = new SoundGenerator::SoundGenerator();
    soundGenerator->init();
    soundGenerator->startSound();

    return true;
}

JNIEXPORT bool JNICALL
Java_com_example_jaycee_mdpobjectsearch_JNIBridge_killSound(JNIEnv* env, jobject obj)
{
    soundGenerator->endSound();
    soundGenerator->kill();

    delete soundGenerator;

    return true;
}

JNIEXPORT void JNICALL
Java_com_example_jaycee_mdpobjectsearch_JNIBridge_playSound(JNIEnv* env, jobject obj, jfloatArray src, jfloatArray list, jfloat gain, jfloat pitch)
{
    soundGenerator->play(env, src, list, gain, pitch);
}

JNIEXPORT bool JNICALL
Java_com_example_jaycee_mdpobjectsearch_JNIBridge_initDetector(JNIEnv* env, jobject obj, jint width, jint height, jfloatArray _focalLength, jfloatArray _principlePoint, jfloatArray _distorionMatrix)
{
    markerDetector = new MarkerDetector::MarkerDetector(width, height);

    jboolean isCopy;

    float* focalLength = env->GetFloatArrayElements(_focalLength, &isCopy);
    float* principlePoint = env->GetFloatArrayElements(_principlePoint, &isCopy);
    float* distortionMatrix = env->GetFloatArrayElements(_distorionMatrix, &isCopy);

    bool success = markerDetector->init(focalLength, principlePoint, distortionMatrix);

    env->ReleaseFloatArrayElements(_focalLength, focalLength, isCopy);
    env->ReleaseFloatArrayElements(_principlePoint, principlePoint, isCopy);
    env->ReleaseFloatArrayElements(_distorionMatrix, distortionMatrix, isCopy);

    return success;
}

JNIEXPORT bool JNICALL
Java_com_example_jaycee_mdpobjectsearch_JNIBridge_killDetector(JNIEnv* env, jobject obj)
{
    bool kill = markerDetector->kill();
    delete markerDetector;

    return kill;
}

JNIEXPORT void JNICALL
Java_com_example_jaycee_mdpobjectsearch_JNIBridge_processImage(JNIEnv* env, jobject obj, jobject data)
{
    __android_log_print(ANDROID_LOG_INFO, MARKERLOG, "Processing image");
    jboolean isCopy;
    void* rawBytes = env->GetDirectBufferAddress(data);
/*    jbyte* rawBytes = env->GetByteArrayElements(data, &isCopy);
    jsize numBytes = env->GetArrayLength(data);*/

    if(rawBytes == NULL)
    {
        __android_log_print(ANDROID_LOG_ERROR, MARKERLOG, "Could not lock on ByteBuffer");
        return;
    }
    // jint* imageData = env->GetIntArrayElements(data, &isCopy);
    unsigned char* imageData = reinterpret_cast<unsigned char*>(rawBytes);
    markerDetector->processImage(imageData);

    // free(imageData);
    // free(rawBytes);

//    env->ReleaseByteArrayElements(data, rawBytes, JNI_ABORT);
    __android_log_print(ANDROID_LOG_INFO, MARKERLOG, "Processed image");
}

#ifdef __cplusplus
}
#endif
