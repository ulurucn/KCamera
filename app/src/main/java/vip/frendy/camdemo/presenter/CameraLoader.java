package vip.frendy.camdemo.presenter;

import android.app.Activity;
import android.hardware.Camera;
import android.util.Log;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;

import vip.frendy.base.Common;
import vip.frendy.camera.CameraHelper;
import vip.frendy.camera.entity.CameraInfo2;
import vip.frendy.camera.settings.SettingFocusMode;

import static vip.frendy.base.Common.MEDIA_TYPE_IMAGE;


/**
 * Created by frendy on 2018/4/9.
 */
public class CameraLoader {
    private Activity mActivity;

    private int mCurrentCameraId = 0;
    private Camera mCameraInstance;
    private CameraHelper mCameraHelper;
    private ILoaderListener mListener;
    private ICameraListener mCameraListener;

    public CameraLoader(Activity activity, ILoaderListener listener, ICameraListener cameraListener) {
        mActivity = activity;
        mCameraHelper = new CameraHelper(mActivity);
        mListener = listener;
        mCameraListener = cameraListener;
    }

    public void onResume() {
        setUpCamera(mCurrentCameraId);
    }

    public void onPause() {
        releaseCamera();
    }

    public void switchCamera() {
        //释放
        releaseCamera();
        //回调处理
        if(mCameraListener != null) mCameraListener.onSwitching();
        //初始化新摄像头
        mCurrentCameraId = (mCurrentCameraId + 1) % mCameraHelper.getNumberOfCameras();
        setUpCamera(mCurrentCameraId);
    }

    public CameraHelper getCameraHelper() {
        return mCameraHelper;
    }

    public Camera getCameraInstance() {
        return mCameraInstance;
    }

    public void takePicture() {
        Camera.Parameters params = mCameraInstance.getParameters();
        params.setRotation(90);
        mCameraInstance.setParameters(params);
        for(Camera.Size size : params.getSupportedPictureSizes()) {
            Log.i("cam", "Supported: " + size.width + "x" + size.height);
        }
        mCameraInstance.takePicture(null, null, new Camera.PictureCallback() {
            @Override
            public void onPictureTaken(byte[] data, final Camera camera) {
                final File pictureFile = Common.getOutputMediaFile(MEDIA_TYPE_IMAGE);
                if (pictureFile == null) {
                    Log.d("cam", "Error creating media file, check storage permissions");
                    return;
                }

                try {
                    FileOutputStream fos = new FileOutputStream(pictureFile);
                    fos.write(data);
                    fos.close();
                } catch (FileNotFoundException e) {
                    Log.d("cam", "File not found: " + e.getMessage());
                } catch (IOException e) {
                    Log.d("cam", "Error accessing file: " + e.getMessage());
                }
                data = null;

                if(mCameraListener != null) mCameraListener.onTakePicture(camera, pictureFile);
            }
        });
    }

    //应用设置参数
    public void apply(Camera.Parameters params) {
        mCameraInstance.setParameters(params);
    }

    public void setISO(int value) {
        Camera.Parameters params = mCameraInstance.getParameters();
        mCameraHelper.setISO(params, value);
        apply(params);
    }

    //EDOF, MACRO, AUTO and etc
    public void setFocusMode(int type) {
        Camera.Parameters params = mCameraInstance.getParameters();
        mCameraHelper.setFocusMode(params, type);
        apply(params);
    }

    //HDR, NIGHT, AUTO and etc
    public void setSceneMode(int type) {
        Camera.Parameters params = mCameraInstance.getParameters();
        mCameraHelper.setSceneMode(params, type);
        apply(params);
    }

    public void setFlashMode(int type) {
        Camera.Parameters params = mCameraInstance.getParameters();
        mCameraHelper.setFlashMode(params, type);
        apply(params);
    }

    public void setWhiteBalance(int type) {
        Camera.Parameters params = mCameraInstance.getParameters();
        mCameraHelper.setWhiteBalance(params, type);
        apply(params);
    }


    //A safe way to get an instance of the Camera object.
    private Camera getCameraInstance(final int id) {
        Camera camera = null;
        try {
            camera = mCameraHelper.openCamera(id);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return camera;
    }

    private void releaseCamera() {
        if(mCameraInstance != null) {
            mCameraInstance.stopPreview();
            mCameraInstance.setPreviewCallback(null);
            mCameraInstance.release();
            mCameraInstance = null;
        }
    }

    private void setUpCamera(final int id) {
        mCameraInstance = getCameraInstance(id);

        //对焦模式
        Camera.Parameters params = mCameraInstance.getParameters();
        mCameraHelper.setFocusMode(params, SettingFocusMode.FOCUS_MODE_CONTINUOUS_VIDEO);
        //预览大小
        Camera.Size previewSize = mCameraHelper.getLargePreviewSize(mCameraInstance);
        params.setPreviewSize(previewSize.width, previewSize.height);
        //图片大小
        Camera.Size pictureSize = mCameraHelper.getLargePictureSize(mCameraInstance);
        params.setPictureSize(pictureSize.width, pictureSize.height);
        apply(params);

        int orientation = mCameraHelper.getCameraDisplayOrientation(mCurrentCameraId);
        CameraInfo2 cameraInfo = new CameraInfo2();
        mCameraHelper.getCameraInfo(mCurrentCameraId, cameraInfo);
        boolean flipHorizontal = cameraInfo.facing == Camera.CameraInfo.CAMERA_FACING_FRONT;
        if(mListener != null) mListener.onCameraSetUp(mCameraInstance, orientation, flipHorizontal, false);
    }


    public interface ILoaderListener {
        void onCameraSetUp(Camera camera, int orientation, boolean flipHorizontal, boolean flipVertical);
    }
    public interface ICameraListener {
        void onTakePicture(Camera camera, File file);
        void onSwitching();
    }
}
