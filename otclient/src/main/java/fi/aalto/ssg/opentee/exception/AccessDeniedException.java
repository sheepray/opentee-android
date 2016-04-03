package fi.aalto.ssg.opentee.exception;

import fi.aalto.ssg.opentee.ITEEClient;

/**
 * Access privileges are not sufficient. This exception can be threw from underlying library when
 * the Client Application has insufficient and/or incorrect authentication data to prove its identity
 * when try to connect to a remote TEE or a Trusted Application.
 */
public class AccessDeniedException extends TEEClientException {
    public AccessDeniedException(String msg){
        super(msg);
    }

    public AccessDeniedException(String msg, ITEEClient.ReturnOriginCode retOrigin){
        super(msg, retOrigin);
    }
}
