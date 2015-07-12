package com.junecong.pjtwo;

import android.app.Activity;
import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.support.v4.app.NotificationCompat;
import android.support.v4.app.NotificationManagerCompat;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import com.twitter.sdk.android.core.Callback;
import com.twitter.sdk.android.core.Result;
import com.twitter.sdk.android.core.TwitterApiClient;
import com.twitter.sdk.android.core.TwitterCore;
import com.twitter.sdk.android.core.TwitterException;
import com.twitter.sdk.android.core.models.Search;
import com.twitter.sdk.android.core.models.Tweet;
import com.twitter.sdk.android.core.services.SearchService;
import com.twitter.sdk.android.tweetcomposer.TweetComposer;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URI;
import java.net.URL;
import java.util.List;
import java.util.Random;
import java.util.concurrent.ExecutionException;

public class Activity2 extends Activity  {

    static final int REQUEST = 1;
    static File STATIC_PHOTO = null;
    Tweet selected;
    public static String tweetSelected = null;
    public static String profileURL = null;
    public Bitmap bip = null;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.tweetactivity);

        File storageDir = Environment.getExternalStoragePublicDirectory(
            Environment.DIRECTORY_PICTURES);
        try {
            STATIC_PHOTO = File.createTempFile("STATIC_PHOTO", ".jpg", storageDir);
            takePicture();
        } catch (IOException except){
            Log.d("File storage", "IO Exception");
        }
    }

    private void takePicture() {
        Intent intent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);

        if (intent.resolveActivity(getPackageManager()) != null){
            intent.putExtra(MediaStore.EXTRA_OUTPUT, Uri.fromFile(STATIC_PHOTO));
            startActivityForResult(intent, REQUEST);
        } else {
            Log.d("Error A2", "Take Picture");
        }

    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {

        composeTweet();
        findTweets();
    }


    public void sendNote(Bitmap bip){
        NotificationCompat.WearableExtender wearableExtender =
                new NotificationCompat.WearableExtender()
                        .setBackground(bip);


        Intent open_activity_intent = new Intent(Intent.ACTION_VIEW);
        Uri profileUri = Uri.parse("https://twitter.com");
        open_activity_intent.setData(profileUri);
        PendingIntent twitterPendingIntent = PendingIntent.getActivity(this, 0,
                open_activity_intent, PendingIntent.FLAG_CANCEL_CURRENT);


        Notification notification =
            new NotificationCompat.Builder(this)
                .setSmallIcon(R.drawable.ic_launcher)
                .setContentTitle("Excited?")
                .setContentText("Your classmate posted this")
                    .extend(wearableExtender)
                .setAutoCancel(true)
                .addAction(R.drawable.twitter, "Open Twitter", twitterPendingIntent)
                    .build();

        NotificationManagerCompat notificationManager = NotificationManagerCompat.from(this);
        int notificationId = 2;
        notificationManager.notify(notificationId, notification);



//  Working code below
//        Notification notification =
//                new NotificationCompat.Builder(this)
//                        .setSmallIcon(R.drawable.ic_launcher)
//                        .setContentTitle("Excited?")
//                        .setContentText("Your classmate posted this")
//                        .extend(wearableExtender)
//                        .addAction(R.drawable.ic_launcher, "Open Camera", pendingInt)
//                        .build();
//
//        NotificationManagerCompat notificationManager =
//                NotificationManagerCompat.from(this);
//
//        int notificationId = 1;
//        notificationManager.notify(notificationId, notification);

    }

    public void composeTweet(){
        Uri myImageUri = Uri.fromFile(STATIC_PHOTO);

        TweetComposer.Builder builder = new TweetComposer.Builder(this)
            .text("#cs160excited")
            .image(myImageUri);
        builder.show();
    }

    public void findTweets(){
        TwitterApiClient twitterApiClient = TwitterCore.getInstance().getApiClient();
        SearchService searchService = twitterApiClient.getSearchService();
        searchService.tweets("#cs160excited", null, null, null, "recent", null, null, null, null, true, new Callback<Search>() {
            @Override
            public void success(Result<Search> searchResult) {
                List<Tweet> tweets = searchResult.data.tweets;
                selected = tweets.get(randomGen());

                Log.d("TweetURL", "thoo");
                Activity2.tweetSelected = selected.entities.media.get(0).mediaUrl;
                Log.d("TweetSelected", tweetSelected);
//                Activity2.profileURL = selected.entities.urls.get(0).displayUrl.toString();



                GetImageClass imageClass = new GetImageClass();
                try {
                    Thread.sleep(4000);
                    try {
                        bip = imageClass.execute(tweetSelected).get();
                        Log.d("Bip is", String.valueOf(bip));
                        if (bip != null) {
                            sendNote(bip);
                        }

                    } catch (InterruptedException | ExecutionException e) {
                        Log.e("onActivityRes 2", "First exception");
                    }
                } catch (Exception e) {
                    Log.e("onActivityRes 2", "Second Exception");
                }

            }

            @Override
            public void failure(TwitterException error) {
                Log.d("TweetSpotTag", "Fail search");
            }
        });
    }

    public int randomGen(){
        Random randomGenerator = new Random();
        int randomInt = randomGenerator.nextInt(10);
        return randomInt;
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();
        if (id == R.id.action_settings) {
            return true;
        }
        return super.onOptionsItemSelected(item);
    }


    @Override
    protected void onStart() {
        super.onStart();
    }

    public class GetImageClass extends AsyncTask<String, Void, Bitmap> {
        Bitmap bip = null;

        @Override
        protected Bitmap doInBackground(String... urll) {
            try{
                URL mURL = new URL(urll[0]);
                if (mURL == null){
                    Log.d("in Get Image Class", "mURL is null");
                }
                HttpURLConnection connection = (HttpURLConnection) mURL.openConnection();
                connection.setDoInput(true);
                Log.d("4", "3");
                connection.connect();
                Log.d("5", String.valueOf(connection));
                InputStream input = connection.getInputStream();
                Log.d("6", "IO");
                bip = BitmapFactory.decodeStream(input);
                Log.d("7", "IO");
            } catch(MalformedURLException x){
                x.printStackTrace();
            } catch(IOException c){
                c.printStackTrace();
            }
            if (bip == null){
                Log.d("Get Image Class in A2", "Bip is Null");
            }
            return bip;
        }
    }


}


