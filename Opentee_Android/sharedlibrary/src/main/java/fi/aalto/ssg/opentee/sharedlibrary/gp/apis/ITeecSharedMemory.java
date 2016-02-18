package fi.aalto.ssg.opentee.sharedlibrary.gp.apis;

import fi.aalto.ssg.opentee.sharedlibrary.gp.datatypes.TeecException;

/**
 * shared memory is a block of buffer resided in Client Application and can be shared with Trusted
 * Application. This interfaces defines all the operations can be made upon the TeecSharedMemory. The
 * real implementation resides in OTSharedMemory.
 */
public interface ITeecSharedMemory {
    /**
     * get the flag of the shared memory.
     * @return the flags of SharedMemory.
     */
    public int getFlag();

    /**
     * get the content of the buffer.
     * @return an byte array reference.
     * @throws TeecException error if not allowed to get buffer
     */
    public byte[] getBuffer() throws TeecException;

}
