package fi.aalto.ssg.opentee.exception;

import fi.aalto.ssg.opentee.ITEEClient;

/**
 * Input data was of invalid format. This exception can be threw by underlying library when the Client
 * Application gives an input data with a wrong format in a sense that either the remote TEE or TA
 * can not parse it correctly.
 */
public class BadFormatException extends TEEClientException {
    public BadFormatException(String msg){
        super(msg);
    }

    public BadFormatException(String msg, ITEEClient.ReturnOriginCode retOrigin){
        super(msg, retOrigin);
    }
}
