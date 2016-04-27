package fi.aalto.ssg.opentee.imps;

import android.content.Context;
import android.os.RemoteException;
import android.util.Log;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Random;
import java.util.UUID;

import fi.aalto.ssg.opentee.ITEEClient;
import fi.aalto.ssg.opentee.exception.BadFormatException;
import fi.aalto.ssg.opentee.exception.BadParametersException;
import fi.aalto.ssg.opentee.exception.BusyException;
import fi.aalto.ssg.opentee.exception.CommunicationErrorException;
import fi.aalto.ssg.opentee.exception.ExcessDataException;
import fi.aalto.ssg.opentee.exception.ExternalCancelException;
import fi.aalto.ssg.opentee.exception.GenericErrorException;
import fi.aalto.ssg.opentee.exception.TEEClientException;
import fi.aalto.ssg.opentee.imps.pbdatatypes.GPDataTypes;

/**
 * This class implements the IContext interface
 */
public class OTContext implements ITEEClient.IContext, OTContextCallback {
    final String TAG = "OTContext";

    String mTeeName = null;
    boolean mInitialized = false;
    ProxyApis mProxyApis = null; // one service connection per context
    Random smIdGenerator = null;

    List<OTSharedMemory> mSharedMemory = new ArrayList<>();
    HashMap<Integer, Integer> mSessionMap = new HashMap<>(); // <sessionId, placeHolder>

    public OTContext(String teeName, Context context) throws TEEClientException {
        this.mTeeName = teeName;
        this.smIdGenerator = new Random();

        /**
         * connect to the IOpenTEE
         */
        Object lock = new Object();
        ServiceGetterThread serviceGetterThread = new ServiceGetterThread(teeName, context, lock);
        serviceGetterThread.run();

        synchronized (lock) {
            // wait 10000 til service connected.
            try {
                lock.wait(10000);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }

        mProxyApis = serviceGetterThread.getProxyApis();

        if(mProxyApis == null){
            throw new CommunicationErrorException("Unable to connect to remote TEE service",
                    ITEEClient.ReturnOriginCode.TEEC_ORIGIN_COMMS);
        }
        this.mInitialized = true;

        Log.d(TAG, "Service connected.");
    }

    @Override
    public void finalizeContext() throws TEEClientException{
        if ( !mInitialized || mProxyApis == null ){
            Log.i(TAG, "Nothing to finalize");
            return;
        }

        if ( mInitialized ) mInitialized = false;
        if ( mProxyApis != null ){
            try {
                mProxyApis.teecFinalizeContext();
            } catch (RemoteException e) {
                throw new CommunicationErrorException("Communication error with remote TEE service.");
            }
            mProxyApis.terminateConnection();
        }

        //clear up resources.
        mSharedMemory.clear();
        mSessionMap.clear();
        mProxyApis = null;

        Log.i(TAG, "context finalized and connection terminated");
    }

    @Override
    public ITEEClient.ISharedMemory registerSharedMemory(byte[] buffer, int flags) throws TEEClientException{
        if ( !mInitialized || mProxyApis == null ){
            Log.e(TAG, "Not ready to register shared memory");
            return null;
        }

        if( buffer == null ){
            throw new BadParametersException("provided buffer is null", ITEEClient.ReturnOriginCode.TEEC_ORIGIN_API);
        }

        if( flags != ITEEClient.ISharedMemory.TEEC_MEM_INPUT &&
            flags != ITEEClient.ISharedMemory.TEEC_MEM_OUTPUT &&
            flags != ( ITEEClient.ISharedMemory.TEEC_MEM_INPUT | ITEEClient.ISharedMemory.TEEC_MEM_OUTPUT)){
            throw new BadParametersException("incorrect flags.", ITEEClient.ReturnOriginCode.TEEC_ORIGIN_COMMS);
        }

        int smId = generateSmId();

        // create a shared memory
        OTSharedMemory otSharedMemory = new OTSharedMemory(buffer, flags, smId);

        // register the shared memory
        try {
            mProxyApis.teecRegisterSharedMemory(otSharedMemory);
        } catch (RemoteException e) {
            throw new CommunicationErrorException("Communication error with remote TEE service.");
        }

        // add the registered shared memory to mSharedMemory list.
        mSharedMemory.add(otSharedMemory);

        return otSharedMemory;
    }

    @Override
    public void releaseSharedMemory(ITEEClient.ISharedMemory sharedMemory) throws TEEClientException{
        if ( !mInitialized || mProxyApis == null ){
            Log.i(TAG, "Not ready to release shared memory");
            return;
        }

        // tell remote tee to release the shared memory.
        if ( sharedMemory != null ) try {
            mProxyApis.teecReleaseSharedMemory(sharedMemory.getId());
        } catch (RemoteException e) {
            throw new CommunicationErrorException("Communication error with remote TEE service.");
        }

        // remove it from shared memory list.
        if (!mSharedMemory.remove(sharedMemory)){
            throw new BadParametersException("Unable to find the input shared memory.",
                    ITEEClient.ReturnOriginCode.TEEC_ORIGIN_COMMS);
        }
    }

    private void updateOperation(OTOperation otOperation, byte[] opInBytes) throws ExcessDataException, BadFormatException {
        GPDataTypes.TeecOperation op = OTFactoryMethods.transferOpInBytesToOperation(TAG, opInBytes);

        //test code
        OTFactoryMethods.print_op(TAG, op);

        for(int i = 0; i < op.getMParamsCount(); i++){
            GPDataTypes.TeecParameter param = op.getMParamsList().get(i);
            if(param.getType() == GPDataTypes.TeecParameter.Type.val){
                OTValue otValue = (OTValue)otOperation.getParam(i);

                otValue.setA(param.getTeecValue().getA());
                otValue.setB(param.getTeecValue().getB());
            }
            else if (param.getType() == GPDataTypes.TeecParameter.Type.smr){
                OTRegisteredMemoryReference otRmr = (OTRegisteredMemoryReference)otOperation.getParam(i);
                OTSharedMemory otSm = (OTSharedMemory)otRmr.getSharedMemory();

                GPDataTypes.TeecSharedMemoryReference teecSmr = param.getTeecSharedMemoryReference();

                otSm.updateBuffer(teecSmr.getParent().getMBuffer().toByteArray(),
                        otRmr.getOffset(),
                        teecSmr.getParent().getMReturnSize());

            }
            else{
                Log.e(TAG, "Unknown type of parameter.");
            }
        }
    }

    @Override
    public ITEEClient.ISession openSession(UUID uuid,
                                ConnectionMethod connectionMethod,
                                Integer connectionData,
                                ITEEClient.IOperation teecOperation) throws TEEClientException{
        if ( !mInitialized || mProxyApis == null ){
            Log.i(TAG, "Not ready to open session");
            return null;
        }

        // sid is used to identify different sessions of one context in the OTGuard.
        int sid = generateSessionId();

        OpenSessionThread openSessionThread = null;
        Thread opWorker = null;
        ReturnValueWrapper rv = null;

        if(connectionData == null) connectionData = 0;

        if(teecOperation == null){
            openSessionThread = new OpenSessionThread(mProxyApis,
                    sid,
                    uuid,
                    connectionMethod,
                    connectionData,
                    null,   // without operation.
                    null);  // without lock.

            opWorker = new Thread(openSessionThread);
            opWorker.start();
            try {
                opWorker.join();
            } catch (InterruptedException e) {
                throw new GenericErrorException(e.getMessage());
            }
        }
        else{
            //teecOperation started field check
            if(teecOperation != null && teecOperation.isStarted()){
                throw new BusyException("the referenced operation is under usage.", ITEEClient.ReturnOriginCode.TEEC_ORIGIN_API);
            }

            OTOperation otOperation = (OTOperation)teecOperation;

            //update started field.
            otOperation.setStarted(1);

            /**
             * parse teecOperation into byte array using protocol buffer.
             */
            byte[] opInArray = OTFactoryMethods.OperationAsByteArray(TAG, teecOperation);

            OTLock otLock = new OTLock();
            openSessionThread = new OpenSessionThread(mProxyApis,
                    sid,
                    uuid,
                    connectionMethod,
                    connectionData,
                    opInArray,
                    otLock);

            opWorker = new Thread(openSessionThread);
            opWorker.start();

            try {
                opWorker.join();
            } catch (InterruptedException e) {
                throw new ExternalCancelException(e.getMessage());
            }

            //test code
            Log.d(TAG, "Thread id " + Thread.currentThread().getId());

            // wait util operation synced back.
            otLock.lock();
            otLock.unlock();

            byte[] teecOperationInBytes = openSessionThread.getNewOperationInBytes();

            if(teecOperationInBytes != null){
                updateOperation(otOperation, teecOperationInBytes);
            }
            else{
                Log.e(TAG, "op is empty");
            }

            // operation is no longer in use.
            otOperation.setStarted(0);
        }

        rv = openSessionThread.getReturnValue();

        if(rv.getReturnCode() != OTReturnCode.TEEC_SUCCESS){
            OTFactoryMethods.throwExceptionWithReturnOrigin(TAG, rv.getReturnCode(), rv.getReturnOrigin());
        }

        // upon success
        OTContextCallback otContextCallback = this;
        OTSession otSession =  new OTSession(sid, otContextCallback);
        mSessionMap.put(sid, 0);

        return otSession;
    }

    @Override
    public void requestCancellation(ITEEClient.IOperation iOperation) {
        //TODO:
    }

    private int generateSmId(){
        int id;
        do{
            id = smIdGenerator.nextInt(50000);
        }while(occupiedSmId(id));

        Log.i(TAG, "generating shared memory id:" + id);
        return id;
    }

    private boolean occupiedSmId(int id){
        for(OTSharedMemory sm: mSharedMemory ){
            if ( sm.getId() == id ) return true;
        }
        return false;
    }

    private int generateSessionId(){
        // reuse the random generator of shared memory.
        int id;
        do{
            id = smIdGenerator.nextInt(50000);
        }while(mSessionMap.containsKey(id));

        Log.i(TAG, "generating session id:" + id);
        return id;
    }

    @Override
    public void closeSession(int sid) throws RemoteException, CommunicationErrorException {
        Log.i(TAG, "closing session with id " + sid);

        // call remote to close session.
        mProxyApis.teecCloseSession(sid);

        // remote session.
        mSessionMap.remove(sid);
    }

    @Override
    public ReturnValueWrapper invokeCommand(int sid, int commandId, ITEEClient.IOperation teecOperation) throws TEEClientException {
        Log.i(TAG, "invoking command with commandId " + commandId);

        if ( !mInitialized || mProxyApis == null ){
            Log.i(TAG, "Not ready to open session");
            return null;
        }

        InvokeCommandThread invokeCommandThread = null;
        Thread opWorker = null;

        //teecOperation started check
        if(teecOperation == null){
            invokeCommandThread = new InvokeCommandThread(mProxyApis,
                    sid,
                    commandId,
                    null,   // no operation
                    null);  // no lock

            opWorker = new Thread(invokeCommandThread);
            opWorker.start();
            try {
                opWorker.join();
            } catch (InterruptedException e) {
                throw new ExternalCancelException(e.getMessage());
            }
        }
        else{
            if(teecOperation.isStarted()){
                throw new BusyException("the referenced operation is under usage.", ITEEClient.ReturnOriginCode.TEEC_ORIGIN_API);
            }

            OTOperation otOperation = (OTOperation)teecOperation;

            //update started field.
            otOperation.setStarted(1);

            /**
             * parse teecOperation into byte array using protocol buffer.
             */
            byte[] opInArray = OTFactoryMethods.OperationAsByteArray(TAG, teecOperation);

            OTLock otLock = new OTLock();
            invokeCommandThread = new InvokeCommandThread(mProxyApis,
                    sid,
                    commandId,
                    opInArray,
                    otLock);

            opWorker = new Thread(invokeCommandThread);
            opWorker.start();
            try {
                opWorker.join();
            } catch (InterruptedException e) {
                throw new ExternalCancelException(e.getMessage());
            }

            //wait operations synced back.
            otLock.lock();
            otLock.unlock();

            byte[] teecOperationInBytes = invokeCommandThread.getNewOperationInBytes();

            if(teecOperationInBytes != null){
                updateOperation(otOperation, teecOperationInBytes);
            }
            else{
                Log.e(TAG, "op is empty");
            }

            // operation is no longer in use.
            otOperation.setStarted(0);
        }

        return invokeCommandThread.getReturnValue();
    }
}
