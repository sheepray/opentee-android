package fi.aalto.ssg.opentee.exception;

import fi.aalto.ssg.opentee.ITEEClient;

/**
 * The requested operation should exist but is not yet implemented. This exception can threw by
 * underlying library when the CA invokes an operation which has not been implemented in TA yet. TA
 * can use this exception to notify the CA that the function it invoked is not ready right now but might
 * be available in the future.
 */
public class NotImplementedException extends TEEClientException{
    public NotImplementedException(String msg){
        super(msg);
    }

    public NotImplementedException(String msg, ITEEClient.ReturnOriginCode retOrigin){
        super(msg, retOrigin);
    }
}
