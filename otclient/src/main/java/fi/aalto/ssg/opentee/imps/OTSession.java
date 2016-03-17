package fi.aalto.ssg.opentee.imps;

import fi.aalto.ssg.opentee.ITEEClient;

/**
 * This class implements the ISession interface.
 */
public class OTSession implements ITEEClient.ISession {
    //session identifier
    int mSessionId;
    ProxyApis mProxyApis = null;

    public OTSession(int sid, ProxyApis pa){
        this.mSessionId = sid;
        this.mProxyApis = pa;
    }

    public int getSessionId(){
        return this.mSessionId;
    }

    @Override
    public void teecInvokeCommand(int commandId, ITEEClient.Operation operation) throws ITEEClient.Exception {
        //TODO: remmebr to update the mReturnOriginCode field when return.
        //TODO: also remember the mReturnCode field.
    }

    @Override
    public void teecCloseSession() throws ITEEClient.Exception {

    }
}
