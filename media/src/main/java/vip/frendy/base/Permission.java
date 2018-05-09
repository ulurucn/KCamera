package vip.frendy.base;

import android.app.Activity;
import android.content.pm.PackageManager;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;

/**
 * Created by frendy on 2018/4/10.
 */

public class Permission {
    private Activity mActivity;
    private boolean isGranted = false;

    private static final int REQUEST_CODE_PERMISSION = 0x11;
    private String[] permissions = {
            "android.permission.CAMERA",
            "android.permission.FLASHLIGHT",
            "android.permission.WRITE_EXTERNAL_STORAGE"
    };

    public Permission(Activity activity) {
        mActivity = activity;
    }

    public void requestPermissions() {
        ActivityCompat.requestPermissions(mActivity, permissions, REQUEST_CODE_PERMISSION);
    }

    public void requestPermissions(String[] permissions) {
        ActivityCompat.requestPermissions(mActivity, permissions, REQUEST_CODE_PERMISSION);
    }

    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults, IPermissionsListener listener) {
        if (requestCode == REQUEST_CODE_PERMISSION) {
            if(grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                isGranted = true;
                listener.onPermissionsGranted();
            } else {
                isGranted = false;
                listener.onPermissionsDenied();
            }
        }
    }

    public interface IPermissionsListener {
        void onPermissionsGranted();
        void onPermissionsDenied();
    }
}
