package vip.frendy.camdemo.picture;

import android.app.Activity;
import android.content.Intent;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.net.Uri;
import android.provider.MediaStore;
import android.view.View;

import vip.frendy.camdemo.model.Constants;
import vip.frendy.edit.common.Common;
import vip.frendy.edit.operate.OperateUtils;

/**
 * Created by frendy on 2018/4/8.
 */

public class PictureHelper {
    private Activity mActivity;
    private OperateUtils mOperateUtils;

    /* 用来标识请求gallery的activity */
    private static final int PHOTO_PICKED_WITH_DATA = 3021;

    public PictureHelper(Activity activity) {
        mActivity= activity;
        mOperateUtils = new OperateUtils(mActivity);
    }

    public Bitmap compressionFiller(String filePath, View contentView) {
        return mOperateUtils.compressionFiller(filePath, contentView);
    }

    public String saveBitmap(Bitmap bitmap, String name) {
        return Common.saveBitmap(bitmap, Constants.DEFAULT_FILE_PATH, name);
    }

    //从相册中获取照片
    public void getPictureFromPhoto() {
        Intent openphotoIntent = new Intent(Intent.ACTION_PICK,
                android.provider.MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
        mActivity.startActivityForResult(openphotoIntent, PHOTO_PICKED_WITH_DATA);
    }

    public void onActivityResult(int requestCode, int resultCode, Intent data, IResultListener listener) {
        switch(requestCode) {
            case PHOTO_PICKED_WITH_DATA:
                Uri selectedImage = data.getData();
                String[] filePathColumns = { MediaStore.Images.Media.DATA };
                Cursor cursor = mActivity.getContentResolver().query(
                        selectedImage, filePathColumns, null, null, null);
                cursor.moveToFirst();
                int columnIndex = cursor.getColumnIndex(filePathColumns[0]);
                String photoPath = cursor.getString(columnIndex);
                cursor.close();

                //回调结果
                listener.onResult(photoPath);
                break;
        }
    }

    public void onDestroy() {
        mOperateUtils = null;
        mActivity = null;
    }


    public interface IResultListener {
        void onResult(String path);
    }
}
