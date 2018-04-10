package vip.frendy.camera.settings;

import android.hardware.Camera;

import java.util.List;

/**
 * Created by frendy on 2018/4/10.
 */

public class SettingFlashMode {

    public static final int FLASH_MODE_AUTO = 0;
    public static final int FLASH_MODE_OFF = 1;
    public static final int FLASH_MODE_ON = 2;
    public static final int FLASH_MODE_RED_EYE = 3;

    public static void setFlashMode(Camera camera, int type) {
        Camera.Parameters params = camera.getParameters();
        List<String> flashModes = params.getSupportedFlashModes();

        switch(type){
            case 0:
                if(flashModes.contains(Camera.Parameters.FLASH_MODE_AUTO)) {
                    params.setFlashMode(Camera.Parameters.FLASH_MODE_AUTO);
                }
                break;
            case 1:
                if(flashModes.contains(Camera.Parameters.FLASH_MODE_OFF)) {
                    params.setFlashMode(Camera.Parameters.FLASH_MODE_OFF);
                }
                break;
            case 2:
                if(flashModes.contains(Camera.Parameters.FLASH_MODE_ON)) {
                    params.setFlashMode(Camera.Parameters.FLASH_MODE_ON);
                }
                break;
            case 3:
                if(flashModes.contains(Camera.Parameters.FLASH_MODE_RED_EYE)) {
                    params.setFlashMode(Camera.Parameters.FLASH_MODE_RED_EYE);
                }
                break;
        }
        camera.setParameters(params);
    }
}
