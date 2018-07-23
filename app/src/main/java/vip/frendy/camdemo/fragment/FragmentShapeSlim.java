package vip.frendy.camdemo.fragment;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.view.View;
import android.widget.SeekBar;

import vip.frendy.base.BitmapExt;
import vip.frendy.camdemo.R;
import vip.frendy.edit.common.Common;
import vip.frendy.edit.interfaces.IPictureEditListener;
import vip.frendy.edit.warp2.CanvasView;
import vip.frendy.edit.warp2.SlimHelper;

/**
 * Created by frendy on 2018/4/12.
 */

public class FragmentShapeSlim extends BaseFragment implements View.OnClickListener, SeekBar.OnSeekBarChangeListener {
    public static String PIC_PATH = "pic_path";

    private String imgPath;
    private Bitmap bitmapSrc;

    private IPictureEditListener mListener;

    private CanvasView mPic;
    private Bitmap bitmap, bitmapOval, bitmapOpUp, bitmapOpDown, bitmapOpLeft, bitmapOpRight;
    private SlimHelper mSlimHelper;
    private SeekBar mSeekbar;

    public static FragmentShapeSlim getInstance(Bundle args, IPictureEditListener listener) {
        FragmentShapeSlim fragment = new FragmentShapeSlim();
        fragment.setArguments(args);
        fragment.setPictureEditListener(listener);
        return fragment;
    }

    public void setPictureEditListener(IPictureEditListener listener) {
        mListener = listener;
    }

    @Override
    protected int getFragmentLayoutResId() {
        return R.layout.fragment_shape;
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
        mSeekbar.setOnSeekBarChangeListener(this);
        mSeekbar.setMax(100);

        imgPath = getArguments().getString(PIC_PATH);
        bitmapSrc = BitmapFactory.decodeFile(imgPath);
        bitmap = bitmapSrc;

        bitmapOpUp = BitmapFactory.decodeResource(getResources(), R.mipmap.ic_edit_shape_op_up);
        bitmapOpDown = BitmapExt.rotateImage(bitmapOpUp, 180);
        bitmapOpLeft = BitmapExt.rotateImage(bitmapOpUp, -90);
        bitmapOpRight = BitmapExt.rotateImage(bitmapOpUp, 90);
        bitmapOval = BitmapFactory.decodeResource(getResources(), R.mipmap.ic_edit_shape_slim);

        //显示图片
        mPic.post(new Runnable() {
            @Override
            public void run() {
                mPic.scaleToImage(bitmap);

                mPic.postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        if(mSlimHelper == null) {
                            mSlimHelper = new SlimHelper();
                        }
                        mSlimHelper.attachCanvasView(mPic);
                        mSlimHelper.setOpBitmap(bitmapOpUp, bitmapOpDown, bitmapOpLeft, bitmapOpRight);
                        mSlimHelper.setOvalBitmap(bitmapOval);
                        mSlimHelper.initMorpher();
                        mPic.isBaseDrawingEnabled(false);

                        //设置初始化值
                        mSeekbar.setProgress(50);
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
            mSlimHelper.setOriginal(!mSlimHelper.getOriginal());
        }
    }

    @Override
    public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
        mSlimHelper.setStrength(progress - 50);
    }

    @Override
    public void onStartTrackingTouch(SeekBar seekBar) {}

    @Override
    public void onStopTrackingTouch(SeekBar seekBar) {}

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
        if(bitmapOval != null) {
            bitmapOval.recycle();
            bitmapOval = null;
        }
        if(bitmapOpUp != null) {
            bitmapOpUp.recycle();
            bitmapOpUp = null;
        }
        if(bitmapOpDown != null) {
            bitmapOpDown.recycle();
            bitmapOpDown = null;
        }
        if(bitmapOpLeft != null) {
            bitmapOpLeft.recycle();
            bitmapOpLeft = null;
        }
        if(bitmapOpRight != null) {
            bitmapOpRight.recycle();
            bitmapOpRight = null;
        }
    }
}
