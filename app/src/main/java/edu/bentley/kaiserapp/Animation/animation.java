package edu.bentley.kaiserapp.Animation;

import android.app.Activity;
import android.content.Intent;
import android.graphics.PixelFormat;
import android.os.Bundle;
import android.view.Window;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.ImageView;
import android.widget.RelativeLayout;

import edu.bentley.kaiserapp.DeveloperActivity;
import edu.bentley.kaiserapp.R;

/**
 * Created by COOPE1_DAVI on 4/26/2016.
 */
public class animation extends Activity {

    private RelativeLayout layout;
    private ImageView image;
    Thread splashTread;

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

        layout = (RelativeLayout) findViewById(R.id.layout);
        image = (ImageView) findViewById(R.id.logo2);
        image.setImageResource(R.drawable.largecone);
        Animation an = AnimationUtils.loadAnimation(this, R.anim.animate);
        // Start the animation
        layout.startAnimation(an);


        splashTread = new Thread() {
            @Override
            public void run() {
                try {
                    int waited = 0;
                    // Splash screen pause time
                    while (waited < 6000) {
                        sleep(100);
                        waited += 100;
                    }
                    Intent intent = new Intent(animation.this,
                            DeveloperActivity.class);
                    intent.setFlags(Intent.FLAG_ACTIVITY_NO_ANIMATION);
                    startActivity(intent);
                    animation.this.finish();
                } catch (InterruptedException e) {
                    // do nothing
                } finally {
                    animation.this.finish();
                }

            }
        };
        splashTread.start();
    }


}

