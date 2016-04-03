package fi.aalto.ssg.opentee.exception;

import fi.aalto.ssg.opentee.ITEEClient;

/**
 * The Trusted Application has terminated.
 */
public class TargetDeadException extends TEEClientException {
    public TargetDeadException(String msg){
        super(msg);
    }

    public TargetDeadException(String msg, ITEEClient.ReturnOriginCode retOrigin){
        super(msg, retOrigin);
    }
}
