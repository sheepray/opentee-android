package fi.aalto.ssg.opentee.testapp;

import fi.aalto.ssg.opentee.ITEEClient;

/**
 * Created by yangr1 on 5/2/16.
 */
public interface WorkerCallback {
    void updateClient(ITEEClient client);

    void updateContext(ITEEClient.IContext ctx);

    void updateSession(ITEEClient.ISession ses);

    void updateRootKey(byte[] newKey);
}
