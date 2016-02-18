package fi.aalto.ssg.opentee.sharedlibrary.gp.datatypes;

/**
 * return code for ITeecContext interface and Teec.initializeContext.
 */
public class TeecResult {
    public static final int TEEC_SUCCESS = 0x00000000;

    public static final int TEEC_ERROR_GENERIC = 0xffff0000;
    public static final int TEEC_ERROR_ACESS_DENIED = 0xffff0001;
    public static final int TEEC_ERROR_CANCEL = 0xffff0002;
    public static final int TEEC_ERROR_ACCESS_CONFLICT = 0xffff0003;
    public static final int TEEC_ERROR_EXCESS_DATA = 0xffff0004;
    public static final int TEEC_ERROR_BAD_FORMAT = 0xffff0005;
    public static final int TEEC_ERROR_BAD_PARAMETERS = 0xffff0006;
    public static final int TEEC_ERROR_BAD_STATE = 0xffff0007;
    public static final int TEEC_ERROR_ITEM_NOT_FOUND = 0xffff0008;
    public static final int TEEC_ERROR_NOT_IMPLEMENTED = 0xffff0009;
    public static final int TEEC_ERROR_NOT_SUPPORTED = 0xffff000a;
    public static final int TEEC_ERROR_NO_DATA = 0xffff000b;
    public static final int TEEC_ERROR_OUT_OF_MEMORY = 0xffff000c;
    public static final int TEEC_ERROR_BUSY = 0xffff000d;
    public static final int TEEC_ERROR_COMMUNICATION = 0xffff000e;
    public static final int TEEC_ERROR_SECURITY = 0xffff000f;
    public static final int TEEC_ERROR_SHORT_BUFFER = 0xffff0010;
}
