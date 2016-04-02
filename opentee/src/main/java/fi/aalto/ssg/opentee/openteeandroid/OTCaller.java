package fi.aalto.ssg.opentee.openteeandroid;

import android.util.Log;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import fi.aalto.ssg.opentee.imps.OTSession;
import fi.aalto.ssg.opentee.imps.OTSharedMemory;

/**
 * Caller resource hierarchy.
 */
public class OTCaller {
    String TAG = "OTCaller.class";

    int mID;
    HashMap<Integer, OTSharedMemory> mSharedMemoryList; // <smidInJni, sharedMemory>reuse the OTSharedMemory class.
    HashMap<Integer, OTCallerSession> mSessionList; // <sidInJni, session>

    public OTCaller(int id){
        this.mID = id;
        this.mSharedMemoryList = new HashMap<>();
        this.mSessionList = new HashMap<>();
    }

    public int getID(){return this.mID;}

    public void addSharedMemory(int smIdInJni, OTSharedMemory sharedMemory){
        if ( sharedMemory != null ){
            Log.d(TAG, this.mID + " added SharedMemory");

            mSharedMemoryList.put(smIdInJni, sharedMemory);
        }
    }

    public void addSession(int sidInJni, OTCallerSession session){
        if ( session != null ){
            Log.d(TAG, this.mID + " opened session");

            mSessionList.put(sidInJni, session);
        }
    }

    public void removeSharedMemoryBySmIdInJni(int smIdInJni){
        if ( mSharedMemoryList.size() == 0 ) {
            Log.e(TAG, "SharedMemoryList empty, nothing to remove.");
            return;
        }

        mSharedMemoryList.remove(smIdInJni);

        Log.i(TAG, mID + "'s shared memory:" + smIdInJni + " removed.");
    }

    public int removeSessionBySid(int sid){
        for(Map.Entry<Integer, OTCallerSession> entry: mSessionList.entrySet()){
            if( entry.getValue().getSid() == sid ){
                Log.i(TAG, mID + "'s session:" + sid + " with sidInJni: " + entry.getKey() + " removed.");

                mSessionList.remove(entry);
                return entry.getKey();
            }
        }

        Log.i(TAG, mID + "'s session:" + sid + " not found.");
        return -1;
    }

    public int getSmIdInJniBySmid(int smid){
        for( Map.Entry<Integer, OTSharedMemory> entry: mSharedMemoryList.entrySet() ){
            if( entry.getValue().getId() == smid ) return entry.getKey();
        }

        // not found.
        return -1;
    }

    /**
     * OTSession children class
     */
    public static class OTCallerSession{
        int mSid;

        public OTCallerSession(int sid){
            this.mSid = sid;
        }

        public int getSid(){
            return this.mSid;
        }

    }

    //TODO: more children classes shall be added based on implementation.
}
