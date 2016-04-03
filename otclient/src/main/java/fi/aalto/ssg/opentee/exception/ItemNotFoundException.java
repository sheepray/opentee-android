package fi.aalto.ssg.opentee.exception;

import fi.aalto.ssg.opentee.ITEEClient;

/**
 * The requested data item is not found. This exception can be threw by underlying library when CA
 * tries to refer a shared memory which has already been released.
 */
public class ItemNotFoundException extends TEEClientException {
    public ItemNotFoundException(String msg){
        super(msg);
    }

    public ItemNotFoundException(String msg, ITEEClient.ReturnOriginCode retOrigin){
        super(msg, retOrigin);
    }
}
