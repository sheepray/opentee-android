package fi.aalto.ssg.opentee.sharedlibrary.gp.datatypes;

/**
 * this class indicates where in the software stack the return code was generated for either an
 * openSession or an invokeCommand operation
 */
public class TeecReturnCodeOrigin {
    static enum origin{
        TEEC_ORIGIN_API,
        TEEC_ORIGIN_COMMS,
        TEEC_ORIGIN_TEE,
        TEEC_ORIGIN_TRUSTED_APP
    }
}
