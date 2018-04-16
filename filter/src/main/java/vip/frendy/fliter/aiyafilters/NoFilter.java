package vip.frendy.fliter.aiyafilters;

import android.content.res.Resources;

/**
 * Created by frendy on 2018/4/16.
 */

public class NoFilter extends AFilter {

    public NoFilter(Resources mRes) {
        super(mRes);
    }

    @Override
    protected void onCreate() {
        createProgramByAssetsFile("shader/base_vertex.sh", "shader/base_fragment.sh");
    }

    @Override
    protected void onSizeChanged(int width, int height) {

    }
}
