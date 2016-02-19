package fi.aalto.ssg.opentee;

/**
 * ClientException extends the java.lang.ClientException class. All exceptions in this project should subclass it
 * excluding exceptions defined by Android.
 */
public class ClientException extends java.lang.Exception {
    public ClientException() { super(); }
    public ClientException(String message) { super(message); }
    public ClientException(String message, Throwable cause) { super(message, cause); }
    public ClientException(Throwable cause) { super(cause); }
}
