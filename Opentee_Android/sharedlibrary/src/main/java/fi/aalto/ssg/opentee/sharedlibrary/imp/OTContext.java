package fi.aalto.ssg.opentee.sharedlibrary.imp;

import android.content.Context;
import android.os.RemoteException;

import fi.aalto.ssg.opentee.sharedlibrary.gp.apis.ITeecContext;
import fi.aalto.ssg.opentee.sharedlibrary.gp.apis.ITeecSession;
import fi.aalto.ssg.opentee.sharedlibrary.gp.datatypes.TeecConnectionException;
import fi.aalto.ssg.opentee.sharedlibrary.gp.datatypes.TeecConnectionMethod;
import fi.aalto.ssg.opentee.sharedlibrary.gp.datatypes.TeecException;
import fi.aalto.ssg.opentee.sharedlibrary.gp.datatypes.TeecOperation;
import fi.aalto.ssg.opentee.sharedlibrary.gp.apis.ITeecSharedMemory;
import fi.aalto.ssg.opentee.sharedlibrary.gp.datatypes.TeecParameter;
import fi.aalto.ssg.opentee.sharedlibrary.gp.datatypes.TeecUuid;

/**
 * This class implements the ITeecContext interface
 */
public class OTContext implements ITeecContext {
    String mTeeName;
    boolean mInitialized;
    ProxyApis mProxyApis = null; // one service connection per context

    public OTContext(String teeName, Context context) throws TeecConnectionException, RemoteException {
        this.mTeeName = teeName;

        //connect to the OpenTEE
        mProxyApis = new ProxyApis(teeName, context);

        this.mInitialized = true;
    }

    @Override
    public void teecFinalizeContext() {
        if ( mInitialized ) mInitialized = false;

        if ( mProxyApis != null && mProxyApis.getConnected()) mProxyApis.terminateConnection();
    }

    @Override
    public void teecFinalizeContextNow() {

    }

    @Override
    public ITeecSharedMemory teecRegisterSharedMemory(byte[] buffer, ITeecSharedMemory.flag flag) throws TeecException {
        //new OTSharedMemory(buffer, flag);
        return null;
    }

    @Override
    public void teecReleaseSharedMemory(ITeecSharedMemory sharedMemory) throws TeecException {

    }

    @Override
    public ITeecSession teecOpenSession(TeecUuid uuid,
                                       TeecConnectionMethod connectionMethod,
                                       Integer connectionData,
                                       TeecOperation teecOperation) throws TeecException {
        return null;
    }

    @Override
    public void teecRequestCancellation(TeecOperation teecOperation) {

    }
}
