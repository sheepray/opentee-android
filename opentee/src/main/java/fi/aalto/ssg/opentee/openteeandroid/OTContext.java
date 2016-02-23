package fi.aalto.ssg.opentee.openteeandroid;

/**
 * Java representation for TEEC_Context.
 * typedef struct {
 *  void *imp;
 * } TEEC_Context;
 */
public class OTContext {
    int mIndex;

    public OTContext(int index){
        this.mIndex = index;
    }

    public int getIndex(){return this.mIndex;}

    /**
     * OTSharedMemory children class
     */
    class OTSharedMemory{}

    /**
     * OTSession children class
     */
    class OTSession{}

    //TODO: more children classes shall be added based on implementation.
}
