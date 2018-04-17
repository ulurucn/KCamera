package vip.frendy.fliter.utils;

import vip.frendy.fliter.base.GPUImageFilter;
import vip.frendy.fliter.gpufilters.GPUImageSharpenFilter;
import vip.frendy.fliter.gpufilters.GPUImageVignetteFilter;

public class FilterAdjuster {
    private final Adjuster<? extends GPUImageFilter> adjuster;

    public FilterAdjuster(final GPUImageFilter filter) {
        if (filter instanceof GPUImageVignetteFilter) {
            adjuster = new VignetteAdjuster().filter(filter);
        } else if (filter instanceof GPUImageSharpenFilter) {
            adjuster = new SharpnessAdjuster().filter(filter);
        } else {
            adjuster = null;
        }
    }

    public boolean canAdjust() {
        return adjuster != null;
    }

    public void adjust(final int percentage) {
        if (adjuster != null) {
            adjuster.adjust(percentage);
        }
    }

    private abstract class Adjuster<T extends GPUImageFilter> {
        private T filter;

        @SuppressWarnings("unchecked")
        public Adjuster<T> filter(final GPUImageFilter filter) {
            this.filter = (T) filter;
            return this;
        }

        public T getFilter() {
            return filter;
        }

        public abstract void adjust(int percentage);

        protected float range(final int percentage, final float start, final float end) {
            return (end - start) * percentage / 100.0f + start;
        }

        protected int range(final int percentage, final int start, final int end) {
            return (end - start) * percentage / 100 + start;
        }
    }

    private class VignetteAdjuster extends Adjuster<GPUImageVignetteFilter> {
        @Override
        public void adjust(final int percentage) {
            getFilter().setVignetteStart(range(percentage, 0.0f, 1.0f));
        }
    }

    private class SharpnessAdjuster extends Adjuster<GPUImageSharpenFilter> {
        @Override
        public void adjust(final int percentage) {
            getFilter().setSharpness(range(percentage, -4.0f, 4.0f));
        }
    }
}