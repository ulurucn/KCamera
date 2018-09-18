package vip.frendy.camdemo.fragment;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.SeekBar;

import vip.frendy.base.BitmapExt;
import vip.frendy.camdemo.R;
import vip.frendy.edit.common.Common;
import vip.frendy.edit.interfaces.IPictureEditListener;
import vip.frendy.edit.warp2.BigEyeHelper;
import vip.frendy.edit.warp2.BreastHelper;
import vip.frendy.edit.warp2.CanvasView;

/**
 * Created by frendy on 2018/4/12.
 */

public class FragmentBigEye extends BaseFragment implements View.OnClickListener, SeekBar.OnSeekBarChangeListener,BigEyeHelper.OnCanvasChangeListener{
    public static String PIC_PATH = "pic_path";

    private String imgPath;
    private Bitmap bitmapSrc;

    private IPictureEditListener mListener;

    private CanvasView mPic;
    private Bitmap bitmap;
    private BigEyeHelper mBreastHelper;
    private SeekBar mSeekbar;
    private boolean isShowOp = false;

    public static FragmentBigEye getInstance(Bundle args, IPictureEditListener listener) {
        FragmentBigEye fragment = new FragmentBigEye();
        fragment.setArguments(args);
        fragment.setPictureEditListener(listener);
        return fragment;
    }

    public void setPictureEditListener(IPictureEditListener listener) {
        mListener = listener;
    }

    @Override
    protected int getFragmentLayoutResId() {
        return R.layout.fragment_eye;
    }

    @Override
    protected void initWidgets() {
        mPic = mRootView.findViewById(R.id.pic);
        mSeekbar = mRootView.findViewById(R.id.seekBar);
    }

    @Override
    protected void initAction() {
        mRootView.findViewById(R.id.ok).setOnClickListener(this);
        mRootView.findViewById(R.id.cancel).setOnClickListener(this);
        mRootView.findViewById(R.id.compare).setOnClickListener(this);


        mRootView.findViewById(R.id.redo).setOnClickListener(this);
        mRootView.findViewById(R.id.undo).setOnClickListener(this);
        mSeekbar.setOnSeekBarChangeListener(this);
        mSeekbar.setMax(100);

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
                        if(mBreastHelper == null) {
                            mBreastHelper = new BigEyeHelper();
                        }
                        mBreastHelper.attachCanvasView(mPic);
                        //mBreastHelper.setOpBitmap(null, null);
                        mBreastHelper.initMorpher(80, 40, -100);
                        mPic.isBaseDrawingEnabled(false);

                        //设置初始化值
                        mSeekbar.setProgress(50);

                        mBreastHelper.setCanvasChangeLister(FragmentBigEye.this);
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
        } else if(view.getId() == R.id.compare) {
            mBreastHelper.setOriginal(!mBreastHelper.getOriginal());
        } else if (view.getId() == R.id.redo) {
            mBreastHelper.redo();
        } else if (view.getId() == R.id.undo) {
            mBreastHelper.undo();
        }
    }

    @Override
    public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
        //Log.i("eye", "current progress is " + progress);
        if (mBreastHelper != null) {
            //mBreastHelper.setStrength(progress - 50);
        }
    }

    @Override
    public void onStartTrackingTouch(SeekBar seekBar) {
        //isShowOp = mBreastHelper.getVisible();
        //mBreastHelper.setVisible(false);
    }

    @Override
    public void onStopTrackingTouch(SeekBar seekBar) {
        //mBreastHelper.setVisible(isShowOp);
        //isShowOp = false;
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

    @Override
    public void onChange() {
        Log.i("eye", "big eye change");
    }
}
