package edu.bentley.kaiserapp.voting;

import android.app.NotificationManager;
import android.content.Intent;
import android.speech.tts.TextToSpeech;
import android.os.Bundle;
import android.util.Log;
import android.widget.TextView;

import java.util.Locale;

import edu.bentley.kaiserapp.DrawerActivity;
import edu.bentley.kaiserapp.LoadingActivity;
import edu.bentley.kaiserapp.R;
import edu.bentley.kaiserapp.voting.VotingActivity;

public class VotingCompleteActivity extends DrawerActivity implements TextToSpeech.OnInitListener {

    /**
     * Include the TTS engine that will be used when the
     * user votes on a flavor in the list.
     */
    private TextToSpeech speaker;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_voting_complete);

        speaker = new TextToSpeech(this, this);

        //Clearing the notification
        NotificationManager mNotificationManager = (NotificationManager) getSystemService(NOTIFICATION_SERVICE);
        mNotificationManager.cancel(getIntent().getIntExtra("NotificationId", 0));

        String output = "You have successfully voted for " + getIntent().getStringExtra(LoadingActivity.FLAVOR_KEY) + ".";
        ((TextView)findViewById(R.id.tv_voting_complete)).setText(output);
        speak(output);
    }

    /**
     * Do not allow users to go back when on this page.
     */
    @Override
    public void onBackPressed() {
        startActivity(new Intent(this, VotingActivity.class));
    }

    /**
     * The function to initialize the TTS engine.
     */
    public void onInit(int status) {
        if (status == TextToSpeech.SUCCESS) {
            int result = speaker.setLanguage(Locale.GERMANY);

            if (result == TextToSpeech.LANG_MISSING_DATA ||
                    result == TextToSpeech.LANG_NOT_SUPPORTED)
                Log.e("Speaker", "Language is not available.");
            else
                Log.i("Speaker", "TTS Initialization successful.");
        } else
            Log.e("Speaker", "Could not initialize TextToSpeech.");
    }

    /**
     * Speak what is sent through the String parameter output.
     */
    public void speak(String output) {
        speaker.speak(output, TextToSpeech.QUEUE_FLUSH, null, "Id 0");
    }

    /**
     * Closes the speaker.
     */
    @Override
    public void onDestroy() {
        super.onDestroy();
        speaker.shutdown();
    }
}
