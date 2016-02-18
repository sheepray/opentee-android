package fi.aalto.ssg.opentee.sharedlibrary.gp.datatypes;

/**
 * Connection exceptions subclass the TeecException. It indicates the exceptions with the connection
 * to the remote TEE excluding the exceptions defined in RemoteException.
 */
public class TeecConnectionException extends TeecException {
    public TeecConnectionException(String msg){
        super(msg);
    }
}
