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


    /**
     * get method for Type.
     * @return TeecParameter.Type
     */
    @Override
    public Type getType() {
        return this.mType;
    }

    /**
     * get the reference to buffer.
     * @return a byte array reference.
     */
    public byte[] getBuffer(){return null;}
}
