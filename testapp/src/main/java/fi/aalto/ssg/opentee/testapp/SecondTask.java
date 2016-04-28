package fi.aalto.ssg.opentee.testapp;

import fi.aalto.ssg.opentee.ITEEClient;
import fi.aalto.ssg.opentee.exception.TEEClientException;

/**
 * Example code for a task to send request cancellation of operation.
 */
public class SecondTask implements Runnable {
    final ITEEClient.IContext mCtx;
    final ITEEClient.IOperation mOp;

    public SecondTask(ITEEClient.IContext ctx, ITEEClient.IOperation iop){
        mCtx = ctx;
        mOp = iop;
    }

    @Override
    public void run() {
        try {
            mCtx.requestCancellation(mOp);
        } catch (TEEClientException e) {
            e.printStackTrace();
        }
    }
}
