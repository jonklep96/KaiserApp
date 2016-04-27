package edu.bentley.kaiserapp.main;

import android.app.Notification;
import android.content.Context;
import android.os.Handler;
import android.os.Message;
import android.os.Bundle;
import android.telephony.TelephonyManager;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.ProgressBar;
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

import edu.bentley.kaiserapp.DrawerActivity;
import edu.bentley.kaiserapp.R;

public class VotingActivity extends DrawerActivity {

    /**
     * Tag for the Log in the VotingActivity
     */
    private static final String VOTING_TAG = "VotingActivity";

    public final static String NAME = "voting";

    /**
     * The user's phone number and the flavor
     * that they choose.
     */
    private String phoneNumber;
    private String flavorVote;

    /**
     * ArrayLists and adapter for the list of items that
     * will be displayed in the ListView
     */
    private ArrayList<String> choiceList;
    private ArrayAdapter<String> adapter;
    private ListView listView;

    /**
     * The ProgressBar that will be displayed when accessing the Internet
     */
    private ProgressBar pbVoting;

    /**
     * Gets updated when the program is threading
     */
    private boolean isMultiThreading = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        if (savedInstanceState == null)
            savedInstanceState = new Bundle();
        savedInstanceState.putString(ACTIVITY_KEY, NAME);

        super.onCreate(savedInstanceState);

        /**
         * Grab the cellphone's telephone number.
         * This will be sued to identify the previous
         * votes that are stored in the database.
         */
        TelephonyManager phoneManager = (TelephonyManager)
                getApplicationContext().getSystemService(Context.TELEPHONY_SERVICE);
        phoneNumber = phoneManager.getLine1Number();

        pbVoting = (ProgressBar)findViewById(R.id.pb_voting);

        /**
         * Build the ListView object to help the user
         * select what flavor they want to see on the
         * menu next month.
         */
        choiceList = new ArrayList<>();
        adapter = new ArrayAdapter<>(getApplicationContext(), R.layout.choice_item, choiceList);
        listView = (ListView)findViewById(R.id.lv_voting);
        listView.setAdapter(adapter);
        adapter.notifyDataSetChanged();

        /**
         * Reacts to when a button is clicked
         * to choose what flavor to vote for.
         */
        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                flavorVote = ((TextView)view).getText().toString();
                (new Thread(validateVoteTask)).start();
                listView.setVisibility(View.INVISIBLE);
                pbVoting.setVisibility(View.VISIBLE);
            }
        });

        (new Thread(createList)).start();
    }

    @Override
    public void onBackPressed() {
        if (!isMultiThreading)
            super.onBackPressed();
    }

    /**
     * Connects to the database to see if there is a difference
     * between the locally stored flavors and the ones
     * on the database.
     */
    private Runnable createList = new Runnable() {
        @Override
        public void run() {
            isMultiThreading = true;
            try {
                Class.forName("com.mysql.jdbc.Driver");
            } catch (ClassNotFoundException e) {
                Log.e("JDBC", "Did not load driver");
            }

            Connection con;
            try {
                con = DriverManager.getConnection(URL, USERNAME, PASSWORD);

                PreparedStatement preStmt = con.prepareStatement("SELECT flavor FROM tblVotableFlavors " +
                        "WHERE MONTH(date) = ? AND YEAR(date) = ?;");
                preStmt.setInt(1, (new Date(Calendar.getInstance().getTimeInMillis())).getMonth()+1);
                preStmt.setInt(2, (new Date(Calendar.getInstance().getTimeInMillis())).getYear()+1900);
                ResultSet results = preStmt.executeQuery();
                ArrayList<String> flavorList = new ArrayList<>();
                while(results.next())
                    flavorList.add(results.getString("flavor"));

                con.close();

                Message msg = new Message();
                msg.obj = flavorList;
                listHandler.sendMessage(msg);
            }
            catch (SQLException e) {
                e.printStackTrace();
            } finally {
                isMultiThreading = false;
            }
        }
    };

    /**
     * Handles the creation of the listView and
     * the population of the items in the ListView.
     */
    private Handler listHandler = new Handler() {
        public void handleMessage(Message message) {
            findViewById(R.id.pb_voting).setVisibility(View.INVISIBLE);
            updateList((ArrayList<String>)message.obj);
        }
    };

    /**
     * Adds the flavors downloaded from the database to
     * update the voting list.
     */
    private void updateList(ArrayList<String> list) {
        for (String e : list)
            choiceList.add(e);
        adapter.notifyDataSetChanged();
    }

    /**
     * Checks the other votes placed this month
     * to see if the user has already voted during
     * the current month.
     */
    private Runnable validateVoteTask = new Runnable() {
        public void run(){
            try {
                Class.forName("com.mysql.jdbc.Driver");
            } catch (ClassNotFoundException e) {
                Log.e("JDBC", "Did not load driver");
            }

            Statement stmt;
            Connection con;
            try {
                con = DriverManager.getConnection (URL, USERNAME, PASSWORD);
                stmt = con.createStatement();

                ResultSet result = stmt.executeQuery("SELECT phone_number, date FROM tblVote");

                Calendar c = Calendar.getInstance();
                boolean flag = true;
                while (result.next()) {
                    Date date = result.getDate("date");
                    String num = result.getString("phone_number");
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

    /**
     * Handler used to either place the vote into
     * the database or to reject the user since they
     * already voted that month.
     */
    private Handler checkHandler = new Handler() {
        public void handleMessage(Message message) {
            boolean flag = (Boolean)message.obj;
            if(flag)
                (new Thread(voteTask)).start();
            else {
                /**
                 * Display notification here
                 */
                mNotifyDetails = new Notification.Builder(getApplicationContext())
                        .setContentTitle("Unuccessful")    //set Notification text and icon
                        .setContentText("You have already voted this month")
                        .setSmallIcon(R.drawable.store_marker)
                        .setTicker("You have already voted")
                        .setWhen(System.currentTimeMillis())
                        .setLights(Integer.MAX_VALUE,  500,  500)
                        .build();
                mNotificationManager.notify(NOTIFY_KEY, NOTIFY_ID, mNotifyDetails);
                listView.setVisibility(View.VISIBLE);
                pbVoting.setVisibility(View.INVISIBLE);
            }
        }
    };

    /**
     * Place the vote into the database.
     */
    private Runnable voteTask = new Runnable() {
        @Override
        public void run() {
            try {
                Class.forName("com.mysql.jdbc.Driver");
            } catch (ClassNotFoundException e) {
                Log.e("JDBC", "Did not load driver");
            }
            Connection con;
            try {
                con = DriverManager.getConnection (URL, USERNAME, PASSWORD);

                PreparedStatement preStmt = con.prepareStatement("INSERT INTO tblVote VALUES(?, ?, ?);");
                preStmt.setString(1, phoneNumber);
                preStmt.setString(2, flavorVote);
                Date date = new Date(Calendar.getInstance().getTimeInMillis());
                preStmt.setDate(3, date);
                preStmt.executeUpdate();

                con.close();

                Message msg = new Message();
                msg.obj = flavorVote;
                voteHandler.sendMessage(msg);
            }
            catch (SQLException e) {
                e.printStackTrace();
            }
        }
    };

    /**
     * Recognizes when the placing of the vote into
     * the database had been completed. It will send a
     * notification to let the user know that the
     * transaction is complete.
     */
    private Handler voteHandler = new Handler() {
        public void handleMessage(Message message) {
            listView.setVisibility(View.VISIBLE);
            pbVoting.setVisibility(View.INVISIBLE);
            String vote = (String)message.obj;

            /**
             * Display notification here
             */
            mNotifyDetails = new Notification.Builder(getApplicationContext())
                    .setContentTitle("Successful")    //set Notification text and icon
                    .setContentText("You successfully voted for " + vote)
                    .setSmallIcon(R.drawable.store_marker)
                    .setTicker("You Voted")
                    .setWhen(System.currentTimeMillis())
                    .setLights(Integer.MAX_VALUE,  500,  500)
                    .build();
            mNotificationManager.notify(NOTIFY_KEY, NOTIFY_ID, mNotifyDetails);
        }
    };
}
