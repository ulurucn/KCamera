package vip.frendy.camdemo.fragment;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageView;

import vip.frendy.camdemo.R;
import vip.frendy.edit.common.Common;
import vip.frendy.edit.frame.PhotoFrame;
import vip.frendy.edit.interfaces.IPictureEditListener;

/**
 * Created by frendy on 2018/4/12.
 */

public class FragmentFrame extends BaseFragment implements View.OnClickListener {
    public static String PIC_PATH = "pic_path";

    private String imgPath;
    private Bitmap bitmapSrc;

    private IPictureEditListener mListener;

    private ImageView mPic;
    private PhotoFrame mImageFrame;
    private Bitmap bitmap;

    public static FragmentFrame getInstance(Bundle args, IPictureEditListener listener) {
        FragmentFrame fragment = new FragmentFrame();
        fragment.setArguments(args);
        fragment.setPictureEditListener(listener);
        return fragment;
    }

    public void setPictureEditListener(IPictureEditListener listener) {
        mListener = listener;
    }

    @Override
    protected int getFragmentLayoutResId() {
        return R.layout.fragment_frame;
    }

    @Override
    protected void initWidgets() {
        mPic = mRootView.findViewById(R.id.pic);
    }

    @Override
    protected void initAction() {
        mRootView.findViewById(R.id.ok).setOnClickListener(this);
        mRootView.findViewById(R.id.cancel).setOnClickListener(this);
        mRootView.findViewById(R.id.frame).setOnClickListener(this);

        imgPath = getArguments().getString(PIC_PATH);
        bitmapSrc = BitmapFactory.decodeFile(imgPath);
        bitmap = bitmapSrc;
        mImageFrame = new PhotoFrame(getContext(), bitmap);

        //显示图片
        mPic.setImageBitmap(bitmap);
    }

    @Override
    public void onClick(View view) {
        if(view.getId() == R.id.ok) {
            Common.writeImage(bitmap, imgPath, 100);
            if(mListener != null) mListener.onPictureEditApply(0, imgPath);
        } else if(view.getId() == R.id.cancel) {
            if(mListener != null) mListener.onPictureEditCancel(0);
        } else if(view.getId() == R.id.frame) {
            mImageFrame.setFrameType(PhotoFrame.FRAME_BIG);
            mImageFrame.setFrameResources(R.drawable.frame_big1);
            bitmap = mImageFrame.combineFrameRes();
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
    }
}
