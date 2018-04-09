package vip.frendy.camera;

import android.hardware.Camera;

/**
 * Created by frendy on 2018/4/9.
 */
public interface CameraHelperImpl {

    int getNumberOfCameras();

    Camera openCamera(int id);

    Camera openDefaultCamera();

    Camera openCameraFacing(int facing);

    boolean hasCamera(int cameraFacingFront);

    void getCameraInfo(int cameraId, CameraHelper.CameraInfo2 cameraInfo);
}
