package vip.frendy.camdemo.fragment;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.util.Log;
import android.view.View;

import vip.frendy.camdemo.R;
import vip.frendy.edit.common.Common;
import vip.frendy.edit.interfaces.IPictureEditListener;
import vip.frendy.edit.mosaic.MosaicUtil;
import vip.frendy.edit.mosaic.ScaleMosaicView;

/**
 * Created by frendy on 2018/4/12.
 */

public class FragmentMosaic extends BaseFragment implements View.OnClickListener {
    public static String PIC_PATH = "pic_path";

    private String imgPath;
    private Bitmap bitmapSrc;

    private IPictureEditListener mListener;

    private ScaleMosaicView mPic;
    private Bitmap bitmap;
    private boolean isEraser = false;

    public static FragmentMosaic getInstance(Bundle args, IPictureEditListener listener) {
        FragmentMosaic fragment = new FragmentMosaic();
        fragment.setArguments(args);
        fragment.setPictureEditListener(listener);
        return fragment;
    }

    public void setPictureEditListener(IPictureEditListener listener) {
        mListener = listener;
    }

    @Override
    protected int getFragmentLayoutResId() {
        return R.layout.fragment_mosaic;
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
        mRootView.findViewById(R.id.blur).setOnClickListener(this);
        mRootView.findViewById(R.id.forward).setOnClickListener(this);
        mRootView.findViewById(R.id.backward).setOnClickListener(this);

        imgPath = getArguments().getString(PIC_PATH);
        bitmapSrc = BitmapFactory.decodeFile(imgPath);
        bitmap = MosaicUtil.getMosaic(bitmapSrc);

        //显示图片
        mPic.setMosaicBackgroundResource(imgPath);
        mPic.setMosaicResource(bitmap);
        mPic.setMosaicBrushWidth(20);
        mPic.setOnPathMosaicUpdatedListener(new ScaleMosaicView.OnPathMosaicUpdatedListener() {
            @Override
            public void OnPathMosaicUpdated() {
                Log.e("mosaic", "** mosaic forward = " + mPic.canForward());
                Log.e("mosaic", "** mosaic backward = " + mPic.canBackward());
            }
            @Override
            public void OnPathEraserApplyed() {

            }
        });
    }

    @Override
    public void onClick(View view) {
        if(view.getId() == R.id.ok) {
            Common.writeImage(mPic.getMosaicBitmap(), imgPath, 100);
            if(mListener != null) mListener.onPictureEditApply(0, imgPath);
        } else if(view.getId() == R.id.cancel) {
            if(mListener != null) mListener.onPictureEditCancel(0);
        } else if(view.getId() == R.id.eraser) {
            isEraser = !isEraser;
            mPic.setMosaicType(isEraser ? MosaicUtil.MosaicType.ERASER : MosaicUtil.MosaicType.MOSAIC);
        } else if(view.getId() == R.id.blur) {
            bitmap = MosaicUtil.getBlur(bitmapSrc);
            mPic.setMosaicResource(bitmap);
        } else if(view.getId() == R.id.forward) {
            mPic.forward();
        } else if(view.getId() == R.id.backward) {
            mPic.backward();
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
