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
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ListView;
import android.widget.TextView;

import java.sql.Connection;
import java.sql.Date;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.Calendar;

public class VotingActivity extends AppCompatActivity {

    private final String votingTag = "VotingActivity";

    /**
     * Variables that sotre the information to access
     * the Bentley Frodo database.
     */
    private final String URL = "jdbc:mysql://frodo.bentley.edu:3306/cs480icecream";
    private final String username = "jkleppinger";
    private final String password = "icecream";

    private String phoneNumber;
    private Button tv1, tv2, tv3;
    private String flavorVote;

    private ArrayList<String> choiceList;
    private ArrayAdapter<String> adapter;
    private ListView listView;

    /**
     * Tells whether or not the user
     * has already voted in that month.
     */
    private Handler checkHandler = new Handler() {
        public void handleMessage(Message message) {
            Log.d(votingTag, "checkHandler");
            boolean flag = (Boolean)message.obj;
            if(flag) {
                Thread t = new Thread(placeVote);
                t.start();
            }
            else
                startActivity(new Intent(getApplicationContext(), VotingErrorActivity.class));
        }
    };

    private Handler voteHandler = new Handler() {
        public void handleMessage(Message message) {
            Log.d(votingTag, "voteHandler");

            Thread t = new Thread(displayData);
            t.start();
        }
    };

    private Handler flavorHandler = new Handler() {
        public void handleMessage(Message message) {
            ArrayList<String> list = (ArrayList<String>)message.obj;
            adapter.notifyDataSetChanged();
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_voting);

        /**
         * Grab the cellphone's telephone number.
         * This will be sued to identify the previous
         * votes that are stored in the database.
         */
        TelephonyManager phoneManager = (TelephonyManager)
                getApplicationContext().getSystemService(Context.TELEPHONY_SERVICE);
        phoneNumber = phoneManager.getLine1Number();

        /**
         * Building the ListView object to help the user
         * select what flavor they want to see on the
         * menu next month.
         */
        choiceList = new ArrayList<>();
        adapter = new ArrayAdapter<>(this, R.layout.choice_item, choiceList);
        listView = (ListView)findViewById(android.R.id.list);
        listView.setAdapter(adapter);

        /**
         * Reacts to when a button is clicked
         * to choose what flavor to vote for.
         */
        listView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                flavorVote = ((TextView)v).getText().toString();
                Thread t = new Thread(voteTask);
                t.start();
            }
        });

        Thread t = new Thread(createVoteTable);
        t.start();
        Thread t2 = new Thread(votableFlavors);
        t2.start();
        Thread t3 = new Thread(getVotableFlavors);
        t3.start();
    }

    private Runnable createVoteTable = new Runnable() {
        public void run(){try {
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
                stmt.executeUpdate("CREATE TABLE tblVote(phone_number VARCHAR(14), flavor VARCHAR(25), date DATE);");
                Log.w(votingTag, "Created Table");

                stmt.executeUpdate("INSERT INTO tblVote VALUES('16107371722', 'Vanilla', '2016-01-26');");
                stmt.executeUpdate("INSERT INTO tblVote VALUES('16107371722', 'Chocolate', '2016-02-26');");
                stmt.executeUpdate("INSERT INTO tblVote VALUES('16107371722', 'Strawberry', '2016-03-26');");

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
                checkHandler.sendMessage(msg);
                con.close();
            }
            catch (SQLException e) {
                e.printStackTrace();
            }
        }
    };

    private Runnable placeVote = new Runnable() {
        @Override
        public void run() {
            Log.d(votingTag, "Clicked a Flavor");

            try {
                Class.forName("com.mysql.jdbc.Driver");
            } catch (ClassNotFoundException e) {
                Log.e("JDBC", "Did not load driver");
            }

            Statement stmt;
            Connection con;
            try {
                con = DriverManager.getConnection (URL, username, password);

                PreparedStatement preStmt = con.prepareStatement("INSERT INTO tblVote VALUES(?, ?, ?);");
                preStmt.setString(1, phoneNumber);
                preStmt.setString(2, flavorVote);
                preStmt.setDate(3, new Date(Calendar.getInstance().getTimeInMillis()));
                preStmt.executeUpdate();

                Message msg = new Message();
                msg.obj = "Sending";
                voteHandler.sendMessage(msg);
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
                    Log.d(votingTag, phone + ": " + flavor + " on " + date.toString());
                }

                con.close();
            }
            catch (SQLException e) {
                e.printStackTrace();
            }
        }
    };

    private Runnable votableFlavors = new Runnable() {
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
                con = DriverManager.getConnection(URL, username, password);
                stmt = con.createStatement();

                stmt.executeUpdate("DROP TABLE IF EXISTS tblVotableFlavors;");
                stmt.executeUpdate("CREATE TABLE tblVotableFlavors(flavor VARCHAR(25), date DATE);");
                Log.w(votingTag, "Created Table");

                stmt.executeUpdate("INSERT INTO tblVotableFlavors VALUES('Vanilla', '2016-04-01');");
                stmt.executeUpdate("INSERT INTO tblVotableFlavors VALUES('Chocolate', '2016-04-01');");
                stmt.executeUpdate("INSERT INTO tblVotableFlavors VALUES('Strawberry', '2016-04-01');");

                con.close();
            }
            catch (SQLException e) {
                e.printStackTrace();
            }
        }
    };

    private Runnable getVotableFlavors = new Runnable() {
        @Override
        public void run() {
            try {
                Class.forName("com.mysql.jdbc.Driver");
            } catch (ClassNotFoundException e) {
                Log.e("JDBC", "Did not load driver");
            }

            Connection con;
            try {
                con = DriverManager.getConnection(URL, username, password);

                PreparedStatement preStmt = con.prepareStatement("SELECT flavor FROM tblVotableFlavors" +
                        "WHERE MONTH(date) = ? AND YEAR(date) = ?;");
                preStmt.setInt(1, (new Date(Calendar.getInstance().getTimeInMillis())).getMonth());
                preStmt.setInt(2, (new Date(Calendar.getInstance().getTimeInMillis())).getYear());
                ResultSet results = preStmt.executeQuery();

                ArrayList<String> flavorList = new ArrayList<>();
                while(results.next())
                    flavorList.add(results.getString("flavor"));

                Message msg = new Message();
                msg.obj = flavorList;
                flavorHandler.sendMessage(msg);

                con.close();
            }
            catch (SQLException e) {
                e.printStackTrace();
            }
        }
    };
}
