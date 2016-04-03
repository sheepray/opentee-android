package fi.aalto.ssg.opentee.exception;

import fi.aalto.ssg.opentee.ITEEClient;

/**
 * Too much data for the requested operation was passed. This exception can be threw by under lying
 * library when the CA provides unexpected amount of data to TEE.
 */
public class ExcessDataException extends TEEClientException{
    public ExcessDataException(String msg){
        super(msg);
    }

    public ExcessDataException(String msg, ITEEClient.ReturnOriginCode retOrigin){
        super(msg, retOrigin);
    }
}
