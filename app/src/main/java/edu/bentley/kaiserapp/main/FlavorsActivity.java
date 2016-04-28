package edu.bentley.kaiserapp.main;

import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.speech.tts.TextToSpeech;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.TextView;
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
import java.util.Locale;
import java.util.StringTokenizer;

import edu.bentley.kaiserapp.DrawerActivity;
import edu.bentley.kaiserapp.R;

public class FlavorsActivity extends DrawerActivity implements TextToSpeech.OnInitListener {

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

    private TextToSpeech speaker;

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


        final ListView listView = (ListView)findViewById(android.R.id.list);
        adapter = new ArrayAdapter<>(this, R.layout.list_flavors, R.id.item_flavors, flavorsList);
        listView.setAdapter(adapter);
        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                speak(((TextView)view.findViewById(R.id.item_flavors)).getText().toString());
            }
        });

        speaker = new TextToSpeech(this, this);
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

            while ((line = reader.readLine()) != null) {
                toReturn.add(line);
            }
            reader.close();
        } catch (IOException e) {
            Log.e(FLAVOR_TAG, "IOException: Cannot read flavors.txt");
        }
        return toReturn;
    }

    /**
     * Implements the required method from OnInitListener so
     * the speech engine can say the flavors of ice cream
     */
    public void onInit(int status) {
        if (status == TextToSpeech.SUCCESS) {
            int result = speaker.setLanguage(Locale.GERMANY);

            if (result == TextToSpeech.LANG_MISSING_DATA ||
                    result == TextToSpeech.LANG_NOT_SUPPORTED) {
                Log.e("Speaker", "Language is not available.");
            } else {
                Log.i("Speaker", "TTS Initialization successful.");
            }
        } else {
            Log.e("Speaker", "Could not initialize TextToSpeech.");
        }
    }

    /**
     * Simplify the expression so when the user clicks an item
     * in the list, the speaker says that item name.
     */
    private void speak(String output) {
        speaker.speak(output, TextToSpeech.QUEUE_FLUSH, null, "Id 0");
    }

    /**
     * Close the speaker when the Activity is destroyed
     */
    public void onDestroy() {
        super.onDestroy();
        speaker.shutdown();
    }
}
