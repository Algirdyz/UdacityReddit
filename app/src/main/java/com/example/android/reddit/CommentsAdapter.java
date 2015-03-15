package com.example.android.reddit;

import android.content.Context;
import android.database.Cursor;
import android.support.v4.widget.CursorAdapter;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import org.w3c.dom.Text;

/**
 * Created by Algirdas on 2015-03-14.
 */
public class CommentsAdapter extends CursorAdapter {

    public CommentsAdapter(Context context, Cursor c, int flags) {
        super(context, c, flags);
    }

    @Override
    public View newView(Context context, Cursor cursor, ViewGroup parent) {

        View view = LayoutInflater.from(context).inflate(R.layout.listview_comments_item, parent, false);

        return view;
    }

    @Override
    public void bindView(View view, Context context, Cursor cursor) {
        String comment = cursor.getString(CommentsFragment.COLUMN_BODY);
        ((TextView) view.findViewById(R.id.comment)).setText(comment);
    }
}
