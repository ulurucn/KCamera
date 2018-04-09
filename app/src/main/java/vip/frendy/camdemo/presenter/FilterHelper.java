package vip.frendy.camdemo.presenter;

import android.content.Context;
import android.graphics.BitmapFactory;

import jp.co.cyberagent.android.gpuimage.GPUImageFilter;
import jp.co.cyberagent.android.gpuimage.GPUImageTwoInputFilter;
import vip.frendy.camdemo.R;

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
}
