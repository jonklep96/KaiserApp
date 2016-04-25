package edu.bentley.kaiserapp;

import android.content.Context;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.telephony.TelephonyManager;
import android.view.View;
import android.widget.TextView;

public class VotingActivity extends AppCompatActivity {

    private String phoneNumber;
    private TextView tv1, tv2, tv3;

    private View.OnClickListener votingListener = new View.OnClickListener() {
        @Override
        public void onClick(View v) {

        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_voting);

        TelephonyManager phoneManager = (TelephonyManager)
                getApplicationContext().getSystemService(Context.TELEPHONY_SERVICE);
        phoneNumber = phoneManager.getLine1Number();

        tv1 = (TextView)findViewById(R.id.voting_tv1);
        tv1.setOnClickListener(votingListener);
        tv2 = (TextView)findViewById(R.id.voting_tv2);
        tv2.setOnClickListener(votingListener);
        tv3 = (TextView)findViewById(R.id.voting_tv3);
        tv3.setOnClickListener(votingListener);
    }
}
