package vip.frendy.camera;

import android.app.Activity;
import android.hardware.Camera;

import vip.frendy.camera.entity.CameraInfo2;

/**
 * Created by frendy on 2018/4/9.
 */
public interface CameraHelperImpl {

    int getNumberOfCameras();

    Camera openCamera(int id);

    Camera openDefaultCamera();

    Camera openCameraFacing(int facing);

    boolean hasCamera(int cameraFacingFront);

    void getCameraInfo(int cameraId, CameraInfo2 cameraInfo);

    int getCameraOrientation(int cameraId);

    void setCameraDisplayOrientation(Activity activity, int cameraId, Camera camera);
}
