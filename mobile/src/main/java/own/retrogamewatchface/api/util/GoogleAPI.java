package own.retrogamewatchface.api.util;

import android.content.Context;
import android.location.Location;
import android.os.Bundle;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.common.api.GoogleApiClient.Builder;
import com.google.android.gms.common.api.GoogleApiClient.ConnectionCallbacks;
import com.google.android.gms.location.LocationListener;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.wearable.Wearable;
import own.retrogamewatchface.api.util.handle.BuildHandle;
import own.retrogamewatchface.api.util.handle.Handle;

public class GoogleAPI {
    private static final ConnectionCallbacks CONNECTION_CALLBACKS = new ConnectionCallbacks() {
        public void onConnected(Bundle bundle) {
            if (!(GoogleAPI.initializedLocationRequester || GoogleAPI.locationRequest == null)) {
                LocationServices.FusedLocationApi.requestLocationUpdates(GoogleAPI.client, GoogleAPI.locationRequest, GoogleAPI.LOCATION_LISTENER);
                GoogleAPI.initializedLocationRequester = true;
            }
            GoogleAPI.lastLocation = LocationServices.FusedLocationApi.getLastLocation(GoogleAPI.client);
            if (GoogleAPI.locationHandle != null) {
                GoogleAPI.locationHandle.handle(GoogleAPI.lastLocation);
            }
        }

        public void onConnectionSuspended(int i) {
        }
    };
    private static final LocationListener LOCATION_LISTENER = new LocationListener() {
        public void onLocationChanged(Location location) {
            GoogleAPI.lastLocation = location;
            if (GoogleAPI.locationHandle != null) {
                GoogleAPI.locationHandle.handle(GoogleAPI.lastLocation);
            }
        }
    };
    private static GoogleApiClient client;
    private static boolean initializedLocationRequester = false;
    private static Location lastLocation;
    private static Handle<Location> locationHandle;
    private static LocationRequest locationRequest;

    public static GoogleApiClient build(Context context, BuildHandle<Builder> handle) {
        if (client != null) {
            return client;
        }
        GoogleApiClient build = ((Builder) handle.handle(new Builder(context))).build();
        client = build;
        return build;
    }

    public static GoogleApiClient buildWithLocationAPI(Context context) {
        return build(context, new BuildHandle<Builder>() {
            public Builder handle(Builder builder) {
                return builder.addConnectionCallbacks(GoogleAPI.CONNECTION_CALLBACKS).addApi(LocationServices.API).addApi(Wearable.API);
            }
        });
    }

    public static Location getLastLocation() {
        return lastLocation;
    }

    public static void setLocationHandle(Handle<Location> locationHandle) {
        locationHandle = locationHandle;
    }

    public static void setLocationPollRate(int interval) {
        locationRequest = new LocationRequest();
        locationRequest.setInterval((long) interval);
        locationRequest.setFastestInterval((long) interval);
        locationRequest.setPriority(102);
    }
}
