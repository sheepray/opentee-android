package fi.aalto.ssg.opentee.imps;

/**
 * Wrapper class for return code and return origin.
 */
public class ReturnValueWrapper {
    int mReturnCode;
    int mReturnOrigin;

    public ReturnValueWrapper(int rc, int ro){
        this.mReturnCode = rc;
        this.mReturnOrigin = ro;
    }

    public int getReturnCode(){return this.mReturnCode;}

    public int getReturnOrigin(){return this.mReturnOrigin;}
}
