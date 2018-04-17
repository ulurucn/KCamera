package vip.frendy.fliter;

import android.content.Context;
import android.content.res.Resources;

/**
 * Created by frendy on 2018/4/17.
 */

public class FilterManager {
    private static FilterManager mFilterManager;

    public static FilterManager getInstance() {
        if(mFilterManager == null) {
            mFilterManager = new FilterManager();
        }
        return mFilterManager;
    }


    private Context mContext;

    public void init(Context context) {
        mContext = context;
    }

    public Resources getResources() {
        return mContext.getResources();
    }
}
