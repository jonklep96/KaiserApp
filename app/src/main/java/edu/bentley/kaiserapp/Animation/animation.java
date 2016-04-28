package edu.bentley.kaiserapp.animation;

import android.app.Activity;
import android.content.Intent;
import android.graphics.PixelFormat;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.view.Window;
import android.view.animation.AnimationUtils;
import android.widget.ImageView;

import java.io.IOException;
import java.io.OutputStreamWriter;
import java.sql.Connection;
import java.sql.Date;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.Calendar;

import edu.bentley.kaiserapp.R;
import edu.bentley.kaiserapp.information.ContactActivity;
import edu.bentley.kaiserapp.main.FlavorsActivity;
import edu.bentley.kaiserapp.main.StartActivity;
import edu.bentley.kaiserapp.main.VotingActivity;

public class Animation extends Activity {

    /**
     * Variables that store the information to access
     * the Bentley Frodo database.
     */
    public final static String URL = "jdbc:mysql://frodo.bentley.edu:3306/cs480icecream";
    public final static String USERNAME = "jkleppinger";
    public final static String PASSWORD = "icecream";

    /**
     * Image that will be animated and the Thread that will
     * control the timing of the animation.
     */
    private ImageView image;
    Thread splashThread;

    /**
     * Instantiate the animation and its connection to
     * the GUI
     */
    public void onAttachedToWindow() {
        super.onAttachedToWindow();
        Window window = getWindow();
        window.setFormat(PixelFormat.RGBA_8888);
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_animation);
        (new Thread (createList)).start();
        (new Thread (createVoteList)).start();
        (new Thread (createTruckList)).start();
        startAnimations();
    }

    /**
     * A method that begins the animation of the ice cream
     * cone on the splash screen
     */
    private void startAnimations() {
        image = (ImageView) findViewById(R.id.logo2);
        image.setImageResource(R.drawable.largecone);
        android.view.animation.Animation an = AnimationUtils.loadAnimation(this, R.anim.animate);
        // Start the Animation
        image.startAnimation(an);

        // The Thread t ocontrol the timing of the animation
        splashThread = new Thread() {
            @Override
            public void run() {
                try {
                    int waited = 0;
                    // Splash screen pause time
                    while (waited < 6000) {
                        sleep(100);
                        waited += 100;
                    }
                    Intent intent = new Intent(Animation.this,
                            StartActivity.class);
                    intent.setFlags(Intent.FLAG_ACTIVITY_NO_ANIMATION);
                    startActivity(intent);
                    Animation.this.finish();
                } catch (InterruptedException e) {
                    // do nothing
                } finally {
                    Animation.this.finish();
                }
            }
        };
        splashThread.start();
    }

    /**
     * Sends the downloaded list to the handler where it will
     * compare the new list to the list already shown to the
     * user.
     */
    private Runnable createList = new Runnable() {
        public void run(){
            try {
                Class.forName("com.mysql.jdbc.Driver");
            } catch (ClassNotFoundException e) {
                Log.e("JDBC", "Did not load driver");
            }

            Statement stmt;
            Connection con;
            ArrayList<String> tempList = new ArrayList<>();
            try {
                con = DriverManager.getConnection(URL, USERNAME, PASSWORD);
                stmt = con.createStatement();

                ResultSet result = stmt.executeQuery(
                        "SELECT flavor FROM tblFlavors ORDER BY flavor;");

                while (result.next()) {
                    String flavor = result.getString("flavor");
                    tempList.add(flavor);
                }

                con.close();

                Message msg = new Message();
                msg.obj = tempList;
                listHandler.sendMessage(msg);
            }
            catch (SQLException e) {
                e.printStackTrace();
            }
        }
    };

    /**
     * Checks to see if the list of ice cream
     * is current to the one in the database.
     */
    private Handler listHandler = new Handler() {
        public void handleMessage(Message message) {
            ArrayList<String> tempList;
            if (message.obj != null) {
                tempList = (ArrayList<String>) message.obj;
                saveList(tempList, FlavorsActivity.FILE_NAME);
            }
        }
    };

    /**
     * Connects to the database to see if there is a difference
     * between the locally stored flavors and the ones
     * on the database.
     */
    private Runnable createVoteList = new Runnable() {
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
                voteHandler.sendMessage(msg);
            }
            catch (SQLException e) {
                e.printStackTrace();
            }
        }
    };

    /**
     * Checks to see if the list of voting flavors
     * is current to the one in the database.
     */
    private Handler voteHandler = new Handler() {
            public void handleMessage(Message message) {
                ArrayList<String> tempList;
                if (message.obj != null) {
                    tempList = (ArrayList<String>) message.obj;
                    saveList(tempList, VotingActivity.FILE_NAME);
                }
            }
        };

    /**
     * Sends the downloaded truck list to the handler where it will
     * compare the new list to the list already shown to the
     * user.
     */
    private Runnable createTruckList = new Runnable() {
        public void run(){
            try {
                Class.forName("com.mysql.jdbc.Driver");
            } catch (ClassNotFoundException e) {
                Log.e("JDBC", "Did not load driver");
            }

            Statement stmt;
            Connection con;
            ArrayList<String> tempList = new ArrayList<>();
            try {
                con = DriverManager.getConnection(URL, USERNAME, PASSWORD);
                stmt = con.createStatement();

                ResultSet result = stmt.executeQuery(
                        "SELECT date, lng, lat FROM tblTruck ORDER BY date DESC LIMIT 1;");

                result.next();
                String yr = String.valueOf(result.getDate("date").getYear()+1900);
                String mth = String.valueOf(result.getDate("date").getMonth()+1);
                String dy = String.valueOf(result.getDate("date").getDate());

                tempList.add(yr + "-" + mth + "-" + dy);
                tempList.add(result.getString("lat"));
                tempList.add(result.getString("lng"));

                con.close();

                Message msg = new Message();
                msg.obj = tempList;
                truckHandler.sendMessage(msg);
            }
            catch (SQLException e) {
                e.printStackTrace();
            }
        }
    };

    /**
     * Checks the date and if the date is not the same
     * it goes through the thread process.
     */
    private Handler truckHandler = new Handler() {
        public void handleMessage(Message message) {
            ArrayList<String> tempList;
            if (message.obj != null) {
                tempList = (ArrayList<String>) message.obj;
                saveList(tempList, ContactActivity.FILE_NAME);
            }
        }
    };

    /**
     * Saves the data from the list by writing each
     * flavor from the list to a file.
     */
    private void saveList(ArrayList<String> temp, String fileName) {
        try {
            OutputStreamWriter output = new OutputStreamWriter(openFileOutput(fileName, MODE_PRIVATE));
            output.write("");
            for (String e : temp) {
                output.append(e + "\n");
            }
            output.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}