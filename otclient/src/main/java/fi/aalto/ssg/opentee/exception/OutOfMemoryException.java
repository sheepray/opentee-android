package fi.aalto.ssg.opentee.exception;

import fi.aalto.ssg.opentee.ITEEClient;

/**
 * System ran out of resources. This exception can be threw by underlying library when the remote
 * service runs out of resources. Under such a circumstance, the developer is suggested to release
 * some unused resources or limit the number of calls to remote service.
 */
public class OutOfMemoryException extends TEEClientException {
    public OutOfMemoryException(String msg){
        super(msg);
    }

    public OutOfMemoryException(String msg, ITEEClient.ReturnOriginCode retOrigin){
        super(msg, retOrigin);
    }
}
