package vip.frendy.camdemo.activity;

import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;

import vip.frendy.base.Permission;

/**
 * Created by frendy on 2018/4/8.
 */

public class BaseActivity extends AppCompatActivity implements Permission.IPermissionsListener {
    private Permission mPermission;
    protected boolean shouldRequestPermission = false;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        //权限申请
        requestPermissions();
    }

    protected void requestPermissions() {
        if(shouldRequestPermission) {
            mPermission = new Permission(this);
            mPermission.requestPermissions();
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if(shouldRequestPermission && mPermission != null) {
            mPermission.onRequestPermissionsResult(requestCode, permissions, grantResults, this);
        }
    }

    @Override
    public void onPermissionsGranted() {

    }

    @Override
    public void onPermissionsDenied() {

    }
}
