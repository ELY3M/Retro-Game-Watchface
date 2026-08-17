package own.retrogamewatchface.api;

import android.os.Handler;

public class BackgroundHandler {
    private int delay = 600000;
    private Handler handler = new Handler();
    private final Runnable runnable;
    private boolean running = false;

    public BackgroundHandler(Runnable runnable) {
        this.runnable = runnable;
    }

    public void start() {
        this.running = true;
        this.handler.postDelayed(new Runnable() {
            public void run() {
                BackgroundHandler.this.runnable.run();
                if (BackgroundHandler.this.running) {
                    BackgroundHandler.this.handler.postDelayed(this, (long) BackgroundHandler.this.delay);
                }
            }
        }, (long) delay);
    }

    public void stop() {
        this.running = false;
        this.handler.removeCallbacks(this.runnable);
    }
}
