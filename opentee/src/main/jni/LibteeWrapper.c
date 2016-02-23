#include "LibteeWrapper.h"
#include "tee_client_api.h"

#define MAX_NUM_CONTEXT 3
#define MAX_NUM_SESSION 9

/*
 * Global Var. All contexts and sessions should be kept record in here.
 * SharedMemory should kept record in the OTGuard.java and it can be passed in and passed out.
 * */
static TEEC_Context g_contextRecord[MAX_NUM_CONTEXT];
int g_currentNumOfContext = 0;

static TEEC_Session g_sessionRecord[MAX_NUM_SESSION];
int g_currentNumOfSession = 0;

JNIEXPORT jint JNICALL Java_fi_aalto_ssg_opentee_openteeandroid_LibteeWrapper_teecInitializeContext
        (JNIEnv* env, jclass jc, jstring teeName, jobject otContext){

    // get the class.
    jclass otContextClass = (*env)->GetObjectClass(env, otContext);

    // get the filed ID
    jfieldID indexFiledID = (*env)->GetFieldID(env, otContextClass, "mIndex", "I");

    // get the field value
    jint indexInt = (*env)->GetIntField(env, otContext, indexFiledID);

    //indexInt = 22;

    (*env)->SetIntField(env, otContext, indexFiledID, indexInt);

    //TODO:
    return indexInt;
}