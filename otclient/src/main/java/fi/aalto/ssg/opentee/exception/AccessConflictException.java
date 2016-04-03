package fi.aalto.ssg.opentee.exception;

import fi.aalto.ssg.opentee.ITEEClient;

/**
 * Concurrent accesses caused conflict. This exception can be threw by underlying library when one
 * thread of the Client Application tries to access shared resources,
 * such as ISharedMemory and ISession, while the same resource is held by another thread.
 */
public class AccessConflictException extends TEEClientException{
    public AccessConflictException(String msg){ super(msg);}

    public AccessConflictException(String msg, ITEEClient.ReturnOriginCode retOrigin){
        super(msg, retOrigin);
    }
}
