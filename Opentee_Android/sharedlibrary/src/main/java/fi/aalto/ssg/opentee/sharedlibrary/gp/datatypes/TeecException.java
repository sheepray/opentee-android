package fi.aalto.ssg.opentee.sharedlibrary.gp.datatypes;

/**
 *
 */
public class TeecException extends Exception {
    public TeecException() { super(); }
    public TeecException(String message) { super(message); }
    public TeecException(String message, Throwable cause) { super(message, cause); }
    public TeecException(Throwable cause) { super(cause); }
}
