package fi.aalto.ssg.opentee.imps;

import android.content.Context;

/**
 * This class implements the Runnable interface. It will release the lock object once connected to
 * remote service.
 */
public class ServiceGetterThread implements Runnable {
    ProxyApis mProxyApis = null;
    String mTeeName = null;
    Context mContext = null;
    Object mLock = null;

    public ServiceGetterThread(String teeName, Context context, Object lock){
        this.mTeeName = teeName;
        this.mContext = context;
        this.mLock = lock;
    }

    @Override
    public void run(){
        mProxyApis = new ProxyApis(this.mTeeName, this.mContext, this.mLock);
    }

    public ProxyApis getProxyApis(){return this.mProxyApis;}

}
