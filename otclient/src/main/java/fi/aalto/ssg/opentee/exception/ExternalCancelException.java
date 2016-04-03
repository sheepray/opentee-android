package fi.aalto.ssg.opentee.exception;

import fi.aalto.ssg.opentee.ITEEClient;

/**
 * An external event has caused a User Interface operation to be aborted, which is defined by the
 * Trusted User Interface specification.
 */
public class ExternalCancelException extends TEEClientException {
    public ExternalCancelException(String msg){
        super(msg);
    }

    public ExternalCancelException(String msg, ITEEClient.ReturnOriginCode retOrigin){
        super(msg, retOrigin);
    }
}
