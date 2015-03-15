package com.example.android.reddit;

import android.app.ProgressDialog;
import android.content.Intent;
import android.database.Cursor;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.LoaderManager;
import android.support.v4.content.CursorLoader;
import android.support.v4.content.Loader;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ListView;

import com.example.android.reddit.data.RedditContract;

/**
 * Created by Algirdas on 2015-03-13.
 */
public class CommentsFragment extends Fragment implements LoaderManager.LoaderCallbacks<Cursor> {

    static final int COL_COMMENTS_ID = 0;
    static final int COLUMN_ID = 1;
    static final int COLUMN_SCORE = 2;
    static final int COLUMN_KIND= 3;
    static final int COLUMN_BODY = 4;


    private CommentsAdapter mCommentsAdapter;
    private View view;
    private ListView mListView;
    private static final int COMMENTS_LOADER = 1;
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        mCommentsAdapter = new CommentsAdapter(getActivity(), null, 0);
        view = inflater.inflate(R.layout.fragment_comments, container, false);
        mListView = (ListView) view.findViewById(R.id.listview_comments);
        mListView.setAdapter(mCommentsAdapter);
        view.findViewById(R.id.loadingPanel).setVisibility(View.VISIBLE);
        view.findViewById(R.id.listview_comments).setVisibility(View.GONE);

        if(savedInstanceState == null){
            getActivity().getContentResolver().delete(RedditContract.Comments.CONTENT_URI, null, null);
            Intent intentService = new Intent(getActivity(), RedditService.class);
            Intent intent = getActivity().getIntent();
            String postId;
            Bundle arguments = getArguments();
            if (arguments != null) {
                postId = arguments.getString("postId");
            }
            else{
                postId = intent.getStringExtra("postId");
            }
            if(postId != null){
                intentService.putExtra("id", postId);
                getActivity().startService(intentService);
            }
        }
        getLoaderManager().initLoader(COMMENTS_LOADER, null, this);
        return view;
    }

    @Override
    public Loader<Cursor> onCreateLoader(int id, Bundle args) {
        return new CursorLoader(getActivity(),
                RedditContract.Comments.CONTENT_URI,
                null,
                null,
                null,
                null);
    }

    @Override
    public void onLoadFinished(Loader<Cursor> loader, Cursor data) {
        if(loader.getId() == COMMENTS_LOADER){
            mCommentsAdapter.swapCursor(data);
            if(data.getCount()!= 0){
                view.findViewById(R.id.loadingPanel).setVisibility(View.GONE);
                view.findViewById(R.id.listview_comments).setVisibility(View.VISIBLE);
            }
        }
    }

    @Override
    public void onLoaderReset(Loader<Cursor> loader) {
        mCommentsAdapter.swapCursor(null);
    }
}
