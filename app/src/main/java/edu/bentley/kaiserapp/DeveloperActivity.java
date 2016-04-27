package edu.bentley.kaiserapp;

import android.os.Handler;
import android.os.Message;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.text.Layout;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.sql.Statement;

public class DeveloperActivity extends DrawerActivity {

    private final String DEV_TAG = "DeveloperActivity";

    public final static String NAME = "developer";

    private Button btnCreateFlavorsTable, btnCreateVotingTable, btnVotableFlavorsTable;

    private Handler handler = new Handler() {
        public void handleMessage(Message message) {
            Log.d(DEV_TAG, (String)message.obj);
            Toast.makeText(getApplicationContext(), (String)message.obj, Toast.LENGTH_SHORT).show();
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        if (savedInstanceState == null)
            savedInstanceState = new Bundle();
        savedInstanceState.putString(ACTIVITY_KEY, NAME);

        super.onCreate(savedInstanceState);
    }

    @Override
    public void onResume() {
        super.onResume();

        btnCreateFlavorsTable = (Button)findViewById(R.id.dev_create_flavors_table);
        btnCreateFlavorsTable.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                (new Thread(createFlavorsTable)).start();
            }
        });

        btnCreateVotingTable = (Button)findViewById(R.id.dev_create_voting_table);
        btnCreateVotingTable.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                (new Thread(createVoteTable)).start();
            }
        });

        btnVotableFlavorsTable = (Button)findViewById(R.id.dev_create_votable_flavors_table);
        btnVotableFlavorsTable.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                (new Thread(createVotableFlavorsTable)).start();
            }
        });
    }

    /**
     * Connects to the database and creates a
     * table to input the flavors the user can
     * get at the store.
     */
    private Runnable createFlavorsTable = new Runnable() {
        public void run(){
            try {
                Class.forName("com.mysql.jdbc.Driver");
            } catch (ClassNotFoundException e) {
                Log.e("JDBC", "Did not load driver");
            }

            Statement stmt;
            Connection con;
            try {
                con = DriverManager.getConnection(URL, USERNAME, PASSWORD);
                stmt = con.createStatement();

                stmt.executeUpdate("DROP TABLE IF EXISTS tblFlavors;");
                stmt.executeUpdate("CREATE TABLE tblFlavors(id INTEGER PRIMARY KEY, flavor VARCHAR(25));");

                stmt.executeUpdate("insert into tblFlavors values(1, 'Vanille');");
                stmt.executeUpdate("insert into tblFlavors values(2, 'Schokolade');");
                stmt.executeUpdate("insert into tblFlavors values(3, 'Walnuss');");
                stmt.executeUpdate("insert into tblFlavors values(4, 'Kirschwasser');");
                stmt.executeUpdate("insert into tblFlavors values(5, 'Joghurt');");
                stmt.executeUpdate("insert into tblFlavors values(6, 'Sahne-Grieß-Himbeer');");
                stmt.executeUpdate("insert into tblFlavors values(7, 'Stracciatella');");
                stmt.executeUpdate("insert into tblFlavors values(8, 'Erdbeer');");
                stmt.executeUpdate("insert into tblFlavors values(9, 'Zwetschge');");
                stmt.executeUpdate("insert into tblFlavors values(10, 'Zitronenmelisse');");
                stmt.executeUpdate("insert into tblFlavors values(11, 'Pfirsich');");
                stmt.executeUpdate("insert into tblFlavors values(12, 'Rhabarber');");
                stmt.executeUpdate("insert into tblFlavors values(13, 'Mirabelle');");

                con.close();

                Message msg = new Message();
                msg.obj = "Created tblFlavors";
                handler.sendMessage(msg);
            }
            catch (SQLException e) {
                e.printStackTrace();
            }
        }
    };

    /**
     * Connect to the database and creates a
     * table to input votes from app users.
     */
    private Runnable createVoteTable = new Runnable() {
        public void run(){
            try {
                Class.forName("com.mysql.jdbc.Driver");
            } catch (ClassNotFoundException e) {
                Log.e("JDBC", "Did not load driver");
            }

            Statement stmt;
            Connection con;
            try {
                con = DriverManager.getConnection(URL, USERNAME, PASSWORD);
                stmt = con.createStatement();

                stmt.executeUpdate("DROP TABLE IF EXISTS tblVote;");
                stmt.executeUpdate("CREATE TABLE tblVote(phone_number VARCHAR(14), flavor VARCHAR(25), date DATE);");

                stmt.executeUpdate("INSERT INTO tblVote VALUES('16107371722', 'Vanilla', '2016-01-26');");
                stmt.executeUpdate("INSERT INTO tblVote VALUES('16107371722', 'Chocolate', '2016-02-26');");
                stmt.executeUpdate("INSERT INTO tblVote VALUES('16107371722', 'Strawberry', '2016-03-26');");

                con.close();

                Message msg = new Message();
                msg.obj = "Created tblVote";
                handler.sendMessage(msg);
            }
            catch (SQLException e) {
                e.printStackTrace();
            }
        }
    };

    /**
     * Connect to the database and creates a
     * table that has a list of flavors that
     * can be voted for that month.
     */
    private Runnable createVotableFlavorsTable = new Runnable() {
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
                con = DriverManager.getConnection(URL, USERNAME, PASSWORD);
                stmt = con.createStatement();

                stmt.executeUpdate("DROP TABLE IF EXISTS tblVotableFlavors;");
                stmt.executeUpdate("CREATE TABLE tblVotableFlavors(flavor VARCHAR(25), date DATE);");

                stmt.executeUpdate("INSERT INTO tblVotableFlavors VALUES('Vanilla', '2016-04-01');");
                stmt.executeUpdate("INSERT INTO tblVotableFlavors VALUES('Chocolate', '2016-04-01');");
                stmt.executeUpdate("INSERT INTO tblVotableFlavors VALUES('Strawberry', '2016-04-01');");

                con.close();

                Message msg = new Message();
                msg.obj = "Created tblVotableFlavors";
                handler.sendMessage(msg);
            }
            catch (SQLException e) {
                e.printStackTrace();
            }
        }
    };
}
