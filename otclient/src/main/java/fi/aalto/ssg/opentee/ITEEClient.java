package fi.aalto.ssg.opentee;

import android.content.Context;
import android.os.RemoteException;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import fi.aalto.ssg.opentee.exception.GenericErrorException;
import fi.aalto.ssg.opentee.exception.TEEClientException;

/**
 * Open-TEE Java API entry point. ITEEClient interface embraces all the APIs and other public
 * interfaces. CA can use it to communicate with the remote TEE/TAs. It can be only obtained by
 * calling OpenTEE.newClient function.
 */
public interface ITEEClient {

    /**
     * This interface defines the way to interact with an Operation which is a wrapper for
     * 0 to 4 IParameter(s). It can be created by calling the factory method <code>ITEEClient.newOperation(...)</code>.
     * After a valid <code>IOperation</code> interface is returned, developers can refer the corresponding Operation
     * in either <code>openSession</code> or <code>invokeCommand</code> function calls.
     *
     * When developers are dealing with multi-threads, one IOperation interface can be shared between
     * different threads. So it is possible that multiple threads try to access the same <code>IOperation</code>
     * interface at the same time. If one or some of the <code>IParameter</code> interfaces wrapped inside the <code>IOperation</code>
     * is output for TA, it is highly possible that the <code>IParameter</code>(s) might be in an inconsistent state
     * which may result in an incorrect read of corresponding wrapped resources within <code>IParameter</code>(s),
     * such as <code>IValue</code> and <code>SharedMemory</code>. What's more, if one thread try to apply one <code>IOperation</code> interface
     * in its <code>openSession</code> or <code>InvokeCommand</code> function call while this <code>IOperation</code> interface is being used
     * by another thread, a BusyException will be threw. In addition, if wrapped resources within
     * one <code>IOperation</code> interface are modified by another thread, it is the responsibilities of the developers
     * to avoid such a situation to happen. In order to avoid mis-usage of <code>IOperation</code> interface,
     * the developers should not access any wrapped resources in one under-usage <code>IOperation</code> interface.
     * The state of the <code>IOperation</code> can be easily obtained by calling its <code>isStarted</code> function. So, it
     * is highly recommended for the developers to check the state of the <code>IOperation</code> interface before accessing it, such as
     * passing it into <code>openSession</code> or <code>invokeCommand</code>.
     */
    interface IOperation{
        /**
         * If one <code>IOperation</code> interface is used in an ongoing operation (either openSession or
         * invokeCommand) in a separate thread, this function will return true. Developers can
         * utilize this function to test the availability of the <code>IOperation</code> interface.
         * @return true if <code>IOperation</code> is under usage. Otherwise false if not being used.
         */
        boolean isStarted();
    }

    /**
     * Factory method to create an operation without parameter.
     * @return the <code>IOperation</code> interface for created operation.
     */
    IOperation newOperation();

    /**
     * Factory method to create an operation with one parameter.
     *
     * It is possible to create multiple <code>IOperation</code> interfaces using the same <code>IParameter</code>. But it
     * is not recommended especially when the I/O directory of <code>IParameter</code> is output for TA since it
     * is highly possible that such an <code>IParameter</code> is in an inconsistent state. This rule also
     * apply to other <code>ITEEClient.newOperation</code> overloaded functions which take <code>IParameter</code>(s) as input.
     * @param firstParam the first <code>IParameter</code>.
     * @return the <code>IOperation</code> interface for created operation.
     */
    IOperation newOperation(IParameter firstParam);

    /**
     * Factory method to create an operation with two parameters.
     * The order of input parameters should be aligned with the order of required parameters in TA.
     * This rule also apply to other overloaded <code>newOperation</code> factory function call which takes more than
     * two parameters.
     * @param firstParam the first <code>IParameter</code>.
     * @param secondParam the second <code>IParameter</code>.
     * @return the <code>IOperation</code> interface for created operation.
     */
    IOperation newOperation(IParameter firstParam, IParameter secondParam);

    /**
     * Factory method to create an operation with three parameters.
     * @param firstParam the first <code>IParameter</code>.
     * @param secondParam the second <code>IParameter</code>.
     * @param thirdParam the third <code>IParameter</code>.
     * @return the <code>IOperation</code> interface for created operation.
     */
    IOperation newOperation(IParameter firstParam, IParameter secondParam, IParameter thirdParam);

    /**
     * Factory method to create an operation with four parameters.
     * @param firstParam the first <code>IParameter</code>.
     * @param secondParam the second <code>IParameter</code>.
     * @param thirdParam the third <code>IParameter</code>.
     * @param forthParam the forth <code>IParameter</code>.
     * @return the <code>IOperation</code> interface for created Operation.
     */
    IOperation newOperation(IParameter firstParam, IParameter secondParam, IParameter thirdParam, IParameter forthParam);


    /**
     * <code>IParameter</code> interface is the parent of <code>IRegisteredMemoryReference</code> and <code>IValue</code> interfaces,
     * It can passed into the <code>ITEEClient.newOperation</code> to create an <code>IOperation</code> interface. It is possible
     * to share the <code>IParameter</code> interface between different threads. The developers should be ware of the race condition
     * when accessing the same <code>IParameter</code>. It is also their responsibilities to handle such a scenario.
     */
    interface IParameter{
        /**
         * The enum to indicates the type of the parameter.
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
         * Get the type of the <code>IParameter</code> interface.
         * @return an enum value Type which can be either TEEC_PTYPE_VAL or TEEC_PTYPE_RMR.
         */
        Type getType();
    }

    /**
     * Interface for registered memory reference. It can be only obtained by calling factory method
     * <code>ITEEClient.newRegisteredMemoryReference(...)</code>. If multiple <code>IRegisteredMemoryReference</code>
     * interfaces are referencing the same <code>ISharedMemory</code> interface,
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

        /**
         * Get the referenced registered shared memory.
         * @return <code>ISharedMemory</code> interface for the referenced registered shared memory.
         */
        ITEEClient.ISharedMemory getSharedMemory();

        int getOffset();
    }

    /**
     * Factory method to create a registered memory reference with a valid <code>ISharedMemory</code> interface and a
     * flag to indicate the I/O direction for the referred shared memory. The flag is only valid when
     * the corresponding shared memory also has such a flag. It will not override the flags which the shared memory already have.
     * @param sharedMemory the shared memory to refer.
     * @param flag the flag for referenced shared memory.
     * @param offset the offset from the beginning of the buffer of shared memory.
     */
    IRegisteredMemoryReference newRegisteredMemoryReference(ISharedMemory sharedMemory, IRegisteredMemoryReference.Flag flag, int offset);

    /**
     * Interface to access a pair of two integer values. It can be only obtained by calling
     * <code>ITEEClient.newValue</code> factory method.
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

        int getA();
        int getB();
    }

    /**
     * Factory method to create an interface of a pair of two integer values.
     * @param flag The I/O directory of <code>IValue</code> for TA.
     * @param a The first integer value.
     * @param b The second integer value.
     * @return an <code>IValue</code> interface.
     */
    IValue newValue(IValue.Flag flag, int a, int b);

    /**
     * In order for the CA to communicate with the TA within a TEE, a session must be opened between CA and TA.
     * To open a session, the CA must call <code>openSession</code> within a valid context. When a session is opened,
     * an <code>ISession</code> interface will be returned. It embraces all functions for CA to communicate with TA.
     * Within this session, the developer can call the <code>invokeCommand</code> function to invoke corresponding function within the TA.
     * When the session is no longer needed, the developers should close the session by calling
     * <code>closeSession</code> function.
     */
    interface ISession {

        /**
         * Sending a request to the connected Trusted Application with agreed commandId and parameters.
         * The parameters are encapsulated in the operation.
         *
         * @param commandId command identifier that is agreed with the Trusted Application. Based on the command id, CA can tell TA to perform a certain action. TA will know what to perform.
         * @param operation a wrapper of parameters for the action to invoke from CA. For the certain action to perform for TA, it is quite often that such an action comes with the parameters.
         * @throws exception.AccessConflictException:
         * using shared resources which are occupied by another thread;
         * @throws exception.BadFormatException:
         * providing incorrect format of parameters in operation;
         * @throws exception.BadParametersException:
         * providing parameters with invalid content;
         * @throws exception.BusyException:
         * 1. the TEE is busy working on something else and does not have the computation power to execute
         * requested operation;<br>
         * 2. the referenced <code>IOperation</code> interface is being used by another thread.
         * @throws exception.CancelErrorException:
         * the provided operation is invalid due to the cancellation from another thread;
         * @throws exception.CommunicationErrorException:
         * 1. fatal communication error in the remote TEE and TA side;<br>
         * 2. Communication with remote TEE service failed.
         * @throws exception.ExcessDataException:
         * providing too much parameters in the operation.
         * @throws exception.ExternalCancelException:
         * current operation cancelled by external signal in the CA, remote TEE or TA side.
         * @throws exception.GenericErrorException:
         * non-specific error.
         * @throws exception.ItemNotFoundException:
         * providing invalid reference to a registered shared memory.
         * @throws exception.NoDataException:
         * required data are missing in the operation.
         * @throws exception.NotImplementedException:
         * action mapped with this command id is not implemented in TA yet.
         * @throws fi.aalto.ssg.opentee.exception.NotSupportedException:
         * action mapped with this command id is not supported in TA.
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
        void invokeCommand(int commandId, IOperation operation) throws TEEClientException;

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
     * a shared memory. Then, the CA calls the <code>ITEEClient.registerSharedMemory </code>to register current
     * shared memory to the remote TEE so that the TA can also operate on it. When the CA
     * tries to register a shared memory, the I/O direction of this shared memory must be provided
     * along with the buffer to of the shared memory. The I/O direction is a bit mask of TEEC_MEM_INPUT
     * and TEEC_MEM_OUTPUT. Please note that the I/O direction of this shared memory is for the remote
     * TEE/TA. See the detailed explanation of these two flags in the field description. The size of
     * the shared memory is the same as the buffer that it holds. When the CA successfully register
     * this buffer as a shared memory with a flag of TEEC_MEM_INPUT, any modification on this buffer
     * will be synced to the TEE/TA during each function call from CA to TEE. Similarly, if the
     * shared memory is flagged with TEEC_MEM_OUTPUT, any modification of the shared memory from the
     * TEE side will be synced back to the CA after each remote function call from CA to TEE.
     * <p>
     * <code>ISharedMemory</code> interface provides operations on the shared memory.
     * It is only valid in a <code>IContext</code> interface. This interface can be only obtained
     * by calling <code>registerSharedMemory</code> function within a valid context. If the registered shared memory
     * is not longer needed, the developers should release it by calling <code>releaseSharedMemory</code>
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
         * @return the flags of <code>ISharedMemory</code>.
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
     * Return origin code enum indicates the origin when an exception is threw. It can be obtained
     * by calling <code>getReturnOrigin</code> of a caught exception. The developers can get a valid return origin
     * only when the exceptions are threw by these two functions: <code>openSession</code> and <code>invokeCommand</code>.
     * Otherwise, the return origin will be null.
     */
    enum ReturnOriginCode{
        /**
         * The exception is originated within the TEE Client API implementation.
         */
        TEEC_ORIGIN_API(0x00000001),
        /**
         * The exception is originated within the underlying communications stack linking:<br>
         * 1. the CA with remote TEE Proxy service;<br>
         * 2. the TEE Proxy service with the TEE.
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
     * <code>IContext</code> interface provides all the functions to interact with an initialized context in remote TEE.
     * This interface is returned by the <code>initializeContext</code> function call. When a context
     * is no longer needed, it should be closed by calling <code>finalizeContext</code>. When
     * the <code>IContext</code> interface is passed into different threads, the developers are responsible for
     * providing thread-safe mechanism to avoid the conflict between different threads.
     */
    interface IContext{
        /**
         * Connection Method enum with fixed value corresponding to GP specification when calling
         * <code>openSession</code>.
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
         * thread which initialized the context.
         *
         * @throws exception.CommunicationErrorException:
         * Communication with remote TEE service failed.
         */
        void finalizeContext() throws TEEClientException;

        /**
         * Registering a block of existing Client Application memory as a block of Shared Memory within
         * a valid TEE context. When this function tries to register a buffer as a shared memory which
         * is already used by another shared memory, this function will also return a valid <code>ISharedMemory</code> interface. The
         * TEE will regard this buffer as two identical shared memory. Under such a circumstance,
         * it can easily cause problems such as <code>AccessConflictException</code> etc. So, it is not recommended
         * to do so. However, when a shared memory is released, the buffer it holds can be registered
         * again as a new shared memory. For the CA, the buffer is the same but it is identical for
         * the TEE.
         * @param buffer indicates the reference of pre-allocated byte array which is to be shared.
         * @param flags indicates I/O direction of this shared memory for Trusted Application.
         * @throws exception.BadParametersException:
         * 1. try to register a null/empty buffer as a shared memory;<br>
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
         * Releasing the Shared Memory which is previously obtained using <code>registerSharedMemory</code>. As
         * stated in the description of <code>ISharedMemory</code> interface, when the shared memory is released, the TEE/TA will no longer
         * be able to read or write data to the shared memory. But the buffer that this shared memory
         * holds will still remain valid. When using the same shared memory within multi-threads, it
         * is recommended to release the shared memory in the same thread who registered it.
         * @param sharedMemory the reference the ISharedMemory instance.
         * @throws exception.CommunicationErrorException:
         * Communication with remote TEE service failed.
         * @throws exception.BadParametersException:
         * Incorrect ISharedMemory instance.
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
         * @return an <code>ISession</code> interface.
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
         * @throws exception.ExternalCancelException:
         * Cancelled by external interrupt.
         * @throws exception.GenericErrorException:
         * Non-specific cause.
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
                              Integer connectionData,
                              IOperation operation
                              ) throws TEEClientException;


        /**
         * Requesting the cancellation of a pending open session or a command invocation operation. It must be
         * called other than the thread which is using the <code>IOperation</code> interface. It is not
         * guaranteed that the operation can be cancelled for sure.
         * @param operation the started or pending operation instance.
         * @throws exception.CommunicationErrorException:
         * Communication with remote TEE service failed.
         */
        void requestCancellation(IOperation operation) throws TEEClientException;
    };
}