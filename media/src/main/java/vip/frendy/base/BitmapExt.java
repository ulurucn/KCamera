package vip.frendy.base;

import android.graphics.Bitmap;
import android.graphics.Matrix;
import android.os.Environment;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;

/**
 * Created by frendy on 2018/4/12.
 */

public class BitmapExt {

    /**
     * 图片旋转
     *
     * @param bit 旋转原图像
     * @param degrees 旋转度数
     * @return 旋转之后的图像
     */
    public static Bitmap rotateImage(Bitmap bit, int degrees) {
        Matrix matrix = new Matrix();
        matrix.postRotate(degrees);
        return Bitmap.createBitmap(bit, 0, 0,
                bit.getWidth(), bit.getHeight(), matrix, true);
    }

    /**
     * 翻转图像
     *
     * @param bit 翻转原图像
     * @param x 翻转X轴
     * @param y 翻转Y轴
     * @return 翻转之后的图像
     *
     * 说明:
     * (1,-1)上下翻转
     * (-1,1)左右翻转
     *
     */
    public static Bitmap reverseImage(Bitmap bit,int x,int y) {
        Matrix matrix = new Matrix();
        matrix.postScale(x, y);

        return Bitmap.createBitmap(bit, 0, 0,
                bit.getWidth(), bit.getHeight(), matrix, true);
    }


    /**
     * 保存图片
     *
     * @param bitmap 图像
     * @param path 保存路径
     * @param name 保存文件名
     */
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
}
