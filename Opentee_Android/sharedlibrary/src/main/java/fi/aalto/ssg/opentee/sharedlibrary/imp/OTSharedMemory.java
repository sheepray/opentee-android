package fi.aalto.ssg.opentee.sharedlibrary.imp;

import fi.aalto.ssg.opentee.sharedlibrary.gp.apis.ITeecSharedMemory;
import fi.aalto.ssg.opentee.sharedlibrary.gp.datatypes.TeecException;

/**
 * this class implements the ITeecSharedMemory interface
 */
public class OTSharedMemory implements ITeecSharedMemory {
    byte[] mBuffer;
    int mFlag;

    //static int TEEC_MEM_INPUT = 0x0000001;
    //static int TEEC_MEM_OUTPUT = 0x00000002;

    public OTSharedMemory(byte[] buffer, int flag){
        this.mBuffer = buffer;
        this.mFlag = flag;
    }

    @Override
    public int getFlag() {
        return 0;
    }

    @Override
    public byte[] getBuffer() throws TeecException {
        return new byte[0];
    }
}
