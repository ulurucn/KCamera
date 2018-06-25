package vip.frendy.camdemo.fragment;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Bundle;
import android.view.View;
import android.widget.SeekBar;

import java.io.File;

import vip.frendy.camdemo.R;
import vip.frendy.camdemo.presenter.FilterHelper;
import vip.frendy.edit.common.Common;
import vip.frendy.edit.interfaces.IPictureEditListener;
import vip.frendy.fliter.FilterType;
import vip.frendy.fliter.base.GPUImageFilter;
import vip.frendy.fliter.widget.GPUImage;
import vip.frendy.fliter.widget.GPUImageView;
import vip.frendy.fliter.gpufilters.GPUImageOverlayBlendFilter;
import vip.frendy.fliter.utils.FilterAdjuster;

/**
 * Created by frendy on 2018/4/9.
 */

public class FragmentFilter extends BaseFragment implements View.OnClickListener, SeekBar.OnSeekBarChangeListener {
    public static String PIC_PATH = "pic_path";

    private String imgPath;
    private Bitmap bitmapSrc;

    private GPUImageView mPic;
    private FilterHelper mFilterHelper;
    private IPictureEditListener mListener;

    private GPUImageFilter mFilter;
    private FilterAdjuster mFilterAdjuster;

    public static FragmentFilter getInstance(Bundle args, IPictureEditListener listener) {
        FragmentFilter fragment = new FragmentFilter();
        fragment.setArguments(args);
        fragment.setPictureEditListener(listener);
        return fragment;
    }

    public void setPictureEditListener(IPictureEditListener listener) {
        mListener = listener;
    }

    @Override
    protected int getFragmentLayoutResId() {
        return R.layout.fragment_filter;
    }

    @Override
    protected void initWidgets() {
        mPic = mRootView.findViewById(R.id.gpu_image);
        mPic.setScaleType(GPUImage.ScaleType.CENTER_INSIDE);
    }

    @Override
    protected void initAction() {
        ((SeekBar) mRootView.findViewById(R.id.seekBar)).setOnSeekBarChangeListener(this);
        mRootView.findViewById(R.id.ok).setOnClickListener(this);
        mRootView.findViewById(R.id.cancel).setOnClickListener(this);
        mRootView.findViewById(R.id.filter).setOnClickListener(this);
        mRootView.findViewById(R.id.edge).setOnClickListener(this);
        mRootView.findViewById(R.id.watermark).setOnClickListener(this);
        mRootView.findViewById(R.id.sharpen).setOnClickListener(this);
        mRootView.findViewById(R.id.fairytale).setOnClickListener(this);

        imgPath = getArguments().getString(PIC_PATH);
        bitmapSrc = BitmapFactory.decodeFile(imgPath);
        mPic.setImage(bitmapSrc);

        //滤镜
        mFilterHelper = new FilterHelper(getContext());
    }

    @Override
    public void onClick(View view) {
        if(view.getId() == R.id.ok) {
            String path = vip.frendy.base.Common.getOutputMediaDir() + File.separator + System.currentTimeMillis() + ".jpg";
            mPic.saveToPictures(path, new GPUImageView.OnPictureSavedListener() {
                @Override
                public void onPictureSaved(Uri uri) {
                    if(mListener != null) mListener.onPictureEditApply(0, Common.getRealFilePath(getContext(), uri));
                }
            });
        } else if(view.getId() == R.id.cancel) {
            if(mListener != null) mListener.onPictureEditCancel(0);
        } else if(view.getId() == R.id.filter) {
            mPic.setFilter(mFilterHelper.createBlendFilter(GPUImageOverlayBlendFilter.class, R.mipmap.fliter));
            //隐藏seekbar
            mRootView.findViewById(R.id.seekBar).setVisibility(View.GONE);
        } else if(view.getId() == R.id.edge) {
            //暗角
            mFilter = mFilterHelper.createFilter(FilterType.VIGNETTE, false);
            mPic.setFilter(mFilter);
            mFilterAdjuster = new FilterAdjuster(mFilter);
            //显示seekbar
            mRootView.findViewById(R.id.seekBar).setVisibility(
                    mFilterAdjuster.canAdjust() ? View.VISIBLE : View.GONE);
        } else if(view.getId() == R.id.watermark) {
            //水印
            mFilter = mFilterHelper.createFilter(FilterType.WATER_MARK, false);
            mPic.setFilter(mFilter);
            mFilterAdjuster = new FilterAdjuster(mFilter);
            //隐藏seekbar
            mRootView.findViewById(R.id.seekBar).setVisibility(View.GONE);
        } else if(view.getId() == R.id.sharpen) {
            //锐利
            mFilter = mFilterHelper.createFilter(FilterType.SHARPEN, false);
            mPic.setFilter(mFilter);
            mFilterAdjuster = new FilterAdjuster(mFilter);
            //显示seekbar
            mRootView.findViewById(R.id.seekBar).setVisibility(
                    mFilterAdjuster.canAdjust() ? View.VISIBLE : View.GONE);
        } else if(view.getId() == R.id.fairytale) {
            //童话
            mFilter = mFilterHelper.createFilter(FilterType.FAIRYTALE, false);
            mPic.setFilter(mFilter);
            mFilterAdjuster = new FilterAdjuster(mFilter);
            //显示seekbar
            mRootView.findViewById(R.id.seekBar).setVisibility(
                    mFilterAdjuster.canAdjust() ? View.VISIBLE : View.GONE);
        }
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        if(mFilterHelper != null) mFilterHelper.onDestroy();
    }

    @Override
    public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
        if(mFilterAdjuster != null) {
            mFilterAdjuster.adjust(progress);
            mPic.requestRender();
        }
    }
    @Override
    public void onStartTrackingTouch(SeekBar seekBar) {}
    @Override
    public void onStopTrackingTouch(SeekBar seekBar) {}
}
