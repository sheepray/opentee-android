package fi.aalto.ssg.opentee.imps;

import android.os.RemoteException;

import fi.aalto.ssg.opentee.ITEEClient;
import fi.aalto.ssg.opentee.exception.BadFormatException;
import fi.aalto.ssg.opentee.exception.BusyException;
import fi.aalto.ssg.opentee.exception.CommunicationErrorException;
import fi.aalto.ssg.opentee.exception.ExcessDataException;
import fi.aalto.ssg.opentee.exception.ExternalCancelException;
import fi.aalto.ssg.opentee.exception.TEEClientException;

/**
 * Callback function for OTContext.
 */
interface OTContextCallback {
    void closeSession(int sid) throws RemoteException, CommunicationErrorException;
    ReturnValueWrapper invokeCommand(int sid, int commandId, ITEEClient.IOperation iOperation) throws TEEClientException;
}
