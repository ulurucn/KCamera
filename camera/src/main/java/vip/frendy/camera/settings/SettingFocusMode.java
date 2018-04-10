package vip.frendy.camera.settings;

import android.hardware.Camera;

import java.util.List;

/**
 * Created by frendy on 2018/4/10.
 */

public class SettingFocusMode {

    public static final int FOCUS_MODE_AUTO = 0;
    public static final int FOCUS_MODE_CONTINUOUS_VIDEO = 1;
    public static final int FOCUS_MODE_EDOF = 2;
    public static final int FOCUS_MODE_FIXED = 3;
    public static final int FOCUS_MODE_INFINITY = 4;
    public static final int FOCUS_MODE_MACRO = 5;

    public static void setFocusMode(Camera camera, int type) {
        Camera.Parameters params = camera.getParameters();
        List<String> focusModes = params.getSupportedFocusModes();

        switch(type){
            case 0:
                if(focusModes.contains(Camera.Parameters.FOCUS_MODE_AUTO)) {
                    params.setAutoExposureLock(false);
                    params.setFocusMode(Camera.Parameters.FOCUS_MODE_AUTO);
                }
                break;
            case 1:
                if(focusModes.contains(Camera.Parameters.FOCUS_MODE_CONTINUOUS_VIDEO)) {
                    params.setFocusMode(Camera.Parameters.FOCUS_MODE_CONTINUOUS_VIDEO);
                }
                break;
            case 2:
                if(focusModes.contains(Camera.Parameters.FOCUS_MODE_EDOF)) {
                    params.setFocusMode(Camera.Parameters.FOCUS_MODE_EDOF);
                }
                break;
            case 3:
                if(focusModes.contains(Camera.Parameters.FOCUS_MODE_FIXED)) {
                    params.setAutoExposureLock(true);
                    params.setFocusMode(Camera.Parameters.FOCUS_MODE_FIXED);
                }
                break;
            case 4:
                if(focusModes.contains(Camera.Parameters.FOCUS_MODE_INFINITY)) {
                    params.setFocusMode(Camera.Parameters.FOCUS_MODE_INFINITY);
                }
                break;
            case 5:
                if(focusModes.contains(Camera.Parameters.FOCUS_MODE_MACRO)) {
                    params.setFocusMode(Camera.Parameters.FOCUS_MODE_MACRO);
                }
                break;
        }
        camera.setParameters(params);
    }
}
