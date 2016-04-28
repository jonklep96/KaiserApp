package edu.bentley.kaiserapp.main;

import android.os.Bundle;

import edu.bentley.kaiserapp.*;

public class StartActivity extends DrawerActivity {

    public final static String NAME = "start";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        if (savedInstanceState == null)
            savedInstanceState = new Bundle();
        savedInstanceState.putString(ACTIVITY_KEY, NAME);

        super.onCreate(savedInstanceState);
    }
}
