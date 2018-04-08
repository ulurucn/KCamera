package vip.frendy.camdemo;

import android.content.Intent;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;

import vip.frendy.camdemo.activity.BaseFragmentActivity;
import vip.frendy.camdemo.picture.PictureHelper;

/**
 * Created by frendy on 2018/4/8.
 */

public class MainActivity extends BaseFragmentActivity implements View.OnClickListener {
    private PictureHelper mPictureHelper;

    private LinearLayout mPicContent;
    private ImageView mPic;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        mPicContent = findViewById(R.id.content_pic);
        mPic = findViewById(R.id.pic);

        findViewById(R.id.photo).setOnClickListener(this);

        //图片处理
        mPictureHelper = new PictureHelper(this);
    }

    @Override
    public void onClick(View view) {
        if(view.getId() == R.id.photo) {
            mPictureHelper.getPictureFromPhoto();
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if(resultCode != RESULT_OK) return;

        mPictureHelper.onActivityResult(requestCode, resultCode, data, new PictureHelper.IResultListener() {
            @Override
            public void onResult(String path) {
                //压缩并显示
                compressedAndShow(path);
            }
        });
    }

    private void compressedAndShow(String path) {
        Bitmap resizeBmp = mPictureHelper.compressionFiller(path, mPicContent);
        mPic.setImageBitmap(resizeBmp);
    }
}
