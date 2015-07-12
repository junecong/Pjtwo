package com.junecong.pjtwo;

import android.app.NotificationManager;
import android.app.Service;
import android.content.Intent;
import android.os.Bundle;
import android.os.IBinder;
import android.util.Log;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.common.api.GoogleApiClient.ConnectionCallbacks;
import com.google.android.gms.common.api.Result;
import com.google.android.gms.common.api.ResultCallback;
import com.google.android.gms.wearable.CapabilityApi;
import com.google.android.gms.wearable.CapabilityInfo;
import com.google.android.gms.wearable.MessageApi;
import com.google.android.gms.wearable.Node;
import com.google.android.gms.wearable.NodeApi;
import com.google.android.gms.wearable.Wearable;
import java.util.Arrays;

import java.util.Random;
import java.util.Set;


public class sendMessageService extends Service implements GoogleApiClient.ConnectionCallbacks, GoogleApiClient.OnConnectionFailedListener {
    GoogleApiClient mGoogleApiClient;
    private static final String WEARABLE_LISTENER_PATH = "wearableListener";
    String STUFF = "stuff";
    private String mNodeId = null;

    @Override
    public void onCreate(){
        Log.d("sendMsgService Created ", "yay");
        NotificationManager notificationManager = (NotificationManager) getSystemService(NOTIFICATION_SERVICE);
        notificationManager.cancel(1);
        initGoogleApiClient();
    }

    private void initGoogleApiClient() {
        mGoogleApiClient = new GoogleApiClient.Builder( this )
                .addConnectionCallbacks(this)
                .addOnConnectionFailedListener(this)
                .addApi(Wearable.API)
                .build();

        mGoogleApiClient.connect();
        Log.e("Connected?", String.valueOf(mGoogleApiClient.isConnected()));
    }

    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override
    public void onConnected(Bundle bundle) {
        setUpCapListen();
    }


    private void setUpCapListen(){
        Log.d("setupcaplist", "start");

        new Thread(new Runnable() {
            public void run() {
            CapabilityApi.GetCapabilityResult capResult =
                Wearable.CapabilityApi.getCapability(
                    mGoogleApiClient, STUFF,
                    CapabilityApi.FILTER_REACHABLE)
                    .await();
            updateCapability(capResult.getCapability());
            }
        }).start();

        CapabilityApi.CapabilityListener capabilityListener =
            new CapabilityApi.CapabilityListener() {
                @Override
                public void onCapabilityChanged(CapabilityInfo capabilityInfo) {
                    updateCapability(capabilityInfo);
                }
            };

        Wearable.CapabilityApi.addCapabilityListener(
                mGoogleApiClient,
                capabilityListener,
                STUFF);
    }

    private void updateCapability(CapabilityInfo capabilityInfo) {
        Log.d("updateTranscriptionCap", "start");
        Set<Node> connectedNodes = capabilityInfo.getNodes();
        mNodeId = pickBestNodeId(connectedNodes);
        sendMessage();
        Log.e("Best Node ID", mNodeId);

    }

    private String pickBestNodeId(Set<Node> nodes) {
        Log.d("pickBestNodes", "start");
        String bestNodeId = null;
        // Find a nearby node or pick one arbitrarily
        for (Node node : nodes) {
            if (node.isNearby()) {
                return node.getId();
            }
            bestNodeId = node.getId();
        }
        return bestNodeId;
    }

//    public static String generateRandomWords() {
//        Random random = new Random();
//        char[] word = new char[random.nextInt(5)];
//        for(int j = 0; j < word.length; j++)
//        {
//            word[j] = (char)('a' + random.nextInt(26));
//        }
//        String str = new String(word);
//        return str;
//    }

    private void sendMessage() {
        Log.d("sendMessage", "start");
//        String random = generateRandomWords();
//        byte[] rand = random.getBytes();
        byte[] rand = "hello".getBytes();

            Wearable.MessageApi.sendMessage(mGoogleApiClient, mNodeId,
                    WEARABLE_LISTENER_PATH, rand).setResultCallback(new ResultCallback<MessageApi.SendMessageResult>() {
                @Override
                public void onResult(MessageApi.SendMessageResult sendMessageResult) {
                    Log.e("sendM nodeID", String.valueOf(mNodeId));
                    Log.e("send status", String.valueOf(sendMessageResult.getStatus()));
                    Log.e("send request id", String.valueOf(sendMessageResult.getRequestId()));
                    if (!sendMessageResult.getStatus().isSuccess()) {
                        Log.e("BIG PROBLEM", "Message send status = fail");
                    }
                }
            });

    }

    @Override
    public void onConnectionSuspended(int i) {
        Log.d("sendMsgService ", "connection sus");
    }

    @Override
    public void onConnectionFailed(ConnectionResult connectionResult) {
        Log.d("sendMsgService ", "connection failed");
    }
}
