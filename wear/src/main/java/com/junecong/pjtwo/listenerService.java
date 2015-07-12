package com.junecong.pjtwo;

import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.os.IBinder;
import android.util.Log;

public class listenerService extends Service implements SensorEventListener {
    private SensorManager mSensorManager;
    private Sensor mSensor;
    private static final int TOO_MUCH_SHAKE = 100;

    @Override
    public void onCreate(){
        mSensorManager = (SensorManager) getSystemService(Context.SENSOR_SERVICE);
        mSensor = mSensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER);
        mSensorManager.registerListener(this, mSensor , SensorManager.SENSOR_DELAY_NORMAL);
        Log.d("service started", "service started");

    }

    @Override
    public void onSensorChanged(SensorEvent event) {
        Sensor mySensor = event.sensor;

        if (mySensor.getType() == Sensor.TYPE_ACCELEROMETER) {
            float x = event.values[0];
            float y = event.values[1];
            float z = event.values[2];

            float shakenBy = Math.abs(x + y + z);

            if (shakenBy > TOO_MUCH_SHAKE) {
                Log.d("shake threshold reached", "shake threshold reached");
                sendNotification();
            }
        }
    }

    public void sendNotification() {
        Intent sendMessageIntent = new Intent(this, sendMessageService.class);
        PendingIntent pendingInt = PendingIntent.getService(
                this.getApplicationContext(), 0, sendMessageIntent, 0);

        Bitmap bitmap = null;

        Notification notification = new Notification.Builder(getApplication())
        .setSmallIcon(R.drawable.ic_launcher)
        .setContentTitle("Excited?")
        .setContentText("Snap a picture!")
        .addAction(R.drawable.camera, "Open Camera", pendingInt)
        .extend(new Notification.WearableExtender().setBackground(bitmap))
        .build();

        NotificationManager notificationManager = (NotificationManager) getSystemService(NOTIFICATION_SERVICE);
        notificationManager.notify(1, notification);
    }

//    public PendingIntent capturePhoto() {
//        Intent CameraIntent = new Intent(MediaStore.INTENT_ACTION_STILL_IMAGE_CAMERA);
//        PendingIntent CameraOpen = PendingIntent.getActivity(this, 0, CameraIntent, 0);
//        return CameraOpen;
//    }


    @Override
    public void onAccuracyChanged(Sensor sensor, int accuracy) {
        Log.d("Accuracy Changed", "Accuracy Changed");
    }

    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }



}