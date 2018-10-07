package own.retrogamewatchface.api.util.weather;

import android.content.Context;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;
import android.util.Log;

import own.retrogamewatchface.api.util.io.Internet;
import own.retrogamewatchface.retrogamewatch.MainActivity;

import org.json.JSONObject;

//api.openweathermap.org/data/2.5/weather?lat=35&lon=139

public class OpenWeatherMap {

    public static JSONObject getCurrentWeather(Context context, double latitude, double longitude) {
        String TAG = "retro getcurrentweather";
        String API = "http://api.openweathermap.org/data/2.5/weather";
        SharedPreferences pref;
        pref = PreferenceManager.getDefaultSharedPreferences(context);
        String myapikey = pref.getString("APIKEY", "your api key for openweathermap");

        Log.i(TAG, "myapikey: "+myapikey);
        return Internet.readAsJSON(String.format("%s?lat=%s&lon=%s&appid=%s", new Object[]{API, latitude, longitude, myapikey}));
    }
}
