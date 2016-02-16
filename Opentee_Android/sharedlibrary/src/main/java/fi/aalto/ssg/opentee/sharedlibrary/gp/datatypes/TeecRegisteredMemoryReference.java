package fi.aalto.ssg.opentee.sharedlibrary.gp.datatypes;

/**
 * a reference to pre-registered or allocated memory
 */
public class TeecRegisteredMemoryReference extends TeecParameter {
    Type mType;
    TeecSharedMemory mParent;
    int mOffset;

    /**
     *
     * @param type only accept TEEC_MEMREF_WHOLE, TEEC_MEMREF_PARTIAL_INPUT, TEEC_MEMREF_PARTIAL_OUTPUT and TEEC_MEMREF_PARTIAL_INOUT
     * @param parent must not be null
     * @param offset
     */
    public TeecRegisteredMemoryReference(Type type,
                                         TeecSharedMemory parent,
                                         int offset){}

    @Override
    public Type getType() {
        return this.mType;
    }
}
