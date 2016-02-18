package fi.aalto.ssg.opentee.sharedlibrary.gp.datatypes;

/**
 * this class defines the session login method that the Client Application uses to open a session to
 * a TEE within an existed context. For detailed definition, please refer it in Global Platform TEE
 * Client API Specification Version 1.0.
 */
public class TeecConnectionMethod {
    public final int TEEC_LOGIN_PUBLIC = 0x00000000;
    public final int TEEC_LOGIN_USER = 0x00000001;
    public final int TEEC_LOGIN_GROUP = 0x00000002;
    public final int TEEC_LOGIN_APPLICATION = 0x00000004;
    public final int TEEC_LOGIN_USER_APPLICATION = 0x00000005;
    public final int TEEC_LOGIN_GROUP_APPLICATION = 0x00000006;
}
