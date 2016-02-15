package fi.aalto.ssg.opentee.sharedlibrary.gp.datatypes;


import android.os.Parcel;
import android.os.Parcelable;

/**
 * the main logical container linking at Client Application with a particular TEE
 */
public class TeecContext implements Parcelable{
    private int mContext;

    public TeecContext(int context){this.mContext = context;}


    /**
     * creating a connection to a TEE
     * @param name the name of the TEE. Connection will be made to default TEE if provided with null
     * @throws TeecException is the error message from the TEE side
     */
    public void teecInitializeContext(String name) throws TeecException{}

    /**
     * Finalize the context and close the connection to TEE after all sessions have been terminated
     * and all shared memory has been released
     */
    public void teecFinalizeContext(){}

    /**
     * Finalize the context and close the connection to TEE immediately
     */
    public void teecFinalizeContextNow(){}

    /**
     * register a block of existing Client Application memory as a block of Shared Memory within the
     * scope of the specified TEE context
     * @param sharedMemory
     * @throws TeecException exception message can be the return code in TeecResult or program
     * error such as context not initialized, sharedMemory not correctly populated or trying to
     * initialize the same shared memory structure concurrently from multiple threads
     */
    public void teecRegisterSharedMemory(TeecSharedMemory sharedMemory) throws TeecException{}

    /**
     * allocate a new block of memory as a block of Shared Memory within the scope of the specified
     * TEE Context
     * @param sharedMemory
     * @throws TeecException TeecException exception message can be the return code in TeecResult or
     * program error such as context not initialized, sharedMemory not correctly populated or trying
     * to initialize the same shared memory structure concurrently from multiple threads
     */
    public void teecAllocateSharedMemory(TeecSharedMemory sharedMemory) throws TeecException{}

    /**
     * release the Shared Memory which previously obtained using teecRegisterSharedMemory or
     * teecAllocateSharedMemory
     * @param sharedMemory
     * @throws TeecException program error exceptions including attempting to release Shared Memory
     * which is used by a pending operation or
     * attempting to relaes the same Shared Memory structure concureently from multiple threads
     */
    public void teecReleaseSharedMemory(TeecSharedMemory sharedMemory) throws TeecException{}

    /**
     *
     * @param uuid UUID for Trusted Application
     * @param connectionMethod the method of connection to use
     * @param connectionData any necessary data for connectionMethod
     * @param teecOperation operations to perform
     * @return an TeecSession instance
     * @throws TeecException
     */
    public TeecSession teecOpenSession (final TeecUuid uuid,
                                        TeecConnectionMethod connectionMethod,
                                        int connectionData,
                                        TeecOperation teecOperation) throws TeecException{return new TeecSession(-1);}
                                        //TODO: remove the dummy place holder at the end of function

    /**
     * close a session
     * @param teecSession
     * @throws TeecException throws program error includes calling with a session while still has
     * commands running, attempting to close the same Session concurrently from multiple threads and
     * attempting to close the same Session more than once
     */
    public void teecCloseSession(TeecSession teecSession)throws TeecException{}

    /**
     * request the cancellation of a pending open Session operation or a Command invocation operation
     * in a seperate thread
     * @param teecOperation
     */
    public void teecRequestCancellation(TeecOperation teecOperation){}

    @Override
    public int describeContents() {
        return 0;
    }

    private TeecContext(Parcel in){
        readFromParcel(in);
    }

    public void readFromParcel(Parcel in){
        this.mContext = in.readInt();
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {dest.writeInt(this.mContext);}

    public static final Parcelable.Creator<TeecContext> CREATOR = new
            Parcelable.Creator<TeecContext>(){
                @Override
                public TeecContext createFromParcel(Parcel in){
                    return new TeecContext(in);
                }

                @Override
                public TeecContext[] newArray(int size) {
                    return new TeecContext[size];
                }


            };
}
