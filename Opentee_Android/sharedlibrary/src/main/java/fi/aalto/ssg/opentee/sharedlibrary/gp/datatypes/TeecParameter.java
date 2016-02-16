package fi.aalto.ssg.opentee.sharedlibrary.gp.datatypes;

/**
 * super class for TeecTempMemoryReference, TeecRegisteredMemoryReference and TeecValue
 */
abstract public class TeecParameter {
    public enum Type{
        //for TeecValue
        TEEC_VALUE_INPUT,
        TEEC_VALUE_OUTPUT,
        TEEC_VALUE_INOUT,

        //for TeecTempMemoryReference
        TEEC_MEMREF_TMP_INPUT,
        TEEC_MEMREF_TMP_OUTPUT,
        TEEC_MEMREF_TMP_INOUT,

        //for TeecRegisteredMemoryReference
        TEEC_MEMREF_WHOLE,
        TEEC_MEMREF_PARTIAL_INPUT,
        TEEC_MEMREF_PARTIAL_OUTPUT,
        TEEC_MEMREF_PARTIAL_INOUT
    }

    abstract public Type getType();
}
