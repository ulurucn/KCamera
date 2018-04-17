package vip.frendy.camdemo.presenter;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.PointF;

import vip.frendy.camdemo.R;
import vip.frendy.fliter.aiyafilters.GPUImageFilterCompat;
import vip.frendy.fliter.aiyafilters.WaterMarkFilter;
import vip.frendy.fliter.FilterType;
import vip.frendy.fliter.base.GPUImageFilter;
import vip.frendy.fliter.gpufilters.GPUImageSharpenFilter;
import vip.frendy.fliter.gpufilters.GPUImageTwoInputFilter;
import vip.frendy.fliter.gpufilters.GPUImageVignetteFilter;
import vip.frendy.fliter.magicfilters.MagicBeautyFilter;
import vip.frendy.fliter.magicfilters.MagicFairytaleFilter;

/**
 * Created by frendy on 2018/4/9.
 */

public class FilterHelper {
    private Context mContext;

    public FilterHelper(Context context) {
        mContext = context;
    }

    public void onDestroy() {
        mContext = null;
    }


    public GPUImageFilter createBlendFilter(Class<? extends GPUImageTwoInputFilter> filterClass, int maskId) {
        try {
            GPUImageTwoInputFilter filter = filterClass.newInstance();
            filter.setBitmap(BitmapFactory.decodeResource(mContext.getResources(), maskId));
            return filter;
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    public GPUImageFilter createFilter(FilterType type) {
        switch (type) {
            case VIGNETTE:
                PointF centerPoint = new PointF();
                centerPoint.x = 0.5f;
                centerPoint.y = 0.5f;
                return new GPUImageVignetteFilter(centerPoint, new float[] {0.0f, 0.0f, 0.0f}, 0.3f, 0.75f);
            case WATER_MARK:
                WaterMarkFilter water =  new WaterMarkFilter(mContext.getResources());
                water.setWaterMark(BitmapFactory.decodeResource(mContext.getResources(), R.mipmap.ic_launcher_round));
                water.setPosition(200, 200, 200, 200);
                return new GPUImageFilterCompat(water);
            case SHARPEN:
                GPUImageSharpenFilter sharpness = new GPUImageSharpenFilter();
                sharpness.setSharpness(2.0f);
                return sharpness;
            case BEAUTY:
                return new MagicBeautyFilter();
            case FAIRYTALE:
                return new MagicFairytaleFilter();
            default:
                throw new IllegalStateException("No filter of that type!");
        }
    }

    //添加水印
    public Bitmap addWaterMark(Bitmap bitmap, Bitmap watermark) {
        android.graphics.Bitmap.Config bitmapConfig = bitmap.getConfig();
        if (bitmapConfig == null) {
            bitmapConfig = android.graphics.Bitmap.Config.ARGB_8888;
        }
        bitmap = bitmap.copy(bitmapConfig, true);

        Canvas canvas = new Canvas(bitmap);
        Paint paint = new Paint(Paint.FILTER_BITMAP_FLAG);

        int x = (bitmap.getWidth() - watermark.getWidth()) / 2;
        int y = bitmap.getHeight() - watermark.getHeight();

        canvas.drawBitmap(watermark, x, y, paint);

        return bitmap;
    }
}
