#include "LibteeWrapper.h"
#include "tee_shared_data_types.h"
#include "gpdatatypes/GPDataTypes.pb.h"

#include <pthread.h>
#include <stdbool.h>
#include <android/log.h>

#include <google/protobuf/message.h>
#include <string.h>
#include <vector>

#define MAX_NUM_SESSION 9

using namespace std;
using namespace google::protobuf;
using namespace fi::aalto::ssg::opentee::imps::pbdatatypes;

#ifdef __cplusplus
extern "C" {
#endif
#include "tee_client_api.h"

class TEEC_SharedMemoryWithId{
private:
    TEEC_SharedMemory mSharedMemory;
    int mSmId;
public:
    TEEC_SharedMemoryWithId(TEEC_SharedMemory sm, int id){
        mSharedMemory = sm;
        mSmId = id;
    }

    int getId(){
        return mSmId;
    }

    TEEC_SharedMemory getSharedMemory(){
        return mSharedMemory;
    }

};
TEEC_SharedMemoryWithId NULLSharedMemoryWithId({0}, 0);

/*
 * Global Var. All contexts and sessions should be kept record in here.
 * SharedMemory should kept record in the 'OTGuard.java' and it can be passed in and passed out.
 * */
static TEEC_Context g_contextRecord = {0};

static vector<TEEC_SharedMemoryWithId> sharedMemoryWithIdList;

static TEEC_Session g_sessionRecord[MAX_NUM_SESSION] = {{0}};
static bool g_sessionRecordOccupied[MAX_NUM_SESSION] = {false};
static int g_currentNumOfSession = 0;
static pthread_mutex_t session_lock;

int open_tee_socket_env_set = 0;

TEEC_SharedMemoryWithId findSharedMemoryById(int smId){
    for ( auto& sm : sharedMemoryWithIdList ){
        if ( sm.getId() == smId ){
            __android_log_print(ANDROID_LOG_INFO,
                                "[JNI] findSharedMemoryById",
                                "shared memory with id:%d found.", smId);

            return sm;
        }
    }

    __android_log_print(ANDROID_LOG_INFO,
                        "[JNI] findSharedMemoryById",
                        "shared memory with id:%d not found.", smId);
    return NULLSharedMemoryWithId;
}

bool compareSharedMemoryWithId(TEEC_SharedMemoryWithId sm1, TEEC_SharedMemoryWithId sm2){
    if ( sm1.getId() == sm2.getId()) return true;
    return false;
}

void preparationFunc(JNIEnv *env, jstring otSocketFilePathInJava) {
    /**
     * set up OPENTEE_SOCKET_FILE_PATH env var.
     */
    char *tmpEnv = getenv("OPENTEE_SOCKET_FILE_PATH");

    if (NULL == tmpEnv) {
        __android_log_print(ANDROID_LOG_ERROR,
                            "preparationFunc",
                            "OPENTEE_SOCKET_FILE_PATH not set. Try to overwrite.");

        const char *otSocketFilePath = env->GetStringUTFChars(otSocketFilePathInJava, 0);

        int return_code = setenv("OPENTEE_SOCKET_FILE_PATH",
                                 otSocketFilePath,
                                 1);
        env->ReleaseStringUTFChars(otSocketFilePathInJava, otSocketFilePath);

        if (return_code == 0) {
            __android_log_print(ANDROID_LOG_INFO,
                                "preparationFunc",
                                "Set socket env val succeed.");
            open_tee_socket_env_set = 1;
        } else {
            __android_log_print(ANDROID_LOG_ERROR,
                                "preparationFunc",
                                "Set socket env val failed");
        }
    }
    else {
        __android_log_print(ANDROID_LOG_INFO,
                            "preparationFunc",
                            "%s is already set.",
                            tmpEnv);
    }
}
/**
 * Initialize Context.
 */
JNIEXPORT jint JNICALL Java_fi_aalto_ssg_opentee_openteeandroid_NativeLibtee_teecInitializeContext
        (JNIEnv *env, jclass jc, jstring teeName, jstring otSocketFilePathInJava) {

    if (0 == open_tee_socket_env_set)
        preparationFunc(env, otSocketFilePathInJava);

    /**
     * Initialize Context.
     */
    // preparation to initialize the context
    TEEC_Result tmpResult;

    if (teeName == NULL) {
        // initialize context
        tmpResult = TEEC_InitializeContext(NULL, &g_contextRecord);
    }
    else {
        // get string in char* style
        const char *teeNameInC = env->GetStringUTFChars(teeName, 0);

        // initialize context
        tmpResult = TEEC_InitializeContext(teeNameInC, &g_contextRecord);

        // release the string
        env->ReleaseStringUTFChars(teeName, teeNameInC);
    }

    return tmpResult;
}

/*
 * Finalize Context.
 */
JNIEXPORT void JNICALL Java_fi_aalto_ssg_opentee_openteeandroid_NativeLibtee_teecFinalizeContext
        (JNIEnv *env, jclass jc) {
    __android_log_print(ANDROID_LOG_INFO,
                        "JNI",
                        "Shutdown libprotobuf lib and Finialize Context");

    // Optional:  Delete all global objects allocated by libprotobuf.
    google::protobuf::ShutdownProtobufLibrary();

    //clean resources
    sharedMemoryWithIdList.clear();

    TEEC_FinalizeContext(&g_contextRecord);
    g_contextRecord = {0};
}

/*
 * Class:     fi_aalto_ssg_opentee_openteeandroid_NativeLibtee
 * Method:    teecRegisterSharedMemory
 * Signature: (Lfi/aalto/ssg/opentee/imps/OTSharedMemory;)I
 */
JNIEXPORT jint JNICALL Java_fi_aalto_ssg_opentee_openteeandroid_NativeLibtee_teecRegisterSharedMemory
        (JNIEnv *env, jclass jc, jbyteArray jOTSharedMemory, jint jSmId) {
    int l = env->GetArrayLength(jOTSharedMemory);
    uint8_t otSharedMemory[l];
    env->GetByteArrayRegion(jOTSharedMemory, 0, l, (jbyte* )otSharedMemory);

    // Verify that the version of the library that we linked against is
    // compatible with the version of the headers we compiled against.
    GOOGLE_PROTOBUF_VERIFY_VERSION;

    // transfer java type shared memory into c type.
    Message* message = new TeecSharedMemory;
    string data(otSharedMemory, otSharedMemory + l);
    message->ParseFromString(data);
    const Descriptor* descriptor = message->GetDescriptor();

    const FieldDescriptor* flag_field = descriptor->FindFieldByName("mFlag");
    const FieldDescriptor* buffer_field = descriptor->FindFieldByName("mBuffer");

    const Reflection* relfection = message->GetReflection();
    uint32_t  mFlag = relfection->GetInt32(*message, flag_field);
    string mBuffer = relfection->GetString(*message, buffer_field);

    TEEC_SharedMemory cOTSharedMemory = {.buffer = (void *)mBuffer.c_str(),
                                        .size = strlen(mBuffer.c_str()),
                                        .flags = mFlag,
                                        .imp = NULL
                                        };

    TEEC_Result return_code = TEEC_RegisterSharedMemory(&g_contextRecord, &cOTSharedMemory);

    __android_log_print(ANDROID_LOG_INFO,
                        "JNI",
                        "flag: %x, buffer:%s, return_code:%x",
                        mFlag,
                        mBuffer.c_str(),
                        return_code
    );

    // if register shared memory succeed, add it to the global shared memory array.
    if ( return_code == TEEC_SUCCESS ){
        TEEC_SharedMemoryWithId sm(cOTSharedMemory, jSmId);
        sharedMemoryWithIdList.push_back(sm);
    }

    delete message;

    return return_code;
}

JNIEXPORT void JNICALL Java_fi_aalto_ssg_opentee_openteeandroid_NativeLibtee_teecReleaseSharedMemory
        (JNIEnv* env, jclass jc, jint jsmId){
    TEEC_SharedMemoryWithId smWithId = findSharedMemoryById(jsmId);
    if ( compareSharedMemoryWithId( NULLSharedMemoryWithId, smWithId) ) return;

    TEEC_SharedMemory sm = smWithId.getSharedMemory();
    TEEC_ReleaseSharedMemory(&sm);

    __android_log_print(ANDROID_LOG_INFO,
                        "JNI",
                        "%d is released.", jsmId);
}

#ifdef __cplusplus
}
#endif

