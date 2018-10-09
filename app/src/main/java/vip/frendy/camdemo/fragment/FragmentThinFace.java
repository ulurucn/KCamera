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
import vip.frendy.edit.warp2.ThinFaceView;
import vip.frendy.edit.warp2.WarpHelper;

/**
 * Created by frendy on 2018/4/12.
 */

public class FragmentThinFace extends BaseFragment implements View.OnClickListener, WarpHelper.OnWarpCanvasDrawListener {
    public static String PIC_PATH = "pic_path";

    private String imgPath;
    private Bitmap bitmapSrc;

    private IPictureEditListener mListener;

    private ThinFaceView mPic;
    private Bitmap bitmap;
    //private WarpHelper mWarpHelper;

    public static FragmentThinFace getInstance(Bundle args, IPictureEditListener listener) {
        FragmentThinFace fragment = new FragmentThinFace();
        fragment.setArguments(args);
        fragment.setPictureEditListener(listener);
        return fragment;
    }

    public void setPictureEditListener(IPictureEditListener listener) {
        mListener = listener;
    }

    @Override
    protected int getFragmentLayoutResId() {
        return R.layout.fragment_warp_thin_face;
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
        mRootView.findViewById(R.id.compare).setOnClickListener(this);

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
//                        if(mWarpHelper == null) {
//                            mWarpHelper = new WarpHelper();
//                        }
                        //mWarpHelper.setOnWarpCanvasDrawListener(FragmentThinFace.this);
                        //mWarpHelper.attachCanvasView(mPic);
                        mPic.initMorpher(40, 100, 3);
                        //mPic.isBaseDrawingEnabled(false);
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
        } else if(view.getId() == R.id.forward && mPic != null) {
            mPic.redo();
        } else if(view.getId() == R.id.backward && mPic != null) {
            mPic.undo();
        } else if(view.getId() == R.id.compare) {
            mPic.setOriginal(!mPic.getOriginal());
        }
    }

    public boolean isUndoActive() {
        if(mPic != null) {
            return mPic.isUndoActive();
        }
        return false;
    }

    public boolean isRedoActive() {
        if(mPic != null) {
            return mPic.isRedoActive();
        }
        return false;
    }

    @Override
    public void onWarpCanvasDrawed() {
        ((Button) mRootView.findViewById(R.id.forward)).setTextColor(getResources().getColor(
                isRedoActive() ? android.R.color.holo_red_dark : android.R.color.black));
        ((Button) mRootView.findViewById(R.id.backward)).setTextColor(getResources().getColor(
                isUndoActive() ? android.R.color.holo_red_dark : android.R.color.black));
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
