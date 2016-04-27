package edu.bentley.kaiserapp.information;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.ObjectOutputStream;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.Calendar;

public class Weather {

    private float currentTemperature, highTemperature, lowTemperature;
    private int id;
    private String iconUrl = "http://openweathermap.org/img/w/";
    private Bitmap icon;

    private final float KELVIN_CONVERSION = 273.15f;

    public Weather(String _currentTemperature, String _highTemperature, String _lowTemperature, String _id) {
        this.setCurrentTemperature(_currentTemperature);
        this.setHighTemperature(_highTemperature);
        this.setLowTemperature(_lowTemperature);
        this.setId(_id);
        this.setIconUrl(iconUrl + calcWeatherCode(this.id));

        try {
            URL url = new URL(iconUrl);
            InputStream is = (InputStream) url.getContent();
            byte[] buffer = new byte[8192];
            int bytesRead;
            ByteArrayOutputStream output = new ByteArrayOutputStream();
            while ((bytesRead = is.read(buffer)) != -1) {
                output.write(buffer, 0, bytesRead);
            }

            ObjectOutputStream os = new ObjectOutputStream(output);
            os.writeObject(output.toByteArray());
            icon = BitmapFactory.decodeByteArray(output.toByteArray(), 0, output.toByteArray().length);
        } catch (MalformedURLException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void setCurrentTemperature(String _temperature) {
        try {
            this.currentTemperature = Float.parseFloat(_temperature) - KELVIN_CONVERSION;
        } catch (Exception e) {
            this.currentTemperature = 0;
        }
    }
    public void setHighTemperature(String _temperature) {
        try {
            this.highTemperature = Float.parseFloat(_temperature) - KELVIN_CONVERSION;
        } catch (Exception e) {
            this.highTemperature = 0;
        }
    }
    public void setLowTemperature(String _temperature) {
        try {
            this.lowTemperature = Float.parseFloat(_temperature) - KELVIN_CONVERSION;
        } catch (Exception e) {
            this.lowTemperature = 0;
        }
    }
    public void setId(String _id) {
        try {
            this.id = Integer.parseInt(_id);
        } catch (Exception e) {
            this.id = 0;
        }
    }
    public void setIconUrl(String _iconUrl) {
        this.iconUrl = _iconUrl;
    }

    public float getCurrentTemperature() {
        return this.currentTemperature;
    }
    public float getHighTemperature() {
        return this.highTemperature;
    }
    public float getLowTemperature() {
        return this.lowTemperature;
    }
    public Bitmap getIcon() {
        return this.icon;
    }

    public String calcWeatherCode(int _id) {
        String toReturn = "";
        switch (_id) {
            case 200:case 201:case 202:case 210:case 211:
            case 212:case 221:case 230:case 231:case 232:
                toReturn = "11"; break;
            case 300:case 301:case 302:case 310:case 311:
            case 312:case 313:case 314:case 321:
                toReturn =  "09"; break;
            case 500:case 501:case 502:case 503:case 504:
                toReturn =  "10"; break;
            case 511:
                toReturn =  "13"; break;
            case 520:case 521:case 522:case 531:
                toReturn =  "09"; break;
            case 600:case 601:case 602:case 611:case 612:
            case 615:case 616:case 620:case 621:case 622:
                toReturn =  "13"; break;
            case 701:case 711:case 721:case 731:case 741:
            case 751:case 761:case 762:case 771:case 781:
                toReturn =  "50"; break;
            case 800:
                toReturn =  "01"; break;
            case 801:
                toReturn = "02"; break;
            case 802:
                toReturn =  "03"; break;
            case 803:case 804:
                toReturn =  "04"; break;
        }

        Calendar c = Calendar.getInstance();
        int hour = c.get(Calendar.HOUR);
        if (hour > 6 && hour < 19)
            toReturn += "d.png";
        else
            toReturn += "n.png";

        return toReturn;
    }

    @Override
    public String toString() {
        return "Temperature: " + this.currentTemperature + "\nHigh: " + this.highTemperature + "\nLow: "
                + this.lowTemperature + "\nId: " + this.id;
    }

}
