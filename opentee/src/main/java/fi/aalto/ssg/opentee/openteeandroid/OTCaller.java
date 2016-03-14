package fi.aalto.ssg.opentee.openteeandroid;

import android.util.Log;

import java.util.ArrayList;
import java.util.List;

import fi.aalto.ssg.opentee.imps.OTSharedMemory;

/**
 * Caller resource hierarchy.
 */
public class OTCaller {
    String TAG = "OTCaller.class";

    int mID;
    List<OTSharedMemory> mSharedMemoryList;

    public OTCaller(int id){
        this.mID = id;
        this.mSharedMemoryList = new ArrayList<OTSharedMemory>();
    }

    public int getID(){return this.mID;}

    public void addSharedMemory(OTSharedMemory sharedMemory){
        if ( sharedMemory != null ){
            Log.d(TAG, this.mID + " add SharedMemory");

            mSharedMemoryList.add(sharedMemory);
        }
    }

    public void removeSharedMemory(OTSharedMemory sharedMemory){
        if ( mSharedMemoryList.size() == 0 ) {
            Log.e(TAG, "SharedMemoryList empty, nothing to remove");
            return;
        }

        if( mSharedMemoryList.remove(sharedMemory) ){
            Log.d(TAG, this.mID + " remove shared memory succeed");
        }else{
            Log.e(TAG, this.mID + " remove shared memory failed");
        }
    }

    public void removeSharedMemoryBySmId(int smId){
        if ( mSharedMemoryList.size() == 0 ) {
            Log.e(TAG, "SharedMemoryList empty, nothing to remove");
            return;
        }

        for (OTSharedMemory sm: mSharedMemoryList){
            if ( sm.getID() == smId ){
                Log.d(TAG, smId + " found and removed.");

                mSharedMemoryList.remove(sm);
                break;
            }
        }
    }

    public OTSharedMemory getSharedMemoryBySmId(int smId){
        if ( mSharedMemoryList.size() == 0 ) {
            Log.e(TAG, "SharedMemoryList empty, nothing to remove");
            return null;
        }

        for (OTSharedMemory sm: mSharedMemoryList){
            if ( sm.getID() == smId ){
                Log.d(TAG, smId + " found.");

                return sm;
            }
        }

        Log.d(TAG, smId + " not found.");

        return null;
    }

    public List<OTSharedMemory> getSharedMemoryList(){
        return this.mSharedMemoryList;
    }

    /**
     * OTSession children class
     */
    class OTSession{}

    //TODO: more children classes shall be added based on implementation.
}
