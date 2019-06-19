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
Java_com_example_jaycee_mdpobjectsearch_JNIBridge_initDetector(JNIEnv* env, jobject obj, jint width, jint height, jfloatArray _focalLength, jfloatArray _principlePoint, jfloatArray _distortionMatrix)
{
    markerDetector = new MarkerDetector::MarkerDetector(width, height);

    jboolean isCopy;

    float* focalLength = env->GetFloatArrayElements(_focalLength, &isCopy);
    float* principlePoint = env->GetFloatArrayElements(_principlePoint, &isCopy);
    float* distortionMatrix = env->GetFloatArrayElements(_distortionMatrix, &isCopy);

    bool success = markerDetector->init(focalLength, principlePoint, distortionMatrix);

    env->ReleaseFloatArrayElements(_focalLength, focalLength, isCopy);
    env->ReleaseFloatArrayElements(_principlePoint, principlePoint, isCopy);
    env->ReleaseFloatArrayElements(_distortionMatrix, distortionMatrix, isCopy);

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
Java_com_example_jaycee_mdpobjectsearch_JNIBridge_processImage(JNIEnv* env, jobject obj, jobject bitmap, jobject data)
{
    void* rawBytes = env->GetDirectBufferAddress(data);

    if(rawBytes == NULL)
    {
        __android_log_print(ANDROID_LOG_ERROR, MARKERLOG, "Could not lock on ByteBuffer");
        return;
    }
    unsigned char* imageData = reinterpret_cast<unsigned char*>(rawBytes);
    markerDetector->processImage(imageData);

    AndroidBitmapInfo info = {0};
    int r = AndroidBitmap_getInfo(env, bitmap, &info);
    if (r != 0)
    {
        // "AndroidBitmap_getInfo() failed ! error=%d", r
        return;
    }
    int width = info.width;
    int height = info.height;
    if (info.format != ANDROID_BITMAP_FORMAT_RGBA_8888 && info.format != ANDROID_BITMAP_FORMAT_A_8)
    {
        // "Bitmap format is not RGBA_8888 or A_8"
        return;
    }
    int bytesPerPixel = info.format == ANDROID_BITMAP_FORMAT_RGBA_8888 ? 4 : 1;
    void* pixels = NULL;
    r = AndroidBitmap_lockPixels(env, bitmap, &pixels);
    if (r != 0)
    {
        // "AndroidBitmap_lockPixels() failed ! error=%d", r
        return;
    }
    if (markerDetector->getImageWidth() == width && markerDetector->getImageHeight() == height && bytesPerPixel == 4)
    {
        memcpy(pixels, imageData, width * height * bytesPerPixel);
    }
    else
    {
        __android_log_print(ANDROID_LOG_ERROR, MARKERLOG, "only grayscale -> RGBA is supported bytesPerPixel=%d", bytesPerPixel);
    }
    AndroidBitmap_unlockPixels(env, bitmap);
}

#ifdef __cplusplus
}
#endif
