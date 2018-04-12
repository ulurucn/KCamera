package vip.frendy.base;

import android.os.Environment;
import android.util.Log;

import java.io.File;
import java.text.SimpleDateFormat;
import java.util.Date;

/**
 * Created by frendy on 2018/4/9.
 */

public class Common {
    public static String DEFAULT_DIR = "kcam";

    public static final int MEDIA_TYPE_IMAGE = 1;
    public static final int MEDIA_TYPE_VIDEO = 2;

    public static String getOutputMediaDir() {
        return Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES).getAbsolutePath() + File.separator + DEFAULT_DIR;
    }

    public static File getOutputMediaFile(final int type) {
        // Create a media file name
        String timeStamp = new SimpleDateFormat("yyyyMMdd_HHmmss").format(new Date());

        File mediaFile;
        if(type == MEDIA_TYPE_IMAGE) {
            mediaFile =  getOutputMediaFile(type, "IMG_" + timeStamp);
        } else if(type == MEDIA_TYPE_VIDEO) {
            mediaFile = getOutputMediaFile(type, "VID_" + timeStamp);
        } else {
            return null;
        }

        return mediaFile;
    }

    public static File getOutputMediaFile(final int type, String fileName) {
        // To be safe, you should check that the SDCard is mounted
        // using Environment.getExternalStorageState() before doing this.

        File mediaStorageDir = new File(getOutputMediaDir());
        // This location works best if you want the created images to be shared
        // between applications and persist after your app has been uninstalled.

        // Create the storage directory if it does not exist
        if(!mediaStorageDir.exists()) {
            if(!mediaStorageDir.mkdirs()) {
                Log.d("Cam", "failed to create directory");
                return null;
            }
        }

        // Create a media file name
        File mediaFile;
        if(type == MEDIA_TYPE_IMAGE) {
            mediaFile = new File(mediaStorageDir.getPath() + File.separator + fileName + ".jpg");
        } else if(type == MEDIA_TYPE_VIDEO) {
            mediaFile = new File(mediaStorageDir.getPath() + File.separator + fileName + ".mp4");
        } else {
            return null;
        }

        return mediaFile;
    }
}
