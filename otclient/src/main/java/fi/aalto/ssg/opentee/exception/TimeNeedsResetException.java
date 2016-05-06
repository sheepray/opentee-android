package fi.aalto.ssg.opentee.exception;

import fi.aalto.ssg.opentee.ITEEClient;

/**
 * Internal TEE error - Documented for completeness.
 */
public class TimeNeedsResetException extends TEEClientException {
    public TimeNeedsResetException(String msg){ super(msg);}

    public TimeNeedsResetException(String msg, ITEEClient.ReturnOriginCode retOrigin){
        super(msg, retOrigin);
    }
}
