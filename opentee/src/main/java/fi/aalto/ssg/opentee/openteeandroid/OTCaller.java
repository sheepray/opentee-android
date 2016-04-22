package fi.aalto.ssg.opentee.openteeandroid;

import android.util.Log;

import java.util.HashMap;
import java.util.Map;

import fi.aalto.ssg.opentee.imps.OTSharedMemory;

/**
 * Caller resource hierarchy.
 */
public class OTCaller {
    String TAG = "OTCaller.class";

    int mID;
    Map<Integer, OTSharedMemory> mSharedMemoryList; // <smidInJni, sharedMemory>reuse the OTSharedMemory class.
    //Map<Integer, OTCallerSession> mSessionList; // <sidInJni, session>
    Map<Integer, Integer> mSessionList; // <sidInJni, session>

    public OTCaller(int id){
        this.mID = id;
        this.mSharedMemoryList = new HashMap<>();
        this.mSessionList = new HashMap<>();
    }

    public synchronized int getID(){return this.mID;}

    public synchronized void addSharedMemory(int smIdInJni, OTSharedMemory sharedMemory){
        if ( sharedMemory != null ){
            Log.d(TAG, this.mID + " added SharedMemory");

            mSharedMemoryList.put(smIdInJni, sharedMemory);
        }
    }

    /*
    public synchronized void addSession(int sidInJni, OTCallerSession session){
        Log.e(TAG, "flag func");

        if(session == null){
            Log.e(TAG, "WTF, session is null");
            return;
        }

        if ( session != null ){
            Log.d(TAG, this.mID + " opened session");

            mSessionList.put(sidInJni, session);
        }
    }
    */

    public synchronized void addSession(int sidInJni, int session){
        mSessionList.put(sidInJni, session);
    }

    public synchronized void removeSharedMemoryBySmIdInJni(int smIdInJni){
        if ( mSharedMemoryList.size() == 0 ) {
            Log.e(TAG, "SharedMemoryList empty, nothing to remove.");
            return;
        }

        mSharedMemoryList.remove(smIdInJni);

        Log.i(TAG, mID + "'s shared memory:" + smIdInJni + " removed.");
    }

    /*
    public synchronized int removeSessionBySid(int sid){
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
    */
    public synchronized int removeSessionBySid(int sid){
        for(Map.Entry<Integer, Integer> entry: mSessionList.entrySet()){
            if( entry.getValue() == sid ){
                Log.i(TAG, mID + "'s session:" + sid + " with sidInJni: " + entry.getKey() + " removed.");

                mSessionList.remove(entry);
                return entry.getKey();
            }
        }

        Log.i(TAG, mID + "'s session:" + sid + " not found.");
        return -1;
    }

    public synchronized int getSmIdInJniBySmid(int smid){
        for( Map.Entry<Integer, OTSharedMemory> entry: mSharedMemoryList.entrySet() ){
            if( entry.getValue().getId() == smid ) return entry.getKey();
        }

        // not found.
        return -1;
    }

    public synchronized int getSmIdBySmIdInJni(int smidInJni){
        return mSharedMemoryList.get(smidInJni).getId();
    }
/*
    public synchronized int getSidInJniBySid(int sid){
        for( Map.Entry<Integer, OTCallerSession> entry: mSessionList.entrySet()){
            if(entry.getValue().getSid() == sid) return entry.getKey();
        }

        //not found
        return -1;
    }
*/

    public synchronized int getSidInJniBySid(int sid){
        for( Map.Entry<Integer, Integer> entry: mSessionList.entrySet()){
            if(entry.getValue() == sid) return entry.getKey();
        }

        //not found
        return -1;
    }

    public synchronized int getSidBySidInJni(int sidInJni){
        return mSessionList.get(sidInJni);
    }
}
