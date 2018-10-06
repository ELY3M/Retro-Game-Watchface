package own.retrogamewatchface.api.util.weather;

import own.retrogamewatchface.api.util.io.Internet;
import org.json.JSONObject;

//api.openweathermap.org/data/2.5/weather?lat=35&lon=139

public class OpenWeatherMap {
    private static final String API = "http://api.openweathermap.org/data/2.5/weather";

    public static JSONObject getCurrentWeather(double latitude, double longitude) {
        return Internet.readAsJSON(String.format("%s?lat=%s&lon=%s&appid=PUT YOUR API KEY HERE!!!!", new Object[]{API, latitude, longitude}));
    }
}
