package fi.aalto.ssg.opentee;

import android.content.Context;
import android.os.RemoteException;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

/**
 * Public interface as main entrances of APIs for developer.
 */
public interface ITEEClient {
    /**
     * the return value for TEEC_SUCCESS.
     * Other value for return value is wrapped into different exceptions.
     *
     */
    int TEEC_SUCCESS = 0;

    /**
     * return origin code
     */
    enum ReturnOriginCode{
        TEEC_ORIGIN_API(0x00000001),
        TEEC_ORIGIN_COMMS(0x00000002),
        TEEC_ORIGIN_TEE(0x00000003),
        TEEC_ORIGIN_TA(0x00000004);

        private int mId;
        ReturnOriginCode(int id){this.mId = id;}
        public int getId(){return this.mId;};
    }

    /**
     * initialize context.
     * @param teeName the name of remote TEE.
     * @param context Android application context.
     * @return IContext interface.
     * @throws Exception
     * @throws RemoteException
     */
    IContext initializeContext(String teeName, Context context) throws Exception, RemoteException;

    /**
     * Exception extends the java.lang.Exception class. All exceptions in this project should subclass it
     * excluding exceptions defined by Android.
     */
    class Exception extends java.lang.Exception {
        ReturnOriginCode mReturnOriginCode;

        public Exception() { super(); }
        public Exception(ReturnOriginCode returnOriginCode){
            super();
            mReturnOriginCode = returnOriginCode;
        }
        public Exception(String message) { super(message); }
        public Exception(String message, ReturnOriginCode returnOriginCode) {
            super(message);
            mReturnOriginCode = returnOriginCode;
        }
        public Exception(String message, Throwable cause) { super(message, cause); }
        public Exception(Throwable cause) { super(cause); }

        public ReturnOriginCode getReturnOrigin(){
            return this.mReturnOriginCode;
        }
    }

    /**
     * Abstract class for Value, TempMemoryReference and RegisteredMemoryReference.
     */
    abstract class Parameter{
        public enum  Type{
            TEEC_PTYPE_VALUE(0x0000000),
            TEEC_PTYPE_SMR(0x00000001); // shared memory reference type code.

            int id;
            Type(int id){this.id = id;}
            public int getId(){return this.id;};
        }

        public abstract int getType();
    };

    /**
     * reference for registered shared memory
     */
    class RegisteredMemoryReference extends Parameter{
        @Override
        public int getType() {
            return Type.TEEC_PTYPE_SMR.getId();
        }

        IContext.ISharedMemory mSharedMemory;
        int mOffset = 0; // initialized to 0.

        /**
         * public constructor for registered memory reference.
         * @param sharedMemory
         */
        public RegisteredMemoryReference(IContext.ISharedMemory sharedMemory){
            this.mSharedMemory = sharedMemory;
        }

        public IContext.ISharedMemory getSharedMemory(){
            return this.mSharedMemory;
        }

        public void setOffset(int offset){
            this.mOffset = offset;
        }

        public int getOffset(){return this.mOffset;}
    }

    /**
     * This class defines a pair of value which can be passed as a parameter for one Operation.
     */
    class Value extends Parameter{
        public enum  Flag{
            TEEC_VALUE_INPUT(0x0000000),
            TEEC_VALUE_OUTPUT(0x00000001),
            TEEC_VALUE_INOUT(0x00000002);

            int id;
            Flag(int id){this.id = id;}
            public int getId(){return this.id;};
        }

        Flag mFlag;
        int mA;
        int mB;

        /**
         *
         * @param flag only accept TEEC_VALUE_INPUT, TEEC_VALUE_OUTPUT and TEEC_VALUE_INOUT
         * @param a
         * @param b
         */
        public Value(Flag flag, int a, int b){
            this.mFlag = flag;
            this.mA = a;
            this.mB = b;
        }

        /**
         * get method for private member A.
         * @return int
         */
        public int getA(){return this.mA;}

        /**
         * get method for private member B.
         * @return int
         */
        public int getB(){return this.mB;}

        /**
         * get method for flags
         * @return Value.Flag enum
         */
        public Flag getFlag(){
            return this.mFlag;
        }

        /**
         * get method for the type of the parameter.
         * @return
         */
        @Override
        public int getType() {return Type.TEEC_PTYPE_VALUE.getId();}
    }

    /**
     * This class extends the Parameter abstract class.
     * It will be subclassed by TempMemoryReference and RegisteredMemoryReference.
     */
    // abstract class MemoryReference extends Parameter {}

    /**
     * this class defines a Temporary Memory Reference which is temporarily registered for data exchange
     * between Client Application and Trusted Application.
     */
    /*
    class TempMemoryReference extends MemoryReference {
        Type mType;
        byte[] mBuffer;

        enum Type{
            TEEC_MEMREF_TEMP_INPUT(0x00000004),
            TEEC_MEMREF_TEMP_OUTPUT(0x00000005),
            TEEC_MEMREF_TEMP_INOUT(0x00000006);

            int id;
            Type(int id){this.id = id;}
            int getId(){return this.id;}
        }

        public TempMemoryReference(Type type, byte[] buffer){}

        @Override
        public int getType() {
            return this.mType.getId();
        }

        public byte[] asByteArray(){return this.mBuffer;}
    }
    */

    /**
     * a reference to pre-registered or allocated memory.
     */
    /*
    class RegisteredMemoryReference extends Parameter {
        Type mType;
        ITEEClient.IContext.ISharedMemory mParent;
        int mOffset;

        enum Type{
            TEEC_MEMREF_PARTIAL_INPUT(0x00000007),
            TEEC_MEMREF_PARTIAL_OUTPUT(0x00000008),
            TEEC_MEMREF_PARTIAL_INOUT(0x00000009),
            TEEC_MEMREF_WHOLE(0x0000000a);

            int id;
            Type(int id){this.id = id;}
            int getId(){return this.id;}
        }

        public RegisteredMemoryReference(Type type,
                                         ITEEClient.IContext.ISharedMemory parent,
                                         int offset){}
        @Override
        public int getType() {return this.mType.getId();}
    }
    */

    /**
     * This class defines the payload of either an open session or an invoke command operation.
     */
    class Operation {
        int started = 0;
        List<Parameter> params = new ArrayList<>();

        /**
         * Public constructor for no Parameter
         * @param started initialized to 0 to indicates this Operation can be cancelled in the future.
         */
        public Operation(int started){}


        /**
         * Public constructor for taking 1 Parameter
         * @param started initialized to 0 to indicates this Operation can be cancelled in the future.
         * @param parameter carry the parameters for this Operation.
         */
        public Operation(int started,
                         ITEEClient.Parameter parameter){}


        /**
         * Public constructor
         * @param started initialized to 0 to indicates this Operation can be cancelled in the future.
         * @param parameter1 carry the first parameters for this Operation.
         * @param parameter2 carry the second parameters for this Operation.
         */
        public Operation(int started,
                         ITEEClient.Parameter parameter1,
                         ITEEClient.Parameter parameter2){}


        /**
         * Public constructor
         * @param started initialized to 0 to indicates this Operation can be cancelled in the future.
         * @param parameter1 carry the first parameters for this Operation.
         * @param parameter2 carry the second parameters for this Operation.
         * @param parameter3 carry the third parameters for this Operation.
         */
        public Operation(int started,
                         ITEEClient.Parameter parameter1,
                         ITEEClient.Parameter parameter2,
                         ITEEClient.Parameter parameter3){}


        /**
         * Public constructor
         * @param started initialized to 0 to indicates this Operation can be cancelled in the future.
         * @param parameter1 carry the first parameters for this Operation.
         * @param parameter2 carry the second parameters for this Operation.
         * @param parameter3 carry the third parameters for this Operation.
         * @param parameter4 carry the forth parameters for this Operation.
         */
        public Operation(int started,
                         ITEEClient.Parameter parameter1,
                         ITEEClient.Parameter parameter2,
                         ITEEClient.Parameter parameter3,
                         ITEEClient.Parameter parameter4){}

        public int getStarted(){
            return this.started;
        }

        public List<Parameter> getParams(){
            return this.params;
        }
    }


    /**
     * this class indicates where in the software stack the return code was generated for either an
     * openSession or an invokeCommand operation.
     */
    class TeecReturnCodeOrigin {
        enum origin{
            TEEC_ORIGIN_API,
            TEEC_ORIGIN_COMMS,
            TEEC_ORIGIN_TEE,
            TEEC_ORIGIN_TRUSTED_APP
        }
    }

    interface IContext{

        /**
         * Connection Method enum with fixed value corresponding to GP specification.
         */
        enum ConnectionMethod{
            LoginPublic(0x0000000),
            LoginUser(0x00000001),
            LoginGroup(0x00000002),
            LoginApplication(0x00000004),
            LoginUserApplication(0x00000005),
            LoginGroupApplication(0x00000006);

            int val;
            ConnectionMethod(int val){this.val = val;}
        }


        /**
         * Session interface.
         */
        interface ISession {

            /**
             *
             * @param commandId command identifier that is agreed with the Trusted Application
             * @param operation
             //* @param returnOriginCode return origin enum value.
             * @throws Exception throws program error including:
             * 1. session not initialized;
             * 2. calling with invalid content in the teecOperation structure
             * 3. encoding Registered Memory Reference which refer to Shared Memory blocks allocated or
             * registered within the scope of a different TEE Context
             * 4. using the same operation structure concurrently for multiple operations
             */
            void teecInvokeCommand(int commandId,
                                   Operation operation
                                   //,ReturnOriginCode returnOriginCode
            ) throws Exception;

            /**
             * close a session.
             * @throws Exception throws program error includes calling with a session while still has
             * commands running, attempting to close the same Session concurrently from multiple threads and
             * attempting to close the same Session more than once.
             */
            void teecCloseSession()throws Exception;
        }


        /**
         * SharedMemory interface. OTSharedMemory implements this interface.
         */
        interface ISharedMemory {
            int TEEC_MEM_INPUT = 0x00000001;
            int TEEC_MEM_OUTPUT = 0x00000002;

            /**
             * get the flag of the shared memory.
             * @return the flags of ISharedMemory.
             */
            int getFlags();

            /**
             * get the content of the buffer.
             * @return an byte array reference.
             * @throws Exception error if not allowed to get buffer
             */
            byte[] asByteArray() throws Exception;

            /**
             * get the size of the output from Trusted Application if there is such an output.
             * @return the size of the actual output byte array.
             */
            int getReturnSize();

            int getId();
        }



        /**
         * Finalize the context and close the connection to TEE after all sessions have been terminated
         * and all shared memory has been released
         */
        void finalizeContext() throws RemoteException;

        /**
         * register a block of existing Client Application memory as a block of Shared Memory within the
         * scope of the specified TEE context
         * @param buffer indicates the reference of pre-allocated byte array which is to be shared.
         * @param flags indicates I/O direction of this shared memory. Its value can only be TEEC_MEM_INPUT and
         *              TEEC_MEM_OUPUT.
         * @throws Exception exception message can be the return code in TeecResult or program
         * error such as context not initialized, sharedMemory not correctly populated or trying to
         * initialize the same shared memory structure concurrently from multiple threads
         */
        ISharedMemory registerSharedMemory(byte[] buffer,
                                                      int flags) throws Exception, RemoteException;

        /**
         * allocate a new block of memory as a block of Shared Memory within the scope of the specified
         * TEE Context
         * @throws TeecException TeecException exception message can be the return code in TeecResult or
         * program error such as context not initialized, sharedMemory not correctly populated or trying
         * to initialize the same shared memory structure concurrently from multiple threads
         */
        // ITeecSharedMemory teecAllocateSharedMemory() throws TeecException;

        /**
         * release the Shared Memory which previously obtained using teecRegisterSharedMemory or
         * teecAllocateSharedMemory.
         * @param sharedMemory the reference the ITeecSharedMemory instance.
         * @throws Exception program error exceptions including attempting to release Shared Memory
         * which is used by a pending operation or
         * attempting to relaes the same Shared Memory structure concureently from multiple threads.
         */
        void releaseSharedMemory(ISharedMemory sharedMemory) throws Exception, RemoteException;

        /**
         * this API opens a session within the context which is already built.
         * @param uuid UUID of Trusted Application.
         * @param connectionMethod the method of connection to use.
         * @param connectionData any necessary data for connectionMethod.
         * @param teecOperation operations to perform.
         //* @param returnOriginCode return origin enum value.
         * @return an ITeecSession instance.
         * @throws Exception
         */
        ISession openSession (final UUID uuid,
                              ConnectionMethod connectionMethod,
                              int connectionData,
                              Operation teecOperation
                              //ReturnOriginCode returnOriginCode
                              ) throws Exception, RemoteException;


        /**
         * this method requests the cancellation of a pending open Session operation or a Command invocation operation
         * in a separate thread.
         * @param teecOperation the started or pending Operation instance.
         */
        void requestCancellation(Operation teecOperation);
    };


    /******************* begin of the client exception definitions ****************************/
    /**
     * Concurrent accesses caused conflict.
     */
    class AccessConflictException extends ITEEClient.Exception {
        public AccessConflictException(String msg){ super(msg);}

        public AccessConflictException(String msg, ReturnOriginCode retOrigin){
            super(msg, retOrigin);
        }
    }

    /**
     * Access privileges are not sufficient
     */
    class AccessDeniedException extends ITEEClient.Exception {
        public AccessDeniedException(String msg){
            super(msg);
        }

        public AccessDeniedException(String msg, ReturnOriginCode retOrigin){
            super(msg, retOrigin);
        }
    }

    /**
     * Input data was of invalid format.
     */
    class BadFormatException extends ITEEClient.Exception {
        public BadFormatException(String msg){
            super(msg);
        }

        public BadFormatException(String msg, ReturnOriginCode retOrigin){
            super(msg, retOrigin);
        }
    }

    /**
     * Input parameters were invalid.
     */
    class BadParametersException extends ITEEClient.Exception {
        public BadParametersException(String msg){
            super(msg);
        }
        public BadParametersException(String msg, ReturnOriginCode retOrigin){
            super(msg, retOrigin);
        }
    }

    /**
     * Operation is not valid in the current state.
     */
    class BadStateException extends ITEEClient.Exception {
        public BadStateException(String msg){
            super(msg);
        }
        public BadStateException(String msg, ReturnOriginCode retOrigin){
            super(msg, retOrigin);
        }
    }

    /**
     * The system is busy working on something else.
     */
    class BusyException extends ITEEClient.Exception {
        public BusyException(String msg){
            super(msg);
        }
        public BusyException(String msg, ReturnOriginCode retOrigin){
            super(msg, retOrigin);
        }
    }

    /**
     * The operation was cancelled
     */
    class CancelErrorException extends ITEEClient.Exception {
        public CancelErrorException(String msg){
            super(msg);
        }
        public CancelErrorException(String msg, ReturnOriginCode retOrigin){
            super(msg, retOrigin);
        }
    }

    /**
     * Communication with a remote party failed.
     */
    class CommunicationErrorException extends ITEEClient.Exception {
        public CommunicationErrorException(String msg){
            super(msg);
        }
        public CommunicationErrorException(String msg, ReturnOriginCode retOrigin){
            super(msg, retOrigin);
        }
    }

    /**
     * Too much data for the requested operation was passed.
     */
    class ExcessDataException extends ITEEClient.Exception {
        public ExcessDataException(String msg){
            super(msg);
        }
        public ExcessDataException(String msg, ReturnOriginCode retOrigin){
            super(msg, retOrigin);
        }
    }

    /**
     * Non-specific cause exception.
     */
    class GenericErrorException extends ITEEClient.Exception {
        public GenericErrorException(String msg){
            super(msg);
        }
        public GenericErrorException(String msg, ReturnOriginCode retOrigin){
            super(msg, retOrigin);
        }
    }

    /**
     * The requested data item is not found.
     */
    class ItemNotFoundException extends ITEEClient.Exception {
        public ItemNotFoundException(String msg){
            super(msg);
        }
        public ItemNotFoundException(String msg, ReturnOriginCode retOrigin){
            super(msg, retOrigin);
        }
    }

    /**
     * Expected data was missing.
     */
    class NoDataException extends ITEEClient.Exception {
        public NoDataException(String msg){
            super(msg);
        }
        public NoDataException(String msg, ReturnOriginCode retOrigin){
            super(msg, retOrigin);
        }
    }

    /**
     * The requested operation should exist but is not yet implemented.
     */
    class NotImplementedException extends ITEEClient.Exception {
        public NotImplementedException(String msg){
            super(msg);
        }
        public NotImplementedException(String msg, ReturnOriginCode retOrigin){
            super(msg, retOrigin);
        }
    }

    /**
     * The requested operation is valid but is not supported in this implementation.
     */
    class NotSupportedException extends ITEEClient.Exception {
        public NotSupportedException(String msg){
            super(msg);
        }
        public NotSupportedException(String msg, ReturnOriginCode retOrigin){
            super(msg, retOrigin);
        }
    }

    /**
     * System ran out of resources.
     */
    class OutOfMemoryException extends ITEEClient.Exception {
        public OutOfMemoryException(String msg){
            super(msg);
        }
        public OutOfMemoryException(String msg, ReturnOriginCode retOrigin){
            super(msg, retOrigin);
        }
    }

    /**
     * A security fault was detected.
     */
    class SecurityErrorException extends ITEEClient.Exception {
        public SecurityErrorException(String msg){
            super(msg);
        }
        public SecurityErrorException(String msg, ReturnOriginCode retOrigin){
            super(msg, retOrigin);
        }
    }

    /**
     * The supplied buffer is too short for the generated output.
     */
    class ShortBufferException extends Exception {
        public ShortBufferException(String msg){
            super(msg);
        }
        public ShortBufferException(String msg, ReturnOriginCode retOrigin){
            super(msg, retOrigin);
        }
    }

    /**
     * An external event has caused a User Interface operation to be aborted.
     */
    class ExternalCancelException extends Exception{
        public ExternalCancelException(String msg){
            super(msg);
        }
        public ExternalCancelException(String msg, ReturnOriginCode retOrigin){
            super(msg, retOrigin);
        }
    }

    /**
     * Internal TEE error - document for completeness.
     */
    class OverflowException extends Exception{
        public OverflowException(String msg){
            super(msg);
        }
        public OverflowException(String msg, ReturnOriginCode retOrigin){
            super(msg, retOrigin);
        }
    }

    /**
     * The Trusted Application has terminated.
     */
    class TargetDeadException extends Exception{
        public TargetDeadException(String msg){
            super(msg);
        }
        public TargetDeadException(String msg, ReturnOriginCode retOrigin){
            super(msg, retOrigin);
        }
    }

    class NoStorageSpaceException extends Exception{
        public NoStorageSpaceException(String msg){
            super(msg);
        }
        public NoStorageSpaceException(String msg, ReturnOriginCode retOrigin){
            super(msg, retOrigin);
        }
    }
    /******************* end of the client exception definitions ******************************/
}
