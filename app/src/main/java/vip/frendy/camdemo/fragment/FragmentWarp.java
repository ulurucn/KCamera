package vip.frendy.camdemo.fragment;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.view.View;

import vip.frendy.camdemo.R;
import vip.frendy.edit.common.Common;
import vip.frendy.edit.interfaces.IPictureEditListener;
import vip.frendy.edit.warp.Picwarp;
import vip.frendy.edit.warp.WarpView;

/**
 * Created by frendy on 2018/4/12.
 */

public class FragmentWarp extends BaseFragment implements View.OnClickListener {
    public static String PIC_PATH = "pic_path";

    private String imgPath;
    private Bitmap bitmapSrc;

    private IPictureEditListener mListener;

    private WarpView mPic;
    private Bitmap bitmap;
    private Picwarp warp = new Picwarp();

    public static FragmentWarp getInstance(Bundle args, IPictureEditListener listener) {
        FragmentWarp fragment = new FragmentWarp();
        fragment.setArguments(args);
        fragment.setPictureEditListener(listener);
        return fragment;
    }

    public void setPictureEditListener(IPictureEditListener listener) {
        mListener = listener;
    }

    @Override
    protected int getFragmentLayoutResId() {
        return R.layout.fragment_warp;
    }

    @Override
    protected void initWidgets() {
        mPic = mRootView.findViewById(R.id.pic);
    }

    @Override
    protected void initAction() {
        mRootView.findViewById(R.id.ok).setOnClickListener(this);
        mRootView.findViewById(R.id.cancel).setOnClickListener(this);
        mRootView.findViewById(R.id.reset).setOnClickListener(this);

        imgPath = getArguments().getString(PIC_PATH);
        bitmapSrc = BitmapFactory.decodeFile(imgPath);
        bitmap = bitmapSrc;
        warp.initArray();

        //显示图片
        mPic.setWarpBitmap(bitmap);
    }

    @Override
    public void onClick(View view) {
        if(view.getId() == R.id.ok) {
            Common.writeImage(mPic.getWrapBitmap(), imgPath, 100);
            if(mListener != null) mListener.onPictureEditApply(0, imgPath);
        } else if(view.getId() == R.id.cancel) {
            if(mListener != null) mListener.onPictureEditCancel(0);
        } else if(view.getId() == R.id.reset) {
            mPic.setWarpBitmap(bitmapSrc);
            mPic.invalidate();
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
        if(bitmap != null) {
            bitmap.recycle();
            bitmap = null;
        }
    }
}
