package fi.aalto.ssg.opentee.exception;

import fi.aalto.ssg.opentee.ITEEClient;

/**
 * Internal TEE error - Documented for completeness
 */
public class TimeNotSetException extends TEEClientException {
    public TimeNotSetException(String msg){ super(msg);}

    public TimeNotSetException(String msg, ITEEClient.ReturnOriginCode retOrigin){
        super(msg, retOrigin);
    }
}
