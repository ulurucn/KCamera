package vip.frendy.camera;

import android.app.Activity;
import android.content.Context;
import android.content.pm.PackageManager;
import android.hardware.Camera;
import android.hardware.Camera.CameraInfo;
import android.view.Surface;

import vip.frendy.camera.entity.CameraInfo2;

public class CameraHelperBase implements CameraHelperImpl {

    private final Context mContext;

    public CameraHelperBase(final Context context) {
        mContext = context;
    }

    @Override
    public int getNumberOfCameras() {
        int num = 0;
        //不支持相机
        if(!hasCameraSupport()) return num;
        //支持
        if(hasFrontFacingCamera()) num++;
        if(hasBackFacingCamera()) num++;

        return num;
    }

    @Override
    public Camera openCamera(final int id) {
        return Camera.open(id);
    }

    @Override
    public Camera openDefaultCamera() {
        return Camera.open();
    }

    @Override
    public boolean hasCamera(final int facing) {
        if (facing == CameraInfo.CAMERA_FACING_BACK) {
            return hasBackFacingCamera();
        } else {
            return hasFrontFacingCamera();
        }
    }

    @Override
    public Camera openCameraFacing(final int facing) {
        if (facing == CameraInfo.CAMERA_FACING_BACK) {
            return Camera.open();
        }
        return null;
    }

    @Override
    public void getCameraInfo(final int cameraId, final CameraInfo2 cameraInfo) {
        cameraInfo.facing = cameraId;
        cameraInfo.orientation = cameraId == CameraInfo.CAMERA_FACING_BACK ? 90 : 270;
    }

    @Override
    public int getCameraOrientation(int cameraId) {
        Camera.CameraInfo info = new Camera.CameraInfo();
        Camera.getCameraInfo(cameraId, info);
        return info.orientation;
    }

    @Override
    public void setCameraDisplayOrientation(Activity activity, int cameraId, Camera camera) {
        Camera.CameraInfo info = new Camera.CameraInfo();
        Camera.getCameraInfo(cameraId, info);
        int rotation = activity.getWindowManager().getDefaultDisplay().getRotation();
        int degrees = 0;
        switch (rotation) {
            case Surface.ROTATION_0:
                degrees = 0;
                break;
            case Surface.ROTATION_90:
                degrees = 90;
                break;
            case Surface.ROTATION_180:
                degrees = 180;
                break;
            case Surface.ROTATION_270:
                degrees = 270;
                break;
        }

        int result;
        if (info.facing == Camera.CameraInfo.CAMERA_FACING_FRONT) {
            result = (info.orientation + degrees) % 360;
            //compensate the mirror
            result = (360 - result) % 360;
        } else {
            //back-facing
            result = (info.orientation - degrees + 360) % 360;
        }
        camera.setDisplayOrientation(result);
    }

    private boolean hasCameraSupport() {
        return mContext.getPackageManager().hasSystemFeature(PackageManager.FEATURE_CAMERA);
    }

    /**
     * 检查设备是否有后置摄像头
     */
    public boolean hasBackFacingCamera() {
        return checkCameraFacing(CameraInfo.CAMERA_FACING_BACK);
    }

    /**
     * 检查设备是否有前置摄像头
     */
    public boolean hasFrontFacingCamera() {
        return checkCameraFacing(CameraInfo.CAMERA_FACING_BACK);
    }

    private boolean checkCameraFacing(final int facing) {
        final int cameraCount = Camera.getNumberOfCameras();
        Camera.CameraInfo info = new Camera.CameraInfo();
        for (int i = 0; i < cameraCount; i++) {
            Camera.getCameraInfo(i, info);
            if (facing == info.facing) {
                return true;
            }
        }
        return false;
    }
}
