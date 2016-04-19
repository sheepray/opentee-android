package fi.aalto.ssg.opentee.imps;

import android.content.Context;
import android.os.RemoteException;

import fi.aalto.ssg.opentee.ITEEClient;
import fi.aalto.ssg.opentee.exception.TEEClientException;

/**
 * This class implements the ITEEClient interface.
 */
public class OTClient implements ITEEClient {
    @Override
    public IOperation newOperation() {
        return new OTOperation(0);
    }

    @Override
    public IOperation newOperation(IParameter firstParam) {
        return new OTOperation(0, firstParam);
    }

    @Override
    public IOperation newOperation(IParameter firstParam, IParameter secondParam) {
        return new OTOperation(0, firstParam, secondParam);
    }

    @Override
    public IOperation newOperation(IParameter firstParam, IParameter secondParam, IParameter thirdParam) {
        return new OTOperation(0, firstParam, secondParam, thirdParam);
    }

    @Override
    public IOperation newOperation(IParameter firstParam, IParameter secondParam, IParameter thirdParam, IParameter forthParam) {
        return new OTOperation(0, firstParam, secondParam, thirdParam, forthParam);
    }

    @Override
    public IRegisteredMemoryReference newRegisteredMemoryReference(ISharedMemory sharedMemory, IRegisteredMemoryReference.Flag flag, int offset) {
        return new OTRegisteredMemoryReference(sharedMemory, flag, offset);
    }

    @Override
    public IValue newValue(IValue.Flag flag, int a, int b) {
        return new OTValue(flag, a, b);
    }

    @Override
    public IContext initializeContext(String teeName, Context context) throws TEEClientException{
        return new OTContext(teeName, context);
    }
}
