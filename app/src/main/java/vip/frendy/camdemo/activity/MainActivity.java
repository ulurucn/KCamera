package vip.frendy.camdemo.activity;

import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

import vip.frendy.camdemo.R;
import vip.frendy.camdemo.fragment.FragmentCrop;
import vip.frendy.camdemo.fragment.FragmentEnhance;
import vip.frendy.camdemo.fragment.FragmentFilter;
import vip.frendy.camdemo.fragment.FragmentFrame;
import vip.frendy.camdemo.fragment.FragmentMain;
import vip.frendy.camdemo.fragment.FragmentMosaic;
import vip.frendy.camdemo.fragment.FragmentRotate;
import vip.frendy.base.Permission;
import vip.frendy.camdemo.fragment.FragmentSticker;
import vip.frendy.camdemo.fragment.FragmentWarp;
import vip.frendy.camdemo.fragment.FragmentWarp2;
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
        } else if(id == R.id.rotate) {
            switchFragment(R.id.content_fragment, FragmentRotate.getInstance(args, this));
        } else if(id == R.id.crop) {
            switchFragment(R.id.content_fragment, FragmentCrop.getInstance(args, this));
        } else if(id == R.id.sticker) {
            switchFragment(R.id.content_fragment, FragmentSticker.getInstance(args, this));
        } else if(id == R.id.frame) {
            switchFragment(R.id.content_fragment, FragmentFrame.getInstance(args, this));
        } else if(id == R.id.mosaic) {
            switchFragment(R.id.content_fragment, FragmentMosaic.getInstance(args, this));
        } else if(id == R.id.warp) {
            switchFragment(R.id.content_fragment, FragmentWarp.getInstance(args, this));
        } else if(id == R.id.warp2) {
            switchFragment(R.id.content_fragment, FragmentWarp2.getInstance(args, this));
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
