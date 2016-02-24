#include "LibteeWrapper.h"
#include "tee_client_api.h"

#include <pthread.h>
#include <stdbool.h>
#include <android/log.h>

#define MAX_NUM_CONTEXT 3
#define MAX_NUM_SESSION 9

/*
 * Global Var. All contexts and sessions should be kept record in here.
 * SharedMemory should kept record in the 'OTGuard.java' and it can be passed in and passed out.
 * */
TEEC_Context g_contextRecord[MAX_NUM_CONTEXT] = {{0}};
bool g_contextRecordOccupied[MAX_NUM_CONTEXT] = {false};
int g_currentNumOfContext = 0;
pthread_mutex_t context_lock;

TEEC_Session g_sessionRecord[MAX_NUM_SESSION] = {{0}};
bool g_sessionRecordOccupied[MAX_NUM_SESSION] = {false};
int g_currentNumOfSession = 0;
pthread_mutex_t session_lock;

JNIEXPORT jint JNICALL Java_fi_aalto_ssg_opentee_openteeandroid_LibteeWrapper_teecInitializeContext
        (JNIEnv* env, jclass jc, jstring teeName, jobject otContext){

    testFunc();

    /**
     * check current allocated context;
     * */
    if ( g_currentNumOfContext >= MAX_NUM_CONTEXT ) return TEEC_ERROR_BUSY;

    /**
     * Initialize Context.
     */
    // preparation to initialize the context
    TEEC_Context tmpContext = {0};
    TEEC_Result tmpResult;

    if ( teeName ==  NULL){
        // initialize context
        tmpResult = TEEC_InitializeContext(NULL, &tmpContext);
    }
    else{
        // get string in char* style
        const char* teeNameInC = (*env)->GetStringUTFChars(env, teeName, 0);

        // initialize context
        tmpResult = TEEC_InitializeContext(teeNameInC, &tmpContext);

        // release the string
        (*env)->ReleaseStringUTFChars(env, teeName, teeNameInC);
    }

    // store the tmpContext into g_contextRecord;
    pthread_mutex_lock(&context_lock);
    int i = 0;
    for (; i < MAX_NUM_CONTEXT; i++ ){
        if ( g_contextRecordOccupied[i] == false ){
            // find an empty place. Store it and update the occupied indication table.
            g_contextRecordOccupied[i] = true;

            g_contextRecord[i] = tmpContext;

            // update g_currentNumOfContext.
            g_currentNumOfContext++;

            break;
        }
    }
    pthread_mutex_unlock(&context_lock);

    if ( tmpResult == TEEC_SUCCESS ){
        /**
        * return the index back to caller only initializeContext succeeds.
        */
        // get the class.
        jclass otContextClass = (*env)->GetObjectClass(env, otContext);

        // get the filed ID
        jfieldID indexFiledID = (*env)->GetFieldID(env, otContextClass, "mIndex", "I");

        // get the field value
        jint indexInt = (*env)->GetIntField(env, otContext, indexFiledID);

        // store back
        indexInt = i;
        (*env)->SetIntField(env, otContext, indexFiledID, indexInt);
    }




    return tmpResult;
}

void testFunc(){
    __android_log_print(ANDROID_LOG_ERROR,
                        "Test function",
                        "Test message");

    char* tmpEnv = getenv("OPENTEE_SOCKET_FILE_PATH");

    if ( NULL == tmpEnv){
        __android_log_print(ANDROID_LOG_ERROR,
                            "Test function",
                            "OPENTEE_SOCKET_FILE_PATH not set. Default one is being used and try to overwrite.");

        int return_code = setenv("OPENTEE_SOCKET_FILE_PATH",
                                 "/data/data/fi.aalto.ssg.opentee.openteeandroid/opentee/open_tee_socket",
                                 1);

        if ( return_code == 0 ){
            __android_log_print(ANDROID_LOG_ERROR,
                                "Test function",
                                "Set socket env val succeed.");
        } else{
            __android_log_print(ANDROID_LOG_ERROR,
                                "Test function",
                                "Set socket env val failed");
        }
    }
    else{
        __android_log_print(ANDROID_LOG_ERROR,
                            "Test function",
                            "%s",
                            tmpEnv);
    }
}