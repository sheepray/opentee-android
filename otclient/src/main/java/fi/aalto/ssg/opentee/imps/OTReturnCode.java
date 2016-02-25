package fi.aalto.ssg.opentee.imps;

/**
 * This class defines all the value of return value which should be agreed with the underlying libraries
 * of Client Application and the remote OT service. The remote OT service pass the value back to
 * underlying libraries of Client Application. Then the underlying libraries can determine how to
 * notify the developers in the upper layer such as throwing exceptions defined in ITEEClient interface.
 */
public class OTReturnCode {
    public static int TEEC_SUCCESS = 0x00000000;
    public static int TEEC_ERROR_GENERIC = 0xFFFF0000;
    public static int TEEC_ERROR_ACCESS_DENIED = 0xFFFF0001;
    public static int TEEC_ERROR_CANCEL = 0xFFFF0002;
    public static int TEEC_ERROR_ACCESS_CONFLICT = 0xFFFF0003;
    public static int TEEC_ERROR_EXCESS_DATA = 0xFFFF0004;
    public static int TEEC_ERROR_BAD_FORMAT = 0xFFFF0005;
    public static int TEEC_ERROR_BAD_PARAMETERS = 0xFFFF0006;
    public static int TEEC_ERROR_BAD_STATE = 0xFFFF0007;
    public static int TEEC_ERROR_ITEM_NOT_FOUND = 0xFFFF0008;
    public static int TEEC_ERROR_NOT_IMPLEMENTED = 0xFFFF0009;
    public static int TEEC_ERROR_NOT_SUPPORTED = 0xFFFF000A;
    public static int TEEC_ERROR_NO_DATA = 0xFFFF000B;
    public static int TEEC_ERROR_OUT_OF_MEMORY = 0xFFFF000C;
    public static int TEEC_ERROR_BUSY = 0xFFFF000D;
    public static int TEEC_ERROR_COMMUNICATION = 0xFFFF000E;
    public static int TEEC_ERROR_SECURITY = 0xFFFF000F;
    public static int TEEC_ERROR_SHORT_BUFFER = 0xFFFF0010;

}
