package edu.bentley.kaiserapp.information;

import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.TextView;

import edu.bentley.kaiserapp.DrawerActivity;
import edu.bentley.kaiserapp.R;

public class ScheduleActivity extends DrawerActivity {

    public final static String NAME = "schedule";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        if (savedInstanceState == null)
            savedInstanceState = new Bundle();
        savedInstanceState.putString(ACTIVITY_KEY, NAME);

        super.onCreate(savedInstanceState);

        /**
         * Write the schedule to the screen programmatically.
         */
        TextView scheduleDays = (TextView) findViewById(R.id.tv_schedule_days);
        TextView scheduleTimes = (TextView) findViewById(R.id.tv_schedule_time);
        for (String e : getResources().getStringArray(R.array.schedule_days))
            scheduleDays.append(e + "\n");
        for (String e : getResources().getStringArray(R.array.schedule_times))
            scheduleTimes.append(e + "\n");

        findViewById(R.id.btn_map).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(getApplicationContext(), ContactActivity.class));
            }
        });
    }
}
