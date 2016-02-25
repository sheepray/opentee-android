package fi.aalto.ssg.opentee.openteeandroid;

/**
 * Caller resource hierarchy.
 */
public class OTCaller {
    int mID;

    public OTCaller(int id){this.mID = id;}

    public int getID(){return this.mID;}

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
