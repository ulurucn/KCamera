package vip.frendy.camdemo.fragment;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;

import vip.frendy.camdemo.R;
import vip.frendy.edit.common.Common;
import vip.frendy.edit.interfaces.IPictureEditListener;
import vip.frendy.edit.warp2.CanvasView;
import vip.frendy.edit.warp2.WarpHelper;

/**
 * Created by frendy on 2018/4/12.
 */

public class FragmentWarp2 extends BaseFragment implements View.OnClickListener, WarpHelper.OnWarpCanvasDrawListener {
    public static String PIC_PATH = "pic_path";

    private String imgPath;
    private Bitmap bitmapSrc;

    private IPictureEditListener mListener;

    private CanvasView mPic;
    private Bitmap bitmap;
    private WarpHelper mWarpHelper;

    public static FragmentWarp2 getInstance(Bundle args, IPictureEditListener listener) {
        FragmentWarp2 fragment = new FragmentWarp2();
        fragment.setArguments(args);
        fragment.setPictureEditListener(listener);
        return fragment;
    }

    public void setPictureEditListener(IPictureEditListener listener) {
        mListener = listener;
    }

    @Override
    protected int getFragmentLayoutResId() {
        return R.layout.fragment_warp_2;
    }

    @Override
    protected void initWidgets() {
        mPic = mRootView.findViewById(R.id.pic);
    }

    @Override
    protected void initAction() {
        mRootView.findViewById(R.id.ok).setOnClickListener(this);
        mRootView.findViewById(R.id.cancel).setOnClickListener(this);
        mRootView.findViewById(R.id.forward).setOnClickListener(this);
        mRootView.findViewById(R.id.backward).setOnClickListener(this);

        imgPath = getArguments().getString(PIC_PATH);
        bitmapSrc = BitmapFactory.decodeFile(imgPath);
        bitmap = bitmapSrc;

        //显示图片
        mPic.post(new Runnable() {
            @Override
            public void run() {
                mPic.scaleToImage(bitmap);

                mPic.postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        if(mWarpHelper == null) {
                            mWarpHelper = new WarpHelper();
                        }
                        mWarpHelper.setOnWarpCanvasDrawListener(FragmentWarp2.this);
                        mWarpHelper.attachCanvasView(mPic);
                        mWarpHelper.initMorpher();
                        mPic.isBaseDrawingEnabled(false);
                    }
                }, 100);
            }
        });
    }

    @Override
    public void onClick(View view) {
        if(view.getId() == R.id.ok) {
            Common.writeImage(mPic.generateBitmap(), imgPath, 100);
            if(mListener != null) mListener.onPictureEditApply(0, imgPath);
        } else if(view.getId() == R.id.cancel) {
            if(mListener != null) mListener.onPictureEditCancel(0);
        } else if(view.getId() == R.id.forward && mPic != null && mWarpHelper != null && mWarpHelper.isAttached()) {
            mWarpHelper.redo();
        } else if(view.getId() == R.id.backward && mPic != null && mWarpHelper != null && mWarpHelper.isAttached()) {
            mWarpHelper.undo();
        }
    }

    public boolean isUndoActive() {
        if(mWarpHelper != null) {
            return mWarpHelper.isUndoActive();
        }
        return false;
    }

    public boolean isRedoActive() {
        if(mWarpHelper != null) {
            return mWarpHelper.isRedoActive();
        }
        return false;
    }

    @Override
    public void onWarpCanvasDrawed() {
        ((Button) mRootView.findViewById(R.id.forward)).setTextColor(getResources().getColor(
                mWarpHelper.isRedoActive() ? android.R.color.holo_red_dark : android.R.color.black));
        ((Button) mRootView.findViewById(R.id.backward)).setTextColor(getResources().getColor(
                mWarpHelper.isUndoActive() ? android.R.color.holo_red_dark : android.R.color.black));
    }

    @Override
    public void onDestroy() {
        recycleBitmap();
        super.onDestroy();
    }

    private void recycleBitmap() {
        if(bitmapSrc != null) {
            bitmapSrc.recycle();
            bitmapSrc = null;
        }
        if(bitmap != null) {
            bitmap.recycle();
            bitmap = null;
        }
    }
}
