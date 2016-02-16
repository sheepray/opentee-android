package fi.aalto.ssg.opentee.sharedlibrary.gp.datatypes;

/**
 * this class defines a Temporary Memory Reference which is temporarily registered for data exchange
 * between Client Application and Trust Application
 */
public class TeecTempMemoryReference extends TeecParameter {
    private Type mType;
    private byte[] mBuffer;

    /**
     *
     * @param type only accept TEEC_MEMREF_TEMP_INPUT, TEEC_MEMREF_TEMP_OUTPUT and TEEC_MEMREF_TEMP_INOUT
     * @param buffer
     */
    public TeecTempMemoryReference(Type type, byte[] buffer){}


    @Override
    public Type getType() {
        return this.mType;
    }

    /**
     * set the content of the buffer if allowed based on the type
     * @param buffer
     * @throws TeecException unable to set buffer exception
     */
    public void setBuffer(byte[] buffer) throws TeecException{}

    public byte[] getBuffer(){return this.mBuffer;} //TODO: remove the place holder at the end of the function
}
