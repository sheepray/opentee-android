package fi.aalto.ssg.opentee.sharedlibrary.gp.datatypes;

/**
 * this class defines the payload of either an open session or an invoke command operation.
 */
public class TeecOperation {
    private int started = 0;
    private TeecParameter[] params;

    /**
     * Public constructor
     * @param started initialized to 0 to indicates this TeecOperation can be cancelled in the future.
     * @param params carry the parameters for this TeecOperation.
     */
    public TeecOperation(int started,
                         TeecParameter[] params){}
}
