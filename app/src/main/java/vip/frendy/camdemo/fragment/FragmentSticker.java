package vip.frendy.camdemo.fragment;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;

import vip.frendy.base.BitmapExt;
import vip.frendy.camdemo.R;
import vip.frendy.camdemo.extension.HandlerExt;
import vip.frendy.edit.common.Common;
import vip.frendy.edit.interfaces.IPictureEditListener;
import vip.frendy.edit.operate.ImageObject;
import vip.frendy.edit.operate.OperateUtils;
import vip.frendy.edit.operate.OperateView;
import vip.frendy.edit.operate.TextObject;

/**
 * Created by frendy on 2018/4/12.
 */

public class FragmentSticker extends BaseFragment implements View.OnClickListener {
    public static String PIC_PATH = "pic_path";

    private String imgPath;
    private Bitmap bitmapSrc;

    private IPictureEditListener mListener;

    private LinearLayout mContent;
    private OperateView mOperateView;
    private OperateUtils mOperateUtils;

    public static FragmentSticker getInstance(Bundle args, IPictureEditListener listener) {
        FragmentSticker fragment = new FragmentSticker();
        fragment.setArguments(args);
        fragment.setPictureEditListener(listener);
        return fragment;
    }

    public void setPictureEditListener(IPictureEditListener listener) {
        mListener = listener;
    }

    @Override
    protected int getFragmentLayoutResId() {
        return R.layout.fragment_sticker;
    }

    @Override
    protected void initWidgets() {
        mContent = mRootView.findViewById(R.id.content);
    }

    @Override
    protected void initAction() {
        mRootView.findViewById(R.id.ok).setOnClickListener(this);
        mRootView.findViewById(R.id.cancel).setOnClickListener(this);
        mRootView.findViewById(R.id.sitcker).setOnClickListener(this);
        mRootView.findViewById(R.id.text).setOnClickListener(this);

        mOperateUtils = new OperateUtils(getActivity());
        imgPath = getArguments().getString(PIC_PATH);
        bitmapSrc = BitmapFactory.decodeFile(imgPath);

        //显示图片
        showImage(bitmapSrc);
    }

    private void showImage(final Bitmap bitmap) {
        if(mContent.getWidth() != 0) {
            mOperateView = new OperateView(getContext(), bitmap);
            LinearLayout.LayoutParams layoutParams = new LinearLayout.LayoutParams(
                    bitmap.getWidth(), bitmap.getHeight());
            layoutParams.gravity = Gravity.CENTER;
            mOperateView.setLayoutParams(layoutParams);
            mContent.addView(mOperateView);
            //设置可以添加多个图片
            mOperateView.setMultiAdd(true);
        } else {
            HandlerExt.postDelayToUI(new Runnable() {
                @Override
                public void run() {
                    showImage(bitmap);
                }
            }, 500L);
        }
    }

    @Override
    public void onClick(View view) {
        if(view.getId() == R.id.ok) {
            Common.writeImage(BitmapExt.getBitmapByView(mOperateView), imgPath, 100);
            if(mListener != null) mListener.onPictureEditApply(0, imgPath);
        } else if(view.getId() == R.id.cancel) {
            if(mListener != null) mListener.onPictureEditCancel(0);
        } else if(view.getId() == R.id.sitcker) {
            addSticker(BitmapFactory.decodeResource(getContext().getResources(), R.drawable.wanhuaile));
        } else if(view.getId() == R.id.text) {
            addText("写死的测试文本");
        }
    }

    private void addSticker(Bitmap bitmap) {
        ImageObject sticker = mOperateUtils.getImageObject(bitmap, mOperateView, 5, 150, 100);
        mOperateView.addItem(sticker);
    }

    private void addText(String text) {
        TextObject sticker = mOperateUtils.getTextObject(text, mOperateView, 5, 150, 100);
        sticker.setText(text);
        sticker.commit();
        mOperateView.addItem(sticker);
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
