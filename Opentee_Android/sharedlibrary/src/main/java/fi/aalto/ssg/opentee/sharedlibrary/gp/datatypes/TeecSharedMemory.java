package fi.aalto.ssg.opentee.sharedlibrary.gp.datatypes;

/**
 * shared memory which has either been registered with the implementation or allocated by it. Its usage
 * depending on the
 */
public class TeecSharedMemory {
    byte[] mBuffer;
    int mFlag;

    static int TEEC_MEM_INPUT = 0x0000001;
    static int TEEC_MEM_OUTPUT = 0x00000002;


    /**
     *
     * @param flags should only be TEEC_MEM_INPUT, TEEC_MEM_OUTPUT or TEEC_MEM_INPUT | TEEC_MEM_OUTPUT
     */
    public TeecSharedMemory(int flags){}


    /**
     * get the flag of the shared memory
     * @return
     */
    public int getFlag(){return this.mFlag;}

    /**
     *
     * @throws TeecException error if not allowed to set buffer
     */
    public void setBuffer( byte[] buffer ) throws TeecException{}

    /**
     *
     * @return
     * @throws TeecException error if not allowed to get buffer
     */
    public byte[] getBuffer() throws TeecException{return mBuffer;} //not the real implementation

}
