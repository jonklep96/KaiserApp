package edu.bentley.kaiserapp;

import android.content.Context;
import android.content.Intent;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.view.View;
import android.support.design.widget.NavigationView;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.TextView;
import android.widget.ViewFlipper;

import edu.bentley.kaiserapp.contact.*;
import edu.bentley.kaiserapp.voting.VotingActivity;

public class DrawerActivity extends AppCompatActivity implements NavigationView.OnNavigationItemSelectedListener {

    /**
     * Stores a String to serve as a key so the
     * child Activity can be identified.
     */
    public final static String ACTIVITY_KEY = "activity";

    /**
     * Variables that store the information to access
     * the Bentley Frodo database.
     */
    public final static String URL = "jdbc:mysql://frodo.bentley.edu:3306/cs480icecream";
    public final static String USERNAME = "jkleppinger";
    public final static String PASSWORD = "icecream";

    /**
     * Use a ViewFlipper to change the content inside of the
     * application.
     */
    ViewFlipper vf;

    /**
     * Time to set the connection Thread to sleep for
     */
    private final static long SLEEP_TIME = 15000;
    private boolean isConnected = true;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_drawer);

        /**
         * Creates a Toolbar on the first creation of the application.
         */
        Toolbar toolbar = (Toolbar)findViewById(R.id.toolbar);
        if (getSupportActionBar() == null)
            setSupportActionBar(toolbar);

        /**
         * Find the ViewFlipper which will allow the
         * application to set the content on each page to
         * an activity layout.
         */
        vf = (ViewFlipper)findViewById(R.id.vf);
        if (savedInstanceState != null)
            setViewFlipperContent(savedInstanceState.getString(ACTIVITY_KEY));
        else
            vf.setDisplayedChild(2);

        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(
                this, drawer, toolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close);
        drawer.setDrawerListener(toggle);
        toggle.syncState();

        NavigationView navigationView = (NavigationView) findViewById(R.id.nav_view);
        navigationView.setNavigationItemSelectedListener(this);

        (new Thread(checkConnection)).start();
    }

    @Override
    public void onBackPressed() {
        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        if (drawer.isDrawerOpen(GravityCompat.START)) {
            drawer.closeDrawer(GravityCompat.START);
        } else {
            super.onBackPressed();
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.drawer, menu);

        TextView header_phone = (TextView)findViewById(R.id.header_phone);
        header_phone.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(Intent.ACTION_DIAL, Uri.parse("tel:"+getResources().getString(R.string.phone_number))));
            }
        });
        TextView header_email = (TextView)findViewById(R.id.header_email);
        header_email.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent msg = new Intent(Intent.ACTION_SENDTO, Uri.parse("mailto:"));
                msg.putExtra(Intent.EXTRA_EMAIL, new String[] {getResources().getString(R.string.email_address)});
                if (msg.resolveActivity(getPackageManager()) != null) {
                    startActivity(msg);
                }
            }
        });

        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    /**
     * The list of items able to be selected from the Navigation Drawer.
     * When selected, they will start a new Activity
     */
    @SuppressWarnings("StatementWithEmptyBody")
    @Override
    public boolean onNavigationItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.nav_start: break;
            case R.id.nav_flavors:
                startActivity(new Intent(this, FlavorsActivity.class));
                break;
            case R.id.nav_voting:
                startActivity(new Intent(this, VotingActivity.class));
                break;
            case R.id.nav_developer:
                startActivity(new Intent(this, DeveloperActivity.class));
                break;
            case R.id.nav_map:
                startActivity(new Intent(this, ContactActivity.class));
                break;
            case R.id.nav_sched: break;
            case R.id.nav_contact: break;
        }

        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        drawer.closeDrawer(GravityCompat.START);
        return true;
    }

    /**
     * Sets the content view to the layout chosen
     * in the navigation drawer.
     */
    public void setViewFlipperContent(String activityName) {
        if (activityName == null)
            vf.setDisplayedChild(0);
        switch (activityName) {
            //case StartActivity.NAME: break;
            case FlavorsActivity.NAME: vf.setDisplayedChild(0); break;
            case VotingActivity.NAME: vf.setDisplayedChild(1); break;
            case DeveloperActivity.NAME: vf.setDisplayedChild(2); break;
            case ContactActivity.NAME: vf.setDisplayedChild(3); break;
        }
    }

    /**
     * Checks the internet connection every so often
     */
    private Runnable checkConnection = new Runnable() {
        @Override
        public void run() {
            try {
                Thread.sleep(SLEEP_TIME);

                Message msg = new Message();
                msg.obj = isNetworkAvailable();
                connectionHandler.sendMessage(msg);

                (new Thread(checkConnection)).start();
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
    };

    /**
     * Should be run on a Thread to check and see if there is
     * a network connection. If not, layouts should be
     * modified to fit the screen better.
     */
    private boolean isNetworkAvailable() {
        ConnectivityManager connectivityManager
                = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo activeNetworkInfo = connectivityManager.getActiveNetworkInfo();
        return activeNetworkInfo != null && activeNetworkInfo.isConnected();
    }

    /**
     * Handler to act if there is no Internet connection
     */
    private Handler connectionHandler = new Handler() {
        public void handleMessage(Message message) {
            if (!(Boolean)message.obj) {
                isConnected = false;
                Snackbar.make(vf, "Offline", Snackbar.LENGTH_INDEFINITE).show();
            }
        }
    };
}
