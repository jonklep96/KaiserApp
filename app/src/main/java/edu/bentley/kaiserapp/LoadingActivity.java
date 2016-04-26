package edu.bentley.kaiserapp;

import android.content.Context;
import android.content.Intent;
import android.os.Handler;
import android.os.Message;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.telephony.TelephonyManager;
import android.util.Log;
import android.widget.Toast;

import java.sql.Connection;
import java.sql.Date;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.Calendar;

public class LoadingActivity extends AppCompatActivity {

    private final String LOADING_TAG = "LoadingActivity";
    public static final String LOADING_KEY = "loadingKey";
    public static final String FLAVOR_KEY = "flavorKey";

    /**
     * Variables that store the information to access
     * the Bentley Frodo database.
     */
    private final String URL = "jdbc:mysql://frodo.bentley.edu:3306/cs480icecream";
    private final String username = "jkleppinger";
    private final String password = "icecream";

    /**
     * The user's phone number and the flavor
     * that they choose.
     */
    private String phoneNumber;
    private String flavorVote; // Has a flavorKey to access from Intent

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_loading);

        /**
         * Grab the cellphone's telephone number.
         * This will be sued to identify the previous
         * votes that are stored in the database.
         */
        TelephonyManager phoneManager = (TelephonyManager)
                getApplicationContext().getSystemService(Context.TELEPHONY_SERVICE);
        phoneNumber = phoneManager.getLine1Number();

        switch (getIntent().getStringExtra(LOADING_KEY)) {
            case "vote":
                flavorVote = getIntent().getStringExtra(FLAVOR_KEY);
                (new Thread(placeVote)).start();
                break;
        }
    }

    /**
     * Do not allow users to go back when on this page.
     */
    @Override
    public void onBackPressed() {
        Toast.makeText(this, "Wait until connection is complete", Toast.LENGTH_LONG).show();
    }

    private Runnable placeVote = new Runnable() {
        @Override
        public void run() {
            try {
                Class.forName("com.mysql.jdbc.Driver");
            } catch (ClassNotFoundException e) {
                Log.e("JDBC", "Did not load driver");
            }

            Connection con;
            try {
                con = DriverManager.getConnection (URL, username, password);

                PreparedStatement preStmt = con.prepareStatement("INSERT INTO tblVote VALUES(?, ?, ?);");
                preStmt.setString(1, phoneNumber);
                preStmt.setString(2, flavorVote);
                Date date = new Date(Calendar.getInstance().getTimeInMillis());
                preStmt.setDate(3, date);
                preStmt.executeUpdate();

                Message msg = new Message();
                ArrayList<String> list = new ArrayList<>();
                list.add(phoneNumber);
                list.add(flavorVote);
                list.add(date.toString());
                msg.obj = list;
                voteHandler.sendMessage(msg);
                con.close();
            }
            catch (SQLException e) {
                e.printStackTrace();
            }
        }
    };

    private Handler voteHandler = new Handler() {
        public void handleMessage(Message message) {
            Log.d(LOADING_TAG, "voteHandler");
            ArrayList<String> list = (ArrayList<String>)message.obj;

            Intent i = new Intent(getApplicationContext(), VotingCompleteActivity.class);
            i.putExtra(FLAVOR_KEY, list.get(1));
            startActivity(i);
        }
    };

    private Runnable voteTask = new Runnable() {
        public void run(){
            try {
                Class.forName("com.mysql.jdbc.Driver");
            } catch (ClassNotFoundException e) {
                Log.e("JDBC", "Did not load driver");
            }

            Statement stmt;
            Connection con;
            try {
                con = DriverManager.getConnection (URL, username, password);
                stmt = con.createStatement();

                ResultSet result = stmt.executeQuery("SELECT phone_number, date FROM tblVote");

                Calendar c = Calendar.getInstance();
                boolean flag = true;
                while (result.next()) {
                    Date date = result.getDate("date");
                    String num = result.getString("phone_number");
                    Log.e(LOADING_TAG, num + ": " + date.toString());
                    if ((date.getMonth() == c.get(Calendar.MONTH)) && (date.getYear() == (c.get(Calendar.YEAR)-1900))) {
                        flag = false;
                        break;
                    }
                }

                Message msg = new Message();
                msg.obj = flag;
                checkHandler.sendMessage(msg);
                con.close();
            }
            catch (SQLException e) {
                e.printStackTrace();
            }
        }
    };

    private Runnable displayData = new Runnable() {
        @Override
        public void run() {
            try {
                Class.forName("com.mysql.jdbc.Driver");
            } catch (ClassNotFoundException e) {
                Log.e("JDBC", "Did not load driver");
            }

            Statement stmt;
            Connection con;
            try {
                con = DriverManager.getConnection (URL, username, password);
                stmt = con.createStatement();

                ResultSet result = stmt.executeQuery("SELECT phone_number, flavor, date FROM tblVote");
                while(result.next()) {
                    String phone = result.getString("phone_number");
                    String flavor = result.getString("flavor");
                    Date date = result.getDate("date");
                    Log.d(LOADING_TAG, phone + ": " + flavor + " on " + date.toString());
                }

                con.close();
            }
            catch (SQLException e) {
                e.printStackTrace();
            }
        }
    };

    /**
     * Tells whether or not the user
     * has already voted in that month.
     */
    private Handler checkHandler = new Handler() {
        public void handleMessage(Message message) {
            boolean flag = (Boolean)message.obj;
            if(flag)
                (new Thread(placeVote)).start();
            else
                startActivity(new Intent(getApplicationContext(), VotingErrorActivity.class));
        }
    };
}
