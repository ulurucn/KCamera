package vip.frendy.camdemo.extension;

import android.os.Handler;
import android.os.Looper;

/**
 * Created by frendy on 2017/11/15.
 */
public class HandlerExt {

    public static void postToUI(Runnable runnable) {
        Handler mainHandler = new Handler(Looper.getMainLooper());
        mainHandler.post(runnable);
    }

    public static void postDelayToUI(Runnable runnable, Long delay) {
        Handler mainHandler = new Handler(Looper.getMainLooper());
        mainHandler.postDelayed(runnable, delay);
    }
}

