package com.example.android.reddit.data;

import android.content.ContentResolver;
import android.content.ContentUris;
import android.net.Uri;
import android.provider.BaseColumns;
import android.text.format.Time;

/**
 * Created by Algirdas on 2015-03-10.
 */
public class RedditContract {

    // The "Content authority" is a name for the entire content provider, similar to the
    // relationship between a domain name and its website.  A convenient string to use for the
    // content authority is the package name for the app, which is guaranteed to be unique on the
    // device.
    public static final String CONTENT_AUTHORITY = "com.example.android.reddit";

    // Use CONTENT_AUTHORITY to create the base of all URI's which apps will use to contact
    // the content provider.
    public static final Uri BASE_CONTENT_URI = Uri.parse("content://" + CONTENT_AUTHORITY);

    // Possible paths (appended to base content URI for possible URI's)
    // For instance, content://com.example.android.sunshine.app/weather/ is a valid path for
    // looking at weather data. content://com.example.android.sunshine.app/givemeroot/ will fail,
    // as the ContentProvider hasn't been given any information on what to do with "givemeroot".
    // At least, let's hope not.  Don't be that dev, reader.  Don't be that dev.
    public static final String PATH_POSTS = "posts";
    public static final String PATH_COMMENTS= "comments";

    public static final class PostsEntry implements BaseColumns {

        public static final Uri CONTENT_URI = BASE_CONTENT_URI.buildUpon().appendPath(PATH_POSTS).build();

        public static final String CONTENT_TYPE =
                ContentResolver.CURSOR_DIR_BASE_TYPE + "/" + CONTENT_AUTHORITY + "/" + PATH_POSTS;
        public static final String CONTENT_ITEM_TYPE =
                ContentResolver.CURSOR_ITEM_BASE_TYPE + "/" + CONTENT_AUTHORITY + "/" + PATH_POSTS;

        // Table name
        public static final String TABLE_NAME = "posts";

        public final static String COLUMN_DOMAIN = "domain";
        public final static String COLUMN_SUBREDDIT = "subreddit";
        public final static String COLUMN_SELFTEXT = "selftext_html";
        public final static String COLUMN_ID = "id";
        public final static String COLUMN_SCORE = "score";
        public final static String COLUMN_NSFW = "over_18";
        public final static String COLUMN_PERMALINK = "permalink";
        public final static String COLUMN_TITLE = "title";
        public final static String COLUMN_VISITED = "visited";
        public final static String COLUMN_THUMBNAIL_URL = "thumbnail_url";
        public final static String COLUMN_THUMBNAIL = "thumbnail";
        public final static String COLUMN_NUM_COMMENTS = "num_comments";
        public final static String COLUMN_URL = "url";
        public final static String COLUMN_KIND = "kind";
        public final static String COLUMN_BODY = "body";

        public static Uri buildPostsUri(long id) {
            return ContentUris.withAppendedId(CONTENT_URI, id);
        }
    }

    public static final class Comments implements BaseColumns {

        public static final Uri CONTENT_URI = BASE_CONTENT_URI.buildUpon().appendPath(PATH_COMMENTS).build();

        public static final String CONTENT_TYPE =
                ContentResolver.CURSOR_DIR_BASE_TYPE + "/" + CONTENT_AUTHORITY + "/" + PATH_COMMENTS;
        public static final String CONTENT_ITEM_TYPE =
                ContentResolver.CURSOR_ITEM_BASE_TYPE + "/" + CONTENT_AUTHORITY + "/" + PATH_COMMENTS;

        // Table name
        public static final String TABLE_NAME = "comments";
        public final static String COLUMN_ID = "id";
        public final static String COLUMN_SCORE = "score";
        public final static String COLUMN_KIND = "kind";
        public final static String COLUMN_BODY = "body";

        public static Uri buildPostsUri(long id) {
            return ContentUris.withAppendedId(CONTENT_URI, id);
        }
    }


}
