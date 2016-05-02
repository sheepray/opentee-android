package fi.aalto.ssg.opentee.testapp;

import android.util.Log;

import java.util.UUID;

/**
 * Created by yangr1 on 4/28/16.
 */
public class OmnishareUtils {
    public static long strToLong(String strVal){
        if(strVal == null || strVal.isEmpty()) return 0;

        byte[] vals = strVal.getBytes();
        int tailFlag = vals.length > 8 ? 8 : vals.length;
        long result = 0;
        for(int i = 0; i < tailFlag; i++){
            result = result << 8;
            result += vals[i];
        }
        return result;
    }

    public static synchronized UUID getOmnishareTaUuid(){
        long clockSeqAndNode = strToLong(new String("OMNISHAR"));
        UUID TA_CONN_TEST_UUID = new UUID(0x1234567887654321L, clockSeqAndNode);

        return TA_CONN_TEST_UUID;
    }
}
