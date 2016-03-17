package fi.aalto.ssg.opentee.openteeandroid;

import android.util.Log;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import fi.aalto.ssg.opentee.imps.OTSharedMemory;

/**
 * Caller resource hierarchy.
 */
public class OTCaller {
    String TAG = "OTCaller.class";

    int mID;
    Map<Integer, OTSharedMemory> mSharedMemoryList; // <smid, sharedMemory>reuse the OTSharedMemory class.
    Map<Integer, OTCallerSession> mSessionList; // <sid, session>

    public OTCaller(int id){
        this.mID = id;
        this.mSharedMemoryList = new HashMap<>();
        this.mSessionList = new HashMap<>();
    }

    public int getID(){return this.mID;}

    public void addSharedMemory(OTSharedMemory sharedMemory){
        if ( sharedMemory != null ){
            Log.d(TAG, this.mID + " added SharedMemory");

            mSharedMemoryList.put(sharedMemory.getId(), sharedMemory);
        }
    }

    public void addSession(OTCallerSession session){
        if ( session != null ){
            Log.d(TAG, this.mID + " opened session");

            mSessionList.put(session.getSid(), session);
        }
    }

    public void removeSharedMemoryBySmId(int smId){
        if ( mSharedMemoryList.size() == 0 ) {
            Log.e(TAG, "SharedMemoryList empty, nothing to remove");
            return;
        }

        mSharedMemoryList.remove(smId);
    }

    public void removeSessionBySid(int sid){
        if( mSessionList.size() == 0 ){
            Log.e(TAG, "Session list is empty, nothing to remove");
            return;
        }

        mSessionList.remove(sid);
    }

    public OTSharedMemory getSharedMemoryBySmId(int smId){
        if ( mSharedMemoryList.size() == 0 ) {
            Log.e(TAG, "SharedMemoryList empty, nothing to remove");
            return null;
        }

        return mSharedMemoryList.get(smId);
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
