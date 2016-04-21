package fi.aalto.ssg.opentee.imps;

import android.util.Log;

import com.google.protobuf.ByteString;
import com.google.protobuf.InvalidProtocolBufferException;

import java.util.List;

import fi.aalto.ssg.opentee.ITEEClient;
import fi.aalto.ssg.opentee.imps.pbdatatypes.GPDataTypes;

/**
 * Shared factory methods.
 */
public class OTFactoryMethods {
    public static void print_op(String tag, GPDataTypes.TeecOperation opToPrint){
        if(opToPrint == null){
            Log.e(tag, "op is null");
            return;
        }

        Log.d(tag, "started:" + opToPrint.getMStarted());
        for(GPDataTypes.TeecParameter param: opToPrint.getMParamsList()){
            if (param.getType() == GPDataTypes.TeecParameter.Type.smr){
                GPDataTypes.TeecSharedMemory sm = param.getTeecSharedMemoryReference().getParent();
                Log.d(tag, "[SMR] flag:" + sm.getMFlag() +
                        " buffer:" + sm.getMBuffer().toStringUtf8().toString());
            }
            else if (param.getType() == GPDataTypes.TeecParameter.Type.val){
                GPDataTypes.TeecValue var = param.getTeecValue();
                Log.d(tag, "[VALUE] flag:" + var.getMFlag() +
                        " a:" + Integer.toHexString(var.getA()) +
                        " b:" + Integer.toHexString(var.getB()) );
            }
            else{
                Log.e(tag, "Incorrect parameter");
            }
        }
    }

    public static void print_op_in_bytes(String tag, byte[] opInBytes){
        Log.i(tag, "[start] print_op_in_bytes");

        print_op(tag, transferOpInBytesToOperation(tag, opInBytes));

        Log.i(tag, "[end] print_op_in_bytes");
    }

    public static GPDataTypes.TeecOperation transferOpInBytesToOperation(String TAG, byte[] opInBytes){
        if(opInBytes == null){
            Log.e(TAG, "Empty operation");
            return null;
        }
        GPDataTypes.TeecOperation.Builder opBuilder = GPDataTypes.TeecOperation.newBuilder();
        try {
            opBuilder.mergeFrom(opInBytes);
        } catch (InvalidProtocolBufferException e) {
            e.printStackTrace();
        }

        return opBuilder.build();
    }

    public static byte[] OperationAsByteArray(String TAG, ITEEClient.IOperation iOperation){
        if ( iOperation == null )return null;
        OTOperation teecOperation = (OTOperation)iOperation;

        GPDataTypes.TeecOperation.Builder toBuilder = GPDataTypes.TeecOperation.newBuilder();

        byte[] opInArray = null;
        if (teecOperation.getParams() != null && teecOperation.getParams().size() > 0){
            /**
             * determine which type of parameter to parse.
             */

            List<ITEEClient.IParameter> parameterList = teecOperation.getParams();

            for ( ITEEClient.IParameter param: parameterList ){
                if( param.getType() == ITEEClient.IParameter.Type.TEEC_PTYPE_VAL){
                    Log.i(TAG, "Param is " + ITEEClient.IParameter.Type.TEEC_PTYPE_VAL);

                    GPDataTypes.TeecValue.Builder builder = GPDataTypes.TeecValue.newBuilder();
                    ITEEClient.IValue iVal = (ITEEClient.IValue)param;
                    OTValue val = (OTValue)iVal;

                    builder.setA(val.getA());
                    builder.setB(val.getB());

                    builder.setMFlag(GPDataTypes.TeecValue.Flag.values()[val.getFlag().ordinal()]);

                    GPDataTypes.TeecParameter.Builder paramBuilder = GPDataTypes.TeecParameter.newBuilder();
                    paramBuilder.setType(GPDataTypes.TeecParameter.Type.val);
                    paramBuilder.setTeecValue(builder.build());
                    toBuilder.addMParams(paramBuilder.build());
                }
                else if ( param.getType() == ITEEClient.IParameter.Type.TEEC_PTYPE_RMR ){
                    Log.i(TAG, "Param is " + ITEEClient.IParameter.Type.TEEC_PTYPE_RMR);

                    GPDataTypes.TeecSharedMemoryReference.Builder builder
                            = GPDataTypes.TeecSharedMemoryReference.newBuilder();
                    ITEEClient.IRegisteredMemoryReference iRmr
                            = (ITEEClient.IRegisteredMemoryReference)param;
                    OTRegisteredMemoryReference rmr = (OTRegisteredMemoryReference)iRmr;
                    OTSharedMemory teecSM = (OTSharedMemory)rmr.getSharedMemory();

                    //create gp shared memory from teec shared memory.
                    GPDataTypes.TeecSharedMemory.Builder gpSMBuilder = GPDataTypes.TeecSharedMemory.newBuilder();
                    gpSMBuilder.setSize(teecSM.getSize());
                    gpSMBuilder.setMID(teecSM.getId());
                    gpSMBuilder.setMFlag(teecSM.getFlags());
                    gpSMBuilder.setMBuffer(ByteString.copyFrom(teecSM.asByteArray()));
                    gpSMBuilder.setMReturnSize(teecSM.getReturnSize());

                    builder.setParent(gpSMBuilder.build());
                    builder.setMOffset(rmr.getOffset());
                    builder.setMFlag(GPDataTypes.TeecSharedMemoryReference.Flag.values()[rmr.getFlag().ordinal()]);

                    GPDataTypes.TeecParameter.Builder paramBuilder = GPDataTypes.TeecParameter.newBuilder();
                    paramBuilder.setType(GPDataTypes.TeecParameter.Type.smr);
                    paramBuilder.setTeecSharedMemoryReference(builder.build());
                    toBuilder.addMParams(paramBuilder.build());
                }
                else{
                    Log.e(TAG, "Unsupported Operation type. Set the operation to null");
                }

            }
        }

        toBuilder.setMStarted(teecOperation.getStarted());
        opInArray = toBuilder.build().toByteArray();

        return opInArray;
    }
}
