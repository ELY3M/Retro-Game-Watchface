package own.retrogamewatchface.retrogamewatch;

import android.content.Context;
import android.location.Location;
import android.location.LocationManager;
import android.net.Uri;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.common.api.GoogleApiClient.Builder;
import com.google.android.gms.wearable.DataEvent;
import com.google.android.gms.wearable.DataEventBuffer;
import com.google.android.gms.wearable.DataMap;
import com.google.android.gms.wearable.DataMapItem;
import com.google.android.gms.wearable.PutDataMapRequest;
import com.google.android.gms.wearable.Wearable;
import com.google.android.gms.wearable.WearableListenerService;
import own.retrogamewatchface.api.util.GoogleAPI;
import own.retrogamewatchface.api.util.handle.BuildHandle;
import own.retrogamewatchface.api.util.weather.OpenWeatherMap;
import java.util.Arrays;
import java.util.Date;
import java.util.Iterator;
import java.util.List;
import org.json.JSONObject;

public class WeatherRequestListenerService extends WearableListenerService {
    private static final String PATH_REQUEST_WEATHER = "/request_weather";
    private static GoogleApiClient client;
    private static Context context;
    private Location location;
    private static double latitude;
    private static double longitude;

    public WeatherRequestListenerService() {
        context = this;
    }


    private Location getLocation() {
        if (location == null) {
            location = GoogleAPI.getLastLocation();
            if (location == null) {
                location = ((LocationManager) this.getSystemService(LOCATION_SERVICE)).getLastKnownLocation("gps");
            }
        }

        longitude = location.getLongitude();
        latitude = location.getLatitude();
        setLocation(latitude, longitude);
        return location;
    }

    public void setLocation(double latitude, double longitude) {
        latitude = latitude;
        longitude = longitude;
    }

    public void onDataChanged(DataEventBuffer events) {
        super.onDataChanged(events);
        Iterator it = events.iterator();
        while (it.hasNext()) {
            DataEvent event = (DataEvent) it.next();
            Uri uri = event.getDataItem().getUri();
            if (PATH_REQUEST_WEATHER.equals(uri != null ? uri.getPath() : null)) {
                DataMap map = DataMapItem.fromDataItem(event.getDataItem()).getDataMap();
                //setLocation(map.getInt("latitude"), map.getInt("longitude"));
                getLocation();
                new Thread(new Runnable() {
                    public void run() {
                        WeatherRequestListenerService.syncWeather();
                    }
                }).start();
            }
        }
    }

    public static void syncWeather() {
        new Thread(new Runnable() {
            public void run() {
                if (WeatherRequestListenerService.client == null) {
                    WeatherRequestListenerService.client = GoogleAPI.build(WeatherRequestListenerService.context, new BuildHandle<Builder>() {
                        public Builder handle(Builder builder) {
                            return builder.addApi(Wearable.API);
                        }
                    });
                    WeatherRequestListenerService.client.connect();
                }
                JSONObject json = OpenWeatherMap.getCurrentWeather(context, WeatherRequestListenerService.latitude, WeatherRequestListenerService.longitude);
                if (json != null) {
                    try {
                        int weatherTypeIndex;
                        double kelvin = json.getJSONObject("main").getDouble("temp");
                        String weatherType = json.getJSONArray("weather").getJSONObject(0).getString("icon");
                        PutDataMapRequest request = PutDataMapRequest.create("/update_weather");
                        List<String> clear = Arrays.asList(new String[]{"01d", "01n", "02d", "02n"});
                        List<String> cloudy = Arrays.asList(new String[]{"03d", "03n", "04d", "04n"});
                        List<String> rainy = Arrays.asList(new String[]{"10d", "10n", "11d", "11n"});
                        DataMap map = request.getDataMap();
                        map.putLong("timestamp", new Date().getTime());
                        map.putDouble("kelvin", kelvin);
                        if (clear.contains(weatherType)) {
                            weatherTypeIndex = 0;
                        } else if (cloudy.contains(weatherType)) {
                            weatherTypeIndex = 1;
                        } else if (rainy.contains(weatherType)) {
                            weatherTypeIndex = 2;
                        } else {
                            weatherTypeIndex = 3;
                        }
                        map.putInt("weather-type", weatherTypeIndex);
                        Wearable.DataApi.putDataItem(WeatherRequestListenerService.client, request.asPutDataRequest());
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }
            }
        }).start();
    }
}
