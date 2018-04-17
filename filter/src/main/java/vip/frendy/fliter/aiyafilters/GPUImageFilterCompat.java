package vip.frendy.fliter.aiyafilters;

import java.nio.FloatBuffer;

import vip.frendy.fliter.base.GPUImageFilter;
import vip.frendy.fliter.utils.OpenGlUtils;

/**
 * Created by frendy on 2018/4/9.
 */
public class GPUImageFilterCompat<T extends AFilter> extends GPUImageFilter {
    private T innerFilter;

    public GPUImageFilterCompat(T filter) {
        super();
        innerFilter = filter;
    }

    @Override
    public void onInit() {
        super.onInit();
        innerFilter.create();
    }

    @Override
    public int onDrawFrame(int textureId, FloatBuffer cubeBuffer, FloatBuffer textureBuffer) {
        super.onDrawFrame(textureId, cubeBuffer, textureBuffer);

        innerFilter.setTextureId(textureId);
        innerFilter.draw();

        return OpenGlUtils.ON_DRAWN;
    }

    @Override
    protected void onDrawArraysPre() {
        super.onDrawArraysPre();
    }

    @Override
    public void onOutputSizeChanged(int width, int height) {
        super.onOutputSizeChanged(width, height);
        innerFilter.setSize(width, height);
    }
}
