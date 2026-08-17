package own.retrogamewatchface.retrogamewatch;

import android.net.Uri;
import com.google.android.gms.common.data.FreezableUtils;
import com.google.android.gms.wearable.DataEvent;
import com.google.android.gms.wearable.DataEventBuffer;
import com.google.android.gms.wearable.DataMap;
import com.google.android.gms.wearable.DataMapItem;
import com.google.android.gms.wearable.WearableListenerService;

public class WeatherListenerService extends WearableListenerService {
    private static final String PATH_UPDATE_WEATHER = "/update_weather";
    private static double kelvin = Double.MIN_VALUE;
    private static boolean metric = false;
    private static boolean twelveHour = true;
    private static boolean usDate = true;
    private static int weatherType = 0;

    public static double getKelvin() {
        return kelvin;
    }

    public static int getWeatherType() {
        return weatherType;
    }

    public static boolean isMetric() {
        return metric;
    }

    public static boolean isTwelveHour() {
        return twelveHour;
    }

    public static boolean isUnitedStatesDate() {
        return usDate;
    }

    public void onDataChanged(DataEventBuffer dataEvents) {
        super.onDataChanged(dataEvents);
        for (DataEvent event : FreezableUtils.freezeIterable(dataEvents)) {
            Uri uri = event.getDataItem().getUri();
            if (PATH_UPDATE_WEATHER.equals(uri != null ? uri.getPath() : null)) {
                DataMap map = DataMapItem.fromDataItem(event.getDataItem()).getDataMap();
                if (map.containsKey("kelvin")) {
                    kelvin = map.getDouble("kelvin");
                }
                if (map.containsKey("weather-type")) {
                    weatherType = map.getInt("weather-type");
                }
                if (map.containsKey("metric")) {
                    metric = map.getBoolean("metric");
                }
                if (map.containsKey("twelve-hour")) {
                    twelveHour = map.getBoolean("twelve-hour");
                }
                if (map.containsKey("mm/dd/yyyy")) {
                    usDate = map.getBoolean("mm/dd/yyyy");
                }
                RetroGameWatch.getEngineInstance().invalidate();
            }
        }
    }
}
