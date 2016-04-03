package fi.aalto.ssg.opentee.exception;

import fi.aalto.ssg.opentee.ITEEClient;

/**
 * The requested operation is valid but is not supported in this implementation. This exception can
 * be threw by underlying library when the CA tries to invoke a valid operation which does not exists
 * in current implementation of TA. This exception can be used to notify the CA that it talks to an older
 * version of TA which does not support such an operation. So, this exception can help the CA to be
 * backward compatible.
 */
public class NotSupportedException extends TEEClientException {
    public NotSupportedException(String msg){
        super(msg);
    }

    public NotSupportedException(String msg, ITEEClient.ReturnOriginCode retOrigin){
        super(msg, retOrigin);
    }
}
