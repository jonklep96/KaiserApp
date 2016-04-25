package edu.bentley.kaiserapp;

import android.content.Context;
import android.content.Intent;
import android.os.Handler;
import android.os.Message;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.telephony.TelephonyManager;
import android.util.Log;
import android.view.View;
import android.widget.TextView;

import java.sql.Connection;
import java.sql.Date;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.Calendar;

public class VotingActivity extends AppCompatActivity {

    private String phoneNumber;
    private TextView tv1, tv2, tv3;

    private View.OnClickListener votingListener = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            Thread t = new Thread(voteTask);
            t.start();
        }
    };

    private Handler handler = new Handler() {
        public void handleMessage(Message message) {
            boolean flag = (Boolean)message.obj;
            if(flag)
                startActivity(new Intent(getApplicationContext(), VotingErrorActivity.class));
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

    private Runnable voteTask = new Runnable() {
        public void run(){
            String URL = "jdbc:mysql://frodo.bentley.edu:3306/cs480icecream";
            String username = "jkleppinger";
            String password = "icecream";

            try {
                Class.forName("com.mysql.jdbc.Driver");
            } catch (ClassNotFoundException e) {
                Log.e("JDBC", "Did not load driver");
            }

            Statement stmt;
            Connection con;
            try {
                con = DriverManager.getConnection (URL, username, password);

                PreparedStatement preStmt = con.prepareStatement("SELECT phone_number, timestamp FROM tblVote" +
                        "WHERE phone_number = ?");
                preStmt.setString(1, phoneNumber);

                ResultSet result = preStmt.executeQuery();

                Calendar c = Calendar.getInstance();
                boolean flag = true;
                while (result.next()) {
                    Date timestamp = result.getDate(2);
                    Log.e("Voting", timestamp.toString());
                    if (timestamp.getMonth() == c.get(Calendar.MONTH)) {
                        flag = false;
                        break;
                    }
                }

                Message msg = new Message();
                msg.obj = flag;
                handler.sendMessage(msg);
                con.close();
            }
            catch (SQLException e) {
                e.printStackTrace();
            }
        }
    };
}
