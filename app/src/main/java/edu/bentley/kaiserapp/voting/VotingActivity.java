package edu.bentley.kaiserapp.voting;

import android.content.Context;
import android.content.Intent;
import android.os.Handler;
import android.os.Message;
import android.speech.tts.TextToSpeech;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.telephony.TelephonyManager;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
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
import java.util.Locale;
import java.util.StringTokenizer;

import edu.bentley.kaiserapp.DrawerActivity;
import edu.bentley.kaiserapp.LoadingActivity;
import edu.bentley.kaiserapp.R;

public class VotingActivity extends DrawerActivity {

    private static final String VOTING_TAG = "VotingActivity";

    public final static String NAME = "voting";

    /**
     * The user's phone number and the flavor
     * that they choose.
     */
    private String phoneNumber;
    private String flavorVote; // Has a flavorKey to access from Intent

    /**
     * ArrayLists and adapter for the list of items that
     * will be displayed in the ListView
     */
    private ArrayList<String> choiceList;
    private ArrayAdapter<String> adapter;
    private ListView listView;

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

        /**
         * Build the ListView object to help the user
         * select what flavor they want to see on the
         * menu next month.
         */
        choiceList = new ArrayList<>();
        (new Thread(createList)).start();
    }

    /**
     * Handles the creation of the listView and
     * the population of the items in the ListView.
     */
    private Handler listHandler = new Handler() {
        public void handleMessage(Message message) {
            choiceList = (ArrayList<String>)message.obj;
            adapter = new ArrayAdapter<>(getApplicationContext(), R.layout.choice_item, choiceList);
            listView = (ListView)findViewById(android.R.id.list);
            listView.setAdapter(adapter);

            /**
             * Reacts to when a button is clicked
             * to choose what flavor to vote for.
             */
            listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
                @Override
                public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                    flavorVote = ((TextView)view).getText().toString();
                    Intent i = new Intent(getApplicationContext(), LoadingActivity.class);
                    i.putExtra(LoadingActivity.LOADING_KEY, "vote");
                    i.putExtra(LoadingActivity.FLAVOR_KEY, flavorVote);
                    startActivity(i);
                }
            });
        }
    };

    private Runnable createList = new Runnable() {
        @Override
        public void run() {
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
            }
        }
    };
}
