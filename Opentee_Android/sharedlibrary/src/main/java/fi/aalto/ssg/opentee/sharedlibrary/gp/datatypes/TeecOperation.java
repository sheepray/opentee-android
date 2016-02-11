package fi.aalto.ssg.opentee.sharedlibrary.gp.datatypes;

/**
 * this class defines the payload of either an open session or an invoke command operation
 */
public class TeecOperation {
    private int started = 0;
    private TeecParameter[] parmas;

    public TeecOperation(int started,
                         TeecParameter[] parmas){}
}
