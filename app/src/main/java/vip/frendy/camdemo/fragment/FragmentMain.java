package vip.frendy.camdemo.fragment;

import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;

import vip.frendy.camdemo.R;
import vip.frendy.camdemo.presenter.PictureHelper;

/**
 * Created by frendy on 2018/4/8.
 */

public class FragmentMain extends BaseFragment implements View.OnClickListener {
    private PictureHelper mPictureHelper;
    private String mPicturePath;

    private LinearLayout mPicContent;
    private ImageView mPic;

    private IRouter mRouter;

    public static FragmentMain getInstance(IRouter router) {
        FragmentMain fragment = new FragmentMain();
        fragment.setRouter(router);
        return fragment;
    }

    public void setRouter(IRouter router) {
        mRouter = router;
    }

    @Override
    protected int getFragmentLayoutResId() {
        return R.layout.fragment_main;
    }

    @Override
    protected void initWidgets() {
        mPicContent = mRootView.findViewById(R.id.content_pic);
        mPic = mRootView.findViewById(R.id.pic);
    }

    @Override
    protected void initAction() {
        mRootView.findViewById(R.id.photo).setOnClickListener(this);
        mRootView.findViewById(R.id.camera).setOnClickListener(this);
        mRootView.findViewById(R.id.enhance).setOnClickListener(this);

        //图片处理
        mPictureHelper = new PictureHelper(getActivity());
    }

    @Override
    public void onClick(View view) {
        if(view.getId() == R.id.photo) {
            mPictureHelper.getPictureFromPhoto();
        } else if(view.getId() == R.id.camera) {
            mPictureHelper.getPictureFormCamera();
        } else if(view.getId() == R.id.enhance) {
            Bundle args = new Bundle();
            args.putString(FragmentEnhance.PIC_PATH, mPicturePath);
            if(mRouter != null) mRouter.onRouteTo(view.getId(), args);
        }
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        mPictureHelper.onActivityResult(requestCode, resultCode, data, new PictureHelper.IResultListener() {
            @Override
            public void onResult(String path) {
                mPicturePath = path;
                //压缩并显示
                compressedAndShow(path);
            }
        });
    }

    public void show(String path) {
        mPicturePath = path;
        Bitmap resultBitmap = BitmapFactory.decodeFile(mPicturePath);
        mPic.setImageBitmap(resultBitmap);
    }

    private void compressedAndShow(String path) {
        Bitmap resizeBmp = mPictureHelper.compressionFiller(path, mPicContent);
        mPic.setImageBitmap(resizeBmp);
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        mPictureHelper.onDestroy();
    }

    public interface IRouter {
        void onRouteTo(int id, Bundle args);
    }
}
