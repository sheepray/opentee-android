package fi.aalto.ssg.opentee.exception;

import fi.aalto.ssg.opentee.ITEEClient;

/**
 * Internal TEE error - Documented for completeness.
 */
public class MacInvalidException extends TEEClientException {
    public MacInvalidException(String msg){ super(msg);}

    public MacInvalidException(String msg, ITEEClient.ReturnOriginCode retOrigin){
        super(msg, retOrigin);
    }
}
