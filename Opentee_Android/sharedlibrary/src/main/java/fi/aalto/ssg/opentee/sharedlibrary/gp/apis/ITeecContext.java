package fi.aalto.ssg.opentee.sharedlibrary.gp.apis;


import fi.aalto.ssg.opentee.sharedlibrary.gp.datatypes.TeecConnectionMethod;
import fi.aalto.ssg.opentee.sharedlibrary.gp.datatypes.TeecException;
import fi.aalto.ssg.opentee.sharedlibrary.gp.datatypes.TeecOperation;
import fi.aalto.ssg.opentee.sharedlibrary.gp.datatypes.TeecUuid;

/**
 * this interface lists all the actions which can be taken by Client Application once successfully
 * connected to one particular TEE using Teec factory method initializedContext.
 */
public interface ITeecContext {

    /**
     * Finalize the context and close the connection to TEE after all sessions have been terminated
     * and all shared memory has been released
     */
    public void teecFinalizeContext();

    /**
     * Finalize the context and close the connection to TEE immediately
     */
    public void teecFinalizeContextNow();

    /**
     * register a block of existing Client Application memory as a block of Shared Memory within the
     * scope of the specified TEE context
     * @param buffer indicates the reference of pre-allocated byte array which is to be shared.
     * @param flags indicates I/O direction of this shared memory. Its value can only be TEEC_MEM_INPUT and
     *              TEEC_MEM_OUPUT.
     * @throws TeecException exception message can be the return code in TeecResult or program
     * error such as context not initialized, sharedMemory not correctly populated or trying to
     * initialize the same shared memory structure concurrently from multiple threads
     */
    public ITeecSharedMemory teecRegisterSharedMemory(byte[] buffer, ITeecSharedMemory.flag flags) throws TeecException;

    /**
     * allocate a new block of memory as a block of Shared Memory within the scope of the specified
     * TEE Context
     * @throws TeecException TeecException exception message can be the return code in TeecResult or
     * program error such as context not initialized, sharedMemory not correctly populated or trying
     * to initialize the same shared memory structure concurrently from multiple threads
     */
    //public ITeecSharedMemory teecAllocateSharedMemory() throws TeecException;

    /**
     * release the Shared Memory which previously obtained using teecRegisterSharedMemory or
     * teecAllocateSharedMemory.
     * @param sharedMemory the reference the ITeecSharedMemory instance.
     * @throws TeecException program error exceptions including attempting to release Shared Memory
     * which is used by a pending operation or
     * attempting to relaes the same Shared Memory structure concureently from multiple threads.
     */
    public void teecReleaseSharedMemory(ITeecSharedMemory sharedMemory) throws TeecException;

    /**
     * this API opens a session within the context which is already built.
     * @param uuid UUID for Trusted Application.
     * @param connectionMethod the method of connection to use.
     * @param connectionData any necessary data for connectionMethod.
     * @param teecOperation operations to perform.
     * @return an ITeecSession instance.
     * @throws TeecException
     */
    public ITeecSession teecOpenSession (final TeecUuid uuid,
                                        TeecConnectionMethod connectionMethod,
                                        Integer connectionData,
                                        TeecOperation teecOperation) throws TeecException;

    /**
     * this method requests the cancellation of a pending open Session operation or a Command invocation operation
     * in a seperate thread.
     * @param teecOperation the reference to the TeecOperation instance.
     */
    public void teecRequestCancellation(TeecOperation teecOperation);

}
