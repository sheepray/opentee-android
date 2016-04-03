package fi.aalto.ssg.opentee.exception;

import fi.aalto.ssg.opentee.ITEEClient;

/**
 * The supplied buffer is too short for the generated output. This exception can be threw by underlying
 * library when the TA tries to copy the result to a shorter output buffer which is previously given
 * by CA. This scenario can happen when the provided Shared Memory or Value for output is too short.
 * Under such a circumstance, the developers are suggested to allocate a bigger buffer for output. If
 * the Value is not bigger enough for the output, the Shared Memory should be used instead.
 */
public class ShortBufferException extends TEEClientException {
    public ShortBufferException(String msg){
        super(msg);
    }

    public ShortBufferException(String msg, ITEEClient.ReturnOriginCode retOrigin){
        super(msg, retOrigin);
    }
}
