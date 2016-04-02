package fi.aalto.ssg.opentee.imps;

import android.os.RemoteException;

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
    public void invokeCommand(int commandId, ITEEClient.Operation operation) throws ITEEClient.Exception {
        //TODO: remember to update the mReturnOriginCode field when return.
        //TODO: also remember the mReturnCode field.
    }

    @Override
    public void closeSession() throws ITEEClient.Exception, RemoteException {
        mProxyApis.teecCloseSession(mSessionId);
    }
}
