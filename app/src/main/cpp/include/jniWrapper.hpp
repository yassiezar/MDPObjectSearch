#ifndef POMDPOBJECTSEARCH_JNIWRAPPER_HPP
#define POMDPOBJECTSEARCH_JNIWRAPPER_HPP

#include <jni.h>
#include <android/log.h>
#include <SoundGenerator/SoundGenerator.hpp>
#include <MarkerDetector/MarkerDetector.hpp>

#ifdef __cplusplus
extern "C" {
#endif

JNIEXPORT bool JNICALL Java_com_example_jaycee_mdpobjectsearch_JNIBridge_initSound(JNIEnv*, jobject);
JNIEXPORT bool JNICALL Java_com_example_jaycee_mdpobjectsearch_JNIBridge_killSound(JNIEnv*, jobject);
JNIEXPORT void JNICALL_Java_com_example_jaycee_mdpobjectsearch_JNIBridge_playSound(JNIEnv*, jobject, jfloatArray, jfloatArray, jfloat, jfloat);

JNIEXPORT bool JNICALL Java_com_example_jaycee_mdpobjectsearch_JNIBridge_initDetector(JNIEnv*, jobject, jint width, jint height);
JNIEXPORT bool JNICALL Java_com_example_jaycee_mdpobjectsearch_JNIBridge_killDetector(JNIEnv*, jobject);
JNIEXPORT void JNICALL_Java_com_example_jaycee_mdpobjectsearch_JNIBridge_processImage(JNIEnv*, jobject, jintArray);

#ifdef __cplusplus
}
#endif

SoundGenerator::SoundGenerator *soundGenerator;
MarkerDetector::MarkerDetector *markerDetector;

#endif
