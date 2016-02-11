package fi.aalto.ssg.opentee.sharedlibrary.gp.datatypes;

/**
 * Created by yangr1 on 2/11/16.
 */
public class TeecParameterTypes {
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

    public TeecParameterTypes(Type type){}
    public Type getType(){Type type = null;return type;}
    //TODO: remove the place holder at the end of the function
}
