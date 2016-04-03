package fi.aalto.ssg.opentee.exception;

import fi.aalto.ssg.opentee.ITEEClient;

/**
 * Operation is not valid in the current state. This exception can be threw by underlying library
 * when the Client Application tries to initialize context when the TEE is not ready to do so.
 */
public class BadStateException extends TEEClientException {
    public BadStateException(String msg){
        super(msg);
    }

    public BadStateException(String msg, ITEEClient.ReturnOriginCode retOrigin){
        super(msg, retOrigin);
    }
}
