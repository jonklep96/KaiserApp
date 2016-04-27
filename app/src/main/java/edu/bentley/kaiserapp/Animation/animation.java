package edu.bentley.kaiserapp.animation;

import android.app.Activity;
import android.content.Intent;
import android.graphics.PixelFormat;
import android.os.Bundle;
import android.view.Window;
import android.view.animation.AnimationUtils;
import android.widget.ImageView;
import android.widget.RelativeLayout;

import edu.bentley.kaiserapp.DeveloperActivity;
import edu.bentley.kaiserapp.R;

public class Animation extends Activity {

    private RelativeLayout layout;
    private ImageView image;
    Thread splashThread;

    public void onAttachedToWindow() {
        super.onAttachedToWindow();
        Window window = getWindow();
        window.setFormat(PixelFormat.RGBA_8888);
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_animation);
        StartAnimations();
    }

    private void StartAnimations() {
        image = (ImageView) findViewById(R.id.logo2);
        image.setImageResource(R.drawable.largecone);
        android.view.animation.Animation an = AnimationUtils.loadAnimation(this, R.anim.animate);
        // Start the Animation
        image.startAnimation(an);

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
                            DeveloperActivity.class);
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
}