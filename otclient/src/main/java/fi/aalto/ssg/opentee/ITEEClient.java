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
     * The session interface provides the invokeCommand operation. This interface can be only
     * obtained by calling openSession function within a valid context.
     */
    interface ISession {

        /**
         * Sending a request to the connected Trusted Application with agreed commandId and parameters.
         *
         * @param commandId command identifier that is agreed with the Trusted Application.
         * @param operation parameters for the command to invoke.
         * @throws Exception throws program error including:<br>
         * 1. calling with invalid content in the teecOperation structure;<br>
         * 2. using the same operation structure concurrently for multiple operations.
         */
        void invokeCommand(int commandId, Operation operation) throws Exception;

        /**
         * Close the connection to the remote Trusted Application.
         * @throws Exception throws program error including:<br>
         * 1. calling with a session while still has commands running;<br>
         * 2. attempting to close the same Session concurrently from multiple threads and
         * attempting to close the same Session more than once.
         */
        void closeSession()throws Exception;
    }


    /**
     * SharedMemory interface provides operations on shared memory. This interface can be only obtained
     * by calling registerSharedMemory function within a valid context.
     */
    interface ISharedMemory {
        /**
         * This value indicates the I/O direction of the shared memory is input for Trusted Application.
         */
        int TEEC_MEM_INPUT = 0x00000001;
        /**
         * This value indicates the I/O direction of the shared memory is output for Trusted Application.
         */
        int TEEC_MEM_OUTPUT = 0x00000002;

        /**
         * Get the I/O direction of the shared memory.
         * @return the flags of ISharedMemory.
         */
        int getFlags();

        /**
         * Get the content of the shared memory.
         * @return an byte array reference.
         * @throws Exception if failed to get the shared memory.
         */
        byte[] asByteArray() throws Exception;

        /**
         * Get the size of the output from Trusted Application if there is such an output.
         * @return the actual size of the output byte array.
         */
        int getReturnSize();

        /**
         * Get the id of the shared memory.
         * @return the id of the shared memory.
         */
        int getId();
    }



    /**
     * The return value for TEEC_SUCCESS.
     */
    int TEEC_SUCCESS = 0;

    /**
     * Return origin code enum which indicates the origin when an exception is throwed.
     */
    enum ReturnOriginCode{
        /**
         * The exception is originated within the TEE Client API implementation.
         */
        TEEC_ORIGIN_API(0x00000001),
        /**
         * The exception is originated within the underlying communications stack linking the Rich
         * OS with the TEE.
         */
        TEEC_ORIGIN_COMMS(0x00000002),
        /**
         * The exception is originated within the common TEE code.
         */
        TEEC_ORIGIN_TEE(0x00000003),
        /**
         * The exception is originated within the Trusted Application.
         */
        TEEC_ORIGIN_TA(0x00000004);

        private int mId;
        ReturnOriginCode(int id){this.mId = id;}
        //public int getId(){return this.mId;};
    }

    /**
     * Initialize a context to a TEE.
     * @param teeName the name of remote TEE.
     * @param context Android application context.
     * @return IContext interface.
     * @throws Exception when try to initialize the same context more than once within a single thread.
     * @throws RemoteException when connection disconnected with the remote TEE.
     */
    IContext initializeContext(String teeName, Context context) throws Exception, RemoteException;

    /**
     * Exception extends the java.lang.Exception class. All exceptions in this project should subclass it
     * excluding exceptions defined by Android.
     */
    class Exception extends java.lang.Exception {
        /**
         * The field indicates the return origin which cause this exception.
         */
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

        /**
         * Get the return origin.
         * @return The return origin code.
         */
        public ReturnOriginCode getReturnOrigin(){
            return this.mReturnOriginCode;
        }
    }

    /**
     * Abstract class for Value and RegisteredMemoryReference.
     */
    abstract class Parameter{
        /**
         * The enum to indicates the type of the Parameter.
         */
        public enum  Type{
            /**
             * This Parameter is a Value.
             */
            TEEC_PTYPE_VALUE(0x0000000),
            /**
             * This Parameter is a RegisteredMemoryReference.
             */
            TEEC_PTYPE_SMR(0x00000001);

            int id;
            Type(int id){this.id = id;}
            public int getId(){return this.id;};
        }

        public abstract int getType();
    };

    /**
     * Reference for registered shared memory.
     */
    class RegisteredMemoryReference extends Parameter{
        /**
         * Flag enum indicates the I/O direction of the referenced registered shared memory.
         */
        public enum Flag{
            /**
             * The I/O direction of the referenced registered shared memory is input for
             * Trusted Application.
             */
            TEEC_MEMREF_INPUT(0x0000000),
            /**
             * The I/O direction of the referenced registered shared memory is output for
             * Trusted Application.
             */
            TEEC_MEMREF_OUTPUT(0x00000001),
            /**
             * The I/O directions of the referenced registered shared memory are both input and output
             * for Trusted Application.
             */
            TEEC_MEMREF_INOUT(0x00000002);

            int id;
            Flag(int id){this.id = id;}
        }

        @Override
        public int getType() {
            return Type.TEEC_PTYPE_SMR.getId();
        }

        ISharedMemory mSharedMemory;
        int mOffset = 0; // initialized to 0.
        Flag mFlag;

        /**
         * Create a registered memory reference with a valid ISharedMemory interface and a
         * flag to indicate the I/O direction for this memory reference. The flag is only valid when
         * the corresponding shared memory also has such a flag.
         * @param sharedMemory
         * @param flag
         */
        public RegisteredMemoryReference(ISharedMemory sharedMemory, Flag flag, int offset){
            this.mSharedMemory = sharedMemory;
            this.mFlag = flag;
            this.mOffset = offset;
        }

        /**
         * Get the referenced registered shared memory.
         * @return ISharedMemory interface for the referenced registered shared memory.
         */
        public ISharedMemory getSharedMemory(){
            return this.mSharedMemory;
        }

        public int getOffset(){return this.mOffset;}

        public Flag getFlag(){return this.mFlag;}
    }

    /**
     * This class defines a pair of value which can be passed as a parameter for one Operation.
     */
    class Value extends Parameter{
        /**
         * Flag enum indicates the I/O direction of Value.
         */
        public enum  Flag{
            /**
             * The I/O direction for Value is input for Trusted Application.
             */
            TEEC_VALUE_INPUT(0x0000000),
            /**
             * The I/O direction for Value is output for Trusted Application.
             */
            TEEC_VALUE_OUTPUT(0x00000001),
            /**
             * The I/O directions for Value are both input and output for Trusted Application.
             */
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
         * @param flag
         * @param a
         * @param b
         */
        public Value(Flag flag, int a, int b){
            this.mFlag = flag;
            this.mA = a;
            this.mB = b;
        }

        /**
         * Get method for private member A.
         * @return int
         */
        public int getA(){return this.mA;}

        /**
         * Get method for private member B.
         * @return int
         */
        public int getB(){return this.mB;}

        /**
         * Get method for flags
         * @return Value.Flag enum
         */
        public Flag getFlag(){
            return this.mFlag;
        }

        /**
         * Get method for the type of the parameter.
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
        ITEEClient.ISharedMemory mParent;
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
                                         ITEEClient.ISharedMemory parent,
                                         int offset){}
        @Override
        public int getType() {return this.mType.getId();}
    }
    */

    /**
     * This class defines the payload for either an open session or an invoke command operation.
     */
    class Operation {
        int started = 0;
        List<Parameter> params = new ArrayList<>();

        /**
         * Public constructor with no Parameter.
         * @param started initialized to 0 to indicates this Operation can be cancelled in the future.
         */
        public Operation(int started){
            this.started = started;
        }


        /**
         * Public constructor with 1 Parameter.
         * @param started initialized to 0 to indicates this Operation can be cancelled in the future.
         * @param parameter carry the parameters for this Operation.
         */
        public Operation(int started,
                         ITEEClient.Parameter parameter){
            this.started = started;
            params.add(parameter);
        }


        /**
         * Public constructor with 2 Parameters.
         * @param started initialized to 0 to indicates this Operation can be cancelled in the future.
         * @param parameter1 carry the first parameters for this Operation.
         * @param parameter2 carry the second parameters for this Operation.
         */
        public Operation(int started,
                         ITEEClient.Parameter parameter1,
                         ITEEClient.Parameter parameter2){
            this.started = started;
            params.add(parameter1);
            params.add(parameter2);
        }


        /**
         * Public constructor with 3 Parameters.
         * @param started initialized to 0 to indicates this Operation can be cancelled in the future.
         * @param parameter1 carry the first parameters for this Operation.
         * @param parameter2 carry the second parameters for this Operation.
         * @param parameter3 carry the third parameters for this Operation.
         */
        public Operation(int started,
                         ITEEClient.Parameter parameter1,
                         ITEEClient.Parameter parameter2,
                         ITEEClient.Parameter parameter3){
            this.started = started;
            params.add(parameter1);
            params.add(parameter2);
            params.add(parameter3);
        }


        /**
         * Public constructor with 4 Parameters.
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
                         ITEEClient.Parameter parameter4){
            this.started = started;
            params.add(parameter1);
            params.add(parameter2);
            params.add(parameter3);
            params.add(parameter4);
        }

        public int getStarted(){
            return this.started;
        }

        public List<Parameter> getParams(){
            return this.params;
        }
    }

    interface IContext{
        /**
         * Connection Method enum with fixed value corresponding to GP specification when calling
         * openSession.
         */
        enum ConnectionMethod{
            /**
             * No login data is provided.
             */
            LoginPublic(0x0000000),
            /**
             * Login data about the user running the Client Application process is provided.
             */
            LoginUser(0x00000001),
            /**
             * Login data about the group running the Client Application process is provided.
             */
            LoginGroup(0x00000002),
            /**
             * Login data about the running Client Application process itself is provided.
             */
            LoginApplication(0x00000004),
            /**
             * Login data about the user running the Client Application and about the Client
             * Application itself is provided.
             */
            LoginUserApplication(0x00000005),
            /**
             * Login data about the group running the Client Application and about the Client
             * Application andthe about the Client Application itself is provided.
             */
            LoginGroupApplication(0x00000006);

            int val;
            ConnectionMethod(int val){this.val = val;}
        }

        /**
         * Finalizing the context and close the connection to TEE after all sessions have been terminated
         * and all shared memory has been released
         * @throws RemoteException
         */
        void finalizeContext() throws RemoteException;

        /**
         * Registering a block of existing Client Application memory as a block of Shared Memory within
         * current TEE context.
         * @param buffer indicates the reference of pre-allocated byte array which is to be shared.
         * @param flags indicates I/O direction of this shared memory for Trusted Application.
         * @throws Exception
         */
        ISharedMemory registerSharedMemory(byte[] buffer, int flags) throws Exception, RemoteException;

        /**
         * Releasing the Shared Memory which is previously obtained using registerSharedMemory.
         * @param sharedMemory the reference the ISharedMemory instance.
         * @throws Exception program error exceptions including attempting to release Shared Memory
         * which is used by a pending operation.
         */
        void releaseSharedMemory(ISharedMemory sharedMemory) throws Exception, RemoteException;

        /**
         * Opening a session within current context.
         * @param uuid UUID of Trusted Application.
         * @param connectionMethod the method of connection to use.
         * @param connectionData any necessary data for connectionMethod.
         * @param operation operations to perform.
         * @return an ISession interface.
         * @throws Exception
         */
        ISession openSession (final UUID uuid,
                              ConnectionMethod connectionMethod,
                              int connectionData,
                              Operation operation
                              //ReturnOriginCode returnOriginCode
                              ) throws Exception, RemoteException;


        /**
         * Requesting the cancellation of a pending open Session operation or a Command invocation operation
         * in a separate thread.
         * @param operation the started or pending Operation instance.
         */
        void requestCancellation(Operation operation);
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
     * Internal TEE error - documented for completeness.
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

    /**
     * Internal TEE error - documented for completeness.
     */
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