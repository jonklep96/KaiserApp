package edu.bentley.kaiserapp.main;

import android.app.Notification;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
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

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
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
    public final static String FILE_NAME = "voting_flavors.txt";

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
        pbVoting.setVisibility(View.INVISIBLE);

        /**
         * Build the ListView object to help the user
         * select what flavor they want to see on the
         * menu next month.
         */
        try {
            choiceList = readList();
        } catch (IOException e) {
            choiceList = new ArrayList<>();
        }
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
    }

    /**
     * Reads the list from the file if the data has already
     * been accessed from the database before.
     */
    public ArrayList<String> readList() throws IOException {
        ArrayList<String> toReturn = new ArrayList<>();
        try {
            InputStream in;

            try {
                in = openFileInput(FILE_NAME);
            } catch (IOException e) {
                in = getResources().openRawResource(R.raw.voting_flavors);
            }

            InputStreamReader isr = new InputStreamReader(in);
            BufferedReader reader = new BufferedReader(isr);
            String line;

            while ((line = reader.readLine()) != null) { toReturn.add(line); }
            reader.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return toReturn;
    }

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
                final Intent notifyIntent = new Intent(getApplicationContext(), FlavorsActivity.class);
                PendingIntent pendingIntent = PendingIntent.getActivity(
                        getApplicationContext(), NOTIFY_ID, notifyIntent, PendingIntent.FLAG_UPDATE_CURRENT);
                mNotifyDetails = new Notification.Builder(getApplicationContext())
                        .setContentTitle("Unsuccessful")    //set Notification text and icon
                        .setContentText("You have already voted this month")
                        .setSmallIcon(R.drawable.store_marker)
                        .setTicker("You have already voted")
                        .setContentIntent(pendingIntent)
                        .setWhen(System.currentTimeMillis())
                        .setLights(Integer.MAX_VALUE,  500,  500)
                        .setAutoCancel(true)
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
            final Intent notifyIntent = new Intent(getApplicationContext(), FlavorsActivity.class);
            PendingIntent pendingIntent = PendingIntent.getActivity(
                    getApplicationContext(), NOTIFY_ID, notifyIntent, PendingIntent.FLAG_UPDATE_CURRENT);
            mNotifyDetails = new Notification.Builder(getApplicationContext())
                    .setContentTitle("Successful")    //set Notification text and icon
                    .setContentText("You successfully voted for " + vote)
                    .setSmallIcon(R.drawable.store_marker)
                    .setTicker("You Voted")
                    .setContentIntent(pendingIntent)
                    .setWhen(System.currentTimeMillis())
                    .setLights(Integer.MAX_VALUE,  500,  500)
                    .setAutoCancel(true)
                    .build();
            mNotificationManager.notify(NOTIFY_KEY, NOTIFY_ID, mNotifyDetails);
        }
    };
}
