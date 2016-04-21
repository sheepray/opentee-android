package fi.aalto.ssg.opentee.imps;

import android.os.RemoteException;

import fi.aalto.ssg.opentee.ITEEClient;
import fi.aalto.ssg.opentee.exception.CommunicationErrorException;
import fi.aalto.ssg.opentee.exception.TEEClientException;

/**
 * This class implements the ISession interface.
 */
public class OTSession implements ITEEClient.ISession {
    //session identifier
    int mSessionId;
    OTContextCallback mContextCallback = null;

    public OTSession(int sid, OTContextCallback contextCallback){
        this.mSessionId = sid;
        this.mContextCallback = contextCallback;
    }

    public int getSessionId(){
        return this.mSessionId;
    }

    @Override
    public void invokeCommand(int commandId, ITEEClient.IOperation operation) throws TEEClientException {
        //TODO: remember to update the mReturnOriginCode field when return.
        //TODO: also remember the mReturnCode field.
    }

    @Override
    public void closeSession() throws TEEClientException {
        try {
            this.mContextCallback.closeSession(mSessionId);
        } catch (RemoteException e) {
            throw new CommunicationErrorException(e.getMessage(), ITEEClient.ReturnOriginCode.TEEC_ORIGIN_API);
        }
    }
}
