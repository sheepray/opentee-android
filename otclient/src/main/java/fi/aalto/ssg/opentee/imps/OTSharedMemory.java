package fi.aalto.ssg.opentee.imps;

import fi.aalto.ssg.opentee.ClientException;
import fi.aalto.ssg.opentee.ITEEClient;
import fi.aalto.ssg.opentee.sharedlibrary.gp.apis.ITeecSharedMemory;
import fi.aalto.ssg.opentee.sharedlibrary.gp.datatypes.TeecException;

/**
 * this class implements the ISharedMemory interface
 */
public class OTSharedMemory implements ITEEClient.IContext.ISharedMemory {
    byte[] mBuffer;
    Flag mFlag;

    public OTSharedMemory(byte[] buffer, Flag flag){
        this.mBuffer = buffer;
        this.mFlag = flag;
    }

    @Override
    public Flag getFlags() {
        return this.mFlag;
    }

    @Override
    public byte[] asByteArray() throws ClientException {
        return this.mBuffer;
    }
}
