package vip.frendy.camdemo.fragment;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.util.Log;
import android.view.Gravity;
import android.view.MotionEvent;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.SeekBar;

import vip.frendy.base.BitmapExt;
import vip.frendy.camdemo.R;
import vip.frendy.camdemo.extension.HandlerExt;
import vip.frendy.edit.common.Common;
import vip.frendy.edit.interfaces.IPictureEditListener;
import vip.frendy.edit.interfaces.ISettingListener;
import vip.frendy.edit.operate.ImageObject;
import vip.frendy.edit.operate.OperateUtils;
import vip.frendy.edit.operate.OperateView;
import vip.frendy.edit.operate.TextObject;

/**
 * Created by frendy on 2018/4/12.
 */

public class FragmentSticker extends BaseFragment implements View.OnClickListener,ISettingListener,SeekBar.OnSeekBarChangeListener{
    public static String PIC_PATH = "pic_path";

    private String imgPath;
    private Bitmap bitmapSrc;
    private SeekBar mSeekBar;

    private IPictureEditListener mListener;

    private LinearLayout mContent;
    private OperateView mOperateView;
    private OperateUtils mOperateUtils;
    private LinearLayout mSettingBar;
    private LinearLayout mStickerBar;
    private int currentProgress = 0;

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
        mSettingBar = mRootView.findViewById(R.id.settingBar);
        mStickerBar = mRootView.findViewById(R.id.stickerBar);
        mSeekBar = mRootView.findViewById(R.id.seekBar);
    }

    @Override
    protected void initAction() {
        mRootView.findViewById(R.id.ok).setOnClickListener(this);
        mRootView.findViewById(R.id.cancel).setOnClickListener(this);
        mRootView.findViewById(R.id.show).setOnClickListener(this);
        mRootView.findViewById(R.id.sitcker).setOnClickListener(this);
        mRootView.findViewById(R.id.sitcker2).setOnClickListener(this);
        mRootView.findViewById(R.id.sitcker3).setOnClickListener(this);
        mRootView.findViewById(R.id.text).setOnClickListener(this);
        mRootView.findViewById(R.id.seekBar_ok).setOnClickListener(this);
        mRootView.findViewById(R.id.seekBar_cancel).setOnClickListener(this);
        mSeekBar.setOnSeekBarChangeListener(this);

        mOperateUtils = new OperateUtils(getActivity());
        imgPath = getArguments().getString(PIC_PATH);
        bitmapSrc = BitmapFactory.decodeFile(imgPath);

        //显示图片
        showImage(bitmapSrc);
    }

    private void showImage(final Bitmap bitmap) {
        if(mContent.getWidth() != 0) {
            Bitmap resizeBmp = mOperateUtils.compressionFiller(bitmap, mContent);
            mOperateView = new OperateView(getContext(), resizeBmp,this);
            LinearLayout.LayoutParams layoutParams = new LinearLayout.LayoutParams(resizeBmp.getWidth(), resizeBmp.getHeight());
            layoutParams.gravity = Gravity.CENTER;
            mOperateView.setLayoutParams(layoutParams);
            mContent.addView(mOperateView);
            //设置可以添加多个图片
            mOperateView.setMultiAdd(true);
            mOperateView.setObjScale(0.5f);
            //触摸事件监听
            mOperateView.setOnOperateViewTouchListener(new OperateView.OnOperateTouchListener() {
                @Override
                public boolean onOperateViewTouch(MotionEvent event) {
                    if(mStickerBar.getVisibility() == View.VISIBLE) {
                        mStickerBar.setVisibility(View.GONE);
                        return true;
                    }
                    return false;
                }
                @Override
                public void onOperateViewTouched(MotionEvent event, int type) {}
                @Override
                public void onOperateViewAction(int type, String tag) {
                    if(OperateView.ACTION_DELETE == type) {
                        Log.e("sticker", "** delete sticker");
                    } else {
                        Log.e("sticker", "** action sticker : " + type);
                    }
                }
            });
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

            Bitmap bitmap = BitmapFactory.decodeResource(getContext().getResources(), R.drawable.st_ornaments5);

            //Bitmap reBitmap = BitmapExt.reverseImage(bitmap, -1 , 1);

            addSticker(bitmap);

            //addSticker(reBitmap);

        } else if(view.getId() == R.id.sitcker2) {
            addSticker(BitmapFactory.decodeResource(getContext().getResources(), R.mipmap.earring8));
        } else if(view.getId() == R.id.sitcker3) {
            addSticker(BitmapFactory.decodeResource(getContext().getResources(), R.mipmap.face4));
        } else if(view.getId() == R.id.text) {
            addText("写死的测试文本");
        } else if(view.getId() == R.id.seekBar_cancel) {
            mStickerBar.setVisibility(View.VISIBLE);
            mSettingBar.setVisibility(View.GONE);
        } else if(view.getId() == R.id.seekBar_ok) {
            mStickerBar.setVisibility(View.VISIBLE);
            mSettingBar.setVisibility(View.GONE);
        } else if(view.getId() == R.id.show) {
            mStickerBar.setVisibility(View.VISIBLE);
        }
    }

    private void addSticker(Bitmap bitmap) {

        //默认会有两张贴纸
        if (mOperateView.getImgLists().size() > 0 && mOperateView.getImgLists().size() == 2) {

            Bitmap reBitmap = BitmapExt.reverseImage(bitmap, -1, 1);

            if (mOperateView.getImgLists().get(0).getLocation() == 0) {
                mOperateView.getImgLists().get(0).setSrcBm(reBitmap);
            } else {
                mOperateView.getImgLists().get(0).setSrcBm(bitmap);
            }

            if (mOperateView.getImgLists().get(1).getLocation() == 0) {
                mOperateView.getImgLists().get(1).setSrcBm(reBitmap);
            } else {
                mOperateView.getImgLists().get(1).setSrcBm(bitmap);
            }

            mOperateView.invalidate();
            mOperateView.setAllStickerTransparency(currentProgress);
        } else {

            //无drawable资源则传 -1
            ImageObject sticker = mOperateUtils.getImageObject(bitmap, mOperateView, OperateUtils.CENTERLEFT, 150, 100,
                    R.drawable.rotate, R.drawable.delete, R.drawable.flip, R.drawable.setting,
                    R.drawable.rotate, R.drawable.rotate, R.drawable.rotate, R.drawable.rotate, 2);
            sticker.resizeBoxSize = 60;

            Bitmap reBitmap = BitmapExt.reverseImage(bitmap, -1, 1);

            ImageObject resticker = mOperateUtils.getImageObject(reBitmap, mOperateView, OperateUtils.CENTERRIGHT, 150, 100,
                    R.drawable.rotate, R.drawable.delete, R.drawable.flip, R.drawable.setting,
                    R.drawable.rotate, R.drawable.rotate, R.drawable.rotate, R.drawable.rotate, 2);
            resticker.resizeBoxSize = 60;

            mOperateView.addItem(sticker);
            mOperateView.addItem(resticker);
        }
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

    @Override
    public void showSettingBar() {
        mStickerBar.setVisibility(View.GONE);
        mSettingBar.setVisibility(View.VISIBLE);
        mSeekBar.setProgress(mOperateView.getStickerTransparency());
    }

    @Override
    public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
        //mOperateView.setStickerTransparency(progress);
        currentProgress = progress;
        mOperateView.setAllStickerTransparency(progress);
    }

    @Override
    public void onStartTrackingTouch(SeekBar seekBar) {

    }

    @Override
    public void onStopTrackingTouch(SeekBar seekBar) {

    }
}
