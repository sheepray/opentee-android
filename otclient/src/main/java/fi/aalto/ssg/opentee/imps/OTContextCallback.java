package fi.aalto.ssg.opentee.imps;

import android.os.RemoteException;

/**
 * Callback function for OTContext.
 */
interface OTContextCallback {
    void closeSession(int sid) throws RemoteException;
}
