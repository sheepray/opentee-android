/* DO NOT EDIT THIS FILE - it is machine generated */
#include <jni.h>
/* Header for class fi_aalto_ssg_opentee_openteeandroid_NativeLibtee */

#ifndef _Included_fi_aalto_ssg_opentee_openteeandroid_NativeLibtee
#define _Included_fi_aalto_ssg_opentee_openteeandroid_NativeLibtee
#ifdef __cplusplus
extern "C" {
#endif
/*
 * Class:     fi_aalto_ssg_opentee_openteeandroid_NativeLibtee
 * Method:    teecInitializeContext
 * Signature: (Ljava/lang/String;Ljava/lang/String;)I
 */
JNIEXPORT jint JNICALL Java_fi_aalto_ssg_opentee_openteeandroid_NativeLibtee_teecInitializeContext
  (JNIEnv *, jclass, jstring, jstring);

/*
 * Class:     fi_aalto_ssg_opentee_openteeandroid_NativeLibtee
 * Method:    teecFinalizeContext
 * Signature: ()V
 */
JNIEXPORT void JNICALL Java_fi_aalto_ssg_opentee_openteeandroid_NativeLibtee_teecFinalizeContext
  (JNIEnv *, jclass);

/*
 * Class:     fi_aalto_ssg_opentee_openteeandroid_NativeLibtee
 * Method:    teecRegisterSharedMemory
 * Signature: ([BI)I
 */
JNIEXPORT jint JNICALL Java_fi_aalto_ssg_opentee_openteeandroid_NativeLibtee_teecRegisterSharedMemory
  (JNIEnv *, jclass, jbyteArray, jint);

/*
 * Class:     fi_aalto_ssg_opentee_openteeandroid_NativeLibtee
 * Method:    teecReleaseSharedMemory
 * Signature: (I)V
 */
JNIEXPORT void JNICALL Java_fi_aalto_ssg_opentee_openteeandroid_NativeLibtee_teecReleaseSharedMemory
  (JNIEnv *, jclass, jint);

#ifdef __cplusplus
}
#endif
#endif
