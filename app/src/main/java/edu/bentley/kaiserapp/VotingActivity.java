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

    private final String votingTag = "VotingActivity";

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
            Log.d(votingTag, "Handler speaking");
            boolean flag = (Boolean)message.obj;
            if(!flag)
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

        Thread t = new Thread(createVoteTable);
        t.start();
    }

    private Runnable createVoteTable = new Runnable() {
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
                con = DriverManager.getConnection(URL, username, password);
                stmt = con.createStatement();

                stmt.executeUpdate("DROP TABLE IF EXISTS tblVote;");
                stmt.executeUpdate("CREATE TABLE tblVote(phone_number VARCHAR(11), flavor VARCHAR(25), date DATE);");
                Log.w(votingTag, "Created Table");

                stmt.executeUpdate("INSERT INTO tblVote VALUES('6107371722', 'Vanilla', '2016-02-26');");
                stmt.executeUpdate("INSERT INTO tblVote VALUES('6107371722', 'Chocolate', '2016-03-26');");
                stmt.executeUpdate("INSERT INTO tblVote VALUES('6107371722', 'Strawberry', '2016-04-26');");

                con.close();
            }
            catch (SQLException e) {
                e.printStackTrace();
            }
        }
    };

    private Runnable voteTask = new Runnable() {
        public void run(){
            Log.d(votingTag, "Clicked a Flavor");
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

                /*PreparedStatement preStmt = con.prepareStatement("SELECT phone_number, timestamp FROM tblVote" +
                        "WHERE phone_number = ?");
                preStmt.setString(1, phoneNumber);

                ResultSet result = preStmt.executeQuery();*/

                stmt = con.createStatement();
                ResultSet result = stmt.executeQuery("SELECT phone_number, date FROM tblVote");

                Calendar c = Calendar.getInstance();
                boolean flag = true;
                while (result.next()) {
                    Date date = result.getDate("date");
                    String num = result.getString("phone_number");
                    Log.e(votingTag, num + ": " + date.toString());
                    int m = date.getMonth();
                    int m2 = c.get(Calendar.MONTH);
                    int y = date.getYear();
                    int y2 = c.get(Calendar.YEAR);
                    if ((date.getMonth() == c.get(Calendar.MONTH)) && (date.getYear() == (c.get(Calendar.YEAR)-1900))) {
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
