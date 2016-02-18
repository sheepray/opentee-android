package fi.aalto.ssg.opentee.sharedlibrary.gp.apis;

import android.content.Context;
import android.os.RemoteException;

import fi.aalto.ssg.opentee.sharedlibrary.gp.datatypes.TeecConnectionException;
import fi.aalto.ssg.opentee.sharedlibrary.imp.OTContext;

/**
 * Function factory for TEEC
 */
public class Teec {
    /**
     * connect to remote TEE and initialize the context.
     * @param name the name of the TEE, null for default.
     * @param context application context.
     * @return a ITeecContext interface.
     * @throws TeecConnectionException exceptions from underlying API library.
     * @throws RemoteException indicates the standard Android remote exceptions when trying to
     * connect to remote TEE.
     */
    public static ITeecContext initializeContext(String name, Context context) throws TeecConnectionException, RemoteException {
        return new OTContext(name, context);
    }
}
