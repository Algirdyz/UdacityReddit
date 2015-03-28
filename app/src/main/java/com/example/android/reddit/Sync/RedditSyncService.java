package com.example.android.reddit.Sync;

import android.app.Service;
import android.content.Intent;
import android.os.IBinder;
import android.util.Log;

/**
 * Created by Algirdas on 2015-03-28.
 */
public class RedditSyncService extends Service {
    private static final Object sSyncAdapterLock = new Object();
    private static RedditSyncAdapter sRedditSyncAdapter = null;

    @Override
    public void onCreate() {
        Log.d("RedditSyncService", "onCreate - RedditSyncService");
        synchronized (sSyncAdapterLock) {
            if (sRedditSyncAdapter == null) {
                sRedditSyncAdapter = new RedditSyncAdapter(getApplicationContext(), true);
            }
        }
    }

    @Override
    public IBinder onBind(Intent intent) {
        return sRedditSyncAdapter.getSyncAdapterBinder();
    }
}