package fi.aalto.ssg.opentee.sharedlibrary.gp.datatypes;

/**
 * Universally Unique Resource Identifier which can be used to identify the trusted application (TA)
 */
public class TeecUuid {
    public int timeLow;
    public short timeMid;
    public short timeHighAndVersion;
    public byte[] clockSeqAndNode;

    /**
     *
     * @param timeLow
     * @param timeMid
     * @param timeHighAndVersion
     * @param clockSeqAndNode
     */
    public TeecUuid(int timeLow,
                    short timeMid,
                    short timeHighAndVersion,
                    byte[] clockSeqAndNode){}
}
