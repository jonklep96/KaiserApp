package edu.bentley.kaiserapp;

import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.widget.ArrayAdapter;
import android.widget.ListView;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;

public class FlavorsActivity extends DrawerActivity {

    private final String flavorTag = "FlavorsActivity";

    public final static String NAME = "flavors";

    private ArrayAdapter<String> adapter;
    private ArrayList<String> flavorsList;

    private final String userFlavorsFile = "user_flavors.txt";

    private Handler handler = new Handler() {
        public void handleMessage(Message message) {
            adapter.notifyDataSetChanged();
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        if (savedInstanceState == null)
            savedInstanceState = new Bundle();
        savedInstanceState.putString(ACTIVITY_KEY, NAME);

        super.onCreate(savedInstanceState);

        //flavorsList = readFlavorList();
        flavorsList = new ArrayList<>();
        //Thread t = new Thread(insertFlavors);
        Thread t = new Thread(grabFlavors);
        t.start();

        ListView listView = (ListView)findViewById(android.R.id.list);
        adapter = new ArrayAdapter<>(this, R.layout.list_flavors, R.id.item_flavors, flavorsList);
        listView.setAdapter(adapter);
    }

    public ArrayList<String> readFlavorList() {
        ArrayList<String> toReturn = new ArrayList<>();
        try {
            InputStream in;

            // Will open up a user specific file if it exists
            try {
                in = openFileInput(userFlavorsFile);
            } catch (IOException e) {
                Log.d(flavorTag, "IOException: user_flavors.txt does not exist");
                in = getResources().openRawResource(R.raw.flavors);
            }

            InputStreamReader isr = new InputStreamReader(in);
            BufferedReader reader = new BufferedReader(isr);
            String line;

            while ((line = reader.readLine()) != null) { toReturn.add(line); }
            reader.close();
        } catch (IOException e) {
            Log.e(flavorTag, "IOException: Cannot read flavors.txt");
        }
        return toReturn;
    }

    private Runnable grabFlavors = new Runnable() {
        public void run(){try {
                Class.forName("com.mysql.jdbc.Driver");
            } catch (ClassNotFoundException e) {
                Log.e("JDBC", "Did not load driver");
            }

            Statement stmt;
            Connection con;
            try {
                con = DriverManager.getConnection (URL, USERNAME, PASSWORD);
                stmt = con.createStatement();

                ResultSet result = stmt.executeQuery(
                        "SELECT flavor FROM tblFlavors ORDER BY flavor;");

                while (result.next()) {
                    String flavor = result.getString("flavor");
                    flavorsList.add(flavor);
                    Log.e("Flavor", flavor);
                }

                Message msg = new Message();
                msg.obj = "Sending";
                handler.sendMessage(msg);
                con.close();
            }
            catch (SQLException e) {
                e.printStackTrace();
            }
        }
    };

}
