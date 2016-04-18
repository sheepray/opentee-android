package fi.aalto.ssg.opentee;

import android.content.Context;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import fi.aalto.ssg.opentee.exception.GenericErrorException;
import fi.aalto.ssg.opentee.exception.TEEClientException;

/**
 * Open-TEE Java API entry point. ITEEClient interface embraces all the APIs and other public
 * interfaces. CA can use it to communicate with the remote TEE/TAs.
 */
public interface ITEEClient {

    /**
     * This interface defines the way to interact with an Operation which is a wrapper for
     * 0 to 4 IParameter(s). It can be created by calling the factory method ITEEClient.newOperation(...).
     * After a valid IOperation interface is returned, developers can refer the corresponding Operation
     * in either openSession or invokeCommand function calls.
     *
     * When developers are dealing with multi-threads, one IOperation interface can be shared between
     * different threads. So it is possible that multiple threads try to access the same IOperation
     * interface at the same time. If one or some of the IParameters wrapped inside the IOperation
     * is output for TA, it is highly possible that the IParameter(s) might be in an inconsistent state
     * which may result in an incorrect read of corresponding wrapped resources within IParameters,
     * such as Value and SharedMemory. What's more, if one thread try to apply one IOperation interface
     * in its openSession or InvokeCommand function call while this IOperation interface is being used
     * by another thread, an BusyException will be threw. In addition, if wrapped resources within
     * one IOperation interface are modified by another thread, it is the responsibilities of the developers
     * to avoid such a situation to happen. In order to avoid mis-usage of IOperation interface,
     * the developers should not access any wrapped resources in one under-usage IOperation interface.
     * The state of the IOperation can be easily obtained by calling its isStarted function. So, it
     * is highly recommended for the developers to check the state of the IOperation interface when
     * passing it into openSession or invokeCommand.
     */
    interface IOperation{
        /**
         * If one IOperation interface is used in an ongoing operation (either openSession or
         * invokeCommand) in a separate thread, this function will return true. Developers can
         * utilize this function to test the availability of the IOperation interface.
         * @return true if IOperation is under usage. Otherwise false if not being under usage.
         */
        boolean isStarted();
    }

    /**
     * Factory method to create an Operation without Parameter.
     * @return the IOperation interface for created Operation.
     */
    IOperation newOperation();

    /**
     * Factory method to create an Operation with one Parameter.
     *
     * It is possible to create multiple IOperation interfaces using the same IParameter. But it
     * is not recommended especially when the I/O directory of IParameter is output for TA since it
     * is highly possible that such an IParameter is in an inconsistent state. This rule also
     * apply to other ITEEClient.newOperation overloaded functions which take IParameter(s) as input.
     * @param firstParam the first IParameter.
     * @return the IOperation interface for created Operation.
     */
    IOperation newOperation(IParameter firstParam);

    /**
     * Factory method to create an Operation with two Parameters.
     * The order of input Parameters should be aligned with the order of required Parameters in TA.
     * This rule also apply to other overloaded newOperation factory function call which takes more than
     * two Parameters.
     * @param firstParam the first IParameter.
     * @param secondParam the second IParameter.
     * @return the IOperation interface for created Operation.
     */
    IOperation newOperation(IParameter firstParam, IParameter secondParam);

    /**
     * Factory method to create an Operation with three Parameters.
     * @param firstParam the first IParameter.
     * @param secondParam the second IParameter.
     * @param thirdParam the third IParameter.
     * @return the IOperation interface for created Operation.
     */
    IOperation newOperation(IParameter firstParam, IParameter secondParam, IParameter thirdParam);

    /**
     * Factory method to create an Operation with four Parameters.
     * @param firstParam the first IParameter.
     * @param secondParam the second IParameter.
     * @param thirdParam the third IParameter.
     * @param forthParam the forth IParameter.
     * @return the IOperation interface for created Operation.
     */
    IOperation newOperation(IParameter firstParam, IParameter secondParam, IParameter thirdParam, IParameter forthParam);


    /**
     * IParameter interface is the parent of IRegisteredMemoryReference and IValue interfaces,
     * It can passed into the ITEEClient.newOperation to create an IOperation interface.
     */
    interface IParameter{
        /**
         * The enum to indicates the type of the Parameter.
         */
        enum Type{
            /**
             * This Parameter is a Value.
             */
            TEEC_PTYPE_VAL(0x0000001),
            /**
             * This Parameter is a RegisteredMemoryReference.
             */
            TEEC_PTYPE_RMR(0x00000002);

            int id;
            Type(int id){this.id = id;}
        }

        /**
         * Get the type of the IParameter interface.
         * @return an enum value Type which can be either TEEC_PTYPE_VAL or TEEC_PTYPE_RMR.
         */
        Type getType();
    }

    /**
     * Interface for registered memory reference. It can be only obtained by calling factory method
     * ITEEClient.newRegisteredMemoryReference(...).
     */
    interface IRegisteredMemoryReference extends IParameter{
        /**
         * Flag enum indicates the I/O direction of the referenced registered shared memory.
         */
        enum Flag{
            /**
             * The I/O direction of the referenced registered shared memory is input for
             * Trusted Application.
             */
            TEEC_MEMREF_INPUT(0x0000000D),
            /**
             * The I/O direction of the referenced registered shared memory is output for
             * Trusted Application.
             */
            TEEC_MEMREF_OUTPUT(0x0000000E),
            /**
             * The I/O directions of the referenced registered shared memory are both input and output
             * for Trusted Application.
             */
            TEEC_MEMREF_INOUT(0x0000000F);

            int id;
            Flag(int id){this.id = id;}
        }
    }

    /**
     * Factory method to create a registered memory reference with a valid ISharedMemory interface and a
     * flag to indicate the I/O direction for this memory reference. The flag is only valid when
     * the corresponding shared memory also has such a flag.
     * @param sharedMemory
     * @param flag
     * @param offset
     */
    IRegisteredMemoryReference newRegisteredMemoryReference(ISharedMemory sharedMemory, IRegisteredMemoryReference.Flag flag, int offset);

    /**
     * Interface to access a pair of two integer values. It can be only obtained by calling
     * ITEEClient.newValue factory method.
     */
    interface IValue extends IParameter{
        /**
         * Flag enum indicates the I/O direction of Value.
         */
        enum Flag{
            /**
             * The I/O direction for Value is input for Trusted Application.
             */
            TEEC_VALUE_INPUT(0x0000001),
            /**
             * The I/O direction for Value is output for Trusted Application.
             */
            TEEC_VALUE_OUTPUT(0x00000002),
            /**
             * The I/O directions for Value are both input and output for Trusted Application.
             */
            TEEC_VALUE_INOUT(0x00000003);

            int id;
            Flag(int id){this.id = id;}
        }
    }

    /**
     * Factory method to create an interface of a pair of two integer values.
     * @param flag The I/O directory of IValue for TA.
     * @param a The first integer value.
     * @param b The second integer value.
     * @return a IValue interface.
     */
    IValue newValue(IValue.Flag flag, int a, int b);

    /**
     * In order for the CA to communicate with the TA within a TEE, a session must be opened between CA and TA.
     * To open a session, the CA must call openSession within a valid context. When a session is opened,
     * an ISession interface will be returned. It embraces all functions for CA to communicate with TA.
     * Within this session, the developer can call the invokeCommand function to invoke corresponding function within the TA.
     * When the session is no longer needed, the developers should close the session by calling
     * closeSession function.
     */
    interface ISession {

        /**
         * Sending a request to the connected Trusted Application with agreed commandId and parameters.
         * The parameters are encapsulated in the operation.
         *
         * @param commandId command identifier that is agreed with the Trusted Application.
         * @param operation parameters for the command to invoke.
         * @throws exception.AccessConflictException:
         * using shared resources which are occupied by another thread;
         * @throws exception.BadFormatException:
         * providing incorrect format of parameters in operation;
         * @throws exception.BadParametersException:
         * providing parameters with invalid content;
         * @throws exception.BusyException:
         * 1. the TEE is busy working on something else and does not have the computation power to execute
         * requested operation;
         * 2. the referenced IOperation interface is being used by another thread.
         * @throws exception.CancelErrorException:
         * the provided operation parameter is invalid due to the cancellation from another thread;
         * @throws exception.CommunicationErrorException:
         * 1. fatal communication error in the remote TEE and TA side.
         * 2. Communication with remote TEE service failed.
         * @throws exception.ExcessDataException:
         * providing too much parameters in the operation parameter.
         * @throws exception.ExternalCancelException:
         * current operation cancelled by external signal in the remote TEE or TA side.
         * @throws exception.GenericErrorException:
         * non-specific error.
         * @throws exception.ItemNotFoundException:
         * providing invalid reference to a registered shared memory.
         * @throws exception.NoDataException:
         * required data are missing in the operation.
         * @throws exception.OutOfMemoryException:
         * the remote system runs out of memory.
         * @throws exception.OverflowException:
         * an buffer overflow happened in the remote TEE or TA.
         * @throws exception.SecurityErrorException:
         * incorrect usage of shared memory.
         * @throws exception.ShortBufferException:
         * the provided output buffer is too short to hold the output.
         * @throws exception.TargetDeadException:
         * the remote TEE or TA crashed.
         */
        void invokeCommand(int commandId, Operation operation) throws TEEClientException;

        /**
         * Close the connection to the remote Trusted Application. When dealing with multi-threads,
         * this function is recommended to be called with the same thread which opens this session.
         * @throws exception.CommunicationErrorException:
         * Communication with remote TEE service failed.
         * @throws exception.TargetDeadException:
         * the remote TEE or TA crashed.
         */
        void closeSession() throws TEEClientException;
    }


    /**
     * In order to enable data sharing between the CA and TEE/TA, the notation called shared memory has been
     * introduced. To create a shared memory, the CA firstly allocate a buffer which can be used as
     * a shared memory. Then, the CA calls the ITEEClient.registerSharedMemory to register current
     * shared memory to the remote TEE so that the TEE and TA can use it as a shared memory. When the CA
     * tries to register a shared memory, the I/O direction of this shared memory must be provided
     * along with the buffer to hold that shared memory. The I/O direction is a bit mask of TEEC_MEM_INPUT
     * and TEEC_MEM_OUTPUT. Please note that the I/O direction of this shared memory is for the remote
     * TEE/TA. See the detailed explanation of these two flags in the field description. The size of
     * the shared memory is the same as the buffer that it holds. When the CA successfully register
     * this buffer as a shared memory with a flag of TEEC_MEM_INPUT, any modification on this buffer
     * will be synced to the TEE/TA during each function call from CA to TEE. Similarly, if the
     * shared memory is flagged with TEEC_MEM_OUTPUT, any modification of the shared memory from the
     * TEE side will be synced to the CA after each remote function call from CA to TEE.
     * <p>
     * ISharedMemory interface provides operations on the shared memory.
     * It is only valid in the IContext interface. This interface can be only obtained
     * by calling ITEEClient.registerSharedMemory function within a valid context. If the registered shared memory
     * is not longer needed, the developers should release it by calling IContext.releaseSharedMemory
     * function. After the shared memory is released, the buffer it holds will not longer used as a
     * shared memory. So, any modification on it will no longer be synced to the remote TEE.
     */
    interface ISharedMemory {
        /**
         * This value indicates the I/O direction of the shared memory is input for both
         * TEE and Trusted Application.
         */
        int TEEC_MEM_INPUT = 0x00000001;
        /**
         * This value indicates the I/O direction of the shared memory is output for both
         * TEE and Trusted Application.
         */
        int TEEC_MEM_OUTPUT = 0x00000002;

        /**
         * Get the I/O direction of the shared memory.
         * @return the flags of ISharedMemory.
         */
        int getFlags();

        /**
         * Get the content of the shared memory. This function returns a reference to the buffer.
         * @return an byte array reference.
         */
        byte[] asByteArray();

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
     * Return origin code enum which indicates the origin when an exception is threw. It can be obtained
     * by calling TEEClientException.getReturnOrigin. The developers can get a valid return origin
     * only when the exceptions are threw after calling these two functions: openSession and invokeCommand.
     * Otherwise, the return origin will be null.
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
    }

    /**
     * Factory method which initializes a context to a TEE.
     * @param teeName the name of remote TEE. If teeName is null, a context will be initialized within
     *                a default TEE.
     * @param context Android application context.
     * @return IContext interface.
     * @throws exception.AccessDeniedException:
     * Unable to initialize a context with the remote TEE due to insufficient privileges of CA.
     * @throws exception.BadStateException:
     * TEE is not ready to initialize a context for CA.
     * @throws exception.BadParametersException:
     * providing an invalid Android context.
     * @throws exception.BusyException:
     * TEE is busy.
     * @throws exception.CommunicationErrorException:
     * Communication with remote TEE service failed.
     * @throws exception.GenericErrorException:
     * Non-specific cause exception.
     * @throws exception.TargetDeadException:
     * TEE crashed.
     */
    IContext initializeContext(String teeName, Context context) throws TEEClientException;

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
            TEEC_PTYPE_VALUE(0x0000001),
            /**
             * This Parameter is a RegisteredMemoryReference.
             */
            TEEC_PTYPE_SMR(0x00000002);

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

    /**
     * IContext interface provides all the functions to interact with an initialized context in remote TEE.
     * This interface is returned by the ITEEClient.initializeContext function call. When a context
     * is no longer needed, it should be closed by calling ITEEClient.IContext.finalizeContext. When
     * the IContext interface is passed into different threads, the developers are responsible for
     * providing thread-safe mechanism to avoid the conflict between different threads.
     */
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
             * Application and the about the Client Application itself is provided.
             */
            LoginGroupApplication(0x00000006);

            int val;
            ConnectionMethod(int val){this.val = val;}
        }

        /**
         * Finalizing the context and close the connection to TEE after all sessions have been terminated
         * and all shared memory has been released. This function should be called in at the end of the
         * main thread when all the sub-threads are terminated when IContext is used in different sub-threads.
         *
         * @throws exception.CommunicationErrorException:
         * Communication with remote TEE service failed.
         */
        void finalizeContext() throws TEEClientException;

        /**
         * Registering a block of existing Client Application memory as a block of Shared Memory within
         * a valid TEE context. When this function tries to register a buffer as a shared memory which
         * is already used by another shared memory, this function will also return success. The
         * TEE will regard this buffer as two identical shared memory. Under such a circumstance,
         * it can easily cause problems such as AccessConflictException etc. So, it is not recommended
         * to do so. However, when a shared memory is released, the buffer it holds can be registered
         * again as a new shared memory. For the CA, the buffer is the same but it is identical for
         * the TEE.
         * @param buffer indicates the reference of pre-allocated byte array which is to be shared.
         * @param flags indicates I/O direction of this shared memory for Trusted Application.
         * @throws exception.BadParametersException:
         * 1. try to register a null/empty buffer as a shared memory.
         * 2. providing incorrect flag value.
         * @throws exception.BadStateException:
         * TEE is not ready to register a shared memory.
         * @throws exception.BusyException:
         * TEE is busy
         * @throws exception.CommunicationErrorException:
         * Communication with remote TEE service failed.
         * @throws exception.ExternalCancelException:
         * Current operation is cancelled by external signal in TEE.
         * @throws exception.GenericErrorException:
         * Non-specific causes error.
         * @throws exception.NoStorageSpaceException:
         * Insufficient storage in TEE.
         * @throws exception.OutOfMemoryException:
         * Insufficient memory in TEE.
         * @throws exception.OverflowException:
         * Buffer overflow in TEE.
         * @throws exception.TargetDeadException:
         * TEE/TA crashed.
         */
        ISharedMemory registerSharedMemory(byte[] buffer, int flags) throws TEEClientException;

        /**
         * Releasing the Shared Memory which is previously obtained using registerSharedMemory. As
         * stated in the ISharedMemory, when the shared memory is released, the TEE/TA will no longer
         * be able to read or write data to the shared memory. But the buffer that this shared memory
         * holds will still remain valid. When using the same shared memory within multi-threads, it
         * is recommended to release the shared memory in the same thread who registered it.
         * @param sharedMemory the reference the ISharedMemory instance.
         * @throws exception.CommunicationErrorException:
         * Communication with remote TEE service failed.
         */
        void releaseSharedMemory(ISharedMemory sharedMemory) throws TEEClientException;

        /**
         * Opening a session within current context. It opens a channel(another notation for session)
         * to a TA specified by the uuid so that the CA can communicate with it. In order to open
         * such a channel successfully, the CA must provide precise and correct data to authenticate itself
         * to the TA.
         * @param uuid UUID of Trusted Application.
         * @param connectionMethod the method of connection to use.
         * @param connectionData any necessary data for connectionMethod.
         * @param operation operations to perform.
         * @return an ISession interface.
         * @throws exception.AccessDeniedException:
         * Insufficient privilege.
         * @throws exception.BadFormatException:
         * Using incorrect format of parameter(s).
         * @throws exception.BadParametersException:
         * Unexpected value(s) for parameter(s).
         * @throws exception.BadStateException:
         * TEE is not ready to open a session or the referenced IOperation interface is occupied by
         * another thread.
         * @throws exception.BusyException:
         * TEE is busy.
         * @throws exception.CancelErrorException:
         * Current operation is cancelled by another thread.
         * @throws exception.CommunicationErrorException:
         * Communication with remote TEE service failed.
         * @throws exception.GenericErrorException:
         * No specific cause error.
         * @throws exception.ItemNotFoundException:
         * Referred shared memory not found.
         * @throws exception.NoDataException:
         * Extra data expected.
         * @throws exception.NoStorageSpaceException:
         * Insufficient data storage in TEE.
         * @throws exception.OutOfMemoryException:
         * TEE runs out of memory.
         * @throws exception.OverflowException:
         * Buffer overflow in TEE.
         * @throws exception.SecurityErrorException:
         * Incorrect usage of shared memory.
         * @throws exception.ShortBufferException:
         * the provided output buffer is too short to hold the output.
         * @throws exception.TargetDeadException:
         * TEE/TA crashed.
         */
        ISession openSession (final UUID uuid,
                              ConnectionMethod connectionMethod,
                              int connectionData,
                              Operation operation
                              ) throws TEEClientException;


        /**
         * Requesting the cancellation of a pending open Session operation or a Command invocation operation
         * in a separate thread.
         * @param operation the started or pending Operation instance.
         * @throws exception.CommunicationErrorException:
         * Communication with remote TEE service failed.
         */
        void requestCancellation(Operation operation) throws TEEClientException;
    };
}