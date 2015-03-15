package com.example.android.reddit.data;

import android.annotation.TargetApi;
import android.content.ContentProvider;
import android.content.ContentValues;
import android.content.UriMatcher;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteQueryBuilder;
import android.net.Uri;

/**
 * Created by Algirdas on 2015-03-10.
 */
public class RedditProvider extends ContentProvider {

    private static final UriMatcher sUriMatcher = buildUriMatcher();
    private RedditDbHelper mOpenHelper;

    static final int POSTS = 100;
    static final int COMMENTS = 200;

    @Override
    public boolean onCreate() {
        mOpenHelper = new RedditDbHelper(getContext());
        return true;
    }

    static UriMatcher buildUriMatcher() {
        final UriMatcher matcher = new UriMatcher(UriMatcher.NO_MATCH);
        final String authority = RedditContract.CONTENT_AUTHORITY;

        // For each type of URI you want to add, create a corresponding code.
        matcher.addURI(authority, RedditContract.PATH_POSTS, POSTS);
        matcher.addURI(authority, RedditContract.PATH_COMMENTS, COMMENTS);
        return matcher;
    }

    @Override
    public Cursor query(Uri uri, String[] projection, String selection, String[] selectionArgs, String sortOrder) {
        Cursor retCursor;
        switch (sUriMatcher.match(uri)) {
            case POSTS: {
                retCursor = mOpenHelper.getReadableDatabase().query(
                        RedditContract.PostsEntry.TABLE_NAME,
                        projection,
                        selection,
                        selectionArgs,
                        null,
                        null,
                        sortOrder
                );
                break;
            }
            case COMMENTS: {
                retCursor = mOpenHelper.getReadableDatabase().query(
                        RedditContract.Comments.TABLE_NAME,
                        projection,
                        selection,
                        selectionArgs,
                        null,
                        null,
                        sortOrder
                );
                break;
            }
            default:
                throw new UnsupportedOperationException("Unknown uri: " + uri);
        }
        retCursor.setNotificationUri(getContext().getContentResolver(), uri);
        return retCursor;
    }

    @Override
    public String getType(Uri uri) {
        final int match = sUriMatcher.match(uri);

        switch (match) {
            // Student: Uncomment and fill out these two cases
            case POSTS:
                return RedditContract.PostsEntry.CONTENT_TYPE;
            case COMMENTS:
                return RedditContract.Comments.CONTENT_TYPE;
            default:
                throw new UnsupportedOperationException("Unknown uri: " + uri);
        }
    }

    @Override
    public Uri insert(Uri uri, ContentValues values) {
        final SQLiteDatabase db = mOpenHelper.getWritableDatabase();
        final int match = sUriMatcher.match(uri);
        Uri returnUri;

        switch (match) {
            case POSTS: {
                long _id = db.insert(RedditContract.PostsEntry.TABLE_NAME, null, values);
                if ( _id > 0 )
                    returnUri = RedditContract.PostsEntry.buildPostsUri(_id);
                else
                    throw new android.database.SQLException("Failed to insert row into " + uri);
                break;
            }
            case COMMENTS: {
                long _id = db.insert(RedditContract.Comments.TABLE_NAME, null, values);
                if ( _id > 0 )
                    returnUri = RedditContract.Comments.buildPostsUri(_id);
                else
                    throw new android.database.SQLException("Failed to insert row into " + uri);
                break;
            }
            default:
                throw new UnsupportedOperationException("Unknown uri: " + uri);
        }
        getContext().getContentResolver().notifyChange(uri, null);
        return returnUri;
    }

    @Override
    public int delete(Uri uri, String selection, String[] selectionArgs) {
        final SQLiteDatabase db = mOpenHelper.getWritableDatabase();
        final int match = sUriMatcher.match(uri);
        int rowsDeleted;
        // this makes delete all rows return the number of rows deleted
        if ( null == selection ) selection = "1";
        switch (match) {
            case POSTS:
                rowsDeleted = db.delete(
                        RedditContract.PostsEntry.TABLE_NAME, selection, selectionArgs);
                break;
            case COMMENTS:
                rowsDeleted = db.delete(
                        RedditContract.Comments.TABLE_NAME, selection, selectionArgs);
                break;
            default:
                throw new UnsupportedOperationException("Unknown uri: " + uri);
        }
        // Because a null deletes all rows
        if (rowsDeleted != 0) {
            getContext().getContentResolver().notifyChange(uri, null);
        }
        return rowsDeleted;
    }

    @Override
    public int update(Uri uri, ContentValues values, String selection, String[] selectionArgs) {
            final SQLiteDatabase db = mOpenHelper.getWritableDatabase();
            final int match = sUriMatcher.match(uri);
            int rowsUpdated;

            switch (match) {
                case POSTS:
                    rowsUpdated = db.update(RedditContract.PostsEntry.TABLE_NAME, values, selection,
                            selectionArgs);
                    break;
                case COMMENTS:
                    rowsUpdated = db.update(RedditContract.Comments.TABLE_NAME, values, selection,
                            selectionArgs);
                    break;
                default:
                    throw new UnsupportedOperationException("Unknown uri: " + uri);
            }
            if (rowsUpdated != 0) {
                getContext().getContentResolver().notifyChange(uri, null);
            }
            return rowsUpdated;
    }

    @Override
    public int bulkInsert(Uri uri, ContentValues[] values) {
        final SQLiteDatabase db = mOpenHelper.getWritableDatabase();
        final int match = sUriMatcher.match(uri);
        int returnCount = 0;
        switch (match) {
            case POSTS:
                db.beginTransaction();

                try {
                    for (ContentValues value : values) {
                        long _id = db.insert(RedditContract.PostsEntry.TABLE_NAME, null, value);
                        if (_id != -1) {
                            returnCount++;
                        }
                    }
                    db.setTransactionSuccessful();
                } finally {
                    db.endTransaction();
                }
                getContext().getContentResolver().notifyChange(uri, null);
                return returnCount;
            case COMMENTS:
                db.beginTransaction();
                try {
                    for (ContentValues value : values) {
                        long _id = db.insert(RedditContract.Comments.TABLE_NAME, null, value);
                        if (_id != -1) {
                            returnCount++;
                        }
                    }
                    db.setTransactionSuccessful();
                } finally {
                    db.endTransaction();
                }
                getContext().getContentResolver().notifyChange(uri, null);
                return returnCount;
            default:
                return super.bulkInsert(uri, values);
        }
    }
}
