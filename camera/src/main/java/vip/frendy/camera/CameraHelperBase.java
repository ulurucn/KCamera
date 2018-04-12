package vip.frendy.camera;

import android.content.Context;
import android.content.pm.PackageManager;
import android.hardware.Camera;
import android.hardware.Camera.CameraInfo;

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
