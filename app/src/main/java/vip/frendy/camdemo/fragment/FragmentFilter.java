package vip.frendy.camdemo.fragment;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Bundle;
import android.view.View;

import jp.co.cyberagent.android.gpuimage.GPUImageOverlayBlendFilter;
import jp.co.cyberagent.android.gpuimage.GPUImageView;
import vip.frendy.camdemo.R;
import vip.frendy.camdemo.presenter.FilterHelper;
import vip.frendy.edit.common.Common;
import vip.frendy.edit.interfaces.IPictureEditListener;

/**
 * Created by frendy on 2018/4/9.
 */

public class FragmentFilter extends BaseFragment implements View.OnClickListener {
    public static String PIC_PATH = "pic_path";

    private String imgPath;
    private Bitmap bitmapSrc;
    private Bitmap bit = null;

    private GPUImageView mPic;
    private FilterHelper mFilterHelper;
    private IPictureEditListener mListener;

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
    }

    @Override
    protected void initAction() {
        mRootView.findViewById(R.id.ok).setOnClickListener(this);
        mRootView.findViewById(R.id.cancel).setOnClickListener(this);
        mRootView.findViewById(R.id.filter).setOnClickListener(this);

        imgPath = getArguments().getString(PIC_PATH);
        bitmapSrc = BitmapFactory.decodeFile(imgPath);
        mPic.setImage(bitmapSrc);

        //滤镜
        mFilterHelper = new FilterHelper(getContext());
    }

    @Override
    public void onClick(View view) {
        if(view.getId() == R.id.ok) {
            String fileName = System.currentTimeMillis() + ".jpg";
            mPic.saveToPictures("GPUImage", fileName, new GPUImageView.OnPictureSavedListener() {
                @Override
                public void onPictureSaved(Uri uri) {
                    if(mListener != null) mListener.onPictureEditApply(0, Common.getRealFilePath(getContext(), uri));
                }
            });
        } else if(view.getId() == R.id.cancel) {
            if(mListener != null) mListener.onPictureEditCancel(0);
        } else if(view.getId() == R.id.filter) {
            mPic.setFilter(mFilterHelper.createBlendFilter(GPUImageOverlayBlendFilter.class, R.mipmap.fliter));
        }
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        if(mFilterHelper != null) mFilterHelper.onDestroy();
    }
}
