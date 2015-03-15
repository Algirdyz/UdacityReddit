package com.example.android.reddit;

import android.app.IntentService;
import android.content.ContentValues;
import android.content.CursorLoader;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.Bundle;
import android.os.ResultReceiver;
import android.text.TextUtils;
import android.util.Log;
import android.widget.ArrayAdapter;

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
import java.util.ArrayList;
import java.util.Vector;


public class RedditService extends IntentService {
    private ArrayAdapter<String> mForecastAdapter;
    public static final String LOCATION_QUERY_EXTRA = "lqe";
    private final String LOG_TAG = RedditService.class.getSimpleName();
    public RedditService() {
        super("Reddit");
    }

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

    @Override
    protected void onHandleIntent(Intent intent) {

        HttpURLConnection urlConnection = null;
        BufferedReader reader = null;

        final String after = intent.getStringExtra("after");
        final String postId = intent.getStringExtra("id");
        String jsonStr = null;

        try {
            String REDDIT_URL;

            if(postId != null){
                REDDIT_URL = "http://www.reddit.com/comments/" + postId + ".json?";
            }
            else{
                REDDIT_URL = "http://www.reddit.com/hot.json?depth=1";
            }

            Uri.Builder builder = Uri.parse(REDDIT_URL).buildUpon();

            if(after != null){
                Log.v("URL", "building url");
                builder.appendQueryParameter("after", after);
            }
            //builder.appendQueryParameter("limit", "25");
            URL url = new URL(builder.build().toString());

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
            if(postId!=null){
                getCommentsFromJson(jsonStr);
            }
            else{
                getDataFromJson(jsonStr);
            }
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
                this.getContentResolver().delete(RedditContract.PostsEntry.CONTENT_URI, null, null);
                this.getContentResolver().bulkInsert(RedditContract.PostsEntry.CONTENT_URI, cvArray);
            }

            Log.d(LOG_TAG, "Reddit Service Complete. " + cVVector.size() + " Inserted");

        } catch (JSONException e) {
            Log.e(LOG_TAG, e.getMessage(), e);
            e.printStackTrace();
        }
    }

    private void getCommentsFromJson(String jsonStr) throws JSONException {
        try {
            JSONArray json = new JSONArray(jsonStr);

                JSONObject dataObject = json.getJSONObject(1);

                JSONObject commentData = dataObject.getJSONObject(RDT_DATA);
                JSONArray dataArray = commentData.getJSONArray(RDT_CHILDREN);

                Vector<ContentValues> cVVector = new Vector<ContentValues>(dataArray.length());

                for(int i = 0; i < dataArray.length(); i++) {
                    // These are the values that will be collected.
                    String id;
                    int score;
                    String kind;
                    String body;
                    ContentValues postsValues = new ContentValues();
                    // Get the JSON object representing the day
                    JSONObject post = dataArray.getJSONObject(i);
                    kind = post.getString(RDT_KIND);
                    JSONObject postData = post.getJSONObject(RDT_DATA);
                    id = postData.getString(RDT_ID);
                    //Log.v("score", id);
                    if(!postData.has(RDT_SCORE))
                        continue;
                    if(!postData.has(RDT_BODY))
                        continue;
                    score = postData.getInt(RDT_SCORE);
                    body = postData.getString(RDT_BODY);

                    postsValues.put(RedditContract.Comments.COLUMN_ID, id);
                    postsValues.put(RedditContract.Comments.COLUMN_SCORE, score);
                    postsValues.put(RedditContract.Comments.COLUMN_KIND, kind);
                    postsValues.put(RedditContract.Comments.COLUMN_BODY, body);

                    cVVector.add(postsValues);




                }
            if ( cVVector.size() > 0 ) {
                ContentValues[] cvArray = new ContentValues[cVVector.size()];
                cVVector.toArray(cvArray);
                this.getContentResolver().delete(RedditContract.Comments.CONTENT_URI, null, null);
                this.getContentResolver().bulkInsert(RedditContract.Comments.CONTENT_URI, cvArray);
            }
            Log.d(LOG_TAG, "Reddit Comments Service Complete. " + cVVector.size() + " Inserted");

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
}