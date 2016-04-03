package fi.aalto.ssg.opentee.exception;

import fi.aalto.ssg.opentee.ITEEClient;

/**
 * This exception can be threw by underlying library when the system is busy working on something
 * and will not accept any incoming operation requests.
 */
public class BusyException extends TEEClientException {
    public BusyException(String msg){
        super(msg);
    }

    public BusyException(String msg, ITEEClient.ReturnOriginCode retOrigin){
        super(msg, retOrigin);
    }
}
