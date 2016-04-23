package fi.aalto.ssg.opentee;

import fi.aalto.ssg.opentee.ITEEClient;
import fi.aalto.ssg.opentee.imps.OTClient;

/**
 * Created by yangr1 on 4/23/16.
 */
public class OpenTEE{
    public static ITEEClient newClient(){
        return new OTClient();
    }
}
