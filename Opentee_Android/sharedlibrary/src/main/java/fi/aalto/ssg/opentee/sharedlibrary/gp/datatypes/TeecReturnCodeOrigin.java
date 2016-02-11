package fi.aalto.ssg.opentee.sharedlibrary.gp.datatypes;

/**
 * this class indicates whre in the software stack the return code was generated for either an
 * openSession or an invokeCommand operation
 */
public class TeecReturnCodeOrigin {
    public final int TEEC_ORIGIN_API = 0x00000001;
    public final int TEEC_ORIGIN_COMMS = 0x00000002;
    public final int TEEC_ORIGIN_TEE = 0x00000003;
    public final int TEEC_ORIGIN_TRUSTED_APP = 0x00000004;
}
