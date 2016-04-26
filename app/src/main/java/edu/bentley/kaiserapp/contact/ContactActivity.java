package edu.bentley.kaiserapp.contact;

import android.content.Context;
import android.content.Intent;
import android.location.Address;
import android.location.Geocoder;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.Uri;
import android.os.Handler;
import android.os.Message;
import android.support.v4.app.FragmentActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.URL;
import java.net.URLConnection;
import java.util.List;

import edu.bentley.kaiserapp.R;

public class ContactActivity extends FragmentActivity implements OnMapReadyCallback {

    private GoogleMap mMap;

    private String WEATHER_KEY;

    /**
     * Variables to store the location of the store.
     * This will be used to find the store in Google Maps
     * and the weather API.
     */
    private final String STORE_NAME = "Kaiserstühler Landeis";
    private final String STORE_ADDRESS = "Untere Guldenstraße 10, Königschaffhausen 79346 Endingen am Kaiserstuhl, Freiburg, Baden-Württemberg, Germany";
    private LatLng storeLatLng;
    private final float zoom = 16.0f;
    private final String CITY = "ENDINGEN";
    private final String STATE = "DE";

    private Handler weatherHandler;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_contact);

        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);
        mMap = mapFragment.getMap();
        mMap.setMapType(GoogleMap.MAP_TYPE_HYBRID);

        mMap.setOnMarkerClickListener(
                new GoogleMap.OnMarkerClickListener() {
                    public boolean onMarkerClick(Marker m) {
                        Intent i = new Intent(Intent.ACTION_VIEW, Uri.parse("geo:0,0?q=" + Uri.encode(STORE_ADDRESS)));
                        if (i.resolveActivity(getPackageManager()) != null) {
                            startActivity(i);
                        }
                        return true;
                    }
                }
        );

        /**
         * Write the schedule to the screen programmatically.
         */
        TextView scheduleDays = (TextView)findViewById(R.id.tv_schedule_days);
        TextView scheduleTimes = (TextView)findViewById(R.id.tv_schedule_time);
        for(String e : getResources().getStringArray(R.array.schedule_days))
            scheduleDays.append(e+"\n");
        for(String e : getResources().getStringArray(R.array.schedule_times))
            scheduleTimes.append(e+"\n");
    }

    @Override
    public void onResume() {
        super.onResume();
        WEATHER_KEY = getResources().getString(R.string.openweather_key);
        Thread weather = new Thread(weatherTask);
        weather.start();
        weatherHandler = new Handler() {
            public void handleMessage(Message message) {
                Weather _weather = (Weather)message.obj;

                findViewById(R.id.pb_weather_widget).setVisibility(View.INVISIBLE);
                ((TextView)findViewById(R.id.tv_cur_temp)).setText(String.format("%.0f",_weather.getCurrentTemperature()));
                ((TextView)findViewById(R.id.tv_high_temp)).setText(String.format("%.0f",_weather.getHighTemperature()));
                ((TextView)findViewById(R.id.tv_low_temp)).setText(String.format("%.0f",_weather.getLowTemperature()));
                ((ImageView)findViewById(R.id.iv_weather)).setImageBitmap(_weather.getIcon());
            }
        };
    }

    @Override
    public void onMapReady(GoogleMap googleMap) {
        mMap = googleMap;

        try {
            // Returns one address of where the ice cream store is located
            Geocoder geocoder = new Geocoder(this);
            List<Address> address = geocoder.getFromLocationName(STORE_ADDRESS, 1);

            if (address.size() == 0)
                Toast.makeText(getApplicationContext(), "Cannot find restaurant", Toast.LENGTH_SHORT).show();
                // Adds a marker to the map of where the store is located
            else {
                Address a = address.get(0);
                double lat = a.getLatitude();
                double lng = a.getLongitude();
                storeLatLng = new LatLng(lat, lng);
                mMap.addMarker(new MarkerOptions()
                        .title(STORE_ADDRESS)
                        .position(storeLatLng)
                        .icon(BitmapDescriptorFactory.fromResource(R.drawable.store_marker)
                        ));
            }

        } catch (IOException e) {
            e.printStackTrace();
        }

        // Move the camera to the location of the store
        if(storeLatLng != null)
            mMap.animateCamera(CameraUpdateFactory.newLatLngZoom(storeLatLng, zoom));
    }

    /**
     * A Thread that connects the user to OpenWeatherMap then
     * sends the information to a Handler where it can be
     * written to the screen.
     */
    private Runnable weatherTask = new Runnable() {
        @Override
        public void run() {
            String strJSON = String.format("http://api.openweathermap.org/data/2.5/weather?q=%s,%s&APPID=%s", CITY, STATE, WEATHER_KEY);

            String result = "";
            try {
                URL url = new URL(strJSON);
                URLConnection conn = url.openConnection();

                InputStream in = conn.getInputStream();
                BufferedReader reader = new BufferedReader(new InputStreamReader(in));

                String line;
                while((line = reader.readLine()) != null) { result += line; }

            } catch (Exception e) {
                e.printStackTrace();
            }

            try {
                JSONObject data = new JSONObject(result);
                JSONArray weather = data.getJSONArray("weather");
                JSONObject main = data.getJSONObject("main");

                String temp = main.getString("temp");
                String temp_max = main.getString("temp_max");
                String temp_min = main.getString("temp_min");
                JSONObject cur_weather = weather.getJSONObject(0);
                String id = cur_weather.getString("id");
                Weather locationWeather = new Weather(temp, temp_max, temp_min, id);

                Message msg = weatherHandler.obtainMessage();
                msg.obj = locationWeather;
                weatherHandler.sendMessage(msg);
            } catch (JSONException e) {
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
}
