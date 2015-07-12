package com.junecong.pjtwo;

import android.app.PendingIntent;
import android.content.Intent;
import android.provider.MediaStore;
import android.util.Log;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.wearable.MessageEvent;
import com.google.android.gms.wearable.Wearable;
import com.google.android.gms.wearable.WearableListenerService;


public class wearableListener extends WearableListenerService {
    private GoogleApiClient mGoogleApiClient;
    private static final String WEARABLE_LISTENER_PATH = "wearableListener";

    @Override
    public void onCreate() {
        super.onCreate();
        Log.d("wearable Listr started?", "y");

        mGoogleApiClient = new GoogleApiClient.Builder(this)
                .addApi(Wearable.API)
                .build();
        mGoogleApiClient.connect();
        Log.d("first Mobile Connected?", String.valueOf(mGoogleApiClient.isConnected()));
    }

    @Override
    public void onMessageReceived(MessageEvent messageEvent) {
        Log.d("second Connected?", String.valueOf(mGoogleApiClient.isConnected()));
        Log.d("MSG RECEIVED", "wearable listener");
        if (messageEvent.getPath().equals(WEARABLE_LISTENER_PATH)) {
            Log.d("MSG", "wearable listener path called!");
            Intent startIntent = new Intent(this, Activity2.class);
            startIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            startActivity(startIntent);
        }
    }


}
