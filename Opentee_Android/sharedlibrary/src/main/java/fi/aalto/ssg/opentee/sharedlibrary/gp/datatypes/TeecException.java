package fi.aalto.ssg.opentee.sharedlibrary.gp.datatypes;

/**
 * TeecException extends the Exception class. All exceptions in this project should subclass it
 * excluding exceptions defined by Android.
 */
public class TeecException extends Exception {
    public TeecException() { super(); }
    public TeecException(String message) { super(message); }
    public TeecException(String message, Throwable cause) { super(message, cause); }
    public TeecException(Throwable cause) { super(cause); }
}
