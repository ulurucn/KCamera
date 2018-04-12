package vip.frendy.edit.common;

import android.content.ContentResolver;
import android.content.Context;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Environment;
import android.provider.MediaStore;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;

/**
 * Created by frendy on 2018/4/8.
 */

public class Common {

    public static String getRealFilePath(final Context context, final Uri uri ) {
        if(null == uri) return null;

        final String scheme = uri.getScheme();
        String data = null;
        if(scheme == null) {
            data = uri.getPath();
        } else if(ContentResolver.SCHEME_FILE.equals(scheme)) {
            data = uri.getPath();
        } else if(ContentResolver.SCHEME_CONTENT.equals(scheme)) {
            Cursor cursor = context.getContentResolver().query(uri, new String[]{ MediaStore.Images.ImageColumns.DATA }, null, null, null );
            if(null != cursor) {
                if(cursor.moveToFirst()) {
                    int index = cursor.getColumnIndex(MediaStore.Images.ImageColumns.DATA);
                    if(index > -1){
                        data = cursor.getString( index );
                    }
                }
                cursor.close();
            }
        }
        return data;
    }

    public static void writeImage(Bitmap bitmap, String destPath, int quality) {
        try {
            deleteFile(destPath);
            if (createFile(destPath)) {
                FileOutputStream out = new FileOutputStream(destPath);
                if (bitmap.compress(Bitmap.CompressFormat.JPEG, quality, out)) {
                    out.flush();
                    out.close();
                    out = null;
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }


    public static boolean createFile(String filePath) {
        try {
            File file = new File(filePath);
            if (!file.exists()) {
                if (!file.getParentFile().exists()) {
                    file.getParentFile().mkdirs();
                }
                return file.createNewFile();
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        return true;
    }

    public static boolean deleteFile(String filePath) {
        try {
            File file = new File(filePath);
            if (file.exists()) {
                return file.delete();
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return false;
    }
}
