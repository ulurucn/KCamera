package vip.frendy.camera.settings;

import android.hardware.Camera;
import android.os.Build;

import java.util.List;

/**
 * Created by frendy on 2018/4/10.
 */

public class SettingSceneMode {

    public static final int SCENE_MODE_BARCODE = 0;
    public static final int SCENE_MODE_ACTION = 1;
    public static final int SCENE_MODE_AUTO = 2;
    public static final int SCENE_MODE_HDR = 3;
    public static final int SCENE_MODE_NIGHT = 4;

    public static void setSceneMode(Camera.Parameters params, int type) {
        List<String> sceneModes = params.getSupportedSceneModes();

        switch(type){
            case 0:
                if(sceneModes.contains(Camera.Parameters.SCENE_MODE_BARCODE)) {
                    params.setSceneMode(Camera.Parameters.SCENE_MODE_BARCODE);
                }
                break;
            case 1:
                if(sceneModes.contains(Camera.Parameters.SCENE_MODE_ACTION)) {
                    params.setSceneMode(Camera.Parameters.SCENE_MODE_ACTION);
                }
                break;
            case 2:
                if(sceneModes.contains(Camera.Parameters.SCENE_MODE_AUTO)) {
                    params.setSceneMode(Camera.Parameters.SCENE_MODE_AUTO);
                }
                break;
            case 3:
                if(sceneModes.contains(Camera.Parameters.SCENE_MODE_HDR) && Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN_MR1) {
                    params.setSceneMode(Camera.Parameters.SCENE_MODE_HDR);
                }
                break;
            case 4:
                if(sceneModes.contains(Camera.Parameters.SCENE_MODE_NIGHT)) {
                    params.setSceneMode(Camera.Parameters.SCENE_MODE_NIGHT);
                }
                break;
        }
    }
}
