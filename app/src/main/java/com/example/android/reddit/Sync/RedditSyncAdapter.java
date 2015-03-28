package com.example.android.reddit.Sync;

import android.accounts.Account;
import android.accounts.AccountManager;
import android.content.AbstractThreadedSyncAdapter;
import android.content.ContentProviderClient;
import android.content.ContentResolver;
import android.content.ContentValues;
import android.content.Context;
import android.content.SyncRequest;
import android.content.SyncResult;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;

import com.example.android.reddit.R;
import com.example.android.reddit.data.RedditContract;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.Vector;

/**
 * Created by Algirdas on 2015-03-28.
 */
public class RedditSyncAdapter extends AbstractThreadedSyncAdapter {

    public static final int SYNC_INTERVAL = 60 * 180;
    public static final int SYNC_FLEXTIME = SYNC_INTERVAL/3;
    private static final long DAY_IN_MILLIS = 1000 * 60 * 60 * 24;

    private final String LOG_TAG = RedditSyncAdapter.class.getSimpleName();

    final String RDT_CHILDREN = "children";
    final String RDT_DATA = "data";
    final String RDT_DOMAIN = "domain";
    final String RDT_SUBREDDIT = "subreddit";
    final String RDT_SELFTEXT = "selftext";
    final String RDT_ID = "id";
    final String RDT_SCORE = "score";
    final String RDT_NSFW = "over_18";
    final String RDT_PERMALINK = "permalink";
    final String RDT_TITLE = "title";
    final String RDT_VISITED = "visited";
    final String RDT_THUMBNAIL = "thumbnail";
    final String RDT_COMMENTS = "num_comments";
    final String RDT_URL = "url";
    final String RDT_KIND = "kind";
    final String RDT_BODY = "body";

    public RedditSyncAdapter(Context context, boolean autoInitialize) {
        super(context, autoInitialize);
    }

    @Override
    public void onPerformSync(Account account, Bundle extras, String authority, ContentProviderClient provider, SyncResult syncResult) {

        Log.v(LOG_TAG, "syncing");

        HttpURLConnection urlConnection = null;
        BufferedReader reader = null;
        String jsonStr = null;

        try {
            String REDDIT_URL = "http://www.reddit.com/hot.json?depth=1";

            Uri.Builder builder = Uri.parse(REDDIT_URL).buildUpon();

            URL url = new URL(REDDIT_URL);

//            Log.v("URL", url.toString());
//            Log.v("URI", builder.build().toString());

            urlConnection = (HttpURLConnection) url.openConnection();
            urlConnection.setRequestMethod("GET");

            urlConnection.connect();

            // Read the input stream into a String
            InputStream inputStream = urlConnection.getInputStream();
            StringBuffer buffer = new StringBuffer();
            if (inputStream == null) {
                // Nothing to do.
                return;
            }
            reader = new BufferedReader(new InputStreamReader(inputStream));

            String line;
            while ((line = reader.readLine()) != null) {
                // Since it's JSON, adding a newline isn't necessary (it won't affect parsing)
                // But it does make debugging a *lot* easier if you print out the completed
                // buffer for debugging.
                buffer.append(line + "\n");
            }

            if (buffer.length() == 0) {
                // Stream was empty.  No point in parsing.
                return;
            }
            jsonStr = buffer.toString();
            getDataFromJson(jsonStr);
        } catch (IOException e) {
            Log.e(LOG_TAG, "Error ", e);
            // If the code didn't successfully get the weather data, there's no point in attempting
            // to parse it.
        } catch (JSONException e) {
            Log.e(LOG_TAG, e.getMessage(), e);
            e.printStackTrace();
        } finally {
            if (urlConnection != null) {
                urlConnection.disconnect();
            }
            if (reader != null) {
                try {
                    reader.close();
                } catch (final IOException e) {
                    Log.e(LOG_TAG, "Error closing stream", e);
                }
            }
        }
        return;
    }

    private void getDataFromJson(String jsonStr) throws JSONException {
        try {
            JSONObject json = new JSONObject(jsonStr);
            JSONObject dataObject = json.getJSONObject(RDT_DATA);

            JSONArray dataArray = dataObject.getJSONArray(RDT_CHILDREN);

            Vector<ContentValues> cVVector = new Vector<ContentValues>(dataArray.length());

            for(int i = 0; i < dataArray.length(); i++) {
                // These are the values that will be collected.
                String domain;
                String subreddit;
                String selftext;
                String id;
                int score;
                boolean over_18;
                String permalink;
                String title;
                boolean visited;
                String thumbnail;
                int comments;
                String url;
                String kind;

                // Get the JSON object representing the day
                JSONObject post = dataArray.getJSONObject(i);
                kind = post.getString(RDT_KIND);
                JSONObject postData = post.getJSONObject(RDT_DATA);

                domain = postData.getString(RDT_DOMAIN);
                subreddit = postData.getString(RDT_SUBREDDIT);
                selftext = postData.getString(RDT_SELFTEXT);
                id = postData.getString(RDT_ID);
                score = postData.getInt(RDT_SCORE);

                over_18 = postData.getBoolean(RDT_NSFW);
                if(over_18)
                    continue;

                permalink = postData.getString(RDT_PERMALINK);
                title = postData.getString(RDT_TITLE);
                visited = postData.getBoolean(RDT_VISITED);
                thumbnail = postData.getString(RDT_THUMBNAIL);
                comments = postData.getInt(RDT_COMMENTS);
                url = postData.getString(RDT_URL);

                ContentValues postsValues = new ContentValues();

                postsValues.put(RedditContract.PostsEntry.COLUMN_DOMAIN, domain);
                postsValues.put(RedditContract.PostsEntry.COLUMN_SUBREDDIT, subreddit);
                postsValues.put(RedditContract.PostsEntry.COLUMN_SELFTEXT, selftext);
                postsValues.put(RedditContract.PostsEntry.COLUMN_ID, id);
                postsValues.put(RedditContract.PostsEntry.COLUMN_SCORE, score);
                postsValues.put(RedditContract.PostsEntry.COLUMN_NSFW, over_18);
                postsValues.put(RedditContract.PostsEntry.COLUMN_PERMALINK, permalink);
                postsValues.put(RedditContract.PostsEntry.COLUMN_TITLE, title);
                postsValues.put(RedditContract.PostsEntry.COLUMN_VISITED, visited);
                postsValues.put(RedditContract.PostsEntry.COLUMN_THUMBNAIL_URL, thumbnail);
                postsValues.put(RedditContract.PostsEntry.COLUMN_NUM_COMMENTS, comments);
                postsValues.put(RedditContract.PostsEntry.COLUMN_URL, url);
                postsValues.put(RedditContract.PostsEntry.COLUMN_KIND, kind);

                cVVector.add(postsValues);
            }

            int inserted = 0;
            // add to database
            if ( cVVector.size() > 0 ) {
                ContentValues[] cvArray = new ContentValues[cVVector.size()];
                cVVector.toArray(cvArray);
                getBitmapsFromURL(cvArray);
                getContext().getContentResolver().delete(RedditContract.PostsEntry.CONTENT_URI, null, null);
                getContext().getContentResolver().bulkInsert(RedditContract.PostsEntry.CONTENT_URI, cvArray);
            }

            Log.d(LOG_TAG, "Reddit Service Complete. " + cVVector.size() + " Inserted");

        } catch (JSONException e) {
            Log.e(LOG_TAG, e.getMessage(), e);
            e.printStackTrace();
        }
    }

    public static ContentValues[] getBitmapsFromURL(ContentValues[] array) {
        try {
            for (int i=0; i<array.length; i++)
            {
                String urlString = array[i].getAsString(RedditContract.PostsEntry.COLUMN_THUMBNAIL_URL);
                if(urlString.length()==0)
                    continue;
                URL url = new URL(urlString);
                HttpURLConnection connection = (HttpURLConnection) url.openConnection();
                connection.setDoInput(true);
                connection.connect();
                InputStream input = connection.getInputStream();
                Bitmap myBitmap = BitmapFactory.decodeStream(input);
                array[i].put(RedditContract.PostsEntry.COLUMN_THUMBNAIL, getBitmapAsByteArray(myBitmap));
            }
            return array;
        } catch (IOException e) {
            // Log exception
            return null;
        }
    }

    public static byte[] getBitmapAsByteArray(Bitmap bitmap) {
        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
        bitmap.compress(Bitmap.CompressFormat.PNG, 0, outputStream);
        return outputStream.toByteArray();
    }


    /**
     * Helper method to schedule the sync adapter periodic execution
     */
    public static void configurePeriodicSync(Context context, int syncInterval, int flexTime) {
        Account account = getSyncAccount(context);
        String authority = context.getString(R.string.content_authority);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
            // we can enable inexact timers in our periodic sync
            SyncRequest request = new SyncRequest.Builder().
                    syncPeriodic(syncInterval, flexTime).
                    setSyncAdapter(account, authority).
                    setExtras(new Bundle()).build();
            ContentResolver.requestSync(request);
        } else {
            ContentResolver.addPeriodicSync(account,
                    authority, new Bundle(), syncInterval);
        }
    }

    /**
     * Helper method to have the sync adapter sync immediately
     * @param context The context used to access the account service
     */
    public static void syncImmediately(Context context) {
        Bundle bundle = new Bundle();
        bundle.putBoolean(ContentResolver.SYNC_EXTRAS_EXPEDITED, true);
        bundle.putBoolean(ContentResolver.SYNC_EXTRAS_MANUAL, true);
        ContentResolver.requestSync(getSyncAccount(context),
                context.getString(R.string.content_authority), bundle);
    }

    /**
     * Helper method to get the fake account to be used with SyncAdapter, or make a new one
     * if the fake account doesn't exist yet.  If we make a new account, we call the
     * onAccountCreated method so we can initialize things.
     *
     * @param context The context used to access the account service
     * @return a fake account.
     */
    public static Account getSyncAccount(Context context) {
        // Get an instance of the Android account manager
        AccountManager accountManager =
                (AccountManager) context.getSystemService(Context.ACCOUNT_SERVICE);

        // Create the account type and default account
        Account newAccount = new Account(
                context.getString(R.string.app_name), context.getString(R.string.sync_account_type));

        // If the password doesn't exist, the account doesn't exist
        if ( null == accountManager.getPassword(newAccount) ) {

        /*
         * Add the account and account type, no password or user data
         * If successful, return the Account object, otherwise report an error.
         */
            if (!accountManager.addAccountExplicitly(newAccount, "", null)) {
                return null;
            }
            /*
             * If you don't set android:syncable="true" in
             * in your <provider> element in the manifest,
             * then call ContentResolver.setIsSyncable(account, AUTHORITY, 1)
             * here.
             */

            onAccountCreated(newAccount, context);
        }
        return newAccount;
    }

    private static void onAccountCreated(Account newAccount, Context context) {
        /*
         * Since we've created an account
         */
        RedditSyncAdapter.configurePeriodicSync(context, SYNC_INTERVAL, SYNC_FLEXTIME);

        /*
         * Without calling setSyncAutomatically, our periodic sync will not be enabled.
         */
        ContentResolver.setSyncAutomatically(newAccount, context.getString(R.string.content_authority), true);

        /*
         * Finally, let's do a sync to get things started
         */
        syncImmediately(context);
    }

    public static void initializeSyncAdapter(Context context) {
        getSyncAccount(context);
    }
}
