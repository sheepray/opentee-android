package fi.aalto.ssg.opentee.sharedlibrary.gp.datatypes;

/**
 * Created by yangr1 on 2/11/16.
 */
public class TeecSharedMemory {
    private byte[] mBuffer;

    public enum Flags{
        TEEC_MEM_INPUT,
        TEEC_MEM_OUTPUT;
    }

    public TeecSharedMemory(TeecSharedMemory.Flags flags){}

    /**
     *
     * @throws TeecException
     */
    public void setBuffer( byte[] buffer ) throws TeecException{}

    /**
     *
     * @return
     * @throws TeecException
     */
    public byte[] getBuffer() throws TeecException{return mBuffer;} //not the real implementation

}
