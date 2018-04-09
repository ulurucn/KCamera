package vip.frendy.camdemo.presenter;

import android.app.Activity;
import android.hardware.Camera;

import vip.frendy.camera.CameraHelper;

/**
 * Created by frendy on 2018/4/9.
 */
public class CameraLoader {
    private Activity mActivity;

    private int mCurrentCameraId = 0;
    private Camera mCameraInstance;
    private CameraHelper mCameraHelper;
    private ILoaderListener mListener;

    public CameraLoader(Activity activity, ILoaderListener listener) {
        mActivity = activity;
        mCameraHelper = new CameraHelper(mActivity);
        mListener = listener;
    }

    public void onResume() {
        setUpCamera(mCurrentCameraId);
    }

    public void onPause() {
        releaseCamera();
    }

    public void switchCamera() {
        releaseCamera();
        mCurrentCameraId = (mCurrentCameraId + 1) % mCameraHelper.getNumberOfCameras();
        setUpCamera(mCurrentCameraId);
    }

    public CameraHelper getCameraHelper() {
        return mCameraHelper;
    }

    public Camera getCameraInstance() {
        return mCameraInstance;
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
        mCameraInstance.setPreviewCallback(null);
        mCameraInstance.release();
        mCameraInstance = null;
    }

    private void setUpCamera(final int id) {
        mCameraInstance = getCameraInstance(id);
        Camera.Parameters parameters = mCameraInstance.getParameters();
        // TODO adjust by getting supportedPreviewSizes and then choosing
        // the best one for screen size (best fill screen)
        if (parameters.getSupportedFocusModes().contains(
                Camera.Parameters.FOCUS_MODE_CONTINUOUS_PICTURE)) {
            parameters.setFocusMode(Camera.Parameters.FOCUS_MODE_CONTINUOUS_PICTURE);
        }
        mCameraInstance.setParameters(parameters);

        int orientation = mCameraHelper.getCameraDisplayOrientation(mActivity, mCurrentCameraId);
        CameraHelper.CameraInfo2 cameraInfo = new CameraHelper.CameraInfo2();
        mCameraHelper.getCameraInfo(mCurrentCameraId, cameraInfo);
        boolean flipHorizontal = cameraInfo.facing == Camera.CameraInfo.CAMERA_FACING_FRONT;
        if(mListener != null) mListener.onCameraSetUp(mCameraInstance, orientation, flipHorizontal, false);
    }


    public interface ILoaderListener {
        void onCameraSetUp(Camera camera, int orientation, boolean flipHorizontal, boolean flipVertical);
    }
}
