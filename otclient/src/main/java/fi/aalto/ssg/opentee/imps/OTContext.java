package fi.aalto.ssg.opentee.imps;

import android.content.Context;
import android.os.RemoteException;

import java.util.UUID;

import fi.aalto.ssg.opentee.ITEEClient;

/**
 * This class implements the IContext interface
 */
public class OTContext implements ITEEClient.IContext {
    String mTeeName;
    boolean mInitialized;
    ProxyApis mProxyApis = null; // one service connection per context

    public OTContext(String teeName, Context context) throws ITEEClient.ClientException, RemoteException {
        this.mTeeName = teeName;

        //connect to the OpenTEE
        mProxyApis = new ProxyApis(teeName, context);

        this.mInitialized = true;
    }


    @Override
    public void finalizeContext() {

    }

    @Override
    public ISharedMemory registerSharedMemory(byte[] buffer, ISharedMemory.Flag flags) throws ITEEClient.ClientException {
        return null;
    }

    @Override
    public void releaseSharedMemory(ISharedMemory sharedMemory) throws ITEEClient.ClientException {

    }

    @Override
    public ISession openSession(UUID uuid, ConnectionMethod connectionMethod, Integer connectionData, ITEEClient.Operation teecOperation) throws ITEEClient.ClientException {
        return null;
    }

    @Override
    public void requestCancellation(ITEEClient.Operation teecOperation) {

    }
}
