package fi.aalto.ssg.opentee.exception;

import fi.aalto.ssg.opentee.ITEEClient;

/**
 * Internal TEE error - documented for completeness. This exception can be threw when there is a
 * buffer overflow error(s) in the remote TEE.
 */
public class OverflowException extends TEEClientException {
    public OverflowException(String msg){
        super(msg);
    }

    public OverflowException(String msg, ITEEClient.ReturnOriginCode retOrigin){
        super(msg, retOrigin);
    }
}
