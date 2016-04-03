package fi.aalto.ssg.opentee.exception;

import fi.aalto.ssg.opentee.ITEEClient;

/**
 * A security fault was detected. This exception can be threw by underlying library when the CA
 * tries to access a number of resources in a wrong way. For instance, if the shared memory only
 * marked with input for TA, the CA should not require the TA to use the shared memory as an output.
 */
public class SecurityErrorException extends TEEClientException {
    public SecurityErrorException(String msg){
        super(msg);
    }

    public SecurityErrorException(String msg, ITEEClient.ReturnOriginCode retOrigin){
        super(msg, retOrigin);
    }
}
