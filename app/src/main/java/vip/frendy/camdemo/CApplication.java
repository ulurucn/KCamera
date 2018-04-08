package vip.frendy.camdemo;

import android.app.Application;
import android.os.Build;
import android.os.StrictMode;

/**
 * Created by frendy on 2018/4/8.
 */

public class CApplication extends Application {

    @Override
    public void onCreate() {
        super.onCreate();

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN_MR2) {
            StrictMode.VmPolicy.Builder builder = new StrictMode.VmPolicy.Builder();
            StrictMode.setVmPolicy(builder.build());
            builder.detectFileUriExposure();
        }
    }
}
