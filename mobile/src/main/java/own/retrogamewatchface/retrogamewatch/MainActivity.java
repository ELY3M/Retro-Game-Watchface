package own.retrogamewatchface.retrogamewatch;

import android.app.Activity;
import android.os.Bundle;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Switch;
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


public class MainActivity extends Activity {
    private GoogleApiClient client;
    private Switch dayType;
    private Switch metric;
    private Switch twentyFour;

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
        this.metric = (Switch) findViewById(R.id.metric);
        this.metric.setOnClickListener(updateOnClick);
        this.twentyFour = (Switch) findViewById(R.id.twenty_four);
        this.twentyFour.setOnClickListener(updateOnClick);
        this.dayType = (Switch) findViewById(R.id.euro);
        this.dayType.setOnClickListener(updateOnClick);
        restore();
    }

    public void onDestroy() {
        super.onDestroy();
        this.client.disconnect();
    }
}
