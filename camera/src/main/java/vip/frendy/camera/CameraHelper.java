package vip.frendy.camera;

import android.app.Activity;
import android.content.Context;
import android.hardware.Camera;
import android.hardware.Camera.CameraInfo;
import android.util.Log;

import java.util.List;

import vip.frendy.camera.entity.CameraInfo2;
import vip.frendy.camera.settings.SettingFlashMode;
import vip.frendy.camera.settings.SettingFocusMode;
import vip.frendy.camera.settings.SettingISO;
import vip.frendy.camera.settings.SettingSceneMode;
import vip.frendy.camera.settings.SettingWhiteBalance;

public class CameraHelper {
    private static final String TAG = "cam";

    private final CameraHelperImpl mImpl;

    public CameraHelper(final Context context) {
        mImpl = new CameraHelperBase(context);
    }

    public int getNumberOfCameras() {
        return mImpl.getNumberOfCameras();
    }

    public Camera openCamera(final int id) {
        return mImpl.openCamera(id);
    }

    public Camera openDefaultCamera() {
        return mImpl.openDefaultCamera();
    }

    public Camera openFrontCamera() {
        return mImpl.openCameraFacing(CameraInfo.CAMERA_FACING_FRONT);
    }

    public Camera openBackCamera() {
        return mImpl.openCameraFacing(CameraInfo.CAMERA_FACING_BACK);
    }

    public boolean hasFrontCamera() {
        return mImpl.hasCamera(CameraInfo.CAMERA_FACING_FRONT);
    }

    public boolean hasBackCamera() {
        return mImpl.hasCamera(CameraInfo.CAMERA_FACING_BACK);
    }

    public void getCameraInfo(final int cameraId, final CameraInfo2 cameraInfo) {
        mImpl.getCameraInfo(cameraId, cameraInfo);
    }


    public void setCameraDisplayOrientation(final Activity activity, final int cameraId, final Camera camera) {
        mImpl.setCameraDisplayOrientation(activity, cameraId, camera);
    }

    public int getCameraDisplayOrientation(final int cameraId) {
        return mImpl.getCameraOrientation(cameraId);
    }


    /**************************************
     * Operation of Camera
     *************************************/

    public void setISO(Camera.Parameters params, int value) {
        SettingISO.setISO(params, value);
    }

    /**
     * 设置对焦，会影响camera吞吐速率
     */
    public void setFocusMode(Camera.Parameters params, int type) {
        SettingFocusMode.setFocusMode(params, type);
    }

    public void setSceneMode(Camera.Parameters params, int type) {
        SettingSceneMode.setSceneMode(params, type);
    }

    public void setFlashMode(Camera.Parameters params, int type) {
        SettingFlashMode.setFlashMode(params, type);
    }

    public void setWhiteBalance(Camera.Parameters params, int type) {
        SettingWhiteBalance.setWhiteBalance(params, type);
    }

    /**
     * 设置fps
     */
    public void chooseFramerate(Camera.Parameters params, float frameRate) {
        int framerate = (int) (frameRate * 1000);
        List<int[]> rates = params.getSupportedPreviewFpsRange();
        int[] bestFramerate = rates.get(0);
        for (int i = 0; i < rates.size(); i++) {
            int[] rate = rates.get(i);
            Log.i(TAG, "supported preview pfs min " + rate[0] + " max " + rate[1]);
            int curDelta = Math.abs(rate[1] - framerate);
            int bestDelta = Math.abs(bestFramerate[1] - framerate);
            if (curDelta < bestDelta) {
                bestFramerate = rate;
            } else if (curDelta == bestDelta) {
                bestFramerate = bestFramerate[0] < rate[0] ? rate : bestFramerate;
            }
        }
        Log.i(TAG, "closet framerate min " + bestFramerate[0] + " max " + bestFramerate[1]);
        params.setPreviewFpsRange(bestFramerate[0], bestFramerate[1]);
    }

    /**
     * Attempts to find a preview size that matches the provided width and height (which
     * specify the dimensions of the encoded video).  If it fails to find a match it just
     * uses the default preview size for video.
     */
    public static int[] choosePreviewSize(Camera.Parameters params, int width, int height) {
        // We should make sure that the requested MPEG size is less than the preferred
        // size, and has the same aspect ratio.
        Camera.Size ppsfv = params.getPreferredPreviewSizeForVideo();
        if (ppsfv != null) {
            Log.e(TAG, "Camera preferred preview size for video is " + ppsfv.width + "x" + ppsfv.height);
        }
        for (Camera.Size size : params.getSupportedPreviewSizes()) {
            Log.i(TAG, "supported: " + size.width + "x" + size.height);
        }

        for (Camera.Size size : params.getSupportedPreviewSizes()) {
            if (size.width == width && size.height == height) {
                params.setPreviewSize(width, height);
                return new int[]{width, height};
            }
        }

        Log.i(TAG, "Unable to set preview size to " + width + "x" + height);
        if (ppsfv != null) {
            params.setPreviewSize(ppsfv.width, ppsfv.height);
            return new int[]{ppsfv.width, ppsfv.height};
        }
        // else use whatever the default size is
        return new int[]{0, 0};
    }

    public Camera.Size getLargePictureSize(Camera camera){
        if(camera != null){
            List<Camera.Size> sizes = camera.getParameters().getSupportedPictureSizes();
            Camera.Size temp = sizes.get(0);
            for(int i = 1;i < sizes.size();i ++){
                float scale = (float)(sizes.get(i).height) / sizes.get(i).width;
                if(temp.width < sizes.get(i).width && scale < 0.6f && scale > 0.5f)
                    temp = sizes.get(i);
            }
            return temp;
        }
        return null;
    }

    public Camera.Size getLargePreviewSize(Camera camera){
        if(camera != null){
            List<Camera.Size> sizes = camera.getParameters().getSupportedPreviewSizes();
            Camera.Size temp = sizes.get(0);
            for(int i = 1;i < sizes.size();i ++){
                if(temp.width < sizes.get(i).width)
                    temp = sizes.get(i);
            }
            return temp;
        }
        return null;
    }
}
