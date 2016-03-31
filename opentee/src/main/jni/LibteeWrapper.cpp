#include "LibteeWrapper.h"
#include "tee_shared_data_types.h"
#include "LibteeeWrapperConstants.h"
#include "gpdatatypes/GPDataTypes.pb.h"

#include <pthread.h>
#include <stdbool.h>
#include <android/log.h>

#include <google/protobuf/message.h>
#include <string.h>
#include <vector>

#ifdef ANDROID
#  define LOG_TAG "[JNI]"
#  define LOGE(...) __android_log_print(ANDROID_LOG_ERROR, LOG_TAG, __VA_ARGS__)
#  define LOGD(...) __android_log_print(ANDROID_LOG_DEBUG, LOG_TAG, __VA_ARGS__)
#  define LOGI(...) __android_log_print(ANDROID_LOG_INFO, LOG_TAG, __VA_ARGS__)
#endif

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

    TEEC_SharedMemory* getSharedMemory(){
        return &mSharedMemory;
    }

};
TEEC_SharedMemoryWithId NULLSharedMemoryWithId({0}, 0);

/*
 * Global Var. All contexts and sessions should be kept record in here.
 * SharedMemory should kept record in the 'OTGuard.java' and it can be passed in and passed out.
 * */
static TEEC_Context g_contextRecord = {0};

///TODO: replace vector with unordered_map
static vector<TEEC_SharedMemoryWithId> sharedMemoryWithIdList;

static TEEC_Session g_sessionRecord[MAX_NUM_SESSION] = {{0}};
static bool g_sessionRecordOccupied[MAX_NUM_SESSION] = {false};
static int g_currentNumOfSession = 0;
static pthread_mutex_t session_lock;

int open_tee_socket_env_set = 0;

//test code.
void printSharedMemoryList(){
    if ( sharedMemoryWithIdList.size() == 0 ){
        LOGI("%s: shared memory with id list is empty", __FUNCTION__);
        return;
    }

    for(auto& smWithId: sharedMemoryWithIdList){
        TEEC_SharedMemory* sm = smWithId.getSharedMemory();

        LOGI("%s: shared memory id:%d, size:%d, flag:%d buffer:%s",
             __FUNCTION__,
             smWithId.getId(),
             sm->size,
             sm->flags,
             sm->buffer);
    }
}

TEEC_SharedMemoryWithId* findSharedMemoryById(int smId){
    for (auto&sm : sharedMemoryWithIdList ){
        if ( sm.getId() == smId ){
            LOGI("%s: shared memory with id:%d found.", __FUNCTION__, smId);
            return &sm;
        }
    }

    LOGI("%s: shared memory with id:%d not found.", __FUNCTION__, smId);
    return &NULLSharedMemoryWithId;
}

bool removeSharedMemoryById(int smId){
    for ( vector<TEEC_SharedMemoryWithId>::iterator s = sharedMemoryWithIdList.begin();
          s != sharedMemoryWithIdList.end(); s++ ){
        if ( s->getId() == smId ){
            // remove allocated buffer for shared memory
            TEEC_SharedMemory* sm = s->getSharedMemory();
            free(sm->buffer);

            sharedMemoryWithIdList.erase(s);
            return true;
        }
    }

    return false;
}

bool compareSharedMemoryWithId(TEEC_SharedMemoryWithId* sm1, TEEC_SharedMemoryWithId* sm2){
    if ( sm1->getId() == sm2->getId()) return true;
    return false;
}

void preparationFunc(JNIEnv *env, jstring otSocketFilePathInJava) {
    /**
     * set up OPENTEE_SOCKET_FILE_PATH env var.
     */
    char *tmpEnv = getenv("OPENTEE_SOCKET_FILE_PATH");

    if (NULL == tmpEnv) {
        LOGE("%s: OPENTEE_SOCKET_FILE_PATH not set. Try to overwrite.", __FUNCTION__);

        const char *otSocketFilePath = env->GetStringUTFChars(otSocketFilePathInJava, 0);

        int return_code = setenv("OPENTEE_SOCKET_FILE_PATH",
                                 otSocketFilePath,
                                 1);
        env->ReleaseStringUTFChars(otSocketFilePathInJava, otSocketFilePath);

        if (return_code == 0) {
            LOGI("%s: Set socket env val succeed.", __FUNCTION__);
            open_tee_socket_env_set = 1;
        } else {
            LOGE("%s: Set socket env val failed", __FUNCTION__);
        }
    }
    else {
        LOGI("%s: %s is already set.", __FUNCTION__, tmpEnv);
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
    LOGI("%s: Shutdown libprotobuf lib and Finialize Context", __FUNCTION__);

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
    //const FieldDescriptor* size_field = descriptor->FindFieldByName("size");


    const Reflection* relfection = message->GetReflection();
    uint32_t mFlag = relfection->GetInt32(*message, flag_field);
    string mBuffer = relfection->GetString(*message, buffer_field);
    //uint32_t size = relfection->GetInt32(*message, size_field);

    TEEC_SharedMemory cOTSharedMemory = {.buffer = (void *)mBuffer.c_str(),
                                        .size = strlen(mBuffer.c_str()),
                                        .flags = mFlag,
                                        .imp = NULL
                                        };
    cOTSharedMemory.buffer = (void* )malloc(cOTSharedMemory.size * sizeof(uint8_t));
    strcpy((char*)(cOTSharedMemory.buffer), mBuffer.c_str());

    TEEC_Result return_code = TEEC_RegisterSharedMemory(&g_contextRecord, &cOTSharedMemory);

    LOGI("%s: flag: %x, buffer:%s, return_code:%x",
         __FUNCTION__,
         mFlag,
         mBuffer.c_str(),
         return_code);

    // if register shared memory succeed, add it to the global shared memory array.
    if ( return_code == TEEC_SUCCESS ){
        TEEC_SharedMemoryWithId sm(cOTSharedMemory, jSmId);
        sharedMemoryWithIdList.push_back(sm);
    }

    //test code.
    printSharedMemoryList();

    free(cOTSharedMemory.buffer);
    delete message;

    return return_code;
}

JNIEXPORT void JNICALL Java_fi_aalto_ssg_opentee_openteeandroid_NativeLibtee_teecReleaseSharedMemory
        (JNIEnv* env, jclass jc, jint jsmId){
    //test code.
    printSharedMemoryList();

    TEEC_SharedMemoryWithId* smWithId = findSharedMemoryById(jsmId);
    if ( compareSharedMemoryWithId( &NULLSharedMemoryWithId, smWithId) ) return;

    TEEC_SharedMemory* sm = smWithId->getSharedMemory();

    LOGI("%s: %s is to be released.", __FUNCTION__, sm->buffer);

    TEEC_ReleaseSharedMemory(sm);

    LOGI("%s: %d is released.", __FUNCTION__, jsmId);

    // remove shared memory from shared memory list.
    removeSharedMemoryById(jsmId);

    //test code.
    printSharedMemoryList();
}

//test code
void flagFunc(){
    LOGI("JNI", "HELLO");
    LOGE("%s: I am here.", __FUNCTION__);
}

//test code
void printSharedMemory(TEEC_SharedMemory* sm){
    LOGE("%s: buffer:%s, flag:%x, size:%d", __FUNCTION__, sm->buffer, sm->flags, sm->size);
}

//test code
void printTeecOperation(TEEC_Operation* op){
    LOGE("%s: started:%d, paraType:%x", __FUNCTION__, op->started, op->paramTypes);

    for(int i = 0; i < 2; i++){
        TEEC_SharedMemory* sm = op->params[i].memref.parent;
        printSharedMemory(sm);
    }
}


JNIEXPORT jint JNICALL Java_fi_aalto_ssg_opentee_openteeandroid_NativeLibtee_teecOpenSession
        (JNIEnv* env, jclass jc, jint sid, jobject uuid, jint connMethod, jint connData, jbyteArray opInBytes, jobject returnOrigin){
    /*
     * Data structure transfer
     * */
    /* UUID uuid to TEEC_UUID */
    jclass jcUuid = env->GetObjectClass(uuid);
    jmethodID jmGetLeastSignificantBits = env->GetMethodID(jcUuid,
                                                           "getLeastSignificantBits", // method name.
                                                           "()J" // input void, return long.
                                                            );
    jmethodID jmGetMostSignificantBits = env->GetMethodID(jcUuid,
                                                           "getMostSignificantBits", // method name.
                                                           "()J" // input void, return long.
    );

    if( jmGetLeastSignificantBits == 0 || jmGetMostSignificantBits == 0){
        // set return origin TEEC_ORIGIN_API.
        jclass jcRetOrigin = env->GetObjectClass(returnOrigin);
        jfieldID jfROC = env->GetFieldID(jcRetOrigin, "mReturnOrigin", "I");
        env->SetIntField(returnOrigin, jfROC, TEEC_ORIGIN_API);

        return TEEC_ERROR_BAD_PARAMETERS;
    }

    long long lsBits, msBits;
    lsBits = msBits = 0;
    lsBits = env->CallLongMethod(uuid, jmGetLeastSignificantBits);
    msBits = env->CallLongMethod(uuid, jmGetMostSignificantBits);

    LOGI("%s: uuid:%llx %llx.", __FUNCTION__, msBits, lsBits);

    TEEC_UUID teec_uuid = { .timeLow = (uint32_t)(msBits >> 32),
                            .timeMid = (uint16_t)(msBits >> 16),
                            .timeHiAndVersion = (uint16_t)msBits};
    //memcpy(teec_uuid.clockSeqAndNode, &lsBits, sizeof(lsBits));
    for(int i = 7; i >= 0; i--){
        teec_uuid.clockSeqAndNode[i] = (uint8_t)lsBits;
        lsBits = lsBits >> 8;

        LOGI("%s: %x", __FUNCTION__, teec_uuid.clockSeqAndNode[i]);
    }

    LOGI("%s: timeLow:%x, timeMid:%x, timeHighAndVersion:%x",
                        __FUNCTION__,
                        teec_uuid.timeLow,
                        teec_uuid.timeMid,
                        teec_uuid.timeHiAndVersion);

    // teec_uuid construction done.

    /* Parse opInBytes */
    // get length.
    int l = env->GetArrayLength(opInBytes);
    // buffer to receive the byte array.
    uint8_t* opInBytesBuffer = (uint8_t *)malloc(l*sizeof(uint8_t));
    // store the byte array into buffer.
    env->GetByteArrayRegion(opInBytes, 0, l, (jbyte*)opInBytesBuffer);
    // create a string to store this buffer.
    string opsInString(opInBytesBuffer, opInBytesBuffer + l);


    /* real step to create the operation from the string created above.
    // the hard way to parse TeecOperation.
    Message* opCreatorMsg = new TeecOperation;
    opCreatorMsg->ParseFromString(opsInString);

    const Descriptor* descriptor = opCreatorMsg->GetDescriptor();

    const FieldDescriptor* started_field = descriptor->FindFieldByName("mStarted");
    const FieldDescriptor* params_field = descriptor->FindFieldByName("mParams");

    if(started_field == NULL || params_field == NULL){
        // set return origin TEEC_ORIGIN_API.
        jclass jcRetOrigin = env->GetObjectClass(returnOrigin);
        jfieldID jfROC = env->GetFieldID(jcRetOrigin, "mReturnOrigin", "I");
        env->SetIntField(returnOrigin, jfROC, TEEC_ORIGIN_API);

        return TEEC_ERROR_BAD_PARAMETERS;
    }

    const Reflection* reflection = opCreatorMsg->GetReflection();
    uint32_t started = reflection->GetInt32(*opCreatorMsg, started_field);
    //uint32_t numOfParams = reflection->FieldSize(*opCreatorMsg, params_field);
    //TeecParameter = reflection->GetString(*opCreatorMsg, params_field);

    // test code
    __android_log_print(ANDROID_LOG_INFO,
                        "JNI",
                        "started %d. num of params:", started);
*/
    // the easy way to parse TeecOperation.
    TEEC_Operation teec_operation;// = {0};

    TeecOperation op;
    op.ParseFromString(opsInString);

    LOGI( "%s: started %d. num of params:%d %d", __FUNCTION__,
          op.mstarted(),
          op.mparams_size(),
          sizeof(TEEC_NONE) / sizeof(uint8_t));

    // set started field.
    teec_operation.started = op.mstarted();

    // get paramTypes and set the params array.
    uint32_t paramTypesArray[] = {TEEC_NONE, TEEC_NONE, TEEC_NONE, TEEC_NONE};

    for(int i = 0; i < op.mparams_size(); i++){
        const TeecParameter param = op.mparams(i);
        //TEEC_Parameter teec_parameter = {0};
        if( param.has_teecsharedmemoryreference() ){
            // param is TEEC_RegisteredMemoryReference.
            const TeecSharedMemoryReference rmr = param.teecsharedmemoryreference();
            //TEEC_RegisteredMemoryReference teec_rmr = {0};

            // get TEEC_SharedMemory.
            int smId = rmr.parentid();
            TEEC_SharedMemoryWithId* smWithId = findSharedMemoryById(smId);
            TEEC_SharedMemory* sm = smWithId->getSharedMemory();
            teec_operation.params[i].memref.parent = sm;

            // get size.
            teec_operation.params[i].memref.size = sm->size;

            // get offset.
            teec_operation.params[i].memref.offset = rmr.moffset();

            // set type.
            if(rmr.moffset() > 0){
                // using part of the memory.
                switch(rmr.mflag()){
                    case JavaConstants::MEMREF_INPUT:
                        paramTypesArray[i] = TEEC_MEMREF_PARTIAL_INPUT;
                        break;
                    case JavaConstants::MEMREF_OUTPUT:
                        paramTypesArray[i] = TEEC_MEMREF_PARTIAL_OUTPUT;
                        break;
                    case JavaConstants::MEMREF_INOUT:
                        paramTypesArray[i] = TEEC_MEMREF_PARTIAL_INOUT;
                        break;
                    default:
                        break;
                }
            }
            else{
                // using while memory.
                paramTypesArray[i] = TEEC_MEMREF_WHOLE;
            }

            //teec_operation.params[i].memref = teec_rmr;
        }
        else if(param.has_teecvalue()){
            // param is TEEC_Value.
            const TeecValue value = param.teecvalue();
            //TEEC_Value teec_value = {0};
            teec_operation.params[i].value.a = value.a();
            teec_operation.params[i].value.b = value.b();

            //teec_operation.params[i].value = teec_value;

            // set the flag based on the flag value from java layer.
            switch ( value.mflag() ){
                case JavaConstants::VALUE_INPUT:
                    paramTypesArray[i] = TEEC_VALUE_INPUT;
                    break;
                case JavaConstants::VALUE_OUTPUT:
                    paramTypesArray[i] = TEEC_VALUE_OUTPUT;
                    break;
                case JavaConstants::VALUE_INOUT:
                    paramTypesArray[i] = TEEC_VALUE_INOUT;
                    break;
                default:
                    break;
            }

        }else{
            LOGE("%s: Incorrect param. Ignore it.", __FUNCTION__);
            continue;
        }

        // add created teec_parameter into the parameter array.
        //teec_operation.params[i] = teec_parameter;
        //memcpy(&teec_operation.params[i], &teec_parameter, sizeof(TEEC_Parameter));
    }
    // set paramTypes field.

    teec_operation.paramTypes = TEEC_PARAM_TYPES(paramTypesArray[0],
                                                 paramTypesArray[1],
                                                 paramTypesArray[2],
                                                 paramTypesArray[3]);

    TEEC_Session teec_session = {0};
    uint32_t teec_ret_ori = 0;

    printTeecOperation(&teec_operation);

    /**
     * call TEEC_OpenSession.
     */
    TEEC_Result teec_ret = TEEC_OpenSession(
            &g_contextRecord,
            &teec_session,
            &teec_uuid,
            (uint32_t)connMethod,
            &connData,
            //NULL,
            &teec_operation,
            &teec_ret_ori
    );

    LOGD("%s: return code:%.8x, return origin:%.8x",
         __FUNCTION__,
         teec_ret,
         teec_ret_ori
    );

    /**
     * store the session upon success.
     * */

    /**
     * Prepare the variables to return.
     */

    /**
     * clean allocated resources.
     */
    free(opInBytesBuffer);

    return teec_ret;
}



#ifdef __cplusplus
}
#endif

