package vip.frendy.camdemo.activity;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.FragmentTransaction;

import vip.frendy.camdemo.fragment.BaseFragment;

/**
 * Created by frendy on 2017/11/27.
 */

public abstract class BaseFragmentActivity extends BaseActivity {
    protected BaseFragment currentFragment;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    protected void setCurrentFragment(int resId, BaseFragment fragment) {
        FragmentTransaction transaction = getSupportFragmentManager().beginTransaction();
        transaction.replace(resId, fragment).commit();
        currentFragment = fragment;
    }

    protected void switchFragment(int resId, BaseFragment fragment) {
        if(fragment != currentFragment) {
            if(!fragment.isAdded()) {
                getSupportFragmentManager()
                        .beginTransaction()
                        .hide(currentFragment)
                        .replace(resId, fragment)
                        .addToBackStack(null)
                        .commit();
            } else {
                getSupportFragmentManager()
                        .beginTransaction()
                        .hide(currentFragment)
                        .show(fragment)
                        .commit();
            }
            currentFragment = fragment;
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        if(currentFragment != null) currentFragment.onResume();
    }

    @Override
    protected void onPause() {
        super.onPause();
        if(currentFragment != null) currentFragment.onPause();
    }
}
