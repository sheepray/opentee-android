package fi.aalto.ssg.opentee.exception;

import fi.aalto.ssg.opentee.ITEEClient;

/**
 * Non-specific cause exception. This exception can be threw by underlying library when there is an
 * error(s) excluding the errors already defined when CA is interacting with TEE.
 */
public class GenericErrorException extends  TEEClientException{
    public GenericErrorException(String msg){
        super(msg);
    }

    public GenericErrorException(String msg, ITEEClient.ReturnOriginCode retOrigin){
        super(msg, retOrigin);
    }
}
