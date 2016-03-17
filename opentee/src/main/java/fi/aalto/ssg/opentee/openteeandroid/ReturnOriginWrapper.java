package fi.aalto.ssg.opentee.openteeandroid;

/**
 * This class only contains one integer which is used to provide convinence when return the return
 * origin from jni layer to Java layer.
 */
public class ReturnOriginWrapper {
    int mReturnOrigin;

    public ReturnOriginWrapper(int ro){
        this.mReturnOrigin = ro;
    }
}
