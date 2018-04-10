package vip.frendy.camdemo.activity;

import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

import vip.frendy.camdemo.R;
import vip.frendy.camdemo.fragment.FragmentEnhance;
import vip.frendy.camdemo.fragment.FragmentFilter;
import vip.frendy.camdemo.fragment.FragmentMain;
import vip.frendy.camera.Permission;
import vip.frendy.edit.interfaces.IPictureEditListener;

/**
 * Created by frendy on 2018/4/8.
 */

public class MainActivity extends BaseFragmentActivity implements FragmentMain.IRouter, IPictureEditListener {
    private FragmentMain mMainFragment;
    private Permission mPermission;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        mMainFragment = FragmentMain.getInstance(this);
        setCurrentFragment(R.id.content_fragment, mMainFragment);

        //权限申请
        mPermission = new Permission(this);
        mPermission.requestPermissions();
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if(mPermission != null)
            mPermission.onRequestPermissionsResult(requestCode, permissions, grantResults, new Permission.IPermissionsListener() {
                @Override
                public void onPermissionsGranted() {}
                @Override
                public void onPermissionsDenied() {}
            });
    }

    @Override
    public void onBackPressed() {
        if(getSupportFragmentManager().getBackStackEntryCount() > 0) {
            getSupportFragmentManager().popBackStack();
        } else {
            super.onBackPressed();
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if(resultCode != RESULT_OK) return;

        if(mMainFragment != null)
            mMainFragment.onActivityResult(requestCode, resultCode, data);
    }

    @Override
    public void onRouteTo(int id, Bundle args) {
        if(id == R.id.enhance) {
            switchFragment(R.id.content_fragment, FragmentEnhance.getInstance(args, this));
        } else if(id == R.id.filter) {
            switchFragment(R.id.content_fragment, FragmentFilter.getInstance(args, this));
        }
    }

    @Override
    public void onPictureEditApply(int id, String path) {
        //退出编辑页
        getSupportFragmentManager().popBackStack();
        //更新首页
        if(id == 0) {
           mMainFragment.show(path);
        }
    }

    @Override
    public void onPictureEditCancel(int id) {
        //退出编辑页
        getSupportFragmentManager().popBackStack();
    }
}
