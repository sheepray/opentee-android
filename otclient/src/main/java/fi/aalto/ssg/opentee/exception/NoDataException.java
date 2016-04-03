package fi.aalto.ssg.opentee.exception;

import fi.aalto.ssg.opentee.ITEEClient;

/**
 * Expected data was missing. This exception can be threw by underlying library when the CA does not
 * provide enough data for the remote TEE/TA. As a result, the corresponding operation will fail.
 */
public class NoDataException extends TEEClientException {
    public NoDataException(String msg){
        super(msg);
    }

    public NoDataException(String msg, ITEEClient.ReturnOriginCode retOrigin){
        super(msg, retOrigin);
    }
}
