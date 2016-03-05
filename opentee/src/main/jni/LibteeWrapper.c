#include "LibteeWrapper.h"
#include "tee_client_api.h"
//#include "GPDataTypes.pb.h"

#include <pthread.h>
#include <stdbool.h>
#include <android/log.h>

#define MAX_NUM_SESSION 9

/*
 * Global Var. All contexts and sessions should be kept record in here.
 * SharedMemory should kept record in the 'OTGuard.java' and it can be passed in and passed out.
 * */
TEEC_Context g_contextRecord = {0};

TEEC_Session g_sessionRecord[MAX_NUM_SESSION] = {{0}};
bool g_sessionRecordOccupied[MAX_NUM_SESSION] = {false};
int g_currentNumOfSession = 0;
pthread_mutex_t session_lock;

int open_tee_socket_env_set = 0;


void preparationFunc(JNIEnv* env, jstring otSocketFilePathInJava){
    /**
     * set up OPENTEE_SOCKET_FILE_PATH env var.
     */
    char* tmpEnv = getenv("OPENTEE_SOCKET_FILE_PATH");

    if ( NULL == tmpEnv){
        __android_log_print(ANDROID_LOG_ERROR,
                            "preparationFunc",
                            "OPENTEE_SOCKET_FILE_PATH not set. Try to overwrite.");

        const char* otSocketFilePath = (*env)->GetStringUTFChars(env, otSocketFilePathInJava, 0);

        int return_code = setenv("OPENTEE_SOCKET_FILE_PATH",
                                 otSocketFilePath,
                                 1);
        (*env)->ReleaseStringUTFChars(env, otSocketFilePathInJava, otSocketFilePath);

        if ( return_code == 0 ){
            __android_log_print(ANDROID_LOG_INFO,
                                "preparationFunc",
                                "Set socket env val succeed.");
            open_tee_socket_env_set = 1;
        } else{
            __android_log_print(ANDROID_LOG_ERROR,
                                "preparationFunc",
                                "Set socket env val failed");
        }
    }
    else{
        __android_log_print(ANDROID_LOG_INFO,
                            "preparationFunc",
                            "%s is already set.",
                            tmpEnv);
    }
}

/**
 * Initialize Context.
 */
JNIEXPORT jint JNICALL Java_fi_aalto_ssg_opentee_openteeandroid_LibteeWrapper_teecInitializeContext
        (JNIEnv* env, jclass jc, jstring teeName, jstring otSocketFilePathInJava){

    if ( 0 == open_tee_socket_env_set )
        preparationFunc(env, otSocketFilePathInJava);

    /**
     * Initialize Context.
     */
    // preparation to initialize the context
    TEEC_Result tmpResult;

    if ( teeName ==  NULL){
        // initialize context
        tmpResult = TEEC_InitializeContext(NULL, &g_contextRecord);
    }
    else{
        // get string in char* style
        const char* teeNameInC = (*env)->GetStringUTFChars(env, teeName, 0);

        // initialize context
        tmpResult = TEEC_InitializeContext(teeNameInC, &g_contextRecord);

        // release the string
        (*env)->ReleaseStringUTFChars(env, teeName, teeNameInC);
    }

    return tmpResult;
}

/*
 * Finalize Context.
 */
JNIEXPORT void JNICALL Java_fi_aalto_ssg_opentee_openteeandroid_LibteeWrapper_teecFinalizeContext
        (JNIEnv *env, jclass jc){
    __android_log_print(ANDROID_LOG_INFO,
                        "JNI",
                        "Finialize Context");
    TEEC_FinalizeContext(&g_contextRecord);
}

/**
 * Register Shared Memory.
 */
JNIEXPORT jint JNICALL Java_fi_aalto_ssg_opentee_openteeandroid_LibteeWrapper_teecRegisterSharedMemory
        (JNIEnv* env, jclass jc, jbyteArray jOTSharedMemory){
    int l = (*env)->GetArrayLength(env, jOTSharedMemory);
    unsigned char otSharedMemory[l];
    //otSharedMemory = (unsigned char *)malloc( l * sizeof(unsigned char));
    (*env)->GetByteArrayRegion(env, jOTSharedMemory, 0, l, otSharedMemory);

    __android_log_print(ANDROID_LOG_INFO,
                        "JNI",
                        "Shared Memory %s", otSharedMemory);

    return 0;
}

