package vip.frendy.camera.settings;

import android.hardware.Camera;

import java.util.List;

/**
 * Created by frendy on 2018/4/10.
 */

public class SettingWhiteBalance {

    public static final int WHITE_BALANCE_AUTO = 0;
    public static final int WHITE_BALANCE_CLOUDY_DAYLIGHT = 1;
    public static final int WHITE_BALANCE_DAYLIGHT = 2;
    public static final int WHITE_BALANCE_FLUORESCENT = 3;
    public static final int WHITE_BALANCE_INCANDESCENT = 4;
    public static final int WHITE_BALANCE_SHADE = 5;
    public static final int WHITE_BALANCE_TWILIGHT = 6;
    public static final int WHITE_BALANCE_WARM_FLUORESCENT = 7;

    public static void setWhiteBalance(Camera camera, int type) {
        Camera.Parameters params = camera.getParameters();
        List<String> whiteBalanceModes = params.getSupportedWhiteBalance();

        switch(type){
            case 0:
                if(whiteBalanceModes.contains(Camera.Parameters.WHITE_BALANCE_AUTO)) {
                    params.setWhiteBalance(Camera.Parameters.WHITE_BALANCE_AUTO);
                }
                break;
            case 1:
                if(whiteBalanceModes.contains(Camera.Parameters.WHITE_BALANCE_CLOUDY_DAYLIGHT)) {
                    params.setWhiteBalance(Camera.Parameters.WHITE_BALANCE_CLOUDY_DAYLIGHT);
                }
                break;
            case 2:
                if(whiteBalanceModes.contains(Camera.Parameters.WHITE_BALANCE_DAYLIGHT)) {
                    params.setWhiteBalance(Camera.Parameters.WHITE_BALANCE_DAYLIGHT);
                }
                break;
            case 3:
                if(whiteBalanceModes.contains(Camera.Parameters.WHITE_BALANCE_FLUORESCENT)) {
                    params.setWhiteBalance(Camera.Parameters.WHITE_BALANCE_FLUORESCENT);
                }
                break;
            case 4:
                if(whiteBalanceModes.contains(Camera.Parameters.WHITE_BALANCE_INCANDESCENT)) {
                    params.setWhiteBalance(Camera.Parameters.WHITE_BALANCE_INCANDESCENT);
                }
                break;
            case 5:
                if(whiteBalanceModes.contains(Camera.Parameters.WHITE_BALANCE_SHADE)) {
                    params.setWhiteBalance(Camera.Parameters.WHITE_BALANCE_SHADE);
                }
                break;
            case 6:
                if(whiteBalanceModes.contains(Camera.Parameters.WHITE_BALANCE_TWILIGHT)) {
                    params.setWhiteBalance(Camera.Parameters.WHITE_BALANCE_TWILIGHT);
                }
                break;
            case 7:
                if(whiteBalanceModes.contains(Camera.Parameters.WHITE_BALANCE_WARM_FLUORESCENT)) {
                    params.setWhiteBalance(Camera.Parameters.WHITE_BALANCE_WARM_FLUORESCENT);
                }
                break;
        }
        camera.setParameters(params);
    }
}
