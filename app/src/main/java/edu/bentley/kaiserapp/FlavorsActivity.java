package edu.bentley.kaiserapp;

import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.Toast;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.StringTokenizer;

public class FlavorsActivity extends DrawerActivity {

    /**
     * Tag for the Log in the FlavorsActivity
     */
    private final String FLAVOR_TAG = "FlavorsActivity";

    /**
     * The name of the Activity so the DrawerActivity
     * can change the content view.
     */
    public final static String NAME = "flavors";

    /**
     * List objects to display the list of the flavors
     * available at the store.
     */
    private ArrayAdapter<String> adapter;
    private ArrayList<String> flavorsList;

    /**
     * The text file that will store the flavors
     * that are downloaded from Frodo.
     */
    private final static String FILE_NAME = "user_flavors.txt";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        if (savedInstanceState == null)
            savedInstanceState = new Bundle();
        savedInstanceState.putString(ACTIVITY_KEY, NAME);

        super.onCreate(savedInstanceState);

        try {
            flavorsList = readList();
        } catch (IOException e) {
            flavorsList = new ArrayList<>();
        }

        Thread t = new Thread(createList);
        t.start();

        ListView listView = (ListView)findViewById(android.R.id.list);
        adapter = new ArrayAdapter<>(this, R.layout.list_flavors, R.id.item_flavors, flavorsList);
        listView.setAdapter(adapter);
    }

    /**
     * Saves the list in a text file.
     */
    @Override
    public void onPause() {
        super.onPause();
        saveList();
    }

    /**
     * Saves the data from the list by writing each
     * flavor from the list to a file.
     */
    private void saveList() {
        try {
            OutputStreamWriter output = new OutputStreamWriter(openFileOutput(FILE_NAME, MODE_PRIVATE));
            for (String e : flavorsList) {
                output.write(e + "\n");
            }
            output.close();
        } catch (IOException e) {
            Log.e(FLAVOR_TAG, "Error during save");
        }
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
                Log.d(FLAVOR_TAG, "IOException: user_flavors.txt does not exist");
                in = getResources().openRawResource(R.raw.flavors);
            }

            InputStreamReader isr = new InputStreamReader(in);
            BufferedReader reader = new BufferedReader(isr);
            String line;

            while ((line = reader.readLine()) != null) { toReturn.add(line); }
            reader.close();
        } catch (IOException e) {
            Log.e(FLAVOR_TAG, "IOException: Cannot read flavors.txt");
        }
        return toReturn;
    }

    /**
     * Sends the downloaded list to the handler where it will
     * compare the new list to the list already shown to the
     * user.
     */
    private Runnable createList = new Runnable() {
        public void run(){try {
                Class.forName("com.mysql.jdbc.Driver");
            } catch (ClassNotFoundException e) {
                Log.e("JDBC", "Did not load driver");
            }

            Statement stmt;
            Connection con;
            ArrayList<String> tempList = new ArrayList<>();
            try {
                con = DriverManager.getConnection (URL, USERNAME, PASSWORD);
                stmt = con.createStatement();

                ResultSet result = stmt.executeQuery(
                        "SELECT flavor FROM tblFlavors ORDER BY flavor;");

                while (result.next()) {
                    String flavor = result.getString("flavor");
                    tempList.add(flavor);
                    Log.e(FLAVOR_TAG, flavor);
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

    private Handler listHandler = new Handler() {
        public void handleMessage(Message message) {
            ArrayList<String> tempList;
            if (message.obj != null)
                tempList= (ArrayList<String>)message.obj;

        }
    };

}
