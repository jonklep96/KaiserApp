package edu.bentley.kaiserapp.main;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.view.View;

import edu.bentley.kaiserapp.*;

public class StartActivity extends DrawerActivity {

    /**
     * Key to be used to indicate how to start
     * the activity when calling the parent activity
     */
    public final static String NAME = "start";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        if (savedInstanceState == null)
            savedInstanceState = new Bundle();
        savedInstanceState.putString(ACTIVITY_KEY, NAME);

        super.onCreate(savedInstanceState);

        /**
         * Starts up the website when the user clicks on the picture
         */
        findViewById(R.id.start_pic).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent browserIntent = new Intent(Intent.ACTION_VIEW, Uri.parse(getResources().getString(R.string.url_address)));
                startActivity(browserIntent);
            }
        });
    }
}
