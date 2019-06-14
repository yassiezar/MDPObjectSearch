#ifndef POMDPOBJECTSEARCH_JNIWRAPPER_HPP
#define POMDPOBJECTSEARCH_JNIWRAPPER_HPP

#include <jni.h>
#include <android/bitmap.h>
#include <android/log.h>
#include <SoundGenerator/SoundGenerator.hpp>
#include <MarkerDetector/MarkerDetector.hpp>

#ifdef __cplusplus
extern "C" {
#endif

JNIEXPORT bool JNICALL Java_com_example_jaycee_mdpobjectsearch_JNIBridge_initSound(JNIEnv*, jobject);
JNIEXPORT bool JNICALL Java_com_example_jaycee_mdpobjectsearch_JNIBridge_killSound(JNIEnv*, jobject);
JNIEXPORT void JNICALL_Java_com_example_jaycee_mdpobjectsearch_JNIBridge_playSound(JNIEnv*, jobject, jfloatArray, jfloatArray, jfloat, jfloat);

JNIEXPORT bool JNICALL Java_com_example_jaycee_mdpobjectsearch_JNIBridge_initDetector(JNIEnv* env, jobject obj, jint width, jint height, jfloatArray _focalLength, jfloatArray _principlePoint, jfloatArray _distorionMatrix);
JNIEXPORT bool JNICALL Java_com_example_jaycee_mdpobjectsearch_JNIBridge_killDetector(JNIEnv*, jobject);
JNIEXPORT void JNICALL Java_com_example_jaycee_mdpobjectsearch_JNIBridge_processImage(JNIEnv*, jobject, jobject);
JNIEXPORT bool JNICALL Java_com_example_jaycee_mdpobjectsearch_JNIBridge_getBitmap(JNIEnv* env, jobject, jobject, jobject, int, int, int);

#ifdef __cplusplus
}
#endif

SoundGenerator::SoundGenerator *soundGenerator;
MarkerDetector::MarkerDetector *markerDetector;

#endif
