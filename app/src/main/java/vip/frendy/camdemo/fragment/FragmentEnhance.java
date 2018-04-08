package vip.frendy.camdemo.fragment;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageView;
import android.widget.SeekBar;

import vip.frendy.camdemo.R;
import vip.frendy.edit.common.Common;
import vip.frendy.edit.enhance.PhotoEnhance;
import vip.frendy.edit.interfaces.IPictureEditListener;

/**
 * Created by frendy on 2018/4/8.
 */

public class FragmentEnhance extends BaseFragment implements View.OnClickListener, SeekBar.OnSeekBarChangeListener {
    public static String PIC_PATH = "pic_path";

    private String imgPath;
    private Bitmap bitmapSrc;

    private int pregress = 0;
    private Bitmap bit = null;

    private PhotoEnhance mPhotoEnhance;
    private IPictureEditListener mListener;

    private ImageView mPic;
    private SeekBar saturationSeekBar, brightnessSeekBar, contrastSeekBar;

    public static FragmentEnhance getInstance(Bundle args, IPictureEditListener listener) {
        FragmentEnhance fragment = new FragmentEnhance();
        fragment.setArguments(args);
        fragment.setPictureEditListener(listener);
        return fragment;
    }

    public void setPictureEditListener(IPictureEditListener listener) {
        mListener = listener;
    }

    @Override
    protected int getFragmentLayoutResId() {
        return R.layout.fragment_enhance;
    }

    @Override
    protected void initWidgets() {
        saturationSeekBar = mRootView.findViewById(R.id.saturation);
        saturationSeekBar.setMax(255);
        saturationSeekBar.setProgress(128);
        saturationSeekBar.setOnSeekBarChangeListener(this);

        brightnessSeekBar = mRootView.findViewById(R.id.brightness);
        brightnessSeekBar.setMax(255);
        brightnessSeekBar.setProgress(128);
        brightnessSeekBar.setOnSeekBarChangeListener(this);

        contrastSeekBar = mRootView.findViewById(R.id.contrast);
        contrastSeekBar.setMax(255);
        contrastSeekBar.setProgress(128);
        contrastSeekBar.setOnSeekBarChangeListener(this);

        mPic = mRootView.findViewById(R.id.pic);
    }

    @Override
    protected void initAction() {
        mRootView.findViewById(R.id.ok).setOnClickListener(this);
        mRootView.findViewById(R.id.cancel).setOnClickListener(this);

        imgPath = getArguments().getString(PIC_PATH);
        bitmapSrc = BitmapFactory.decodeFile(imgPath);

        //显示图片
        mPic.setImageBitmap(bitmapSrc);
        //图像增强
        mPhotoEnhance = new PhotoEnhance(bitmapSrc);
    }

    @Override
    public void onClick(View view) {
        if(view.getId() == R.id.ok) {
            Common.writeImage(bit, imgPath, 100);
            if(mListener != null) mListener.onPictureEditApply(0, imgPath);
        } else if(view.getId() == R.id.cancel) {
            if(mListener != null) mListener.onPictureEditCancel(0);
        }
    }

    @Override
    public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
        pregress = progress;
    }

    @Override
    public void onStartTrackingTouch(SeekBar seekBar) {}

    @Override
    public void onStopTrackingTouch(SeekBar seekBar) {
        int type = 0;

        switch(seekBar.getId()) {
            case R.id.saturation :
                mPhotoEnhance.setSaturation(pregress);
                type = mPhotoEnhance.Enhance_Saturation;
                break;
            case R.id.brightness :
                mPhotoEnhance.setBrightness(pregress);
                type = mPhotoEnhance.Enhance_Brightness;
                break;
            case R.id.contrast :
                mPhotoEnhance.setContrast(pregress);
                type = mPhotoEnhance.Enhance_Contrast;
                break;
            default :
                break;
        }
        bit = mPhotoEnhance.handleImage(type);
        mPic.setImageBitmap(bit);
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
        if(bit != null) {
            bit.recycle();
            bit = null;
        }
    }
}
