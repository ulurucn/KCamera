package vip.frendy.camdemo.fragment;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.view.View;

import vip.frendy.camdemo.R;
import vip.frendy.edit.common.Common;
import vip.frendy.edit.crop.CropImageType;
import vip.frendy.edit.crop.CropImageView;
import vip.frendy.edit.interfaces.IPictureEditListener;

/**
 * Created by frendy on 2018/4/12.
 */

public class FragmentCrop extends BaseFragment implements View.OnClickListener {
    public static String PIC_PATH = "pic_path";

    private String imgPath;
    private Bitmap bitmapSrc;

    private IPictureEditListener mListener;

    private CropImageView mPic;

    public static FragmentCrop getInstance(Bundle args, IPictureEditListener listener) {
        FragmentCrop fragment = new FragmentCrop();
        fragment.setArguments(args);
        fragment.setPictureEditListener(listener);
        return fragment;
    }

    public void setPictureEditListener(IPictureEditListener listener) {
        mListener = listener;
    }

    @Override
    protected int getFragmentLayoutResId() {
        return R.layout.fragment_crop;
    }

    @Override
    protected void initWidgets() {
        mPic = mRootView.findViewById(R.id.pic);
    }

    @Override
    protected void initAction() {
        mRootView.findViewById(R.id.ok).setOnClickListener(this);
        mRootView.findViewById(R.id.cancel).setOnClickListener(this);
        mRootView.findViewById(R.id.ratio_1_1).setOnClickListener(this);
        mRootView.findViewById(R.id.ratio_16_9).setOnClickListener(this);
        mRootView.findViewById(R.id.freedom).setOnClickListener(this);

        imgPath = getArguments().getString(PIC_PATH);
        bitmapSrc = BitmapFactory.decodeFile(imgPath);

        //显示图片
        mPic.setImageBitmap(bitmapSrc);
        mPic.setCropOverlayCornerBitmap(
                BitmapFactory.decodeResource(this.getResources(), R.mipmap.ic_crop_corner));
        mPic.setGuidelines(CropImageType.CROPIMAGE_GRID_ON_TOUCH);
        mPic.setFixedAspectRatio(false);
    }

    @Override
    public void onClick(View view) {
        if(view.getId() == R.id.ok) {
            Common.writeImage(mPic.getCroppedImage(), imgPath, 100);
            if(mListener != null) mListener.onPictureEditApply(0, imgPath);
        } else if(view.getId() == R.id.cancel) {
            if(mListener != null) mListener.onPictureEditCancel(0);
        } else if(view.getId() == R.id.ratio_1_1) {
            mPic.setFixedAspectRatio(true);
            mPic.setAspectRatio(10, 10);
        } else if(view.getId() == R.id.ratio_16_9) {
            mPic.setFixedAspectRatio(true);
            mPic.setAspectRatio(160, 90);
        } else if(view.getId() == R.id.freedom) {
            mPic.setFixedAspectRatio(false);
        }
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
    }
}
