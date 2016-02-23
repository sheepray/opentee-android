package fi.aalto.ssg.opentee.openteeandroid;

/**
 * Java representation for TEEC_Context.
 * typedef struct {
 *  void *imp;
 * } TEEC_Context;
 */
public class OTContext {
    byte[] imp;

    public OTContext(byte[] imp){
        this.imp = imp;
    }

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
