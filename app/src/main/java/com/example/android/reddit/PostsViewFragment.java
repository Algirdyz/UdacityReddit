package com.example.android.reddit;

import android.annotation.TargetApi;
import android.app.IntentService;
import android.app.ProgressDialog;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.Message;
import android.support.v4.app.Fragment;
import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.support.v4.app.LoaderManager;
import android.support.v4.content.CursorLoader;
import android.support.v4.content.Loader;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.TextView;

import com.example.android.reddit.data.RedditContract;

import java.io.InputStream;
import java.net.URI;
import java.net.URL;

/**
 * Created by Algirdas on 2015-03-10.
 */

public class PostsViewFragment extends Fragment implements LoaderManager.LoaderCallbacks<Cursor> {

    static final int COL_POSTS_ID = 0;
    static final int COLUMN_DOMAIN = 1;
    static final int COLUMN_SUBREDDIT = 2;
    static final int COLUMN_SELFTEXT = 3;
    static final int COLUMN_ID = 4;
    static final int COLUMN_SCORE = 5;
    static final int COLUMN_NSFW = 6;
    static final int COLUMN_PERMALINK = 7;
    static final int COLUMN_TITLE = 8;
    static final int COLUMN_VISITED = 9;
    static final int COLUMN_THUMBNAIL_URL = 10;
    static final int COLUMN_THUMBNAIL = 11;
    static final int COLUMN_NUM_COMMENTS = 12;
    static final int COLUMN_URL = 13;
    static final int COLUMN_KIND = 14;
    static final int COLUMN_BODY = 15;

    private static final int LOADER = 0;

    private DownloadResultReceiver mReceiver;

    ProgressDialog mDialog;
    private final static String TAG = "PostsViewFragment";

    private static final String COMMENTSFRAGMENT_TAG = "CFTAG";

    private View view;
    private Cursor cursor;
    private int cursorPosition;

    public PostsViewFragment() {
    }

    public void setMtwoPane(Boolean mtwoPane) {
        this.mtwoPane = mtwoPane;
    }

    private Boolean mtwoPane = false;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        view = inflater.inflate(R.layout.fragment_main, container, false);

        mDialog = new ProgressDialog(getActivity());
        mDialog.setMessage("Please wait...");
        mDialog.setCancelable(true);
        mDialog.show();


        view.findViewById(R.id.dismissButton).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(!cursor.isLast()) {
                    if (mtwoPane) {
                        cursor.moveToNext();
                        buildPost(cursor);

                        CommentsFragment fragment = new CommentsFragment();
                        Bundle args = new Bundle();
                        args.putString("postId", cursor.getString(COLUMN_ID));
                        fragment.setArguments(args);

                        getActivity().getSupportFragmentManager().beginTransaction()
                                .replace(R.id.comments_container, fragment, COMMENTSFRAGMENT_TAG)
                                .commit();
                    } else {
                        cursor.moveToNext();
                        buildPost(cursor);
                    }
                }
                else{
                    Log.v(TAG, "starting service");
                    Intent intent = new Intent(getActivity(), RedditService.class);
                    getActivity().startService(intent);
                }
            }
        });

        view.findViewById(R.id.content_link).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent browserIntent = new Intent(Intent.ACTION_VIEW, Uri.parse(cursor.getString(COLUMN_URL)));
                startActivity(browserIntent);
            }
        });

        view.findViewById(R.id.num_comments).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(getActivity(), CommentsActivity.class);
                intent.putExtra("postId", cursor.getString(COLUMN_ID));
                startActivity(intent);
            }
        });

        getLoaderManager().initLoader(LOADER, null, this);

        return view;
    }

    private void buildPost(Cursor cursor){
        ((TextView) view.findViewById(R.id.title)).setText(cursor.getString(COLUMN_TITLE));
        ((TextView) view.findViewById(R.id.subreddit)).setText(cursor.getString(COLUMN_SUBREDDIT));
        ((TextView) view.findViewById(R.id.score)).setText(String.format(getActivity().getString(R.string.points), cursor.getInt(COLUMN_SCORE)));
        ((TextView) view.findViewById(R.id.num_comments)).setText(String.format(getActivity().getString(R.string.comments), cursor.getInt(COLUMN_NUM_COMMENTS)));
        byte[] byteArray = cursor.getBlob(COLUMN_THUMBNAIL);
        if(byteArray != null){
            view.findViewById(R.id.content).setVisibility(View.GONE);
            view.findViewById(R.id.thumbnail).setVisibility(View.VISIBLE);
            Bitmap bm = BitmapFactory.decodeByteArray(byteArray, 0, byteArray.length);
            ((ImageView) view.findViewById(R.id.thumbnail)).setImageBitmap(bm);

            if(cursor.getString(COLUMN_DOMAIN) == "youtube.com"){
                view.findViewById(R.id.thumbnail_cover).setVisibility(View.VISIBLE);
            }
            else{
                view.findViewById(R.id.thumbnail_cover).setVisibility(View.GONE);
            }
        }
        else {
            view.findViewById(R.id.thumbnail_cover).setVisibility(View.GONE);
            View contentView = view.findViewById(R.id.content);
            contentView.setVisibility(View.VISIBLE);
            view.findViewById(R.id.thumbnail).setVisibility(View.GONE);
            ((TextView)contentView.findViewById(R.id.link)).setText(cursor.getString(COLUMN_URL));
            URI uri = URI.create(cursor.getString(COLUMN_URL));
            ((TextView)contentView.findViewById(R.id.linkAuthority)).setText(uri.getHost());
        }
    }

    public static Drawable LoadImageFromWebOperations(String url) {
        try {
            InputStream is = (InputStream) new URL(url).getContent();
            Drawable d = Drawable.createFromStream(is, "src name");
            return d;
        } catch (Exception e) {
            return null;
        }
    }

    @Override
    public void onSaveInstanceState(Bundle savedInstanceState) {
        super.onSaveInstanceState(savedInstanceState);
        savedInstanceState.putInt("CursorPosition", cursor.getPosition());
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        if (savedInstanceState != null) {
            cursorPosition = savedInstanceState.getInt("CursorPosition", 0);
        }
    }

    @Override
    public Loader<Cursor> onCreateLoader(int id, Bundle args) {
        return new CursorLoader(
                getActivity(),
                RedditContract.PostsEntry.CONTENT_URI,
                null,
                null,
                null,
                null);
    }

    @Override
    public void onLoadFinished(Loader<Cursor> loader, Cursor data) {
        if(loader.getId() == LOADER) {
            cursor = data;
            cursor.moveToPosition(cursorPosition);
            buildPost(cursor);
            mDialog.cancel();
        }
    }

    @Override
    public void onLoaderReset(Loader<Cursor> loader) {

    }
}
