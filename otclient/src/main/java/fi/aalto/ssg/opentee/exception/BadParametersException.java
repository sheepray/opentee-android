package fi.aalto.ssg.opentee.exception;

import fi.aalto.ssg.opentee.ITEEClient;

/**
 * Input parameters were invalid. This exception can be threw from underlying library when the TEE or
 * TA get a parameter(s) which is not expected as a valid value(s).
 */
public class BadParametersException extends TEEClientException {
    public BadParametersException(String msg){
        super(msg);
    }

    public BadParametersException(String msg, ITEEClient.ReturnOriginCode retOrigin){
        super(msg, retOrigin);
    }
}
