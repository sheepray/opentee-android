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
#include <unordered_map>

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

//<session_id, teec_session>
static unordered_map<int, TEEC_Session> sessions_map;

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
    LOGE("%s: I am here.", __FUNCTION__);
}

//test code
void printSharedMemory(TEEC_SharedMemory* sm){
    if(sm == NULL) return;
    LOGI("%s: buffer:%s, flag:%x, size:%d", __FUNCTION__, sm->buffer, sm->flags, sm->size);
}

//test code
void printTeecOperation(const TEEC_Operation* op){
    LOGE("%s: started:%d, paraType:%x", __FUNCTION__, op->started, op->paramTypes);

    for(int i = 0; i < 4; i++){
        uint32_t  type = TEEC_PARAM_TYPE_GET(op->paramTypes, i);
        if( type == 0){
            LOGI("Param is none");
        }
        else if(type == TEEC_VALUE_INPUT ||
                type == TEEC_VALUE_OUTPUT ||
                type == TEEC_VALUE_INOUT){
            TEEC_Value value = op->params[i].value;
            LOGI("[Value]a:0x%x, b:0x%x", value.a, value.b);
        }
        else if(type == TEEC_MEMREF_WHOLE ||
                type == TEEC_MEMREF_PARTIAL_INPUT ||
                type == TEEC_MEMREF_PARTIAL_OUTPUT ||
                type == TEEC_MEMREF_PARTIAL_INOUT){
            TEEC_SharedMemory* sm = op->params[i].memref.parent;
            printSharedMemory(sm);
        }
    }
}

//test func
void printClockSeqAndNode(uint8_t vars[8]){
    LOGI("ClockSeqAndNode:%s", (char*)vars);
}

__inline void set_return_origin(JNIEnv* env, jobject returnOrigin, int var){
    jclass jcRetOrigin = env->GetObjectClass(returnOrigin);
    jfieldID jfROC = env->GetFieldID(jcRetOrigin, "mValue", "I");
    env->SetIntField(returnOrigin, jfROC, var);
}

__inline void set_return_code(JNIEnv* env, jobject returnCode, int var){
    set_return_origin(env, returnCode, var);
}

void transfer_opString_to_TEEC_Operation(JNIEnv* env, const string opsInString, TEEC_Operation* teec_operation){
    TeecOperation op;
    op.ParseFromString(opsInString);

    LOGI( "%s: started %d. num of params:%d %d", __FUNCTION__,
          op.mstarted(),
          op.mparams_size(),
          sizeof(TEEC_NONE) / sizeof(uint8_t));

    // set started field.
    teec_operation->started = op.mstarted();

    // get paramTypes and set the params array.
    uint32_t paramTypesArray[] = {TEEC_NONE, TEEC_NONE, TEEC_NONE, TEEC_NONE};

    for(int i = 0; i < op.mparams_size(); i++){
        const TeecParameter param = op.mparams(i);
        if( param.has_teecsharedmemoryreference() ){
            // param is TEEC_RegisteredMemoryReference.
            LOGI("%s: param is TEEC_RMR.", __FUNCTION__);
            const TeecSharedMemoryReference rmr = param.teecsharedmemoryreference();

            /*
             * get TEEC_SharedMemory and sync the content if the flag is TEEC_MEM_INPUT.
             */
            int smId = rmr.parent().mid();
            TEEC_SharedMemoryWithId* smWithId = findSharedMemoryById(smId);
            TEEC_SharedMemory* sm = smWithId->getSharedMemory();

            LOGE("%s: old buffer:%s with flag:%x", __FUNCTION__, sm->buffer, sm->flags);

            if(rmr.parent().mflag() != JavaConstants::MEMREF_OUTPUT){
                //the share memory is not only for output.

                int8_t * smBuffer = (int8_t*)rmr.parent().mbuffer().c_str();
                LOGE("%s: new buffer:%s", __FUNCTION__ , smBuffer);
                memcpy(sm->buffer, smBuffer, sizeof(smBuffer));
            }

            teec_operation->params[i].memref.parent = sm;

            // get size.
            teec_operation->params[i].memref.size = sm->size;

            // get offset.
            teec_operation->params[i].memref.offset = rmr.moffset();

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
                // using whole memory.
                paramTypesArray[i] = TEEC_MEMREF_WHOLE;
            }
        }
        else if(param.has_teecvalue()){
            // param is TEEC_Value.
            LOGI("%s: param is TEEC_VALUE.", __FUNCTION__);

            const TeecValue value = param.teecvalue();
            teec_operation->params[i].value.a = value.a();
            teec_operation->params[i].value.b = value.b();

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
    }

    // set paramTypes field.
    teec_operation->paramTypes = TEEC_PARAM_TYPES(paramTypesArray[0],
                                                 paramTypesArray[1],
                                                 paramTypesArray[2],
                                                 paramTypesArray[3]);
}

/**
 * transfer the operation in jbyteArray into TEEC_Operation.
 */
void transfer_op_to_TEEC_Operation(JNIEnv* env, const jbyteArray& opInBytes, TEEC_Operation* teec_operation){
    LOGD("[start]%s", __FUNCTION__);

    if(opInBytes == NULL){
        LOGI("[%s] op is null", __FUNCTION__);
        return;
    }
    // get length.
    int l = env->GetArrayLength(opInBytes);
    // buffer to receive the byte array.
    uint8_t* opInBytesBuffer = (uint8_t *)malloc(l*sizeof(uint8_t));
    // store the byte array into buffer.
    env->GetByteArrayRegion(opInBytes, 0, l, (jbyte*)opInBytesBuffer);
    // create a string to store this buffer.
    string opsInString(opInBytesBuffer, opInBytesBuffer + l);

    free(opInBytesBuffer);

    transfer_opString_to_TEEC_Operation(env, opsInString, teec_operation);

    LOGD("[end]%s", __FUNCTION__);
}

/**
 * update TEEC_Operation to jbyteArray.
 */
jbyteArray transfer_TEEC_Operation_to_op(JNIEnv* env, const TEEC_Operation* teec_operation, jbyteArray opInBytes){
    LOGD("\n[start]%s", __FUNCTION__);
    /**
     * parsing from old opInBytes.
     */
    // get length.
    int l = env->GetArrayLength(opInBytes);
    // buffer to receive the byte array.
    uint8_t* opInBytesBuffer = (uint8_t *)malloc(l*sizeof(uint8_t));
    // store the byte array into buffer.
    env->GetByteArrayRegion(opInBytes, 0, l, (jbyte*)opInBytesBuffer);
    // create a string to store this buffer.
    string opsInString(opInBytesBuffer, opInBytesBuffer + l);

    free(opInBytesBuffer);

    TeecOperation op;
    op.ParseFromString(opsInString);

    //set started field.
    op.set_mstarted(teec_operation->started);

    /**
     * check and validate changes.
     */
    for(int i = 0; i < 4; i++){
        uint32_t type = TEEC_PARAM_TYPE_GET(teec_operation->paramTypes, i);

        LOGI("\t[%s] type=%x", __FUNCTION__, type);

        // for TEEC_Value with output flag.
        if(type == TEEC_VALUE_OUTPUT ||
           type == TEEC_VALUE_INOUT){
            TeecValue value = op.mparams(i).teecvalue();

            LOGI("\t[Old value]a:%x b:%x", value.a(), value.b());
            LOGI("\t[New value]a:%x b:%x", teec_operation->params[i].value.a, teec_operation->params[i].value.b);

            // regardless whether value is changed or not, just sync it back.
            op.mutable_mparams(i)->mutable_teecvalue()->set_a(teec_operation->params[i].value.a);
            op.mutable_mparams(i)->mutable_teecvalue()->set_b(teec_operation->params[i].value.b);
        }

        // for TEEC_SharedMemory with output flag.
        else if(type == TEEC_MEMREF_WHOLE ||
                type == TEEC_MEMREF_PARTIAL_OUTPUT ||
                type == TEEC_MEMREF_PARTIAL_INOUT){
            TeecSharedMemory* sm = op.mutable_mparams(i)->mutable_teecsharedmemoryreference()->mutable_parent();
            const TEEC_SharedMemory* shared_memory = teec_operation->params[i].memref.parent;

            LOGE("\tsm Old Buffer:%s, shared_memory:%s", sm->mbuffer().c_str(), shared_memory->buffer);

            int len_sm = sm->mbuffer().length();
            int len_shared_memory = strlen((char*)shared_memory->buffer);

            sm->set_mbuffer((char*)shared_memory->buffer);
            sm->set_mreturnsize(teec_operation->params[i].memref.size);

            LOGE("\tNew Buffer:%s, old-len_sm:%d, len_shared_memory:%d", sm->mbuffer().c_str(), len_sm, len_shared_memory);
        }
        else{
            LOGI("\tTEEC_NONE or without OUTPUT field, will not be synced back.");
        }
    }

    string new_op;
    op.SerializeToString(&new_op);

    LOGD("\tnew_op len:%d", new_op.length());

    //test code
    for(int i = 0; i < new_op.length(); i++){
        LOGE("\t%d = %d", i, new_op.at(i));
    }


    jbyteArray new_op_in_bytes = env->NewByteArray(new_op.length());
    env->SetByteArrayRegion(new_op_in_bytes, 0, new_op.length(), (jbyte*)new_op.c_str());

    //test code: parsed correctly.
    //TEEC_Operation teec_operation2 = {0};
    //transfer_opString_to_TEEC_Operation(env, new_op, &teec_operation2);
    //LOGE("a:%x, b:%x", teec_operation2.params[1].value.a, teec_operation2.params[1].value.b);

    LOGD("[end]%s\n", __FUNCTION__);

    return new_op_in_bytes;
}

JNIEXPORT jbyteArray JNICALL Java_fi_aalto_ssg_opentee_openteeandroid_NativeLibtee_teecOpenSession
(JNIEnv* env, jclass jc, jint sid, jobject uuid, jint connMethod, jint connData, jbyteArray opInBytes, jobject returnOrigin, jobject returnCode){
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
        set_return_origin(env, returnOrigin, TEEC_ORIGIN_API);

        set_return_code(env, returnCode, TEEC_ERROR_BAD_PARAMETERS);
    }

    uint64_t lsBits, msBits;
    lsBits = msBits = 0;
    lsBits = env->CallLongMethod(uuid, jmGetLeastSignificantBits);
    msBits = env->CallLongMethod(uuid, jmGetMostSignificantBits);

    LOGI("%s: uuid:%llx %llx.", __FUNCTION__, msBits, lsBits);

    TEEC_UUID teec_uuid = { .timeLow = (uint32_t)(msBits >> 32),
                            .timeMid = (uint16_t)(msBits >> 16),
                            .timeHiAndVersion = (uint16_t)msBits};

    for(int i = 7; i >= 0; i--){
        teec_uuid.clockSeqAndNode[i] = (uint8_t)lsBits;
        lsBits = lsBits >> 8;
    }

    LOGI("%s: timeLow:%x, timeMid:%x, timeHighAndVersion:%x",
                        __FUNCTION__,
                        teec_uuid.timeLow,
                        teec_uuid.timeMid,
                        teec_uuid.timeHiAndVersion);

    printClockSeqAndNode(teec_uuid.clockSeqAndNode);

    /**
     * Parsing TEEC_Operation from op in bytes.
     */
    TEEC_Operation teec_operation = {0};
    transfer_op_to_TEEC_Operation(env, opInBytes, &teec_operation);

    printTeecOperation(&teec_operation);

    TEEC_Session teec_session = {0};
    uint32_t teec_ret_ori = 0;

    /**
     * call TEEC_OpenSession.
     */
    /* Dont call open session right now to test shared memory synchronization.
    TEEC_Result teec_ret = TEEC_OpenSession(
            &g_contextRecord,
            &teec_session,
            &teec_uuid,
            (uint32_t)connMethod,
            //TEEC_LOGIN_PUBLIC,
            &connData,
            //NULL,
            &teec_operation,
            //NULL,
            &teec_ret_ori
    );


    LOGD("%s: connMethod:%.8x, connData:%.8x, return code:%.8x, return origin:%.8x",
         __FUNCTION__,
         connMethod,
         connData,
         teec_ret,
         teec_ret_ori
    );


    //store the session upon success.
    if( teec_ret == TEEC_SUCCESS ){
        //TODO: potential issue with teec_session when out of this function. Needs to check.
        sessions_map.emplace((int)sid, teec_session);
    }
    */

    //simulate TEEC_SharedMemory and TEEC_Value have been changed.
    memcpy(teec_operation.params[0].memref.parent->buffer, "LOVE", 4);

    LOGE("a:%x, b:%x", teec_operation.params[1].value.a, teec_operation.params[1].value.b);
    teec_operation.params[1].value.a = 0x520;
    teec_operation.params[1].value.b = 0x1314;
    LOGE("a:%x, b:%x", teec_operation.params[1].value.a, teec_operation.params[1].value.b);

    /**
     * Prepare the variables to return.
     */
    // set return origin
    set_return_origin(env, returnOrigin, teec_ret_ori);

    /**
     * sync shared memory and Value back.
    */
    jbyteArray new_op_in_bytes = transfer_TEEC_Operation_to_op(env, &teec_operation, opInBytes);

    //return teec_ret;
    return new_op_in_bytes;
}

JNIEXPORT void JNICALL Java_fi_aalto_ssg_opentee_openteeandroid_NativeLibtee_teecCloseSession
        (JNIEnv* env, jclass jc, jint sidInJni){
    auto sessionWithId = sessions_map.find(sidInJni);

    if( sessionWithId == sessions_map.end() ){
        LOGE("Unable to find session with id %d.", sidInJni);
        return;
    }

    TEEC_CloseSession(&(sessionWithId->second));
    LOGI("Session with id %d is closed.", sidInJni);
}

#ifdef __cplusplus
}
#endif

