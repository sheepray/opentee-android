package fi.aalto.ssg.opentee.imps;

import fi.aalto.ssg.opentee.ITEEClient;

/**
 * This class implements the ISession interface.
 */
public class OTSession implements ITEEClient.IContext.ISession {
    //session identifier
    int mSession;


    @Override
    public void teecInvokeCommand(int commandId, ITEEClient.Operation operation) throws ITEEClient.ClientException {

    }

    @Override
    public void teecCloseSession() throws ITEEClient.ClientException {

    }
}
