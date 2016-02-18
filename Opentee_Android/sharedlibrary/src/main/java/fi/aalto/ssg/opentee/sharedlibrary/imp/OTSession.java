package fi.aalto.ssg.opentee.sharedlibrary.imp;

import fi.aalto.ssg.opentee.sharedlibrary.gp.apis.ITeecSession;
import fi.aalto.ssg.opentee.sharedlibrary.gp.datatypes.TeecException;
import fi.aalto.ssg.opentee.sharedlibrary.gp.datatypes.TeecOperation;

/**
 * This class implements the ITeecSession interface.
 */
public class OTSession implements ITeecSession{

    @Override
    public void teecInvokeCommand(int commandId, TeecOperation teecOperation) throws TeecException {

    }

    @Override
    public void teecCloseSession(ITeecSession teecSession) throws TeecException {

    }
}
