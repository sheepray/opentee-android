package fi.aalto.ssg.opentee.exception;

import fi.aalto.ssg.opentee.ITEEClient;

/**
 * The operation was cancelled. This exception can be threw by underlying library when the remote TEE
 * or TA tries to access one operation which has already been cancelled by another thread in Client
 * Application. This scenario can only happen when the following two constraints are satisfied:
 * 1. this operation must has a started field set to 0. If developer does not want one operation to be
 * cancelled, he/she can set this field to 1 to tell the TEE that this operation can be cancelled
 * before being invoked by TEE;
 * 2. this operation has not been invoked by the TEE yet before it has been cancelled by another thread
 * in Client Application who managed to call requestCancellation function and the remote TEE actually
 * cancelled it.
 */
public class CancelErrorException extends TEEClientException {
    public CancelErrorException(String msg){
        super(msg);
    }

    public CancelErrorException(String msg, ITEEClient.ReturnOriginCode retOrigin){
        super(msg, retOrigin);
    }
}
