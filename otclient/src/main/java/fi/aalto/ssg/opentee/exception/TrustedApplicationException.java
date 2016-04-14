package fi.aalto.ssg.opentee.exception;

import fi.aalto.ssg.opentee.ITEEClient;

/**
 * Exception for error code customized by developer. This exception can be threw by underlying
 * library within openSession function call when an customized error code is returned. This error
 * code should differ itself from other return code defined by GP specification.
 */
public class TrustedApplicationException extends TEEClientException {
    int mErrorCode;
    public TrustedApplicationException(String msg, int errorCode){
        super(msg, ITEEClient.ReturnOriginCode.TEEC_ORIGIN_TA);
        this.mErrorCode = errorCode;
    }

    public TrustedApplicationException(String msg, ITEEClient.ReturnOriginCode retOrigin){
        super(msg, retOrigin);
    }

    public int getErrorCode(){
        return this.mErrorCode;
    }
}
