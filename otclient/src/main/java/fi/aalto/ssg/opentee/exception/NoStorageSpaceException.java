package fi.aalto.ssg.opentee.exception;

import fi.aalto.ssg.opentee.ITEEClient;

/**
 * Internal TEE error - documented for completeness. This exception can be threw by underlying library
 * when there is not storage space for the remote TEE/TA.
 */
public class NoStorageSpaceException extends TEEClientException {
    public NoStorageSpaceException(String msg){
        super(msg);
    }

    public NoStorageSpaceException(String msg, ITEEClient.ReturnOriginCode retOrigin){
        super(msg, retOrigin);
    }
}
