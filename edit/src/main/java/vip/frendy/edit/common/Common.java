package vip.frendy.edit.common;

import android.graphics.Bitmap;
import android.os.Environment;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;

/**
 * Created by frendy on 2018/4/8.
 */

public class Common {

    public static String DCIM_CAMERA_PATH = Environment.getExternalStorageDirectory() + "/DCIM/Camera/";

    //将生成的图片保存到内存中
    public static String saveBitmap(Bitmap bitmap, String path, String name) {
        if(Environment.getExternalStorageState().equals(Environment.MEDIA_MOUNTED)) {
            File dir = new File(path);
            if (!dir.exists()) dir.mkdir();

            File file = new File(path + name + ".jpg");
            FileOutputStream out;
            try {
                out = new FileOutputStream(file);
                if (bitmap.compress(Bitmap.CompressFormat.JPEG, 90, out)) {
                    out.flush();
                    out.close();
                }
                return file.getAbsolutePath();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        return null;
    }

    public static String getNewFileName() {
        SimpleDateFormat formatter = new SimpleDateFormat("yyyyMMddHHmmss");
        Date curDate = new Date(System.currentTimeMillis());

        return formatter.format(curDate);
    }
}
