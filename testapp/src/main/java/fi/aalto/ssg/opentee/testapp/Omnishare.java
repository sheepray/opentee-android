package fi.aalto.ssg.opentee.testapp;

import android.content.Context;

import java.util.UUID;

import fi.aalto.ssg.opentee.ITEEClient;
import fi.aalto.ssg.opentee.OpenTEE;
import fi.aalto.ssg.opentee.exception.TEEClientException;

/**
 * Omnishare utils
 */
public class Omnishare {

    public static synchronized boolean generateRootKey(byte[] key, Context context){
        if(key == null && key.length == 0) return false;

        ITEEClient client = OpenTEE.newTEEClient();

        ITEEClient.IContext ctx = null;
        try {
            ctx = client.initializeContext(null, context);
        } catch (TEEClientException e) {
            e.printStackTrace();
            return false;
        }

        ITEEClient.ISession ses = null;
        try {
            ses = ctx.openSession(OmnishareUtils.getOmnishareTaUuid(),
                    ITEEClient.IContext.ConnectionMethod.LoginPublic,
                    null,
                    null);
        } catch (TEEClientException e) {
            try {
                ctx.finalizeContext();
            } catch (TEEClientException e1) {
                e1.printStackTrace();
            }
            return false;
        }

        ITEEClient.ISharedMemory sm = null;
        try {
            sm = ctx.registerSharedMemory(key, ITEEClient.ISharedMemory.TEEC_MEM_OUTPUT);
        } catch (TEEClientException e) {

            try {
                ses.closeSession();
            } catch (TEEClientException e1) {
                e1.printStackTrace();
            }


            try {
                ctx.finalizeContext();
            } catch (TEEClientException e1) {
                e1.printStackTrace();
            }

            return false;
        }

        ITEEClient.IRegisteredMemoryReference rmr =
                client.newRegisteredMemoryReference(sm,
                        ITEEClient.IRegisteredMemoryReference.Flag.TEEC_MEMREF_OUTPUT,
                        0);

        ITEEClient.IOperation op = client.newOperation(rmr);

        int CMD_CREATE_ROOT_KEY = 0x00000001;

        boolean succ = true;

        try {
            ses.invokeCommand(CMD_CREATE_ROOT_KEY, op);
        } catch (TEEClientException e) {
            e.printStackTrace();
            succ = false;
        }

        try {
            ctx.releaseSharedMemory(sm);
        } catch (TEEClientException e) {
            e.printStackTrace();
        }

        try {
            ses.closeSession();
        } catch (TEEClientException e) {
            e.printStackTrace();
        }

        try {
            ctx.finalizeContext();
        } catch (TEEClientException e) {
            e.printStackTrace();
        }

        return succ;

    }

    public static synchronized boolean omnishareInit(byte[] rootkey, Context context){
        if(rootkey == null || rootkey.length == 0) return false;

        ITEEClient client = OpenTEE.newTEEClient();

        ITEEClient.IContext ctx = null;

        try {
            ctx = client.initializeContext(null, context);
        } catch (TEEClientException e) {
            e.printStackTrace();
            return  false;
        }

        ITEEClient.ISharedMemory sm = null;
        try {
            sm = ctx.registerSharedMemory(rootkey, ITEEClient.ISharedMemory.TEEC_MEM_INPUT);
        } catch (TEEClientException e) {
            e.printStackTrace();

            try {
                ctx.finalizeContext();
            } catch (TEEClientException e1) {
                e1.printStackTrace();
                return false;
            }
            return  false;
        }

        ITEEClient.IRegisteredMemoryReference rmr =
                client.newRegisteredMemoryReference(sm,
                        ITEEClient.IRegisteredMemoryReference.Flag.TEEC_MEMREF_INPUT,
                        0);

        ITEEClient.IOperation op = client.newOperation(rmr);

        ITEEClient.ISession ses = null;

        try {
            ses = ctx.openSession(OmnishareUtils.getOmnishareTaUuid(),
                    ITEEClient.IContext.ConnectionMethod.LoginPublic,
                    null,
                    null);
        } catch (TEEClientException e) {

            try {
                ctx.releaseSharedMemory(sm);
            } catch (TEEClientException e1) {
                e1.printStackTrace();
                return false;
            }

            try {
                ctx.finalizeContext();
            } catch (TEEClientException e1) {
                e1.printStackTrace();
                return false;
            }

            e.printStackTrace();
        }

        try {
            ctx.releaseSharedMemory(sm);
        } catch (TEEClientException e) {
            e.printStackTrace();
            return false;
        }

        return true;
    }
}
