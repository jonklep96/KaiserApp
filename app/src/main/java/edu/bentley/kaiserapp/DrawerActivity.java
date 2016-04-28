package edu.bentley.kaiserapp;

import android.app.AlertDialog;
import android.app.Notification;
import android.app.NotificationManager;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.Uri;
import android.os.Bundle;
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

import edu.bentley.kaiserapp.information.*;
import edu.bentley.kaiserapp.main.DeveloperActivity;
import edu.bentley.kaiserapp.main.FlavorsActivity;
import edu.bentley.kaiserapp.main.StartActivity;
import edu.bentley.kaiserapp.main.VotingActivity;

public class DrawerActivity extends AppCompatActivity implements NavigationView.OnNavigationItemSelectedListener {

    /**
     * Stores a String to serve as a key so the
     * child Activity can be identified.
     */
    public final static String ACTIVITY_KEY = "activity";

    /**
     * Notification Key to be used to find
     * the notification from an Intent
     */
    public final static String NOTIFY_KEY = "notificationId";

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
    public static ViewFlipper vf;

    /**
     * Boolean value to see if the application is
     * connected to the Internet or not
     */
    private static boolean isConnected = true;

    /**
     * Declare a Snackbar that will be used to
     * notify the user that they have lost connection
     * to the Internet
     */
    private static Snackbar sb;

    /**
     * Notification Manager that will be used to send notifications
     * throughout the application. This will be accessible anywhere
     * that inherits from DrawerActivity.
     * mNotifyDetails will be the Notification from which the
     * application will send out.
     */
    public NotificationManager mNotificationManager;
    public Notification mNotifyDetails;
    public final static int NOTIFY_ID = 100;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_drawer);

        /**
         * Creates a Toolbar on the first creation of the application.
         * Also clears away any notifications that may have opened up
         * the application.
         */
        Toolbar toolbar = (Toolbar)findViewById(R.id.toolbar);
        mNotificationManager = (NotificationManager)getSystemService(NOTIFICATION_SERVICE);
        if (getSupportActionBar() == null) {
            setSupportActionBar(toolbar);

            // Clears the notification
            mNotificationManager.cancel(getIntent().getIntExtra(NOTIFY_KEY, 0));
        }

        /**
         * Find the ViewFlipper which will allow the
         * application to set the content on each page to
         * an activity layout.
         */
        vf = (ViewFlipper)findViewById(R.id.vf);
        if (savedInstanceState != null)
            setViewFlipperContent(savedInstanceState.getString(ACTIVITY_KEY));
        else
            vf.setDisplayedChild(0);

        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(
                this, drawer, toolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close);
        drawer.setDrawerListener(toggle);
        toggle.syncState();

        NavigationView navigationView = (NavigationView) findViewById(R.id.nav_view);
        navigationView.setNavigationItemSelectedListener(this);

        getIsConnected(this);
    }

    /**
     * Overrides the back pressing mechanism so
     * the drawer will be closed rather than going back a page
     */
    @Override
    public void onBackPressed() {
        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        if (drawer.isDrawerOpen(GravityCompat.START)) {
            drawer.closeDrawer(GravityCompat.START);
        } else {
            super.onBackPressed();
        }
    }

    /**
     * Allow the user to contact the store through a phone
     * number, or the user can email the store. The user
     * just has to click the phone number or email.
     */
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.option_menu, menu);

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
        switch (item.getItemId()) {
            case R.id.action_call:
                startActivity(new Intent(Intent.ACTION_DIAL, Uri.parse("tel:"+getResources().getString(R.string.phone_number))));
                break;
            case R.id.action_back:
                super.onBackPressed();
            default:
                super.onContextItemSelected(item);
        }
        return true;
    }

    /**
     * The list of items able to be selected from the Navigation Drawer.
     * When selected, they will start a new Activity
     */
    @SuppressWarnings("StatementWithEmptyBody")
    @Override
    public boolean onNavigationItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.nav_start:
                startActivity(new Intent(this, StartActivity.class));
                break;
            case R.id.nav_flavors:
                startActivity(new Intent(this, FlavorsActivity.class));
                break;
            case R.id.nav_voting:
                if (isConnected)
                    startActivity(new Intent(this, VotingActivity.class));
                else
                    createOfflineDialog("You must be online to vote for the next flavor");
                break;
            case R.id.nav_developer:
                startActivity(new Intent(this, DeveloperActivity.class));
                break;
            case R.id.nav_map:
                if (isConnected)
                    startActivity(new Intent(this, ContactActivity.class));
                else
                    createOfflineDialog("You must be online to access the map");
                break;
            case R.id.nav_sched:
                startActivity(new Intent(this, ScheduleActivity.class));
                break;
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
            case StartActivity.NAME: vf.setDisplayedChild(0);break;
            case FlavorsActivity.NAME: vf.setDisplayedChild(1); break;
            case VotingActivity.NAME: vf.setDisplayedChild(2); break;
            case DeveloperActivity.NAME: vf.setDisplayedChild(3); break;
            case ContactActivity.NAME: vf.setDisplayedChild(4); break;
            case ScheduleActivity.NAME: vf.setDisplayedChild(5); break;
        }
    }

    /**
     * Looks for a change in the Internet connection
     * then updates the variable to let the program
     * know if there is no connection.
     */
    public static class ConnectionReceiver extends BroadcastReceiver {
        @Override
        public void onReceive(Context context, Intent intent) {
            DrawerActivity.getIsConnected(context);
        }
    }

    /**
     * Creates a Snackbar if the phone is Offline
     */
    public static void getIsConnected(Context context) {
        ConnectivityManager connectivityManager = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo activeNetworkInfo = connectivityManager.getActiveNetworkInfo();

        if (activeNetworkInfo != null && activeNetworkInfo.isConnected()) {
            isConnected = true;
            if (sb != null)
                sb.dismiss();
        } else {
            isConnected = false;
            sb = Snackbar.make(vf, "Offline", Snackbar.LENGTH_INDEFINITE);
            sb.show();
        }
    }

    /**
     * Creates an AlertDialog if the user selects an option
     * that requires an Internet connection to use.
     */
    public void createOfflineDialog(String dialogText) {
        AlertDialog dialog = new AlertDialog.Builder(DrawerActivity.this).create();
        dialog.setTitle("Offline");
        dialog.setButton(DialogInterface.BUTTON_POSITIVE, dialogText, new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int whichButton) {
                dialog.dismiss();
            }
        });
        dialog.show();
    }
}
