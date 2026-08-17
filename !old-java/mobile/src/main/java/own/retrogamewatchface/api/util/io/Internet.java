package own.retrogamewatchface.api.util.io;

import android.util.Log;

import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.Scanner;
import org.json.JSONException;
import org.json.JSONObject;

import javax.net.ssl.HttpsURLConnection;

public class Internet {

    static String TAG = "retrogamewatch Internet";
    static String agent = "Retro Game WatchFace - email: elymbmx@gmail.com";


    public static URL urlFromString(String url) {
        try {
            return new URL(url);
        } catch (MalformedURLException e) {
            e.printStackTrace();
            return null;
        }
    }


    static String data;
    public static String readAsString(final String urlLocation) {
        Thread thread = new Thread(new Runnable(){
            @Override
            public void run() {
                try {
                    URL url = new URL(urlLocation);
                    HttpURLConnection conn = (HttpURLConnection) url.openConnection();
                    Log.d(TAG, agent);
                    conn.setRequestProperty("User-Agent", agent);
                    //conn.setRequestProperty("Accept", "application/vnd.noaa.dwml+xml;version=1");

                    conn.setReadTimeout(10000 /* milliseconds */);
                    conn.setConnectTimeout(10000 /* milliseconds */);
                    conn.setRequestMethod("GET");
                    conn.setDoInput(true);
                    // Starts the query
                    conn.connect();
                    InputStream stream = conn.getInputStream();

                    data = convertStreamToString(stream);

                    //readAndParseJSON(data);

                    stream.close();
                } catch (NullPointerException np) {
                    Log.i(TAG, "NullPointerException in fetchJSON()...");
                    np.printStackTrace();
                } catch (IOException io) {
                    Log.i(TAG, "IOException in fetchJSON()...");
                    io.printStackTrace();
                } catch (Exception e) {
                    Log.i(TAG, "Exception in fetchJSON()...");
                    e.printStackTrace();
                }
            }
        });
        Log.i(TAG, "fetchJSON() thread start");
        thread.start();
        Log.i(TAG, "fetchJSON() end...");

        Log.i(TAG, "url: "+urlLocation);
        Log.i(TAG, "data: "+data);
        return data;
    }

    static String convertStreamToString(java.io.InputStream is) {
        java.util.Scanner s = new java.util.Scanner(is).useDelimiter("\\A");
        return s.hasNext() ? s.next() : "";
    }




    public static JSONObject readAsJSON(String urlLocation) {
        String json = readAsString(urlLocation);
        if (json == null) {
            return null;
        }
        try {
            return new JSONObject(json);
        } catch (JSONException e) {
            e.printStackTrace();
            return null;
        }
    }
}
