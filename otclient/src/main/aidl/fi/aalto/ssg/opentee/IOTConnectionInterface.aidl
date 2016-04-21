// IOTConnectionInterface.aidl
package fi.aalto.ssg.opentee;

// Declare any non-default types here with import statements
import fi.aalto.ssg.opentee.imps.OTSharedMemory;
import fi.aalto.ssg.opentee.ISyncOperation;

interface IOTConnectionInterface {
    /**
     * Demonstrates some basic types that you can use as parameters
     * and return values in AIDL.
     */
    void basicTypes(int anInt, long aLong, boolean aBoolean, float aFloat,
            double aDouble, String aString);

    int teecInitializeContext(String name);

    void teecFinalizeContext();

    // It is sophisticated to pass enum in AIDL.
    int teecRegisterSharedMemory(inout OTSharedMemory sharedMemory);

    void teecReleaseSharedMemory(int smId);

    // open session without operation.
    int teecOpenSessionWithoutOp(int sid, in ParcelUuid parcelUuid, int connMethod, int connData, out int[] retOrigin);

    int teecOpenSession(int sid, in ParcelUuid parcelUuid, int connMethod, int connData, in byte[] teecOperation, out int[] retOrigin, in ISyncOperation syncOperation);
    //int teecOpenSessionWithByteArrayWrapper(int sid, in ParcelUuid parcelUuid, int connMethod, int connData, inout ByteArrayWrapper teecOperation, out int[] retOrigin);

    void teecCloseSession(int sid);
}
