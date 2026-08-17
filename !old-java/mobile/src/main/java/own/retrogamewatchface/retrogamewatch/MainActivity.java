package own.retrogamewatchface.retrogamewatch;

import android.app.Activity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Switch;
import android.widget.EditText;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.common.api.GoogleApiClient.Builder;
import com.google.android.gms.common.api.ResultCallback;
import com.google.android.gms.wearable.DataItem;
import com.google.android.gms.wearable.DataItemBuffer;
import com.google.android.gms.wearable.DataMap;
import com.google.android.gms.wearable.DataMapItem;
import com.google.android.gms.wearable.PutDataMapRequest;
import com.google.android.gms.wearable.Wearable;

import own.retrogamewatchface.R;
import own.retrogamewatchface.api.util.GoogleAPI;
import own.retrogamewatchface.api.util.handle.BuildHandle;
import java.util.Iterator;

import android.content.SharedPreferences;
import android.preference.PreferenceManager;

public class MainActivity extends Activity {
    String TAG = "retrogamewatch Main";
    private GoogleApiClient client;
    private Switch dayType;
    private Switch metric;
    private Switch twentyFour;
    private EditText apikey;
    public static String myapikey;
    public static SharedPreferences pref;
    public static SharedPreferences.Editor editor;


    private void updateBooleans() {
        new Thread(new Runnable() {
            public void run() {
                boolean z;
                boolean z2 = true;
                PutDataMapRequest request = PutDataMapRequest.create("/update_weather");
                DataMap map = request.getDataMap();
                map.putBoolean("metric", MainActivity.this.metric.isChecked());
                String str = "twelve-hour";
                if (MainActivity.this.twentyFour.isChecked()) {
                    z = false;
                } else {
                    z = true;
                }
                map.putBoolean(str, z);
                String str2 = "mm/dd/yyyy";
                if (MainActivity.this.dayType.isChecked()) {
                    z2 = false;
                }
                map.putBoolean(str2, z2);
                Wearable.DataApi.putDataItem(MainActivity.this.client, request.asPutDataRequest());
            }
        }).start();
    }

    private void restore() {
        this.client.connect();
        Wearable.DataApi.getDataItems(this.client).setResultCallback(new ResultCallback<DataItemBuffer>() {
            public void onResult(DataItemBuffer dataItems) {
                boolean z = true;
                Iterator it = dataItems.iterator();
                while (it.hasNext()) {
                    DataItem item = (DataItem) it.next();
                    if (item.getUri().getPath().equals("/update_weather")) {
                        DataMap map = DataMapItem.fromDataItem(item).getDataMap();
                        if (map.containsKey("metric")) {
                            MainActivity.this.metric.setChecked(map.getBoolean("metric"));
                        }
                        if (map.containsKey("twelve-hour")) {
                            boolean z2;
                            Switch access$100 = MainActivity.this.twentyFour;
                            if (map.getBoolean("twelve-hour")) {
                                z2 = false;
                            } else {
                                z2 = true;
                            }
                            access$100.setChecked(z2);
                        }
                        if (map.containsKey("mm/dd/yyyy")) {
                            Switch access$200 = MainActivity.this.dayType;
                            if (map.getBoolean("mm/dd/yyyy")) {
                                z = false;
                            }
                            access$200.setChecked(z);
                            return;
                        }
                        return;
                    }
                }
                dataItems.release();
            }
        });
    }

    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        this.client = GoogleAPI.build(this, new BuildHandle<Builder>() {
            public Builder handle(Builder builder) {
                return builder.addApi(Wearable.API);
            }
        });
        OnClickListener updateOnClick = new OnClickListener() {
            public void onClick(View v) {
                MainActivity.this.updateBooleans();
            }
        };

        pref = PreferenceManager.getDefaultSharedPreferences(this);
        editor = pref.edit();
        myapikey = pref.getString("APIKEY", "your api key for openweathermap");

        metric = (Switch) findViewById(R.id.metric);
        metric.setOnClickListener(updateOnClick);
        twentyFour = (Switch) findViewById(R.id.twenty_four);
        twentyFour.setOnClickListener(updateOnClick);
        dayType = (Switch) findViewById(R.id.euro);
        dayType.setOnClickListener(updateOnClick);
        apikey = (EditText) findViewById(R.id.apikey);
        apikey.setText(myapikey);
        Log.i(TAG, "edittext: "+myapikey);
        restore();
    }



    @Override
    public void onStop() {
        super.onStop();

        myapikey=apikey.getText().toString();
        editor.putString("APIKEY", apikey.getText().toString());
        editor.apply();
        Log.i(TAG, "onstop: "+myapikey);
    }

    public void onDestroy() {
        super.onDestroy();
        this.client.disconnect();
    }
}
