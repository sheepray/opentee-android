package fi.aalto.ssg.opentee.sharedlibrary.gp.apis;

import android.content.Context;
import android.os.RemoteException;

import fi.aalto.ssg.opentee.sharedlibrary.gp.datatypes.TeecConnectionException;
import fi.aalto.ssg.opentee.sharedlibrary.imp.OTContext;

/**
 * Function factory for TEEC
 */
public class Teec {
    public static TeecContext initializeContext(String name, Context context) throws TeecConnectionException, RemoteException {
        return new OTContext(name, context);
    }
}
