package fi.aalto.ssg.opentee.sharedlibrary.gp.datatypes;

import fi.aalto.ssg.opentee.sharedlibrary.gp.apis.ITeecSharedMemory;

/**
 * a reference to pre-registered or allocated memory
 */
public class TeecRegisteredMemoryReference extends TeecParameter {
    Type mType;
    ITeecSharedMemory mParent;
    int mOffset;

    /**
     *
     * @param type only accept TEEC_MEMREF_WHOLE, TEEC_MEMREF_PARTIAL_INPUT, TEEC_MEMREF_PARTIAL_OUTPUT
     *             and TEEC_MEMREF_PARTIAL_INOUT.
     * @param parent must not be null and should refer to already registered TeecSharedMemory instance.
     * @param offset the beginning address of TeecSharedMemory instance. It is used to indicates which
     *               part of the TeecSharedMemory should be used.
     */
    public TeecRegisteredMemoryReference(Type type,
                                         ITeecSharedMemory parent,
                                         int offset){}

    /**
     * get method for Type.
     * @return TeecParameter.Type
     */
    @Override
    public Type getType() {
        return this.mType;
    }
}
