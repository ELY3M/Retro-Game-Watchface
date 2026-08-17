package own.retrogamewatchface.api;

import android.os.Handler;

public class BackgroundHandler {
    private final int delay;
    private final Handler handler = new Handler();
    private final int minDelay;
    private final Runnable runnable;
    private boolean running = false;
    private boolean useDelay = true;

    public BackgroundHandler(Runnable runnable, int minDelay, int delay) {
        this.runnable = runnable;
        this.minDelay = minDelay;
        this.delay = delay;
    }

    public void setUseDelay(boolean useDelay) {
        this.useDelay = useDelay;
    }

    public void start() {
        this.running = true;
        this.handler.postDelayed(new Runnable() {
            public void run() {
                BackgroundHandler.this.runnable.run();
                if (BackgroundHandler.this.running) {
                    BackgroundHandler.this.handler.postDelayed(this, BackgroundHandler.this.useDelay ? (long) BackgroundHandler.this.delay : (long) BackgroundHandler.this.minDelay);
                }
            }
        }, (long) this.minDelay);
    }

    public void stop() {
        this.running = false;
        this.handler.removeCallbacks(this.runnable);
    }
}
