package fi.aalto.ssg.opentee.exception;

import fi.aalto.ssg.opentee.ITEEClient;

/**
 * Internal TEE error - Documented for completeness.
 */
public class SignatureInvalidException extends TEEClientException {
    public SignatureInvalidException(String msg){ super(msg);}

    public SignatureInvalidException(String msg, ITEEClient.ReturnOriginCode retOrigin){
        super(msg, retOrigin);
    }
}
