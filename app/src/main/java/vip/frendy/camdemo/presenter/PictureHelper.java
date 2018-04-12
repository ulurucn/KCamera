package vip.frendy.camdemo.presenter;

import android.app.Activity;
import android.content.Intent;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Environment;
import android.provider.MediaStore;
import android.view.View;

import com.huantansheng.easyphotos.EasyPhotos;
import com.huantansheng.easyphotos.engine.GlideEngine;
import com.huantansheng.easyphotos.models.album.entity.Photo;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;

import vip.frendy.camdemo.model.Constants;
import vip.frendy.edit.common.Common;
import vip.frendy.edit.operate.OperateUtils;

/**
 * Created by frendy on 2018/4/8.
 */

public class PictureHelper {
    private Activity mActivity;
    private OperateUtils mOperateUtils;

    //用来标识请求gallery的activity
    private static final int PHOTO_PICKED_WITH_DATA = 3021;
    //用来标识请求照相功能的activity
    private static final int CAMERA_WITH_DATA = 3022;
    //拼图选择照片
    private static final int PUZZLE_PICKED_WITH_DATA = 3023;
    //拼图完成
    private static final int PUZZLE_WITH_DATA = 3024;

    private String tempPhotoPath;

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

    //从相机中获取照片
    public void getPictureFormCamera() {
        Intent intent = new Intent("android.media.action.IMAGE_CAPTURE");

        tempPhotoPath = Common.DCIM_CAMERA_PATH + Common.getNewFileName() + ".jpg";
        File currentFile = new File(tempPhotoPath);

        if (!currentFile.exists()) {
            try {
                currentFile.createNewFile();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

        intent.putExtra(MediaStore.EXTRA_OUTPUT, Uri.fromFile(currentFile));
        mActivity.startActivityForResult(intent, CAMERA_WITH_DATA);
    }

    //从相机中获取照片进行拼图
    public void getPuzzlePictureFromPhoto() {
        EasyPhotos.createAlbum(mActivity, false, GlideEngine.getInstance())
                .setCount(9)
                .setPuzzleMenu(false)
                .start(PUZZLE_PICKED_WITH_DATA);
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
            case CAMERA_WITH_DATA:
                photoPath = tempPhotoPath;
                //回调结果
                listener.onResult(photoPath);
                break;

            //拼图
            case PUZZLE_PICKED_WITH_DATA:
                ArrayList<Photo> resultPhotos = data.getParcelableArrayListExtra(EasyPhotos.RESULT_PHOTOS);
                if (resultPhotos.size() == 1) {
                    resultPhotos.add(resultPhotos.get(0));
                }
                ArrayList<Photo> selectedPhotoList = new ArrayList<>();
                selectedPhotoList.clear();
                selectedPhotoList.addAll(resultPhotos);

                EasyPhotos.startPuzzleWithPhotos(mActivity, selectedPhotoList, Environment.getExternalStorageDirectory().getAbsolutePath(),
                        "AlbumBuilder", PUZZLE_WITH_DATA, false, GlideEngine.getInstance());
                break;
            case PUZZLE_WITH_DATA:
//                Photo puzzlePhoto = data.getParcelableExtra(EasyPhotos.RESULT_PHOTOS);
                String puzzlePath = data.getStringExtra(EasyPhotos.RESULT_PATHS);
                //回调结果
                listener.onResult(puzzlePath);
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
