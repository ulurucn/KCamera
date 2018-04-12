package vip.frendy.camdemo.fragment;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageView;

import vip.frendy.camdemo.R;
import vip.frendy.edit.common.Common;
import vip.frendy.base.BitmapExt;
import vip.frendy.edit.interfaces.IPictureEditListener;

/**
 * Created by frendy on 2018/4/8.
 */

public class FragmentRotate extends BaseFragment implements View.OnClickListener {
    public static String PIC_PATH = "pic_path";

    private String imgPath;
    private Bitmap bitmapSrc;

    private Bitmap bitmap = null;

    private IPictureEditListener mListener;

    private ImageView mPic;

    public static FragmentRotate getInstance(Bundle args, IPictureEditListener listener) {
        FragmentRotate fragment = new FragmentRotate();
        fragment.setArguments(args);
        fragment.setPictureEditListener(listener);
        return fragment;
    }

    public void setPictureEditListener(IPictureEditListener listener) {
        mListener = listener;
    }

    @Override
    protected int getFragmentLayoutResId() {
        return R.layout.fragment_rotate;
    }

    @Override
    protected void initWidgets() {
        mPic = mRootView.findViewById(R.id.pic);
    }

    @Override
    protected void initAction() {
        mRootView.findViewById(R.id.ok).setOnClickListener(this);
        mRootView.findViewById(R.id.cancel).setOnClickListener(this);
        mRootView.findViewById(R.id.rotate).setOnClickListener(this);
        mRootView.findViewById(R.id.updown).setOnClickListener(this);
        mRootView.findViewById(R.id.leftright).setOnClickListener(this);

        imgPath = getArguments().getString(PIC_PATH);
        bitmapSrc = BitmapFactory.decodeFile(imgPath);
        bitmap = bitmapSrc;

        //显示图片
        mPic.setImageBitmap(bitmapSrc);
    }

    @Override
    public void onClick(View view) {
        if(view.getId() == R.id.ok) {
            Common.writeImage(bitmap, imgPath, 100);
            if(mListener != null) mListener.onPictureEditApply(0, imgPath);
        } else if(view.getId() == R.id.cancel) {
            if(mListener != null) mListener.onPictureEditCancel(0);
        } else if(view.getId() == R.id.rotate) {
            bitmap = BitmapExt.rotateImage(bitmap, 90);
            mPic.setImageBitmap(bitmap);
        } else if(view.getId() == R.id.updown) {
            bitmap = BitmapExt.reverseImage(bitmap, 1, -1);
            mPic.setImageBitmap(bitmap);
        } else if(view.getId() == R.id.leftright) {
            bitmap = BitmapExt.reverseImage(bitmap, -1, 1);
            mPic.setImageBitmap(bitmap);
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
