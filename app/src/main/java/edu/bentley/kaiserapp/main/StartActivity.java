package edu.bentley.kaiserapp.main;

import android.graphics.Typeface;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.widget.TextView;

import edu.bentley.kaiserapp.*;

public class StartActivity extends DrawerActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_start);

        TextView tv = (TextView)findViewById(R.id.start_title);
        Typeface face = Typeface.createFromAsset(getAssets(),
                "fonts/cac_champagne.ttf");
        tv.setTypeface(face);
    }
}
