package fi.aalto.ssg.opentee.sharedlibrary.gp.datatypes;

/**
 * Created by yangr1 on 2/11/16.
 */
public class TeecSession {
    private int mSession;

    public TeecSession(int session){}

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
                                  TeecOperation teecOperation) throws TeecException{}
}
