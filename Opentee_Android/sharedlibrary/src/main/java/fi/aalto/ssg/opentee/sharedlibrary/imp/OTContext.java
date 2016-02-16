package fi.aalto.ssg.opentee.sharedlibrary.imp;

import android.content.Context;
import android.os.RemoteException;

import fi.aalto.ssg.opentee.sharedlibrary.gp.datatypes.TeecConnectionException;
import fi.aalto.ssg.opentee.sharedlibrary.gp.datatypes.TeecConnectionMethod;
import fi.aalto.ssg.opentee.sharedlibrary.gp.apis.TeecContext;
import fi.aalto.ssg.opentee.sharedlibrary.gp.datatypes.TeecException;
import fi.aalto.ssg.opentee.sharedlibrary.gp.datatypes.TeecOperation;
import fi.aalto.ssg.opentee.sharedlibrary.gp.datatypes.TeecSession;
import fi.aalto.ssg.opentee.sharedlibrary.gp.datatypes.TeecSharedMemory;
import fi.aalto.ssg.opentee.sharedlibrary.gp.datatypes.TeecUuid;

/**
 * This class implements the TeecContext interface
 */
public class OTContext implements TeecContext {
    String mName;
    boolean mInitialized;
    ProxyApis mProxyApis = null; // one service connection per context

    public OTContext(String name, Context context) throws TeecConnectionException, RemoteException {
        this.mName = name;

        //TODO: connect to the OpenTEE
        mProxyApis = new ProxyApis(context, name);
        //mProxyApis.teecInitializeContext(name);

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
    public void teecRegisterSharedMemory(TeecSharedMemory sharedMemory) throws TeecException {

    }

    @Override
    public void teecAllocateSharedMemory(TeecSharedMemory sharedMemory) throws TeecException {

    }

    @Override
    public void teecReleaseSharedMemory(TeecSharedMemory sharedMemory) throws TeecException {

    }

    @Override
    public TeecSession teecOpenSession(TeecUuid uuid, TeecConnectionMethod connectionMethod, int connectionData, TeecOperation teecOperation) throws TeecException {
        return null;
    }

    @Override
    public void teecCloseSession(TeecSession teecSession) throws TeecException {

    }

    @Override
    public void teecRequestCancellation(TeecOperation teecOperation) {

    }
}
