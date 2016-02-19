package fi.aalto.ssg.opentee.imps;

import android.content.Context;
import android.os.RemoteException;

import fi.aalto.ssg.opentee.ClientException;
import fi.aalto.ssg.opentee.ITEEClient;

/**
 * This class implements the ITEEClient interface.
 */
public class OTClient implements ITEEClient {
    @Override
    public IContext initializeContext(String teeName, Context context) throws ClientException, RemoteException {
        return new OTContext(teeName, context);
    }
}
