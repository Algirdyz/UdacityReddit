package com.example.android.reddit.data;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

/**
 * Created by Algirdas on 2015-03-10.
 */
public class RedditDbHelper extends SQLiteOpenHelper {

    // If you change the database schema, you must increment the database version.
    private static final int DATABASE_VERSION = 11;

    static final String DATABASE_NAME = "reddit.db";

    public RedditDbHelper(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase sqLiteDatabase) {
        // Create a table to hold locations.  A location consists of the string supplied in the
        // location setting, the city name, and the latitude and longitude
        final String SQL_CREATE_POSTS_TABLE = "CREATE TABLE " + RedditContract.PostsEntry.TABLE_NAME + " (" +
                RedditContract.PostsEntry._ID + " INTEGER PRIMARY KEY," +
                RedditContract.PostsEntry.COLUMN_DOMAIN + " TEXT, " +
                RedditContract.PostsEntry.COLUMN_SUBREDDIT + " TEXT, " +
                RedditContract.PostsEntry.COLUMN_SELFTEXT + " TEXT, " +
                RedditContract.PostsEntry.COLUMN_ID + " TEXT NOT NULL, " +
                RedditContract.PostsEntry.COLUMN_SCORE + " INTEGER, " +
                RedditContract.PostsEntry.COLUMN_NSFW + " INTEGER, " +
                RedditContract.PostsEntry.COLUMN_PERMALINK + " TEXT, " +
                RedditContract.PostsEntry.COLUMN_TITLE + " TEXT, " +
                RedditContract.PostsEntry.COLUMN_VISITED + " INTEGER, " +
                RedditContract.PostsEntry.COLUMN_THUMBNAIL_URL + " TEXT, " +
                RedditContract.PostsEntry.COLUMN_THUMBNAIL + " BLOB," +
                RedditContract.PostsEntry.COLUMN_NUM_COMMENTS + " TEXT, " +
                RedditContract.PostsEntry.COLUMN_URL + " TEXT, " +
                RedditContract.PostsEntry.COLUMN_KIND + " TEXT NOT NULL, " +
                RedditContract.PostsEntry.COLUMN_BODY + " TEXT " +

        " );";

        final String SQL_CREATE_COMMENTS_TABLE = "CREATE TABLE " + RedditContract.Comments.TABLE_NAME + " (" +
                RedditContract.Comments._ID + " INTEGER PRIMARY KEY," +
                RedditContract.Comments.COLUMN_ID + " TEXT NOT NULL, " +
                RedditContract.Comments.COLUMN_SCORE + " INTEGER NOT NULL, " +
                RedditContract.Comments.COLUMN_KIND + " TEXT NOT NULL, " +
                RedditContract.Comments.COLUMN_BODY + " TEXT NOT NULL" +

                " );";

        sqLiteDatabase.execSQL(SQL_CREATE_POSTS_TABLE);
        sqLiteDatabase.execSQL(SQL_CREATE_COMMENTS_TABLE);
    }

    @Override
    public void onUpgrade(SQLiteDatabase sqLiteDatabase, int oldVersion, int newVersion) {
        sqLiteDatabase.execSQL("DROP TABLE IF EXISTS " + RedditContract.PostsEntry.TABLE_NAME);
        sqLiteDatabase.execSQL("DROP TABLE IF EXISTS " + RedditContract.Comments.TABLE_NAME);
        onCreate(sqLiteDatabase);
    }
}
