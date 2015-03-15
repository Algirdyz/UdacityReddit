package com.example.android.reddit;

import android.content.Intent;
import android.support.v7.app.ActionBarActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;


public class MainActivity extends ActionBarActivity {
    private static final String COMMENTSFRAGMENT_TAG = "CFTAG";
    private final static String TAG = "MainActivity";
    private boolean mTwoPane;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        if (findViewById(R.id.comments_container) != null) {
            mTwoPane = true;
            PostsViewFragment forecastFragment =  ((PostsViewFragment)getSupportFragmentManager()
                    .findFragmentById(R.id.postsViewFragment));
            forecastFragment.setMtwoPane(mTwoPane);
            if (savedInstanceState == null) {
                getSupportFragmentManager().beginTransaction()
                        .replace(R.id.comments_container, new CommentsFragment(), COMMENTSFRAGMENT_TAG)
                        .commit();
            }
        } else {
            mTwoPane = false;
        }

        if (savedInstanceState == null) {
            Intent intent = new Intent(this, RedditService.class);
            startService(intent);
        }
    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_refresh) {
            Intent intent = new Intent(this, RedditService.class);
            startService(intent);
            return true;
        }

        return super.onOptionsItemSelected(item);
    }
}
