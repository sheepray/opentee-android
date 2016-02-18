package fi.aalto.ssg.opentee.sharedlibrary.gp.apis;

import fi.aalto.ssg.opentee.sharedlibrary.gp.datatypes.TeecException;
import fi.aalto.ssg.opentee.sharedlibrary.gp.datatypes.TeecOperation;

/**
 * public interface for ITeecSession. OTSession implements ITeecSession interface.
 */
public interface ITeecSession {
    int mSession = -1;

    /**
     *
     * @param commandId command identifier that is agreed with the Trusted Application
     * @param teecOperation
     * @throws TeecException throws program error including:
     * 1. session not initialized;
     * 2. calling with invalid content in the teecOperation structure
     * 3. encoding Registered Memory Reference which refer to Shared Memory blocks allocated or
     * registered within the scope of a different TEE Context
     * 4. using the same operation structure concurrently for multiple operations
     */
    public void teecInvokeCommand(int commandId,
                                  TeecOperation teecOperation) throws TeecException;

    /**
     * close a session
     * @param teecSession the reference to the ITeecSession instance.
     * @throws TeecException throws program error includes calling with a session while still has
     * commands running, attempting to close the same Session concurrently from multiple threads and
     * attempting to close the same Session more than once.
     */
    public void teecCloseSession(ITeecSession teecSession)throws TeecException;
}
