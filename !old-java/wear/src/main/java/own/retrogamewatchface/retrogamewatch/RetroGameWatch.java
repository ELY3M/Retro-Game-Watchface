package own.retrogamewatchface.retrogamewatch;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.Bitmap.Config;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Paint.Align;
import android.graphics.Paint.Style;
import android.graphics.PorterDuff.Mode;
import android.graphics.PorterDuffXfermode;
import android.graphics.Rect;
import android.graphics.Typeface;
import android.location.Location;
import android.location.LocationManager;
import android.os.Bundle;
import android.support.v4.view.MotionEventCompat;
import android.support.wearable.watchface.CanvasWatchFaceService;
import android.support.wearable.watchface.WatchFaceService;
import android.support.wearable.watchface.WatchFaceStyle.Builder;
import android.view.SurfaceHolder;
import android.view.WindowInsets;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.location.LocationListener;
import com.google.android.gms.wearable.DataMap;
import com.google.android.gms.wearable.PutDataMapRequest;
import com.google.android.gms.wearable.Wearable;

import own.retrogamewatchface.R;
import own.retrogamewatchface.api.BackgroundHandler;
import own.retrogamewatchface.api.util.GoogleAPI;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;


public class RetroGameWatch extends CanvasWatchFaceService implements LocationListener {
    private static final SimpleDateFormat EU_DATE = new SimpleDateFormat("dd/MM/yyyy", Locale.getDefault());
    private static final SimpleDateFormat TWELVE_HOUR_FORMAT = new SimpleDateFormat("hh:mm a", Locale.getDefault());
    private static final SimpleDateFormat TWENTY_FOUR_HOUR_FORMAT = new SimpleDateFormat("HH:mm a", Locale.getDefault());
    private static final SimpleDateFormat US_DATE = new SimpleDateFormat("MM/dd/yyyy", Locale.getDefault());
    private static android.support.wearable.watchface.CanvasWatchFaceService.Engine instance;
    private GoogleApiClient client;
    private Context context;
    private Location location;

    private class Engine extends android.support.wearable.watchface.CanvasWatchFaceService.Engine {
        private boolean ambient;
        private Paint backgroundPaint;
        private Bitmap battery;
        private Bitmap batteryModified;
        private Paint batteryOverlayPaint;
        private int batteryPercent;
        private Paint bitmapPaint;
        private Bitmap clear;
        private Bitmap cloudy;
        private String date;
        private String day;
        private Paint headerPaint;
        private boolean lowBitAmbient;
        private Bitmap rainy;
        private Bitmap snowy;
        private Paint subTextPaint;
        private String time;
        private Typeface typeface;
        private BackgroundHandler weatherHandler;
        private Bitmap[] weatherImages;
        private float yOff;

        private Engine() {
            super();
            this.batteryPercent = 100;
        }

        public void invalidate() {
            Calendar calendar = Calendar.getInstance(Calendar.getInstance().getTimeZone());
            this.time = (WeatherListenerService.isTwelveHour() ? RetroGameWatch.TWELVE_HOUR_FORMAT : RetroGameWatch.TWENTY_FOUR_HOUR_FORMAT).format(calendar.getTime());
            this.day = calendar.getDisplayName(Calendar.DAY_OF_WEEK, Calendar.LONG, Locale.getDefault());
            this.date = (WeatherListenerService.isUnitedStatesDate() ? RetroGameWatch.US_DATE : RetroGameWatch.EU_DATE).format(calendar.getTime());
            super.invalidate();
        }

        public void onCreate(SurfaceHolder holder) {
            super.onCreate(holder);
            RetroGameWatch.instance = this;
            new Thread(new Runnable() {
                public void run() {
                    GoogleAPI.setLocationPollRate(5000);
                    RetroGameWatch.this.client = GoogleAPI.buildWithLocationAPI(RetroGameWatch.this.context);
                    RetroGameWatch.this.client.connect();
                }
            }).start();
            this.typeface = Typeface.createFromAsset(RetroGameWatch.this.getAssets(), "fonts/pixel.ttf");
            setWatchFaceStyle(new Builder(RetroGameWatch.this).setCardPeekMode(0).setBackgroundVisibility(0).setShowSystemUiTime(false).build());
            Resources resources = RetroGameWatch.this.getResources();
            this.battery = BitmapFactory.decodeResource(resources, R.drawable.battery);
            this.battery = this.battery.copy(Config.ARGB_8888, true);
            this.batteryOverlayPaint = new Paint();
            this.batteryOverlayPaint.setColor(Color.argb(150, MotionEventCompat.ACTION_MASK, 0, 0));
            this.batteryOverlayPaint.setStyle(Style.FILL);
            this.batteryOverlayPaint.setXfermode(new PorterDuffXfermode(Mode.SRC_ATOP));
            this.clear = BitmapFactory.decodeResource(resources, R.drawable.clear);
            this.cloudy = BitmapFactory.decodeResource(resources, R.drawable.cloudy);
            this.rainy = BitmapFactory.decodeResource(resources, R.drawable.rainy);
            this.snowy = BitmapFactory.decodeResource(resources, R.drawable.snowy);
            this.weatherImages = new Bitmap[]{this.clear, this.cloudy, this.rainy, this.snowy};
            this.backgroundPaint = new Paint();
            this.backgroundPaint.setColor(resources.getColor(R.color.digital_background));
            this.headerPaint = createTextPaint(resources.getColor(R.color.digital_text));
            this.headerPaint.setTextAlign(Align.CENTER);
            this.subTextPaint = createTextPaint(resources.getColor(R.color.digital_text));
            this.subTextPaint.setTextSize(resources.getDimension(R.dimen.digital_sub_text_size));
            this.subTextPaint.setTextAlign(Align.RIGHT);
            this.bitmapPaint = new Paint();
            this.bitmapPaint.setTextAlign(Align.LEFT);
            this.yOff = resources.getDimension(R.dimen.digital_y_start);
            this.weatherHandler = new BackgroundHandler(new Runnable() {
                public void run() {
                    Location location = Engine.this.getLocation();
                    if (location != null) {
                        PutDataMapRequest request = PutDataMapRequest.create("/request_weather");
                        DataMap map = request.getDataMap();
                        map.putLong("timestamp", new Date().getTime());
                        map.putInt("latitude", (int) location.getLatitude());
                        map.putInt("longitude", (int) location.getLongitude());
                        Wearable.DataApi.putDataItem(RetroGameWatch.this.client, request.asPutDataRequest());
                        Engine.this.invalidate();
                    }
                }
            }, 5000, 1800000);
            this.weatherHandler.setUseDelay(false);
            this.weatherHandler.start();
            RetroGameWatch.this.registerReceiver(new BroadcastReceiver() {
                public void onReceive(Context context, Intent intent) {
                    Engine.this.batteryPercent = intent.getIntExtra("level", 100);
                    Engine.this.batteryModified = Engine.this.battery.copy(Engine.this.battery.getConfig(), true);
                    Canvas batteryCanvas = new Canvas(Engine.this.batteryModified);
                    batteryCanvas.drawRect(new Rect(0, 0, (int) (((double) Engine.this.batteryPercent) * (((double) batteryCanvas.getWidth()) / 100.0d)), batteryCanvas.getHeight()), Engine.this.batteryOverlayPaint);
                    Engine.this.invalidate();
                }
            }, new IntentFilter("android.intent.action.BATTERY_CHANGED"));
        }

        private Location getLocation() {
            if (location == null) {
                location = GoogleAPI.getLastLocation();
                if (location == null) {
                    location = ((LocationManager) RetroGameWatch.this.getSystemService(LOCATION_SERVICE)).getLastKnownLocation("gps");
                }
            }
            return location;
        }

        public void onDestroy() {
            super.onDestroy();
            this.weatherHandler.stop();
            PutDataMapRequest request = PutDataMapRequest.create("/update_weather");
            DataMap map = request.getDataMap();
            map.putBoolean("metric", WeatherListenerService.isMetric());
            map.putBoolean("twelve-hour", WeatherListenerService.isTwelveHour());
            map.putBoolean("mm/dd/yyyy", WeatherListenerService.isUnitedStatesDate());
            Wearable.DataApi.putDataItem(client, request.asPutDataRequest());
            client.disconnect();
        }

        private Paint createTextPaint(int textColor) {
            Paint paint = new Paint();
            paint.setColor(textColor);
            paint.setTypeface(this.typeface);
            paint.setAntiAlias(true);
            return paint;
        }

        public void onVisibilityChanged(boolean visible) {
            super.onVisibilityChanged(visible);
        }

        public void onApplyWindowInsets(WindowInsets insets) {
            super.onApplyWindowInsets(insets);
            this.headerPaint.setTextSize(RetroGameWatch.this.getResources().getDimension(R.dimen.digital_text_size));
        }

        public void onPropertiesChanged(Bundle properties) {
            super.onPropertiesChanged(properties);
            this.lowBitAmbient = properties.getBoolean(WatchFaceService.PROPERTY_LOW_BIT_AMBIENT, false);
        }

        public void onTimeTick() {
            super.onTimeTick();
            invalidate();
        }

        public void onAmbientModeChanged(boolean inAmbientMode) {
            super.onAmbientModeChanged(inAmbientMode);
            if (this.ambient != inAmbientMode) {
                this.ambient = inAmbientMode;
                if (this.lowBitAmbient) {
                    this.headerPaint.setAntiAlias(!inAmbientMode);
                }
                invalidate();
            }
        }

        public void onDraw(Canvas canvas, Rect bounds) {
            String tempLabel;
            canvas.drawRect(0.0f, 0.0f, (float) bounds.width(), (float) bounds.height(), this.backgroundPaint);
            canvas.drawText(this.time != null ? this.time : "", (float) (canvas.getWidth() / 2), this.yOff, this.headerPaint);
            canvas.drawText(this.day != null ? this.day : "", (float) ((canvas.getWidth() / 2) + 123), this.yOff + 30.0f, this.subTextPaint);
            canvas.drawText(this.date != null ? this.date : "", (float) ((canvas.getWidth() / 2) + 123), this.yOff + 66.0f, this.subTextPaint);
            canvas.drawBitmap(this.batteryModified != null ? this.batteryModified : this.battery, (float) ((canvas.getWidth() / 4) - 45), this.yOff + 10.0f, this.bitmapPaint);
            canvas.drawBitmap(this.weatherImages[WeatherListenerService.getWeatherType()], (float) ((canvas.getWidth() / 4) - 15), this.yOff + 70.0f, this.bitmapPaint);
            boolean metric = WeatherListenerService.isMetric();
            double kelvin = WeatherListenerService.getKelvin();
            if (kelvin != Double.MIN_VALUE) {
                int temp;
                this.weatherHandler.setUseDelay(true);
                GoogleAPI.setLocationPollRate(1800000);
                if (metric) {
                    temp = (int) (kelvin - 272.15d);
                } else {
                    temp = (int) (((kelvin - 273.15d) * 1.8d) + 32.0d);
                }
                tempLabel = temp + (metric ? "C" : "F");
            } else {
                tempLabel = "N/A";
            }
            canvas.drawText(tempLabel, (float) ((canvas.getWidth() / 2) + 73), this.yOff + 122.0f, this.subTextPaint);
        }
    }

    public static android.support.wearable.watchface.CanvasWatchFaceService.Engine getEngineInstance() {
        return instance;
    }

    public Engine onCreateEngine() {
        this.context = this;
        return new Engine();
    }

    public void onLocationChanged(Location location) {
        this.location = location;
    }
}
