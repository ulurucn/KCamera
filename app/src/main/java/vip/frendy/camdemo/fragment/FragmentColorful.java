package vip.frendy.camdemo.fragment;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.util.Log;
import android.view.View;

import vip.frendy.camdemo.R;
import vip.frendy.edit.colorful.ColorfulPath;
import vip.frendy.edit.colorful.ColorfulUtils;
import vip.frendy.edit.colorful.ColorfulView;
import vip.frendy.edit.colorful.ScaleColorfulView;
import vip.frendy.edit.common.Common;
import vip.frendy.edit.interfaces.IPictureEditListener;
import vip.frendy.edit.mosaic.MosaicUtil;
import vip.frendy.edit.mosaic.ScaleMosaicView;

/**
 * Created by frendy on 2018/4/12.
 */

public class FragmentColorful extends BaseFragment implements View.OnClickListener {
    public static String PIC_PATH = "pic_path";

    private String imgPath;
    private Bitmap bitmapSrc;

    private IPictureEditListener mListener;

    private ColorfulView mPic;
    private Bitmap bitmap;
    private boolean isEraser = false;

    public static FragmentColorful getInstance(Bundle args, IPictureEditListener listener) {
        FragmentColorful fragment = new FragmentColorful();
        fragment.setArguments(args);
        fragment.setPictureEditListener(listener);
        return fragment;
    }

    public void setPictureEditListener(IPictureEditListener listener) {
        mListener = listener;
    }

    @Override
    protected int getFragmentLayoutResId() {
        return R.layout.fragment_colorful;
    }

    @Override
    protected void initWidgets() {
        mPic = mRootView.findViewById(R.id.pic);
    }

    @Override
    protected void initAction() {
        mRootView.findViewById(R.id.ok).setOnClickListener(this);
        mRootView.findViewById(R.id.cancel).setOnClickListener(this);
        mRootView.findViewById(R.id.eraser).setOnClickListener(this);
        mRootView.findViewById(R.id.forward).setOnClickListener(this);
        mRootView.findViewById(R.id.backward).setOnClickListener(this);

        mRootView.findViewById(R.id.color1).setOnClickListener(this);
        mRootView.findViewById(R.id.color2).setOnClickListener(this);
        mRootView.findViewById(R.id.color3).setOnClickListener(this);

        imgPath = getArguments().getString(PIC_PATH);
        bitmapSrc = BitmapFactory.decodeFile(imgPath);

        //显示图片
        mPic.setBackgroundResource(imgPath);
        //mPic.setMosaicResource(bitmapSrc);
        //mPic.setMosaicBrushWidth(10);
        mPic.setTouchCircleR(20 + 40);
    }

    @Override
    public void onClick(View view) {
        if(view.getId() == R.id.ok) {
            //Common.writeImage(mPic.getMosaicBitmap(), imgPath, 100);
            if(mListener != null) mListener.onPictureEditApply(0, imgPath);
        } else if(view.getId() == R.id.cancel) {
            if(mListener != null) mListener.onPictureEditCancel(0);
        } else if(view.getId() == R.id.eraser) {
            isEraser = !isEraser;
            mPic.setPaintType(isEraser ? ColorfulUtils.Type.ERASER : ColorfulUtils.Type.COLOR);
            //显示触点圈圈
            mPic.showTouchCircle(true);
            mPic.postDelayed(new Runnable() {
                @Override
                public void run() {
                    if(mPic != null) mPic.showTouchCircle(false);
                }
            }, 500);
        } else if(view.getId() == R.id.blur) {
            bitmap = MosaicUtil.getBlur(bitmapSrc);
            //mPic.setMosaicResource(bitmap);
        } else if(view.getId() == R.id.forward) {
            //mPic.forward();
        } else if(view.getId() == R.id.backward) {
            //mPic.backward();
        } else if (view.getId() == R.id.color1) {
            //mPic.setTouchCirclePaintColor();
            mPic.changeColor(0x60FF4081);
        } else if (view.getId() == R.id.color2) {
            mPic.changeColor(0x60F7A334);
        } else if (view.getId() == R.id.color3) {
            mPic.changeColor(0x606e29a3);
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
