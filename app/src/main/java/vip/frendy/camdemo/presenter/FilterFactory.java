package vip.frendy.camdemo.presenter;

import android.content.res.Resources;

import java.util.ArrayList;
import java.util.List;

import vip.frendy.arcamera.model.Filter;
import vip.frendy.camdemo.R;
import vip.frendy.fliter.aiyafilters.camera.AFilter;
import vip.frendy.fliter.aiyafilters.camera.BeautyFilter;
import vip.frendy.fliter.aiyafilters.camera.BigEyeFilter;
import vip.frendy.fliter.aiyafilters.camera.BinaryFilter;
import vip.frendy.fliter.aiyafilters.camera.BlackEyeFilter;
import vip.frendy.fliter.aiyafilters.camera.BlurFilter;
import vip.frendy.fliter.aiyafilters.camera.CoolFilter;
import vip.frendy.fliter.aiyafilters.camera.DrawPointFilter;
import vip.frendy.fliter.aiyafilters.camera.EmbossFilter;
import vip.frendy.fliter.aiyafilters.camera.FatFaceFilter;
import vip.frendy.fliter.aiyafilters.camera.FireEyeFilter;
import vip.frendy.fliter.aiyafilters.camera.FishEyeFilter;
import vip.frendy.fliter.aiyafilters.camera.FlushFilter;
import vip.frendy.fliter.aiyafilters.camera.GhostFilter;
import vip.frendy.fliter.aiyafilters.camera.GrayFilter;
import vip.frendy.fliter.aiyafilters.camera.LandmarkFilter;
import vip.frendy.fliter.aiyafilters.camera.MagFilter;
import vip.frendy.fliter.aiyafilters.camera.MirrorFilter;
import vip.frendy.fliter.aiyafilters.camera.MosaicFilter;
import vip.frendy.fliter.aiyafilters.camera.NegativeFilter;
import vip.frendy.fliter.aiyafilters.camera.NoFilter;
import vip.frendy.fliter.aiyafilters.camera.Rainbow2Filter;
import vip.frendy.fliter.aiyafilters.camera.Rainbow3Filter;
import vip.frendy.fliter.aiyafilters.camera.RainbowFilter;
import vip.frendy.fliter.aiyafilters.camera.SmallEyeFilter;
import vip.frendy.fliter.aiyafilters.camera.WarmFilter;
import vip.frendy.fliter.aiyafilters.camera.ZipPkmAnimationFilter;

/**
 * Created by Simon on 2017/7/6.
 */

public class FilterFactory {

    public static AFilter getFilter(Resources res, int menuId) {
        switch (menuId) {
            case R.id.menu_camera_default:
                return new NoFilter(res);
            case R.id.menu_camera_beauty:
                return new BeautyFilter(res);
            case R.id.menu_camera_gray:
                return new GrayFilter(res);
            case R.id.menu_camera_binary:
                return new BinaryFilter(res);
            case R.id.menu_camera_cool:
                return new CoolFilter(res);
            case R.id.menu_camera_warm:
                return new WarmFilter(res);
            case R.id.menu_camera_negative:
                return new NegativeFilter(res);
            case R.id.menu_camera_blur:
                return new BlurFilter(res);
            case R.id.menu_camera_mosaic:
                return new MosaicFilter(res);
            case R.id.menu_camera_emboss:
                return new EmbossFilter(res);
            case R.id.menu_camera_mag:
                return new MagFilter(res);
            case R.id.menu_camera_mirror:
                return new MirrorFilter(res);
            case R.id.menu_camera_fisheye:
                return new FishEyeFilter(res);
            case R.id.menu_camera_anim:
                ZipPkmAnimationFilter zipPkmAnimationFilter = new ZipPkmAnimationFilter(res);
                zipPkmAnimationFilter.setAnimation("assets/etczip/dragon.zip");
                return zipPkmAnimationFilter;
            case R.id.menu_camera_point:
                return new DrawPointFilter(res);
            case R.id.menu_camera_landmark:
                return new LandmarkFilter(res);
            case R.id.menu_camera_big_eye:
                return new BigEyeFilter(res);
            case R.id.menu_camera_small_eye:
                return new SmallEyeFilter(res);
            case R.id.menu_camera_fire_eye:
                return new FireEyeFilter(res);
            case R.id.menu_camera_black_eye:
                return new BlackEyeFilter(res);
            case R.id.menu_camera_flush:
                return new FlushFilter(res);
            case R.id.menu_camera_fat_face:
                return new FatFaceFilter(res);
            case R.id.menu_camera_rainbow:
                return new RainbowFilter(res);
            case R.id.menu_camera_rainbow2:
                return new Rainbow2Filter(res);
            case R.id.menu_camera_rainbow3:
                return new Rainbow3Filter(res);
            case R.id.menu_camera_ghost:
                return new GhostFilter(res);
            default:
                return new NoFilter(res);
        }
    }

    public static List<Filter> getPresetFilter() {
        List<Filter> filterList = new ArrayList<>();
        filterList.add(new Filter(R.id.menu_camera_default, R.mipmap.filter_thumb_0, "原图"));
        filterList.add(new Filter(R.id.menu_camera_beauty, R.mipmap.filter_thumb_0, "美颜"));
        filterList.add(new Filter(R.id.menu_camera_gray, R.mipmap.filter_thumb_0, "灰度化"));
        filterList.add(new Filter(R.id.menu_camera_binary, R.mipmap.filter_thumb_0, "二值化"));
        filterList.add(new Filter(R.id.menu_camera_cool, R.mipmap.filter_thumb_0, "冷色调"));
        filterList.add(new Filter(R.id.menu_camera_warm, R.mipmap.filter_thumb_0, "暖色调"));
        filterList.add(new Filter(R.id.menu_camera_negative, R.mipmap.filter_thumb_0, "底片"));
        filterList.add(new Filter(R.id.menu_camera_blur, R.mipmap.filter_thumb_0, "模糊"));
        filterList.add(new Filter(R.id.menu_camera_emboss, R.mipmap.filter_thumb_0, "浮雕"));
        filterList.add(new Filter(R.id.menu_camera_mosaic, R.mipmap.filter_thumb_0, "马赛克"));
        filterList.add(new Filter(R.id.menu_camera_mirror, R.mipmap.filter_thumb_0, "镜像"));
        filterList.add(new Filter(R.id.menu_camera_fisheye, R.mipmap.filter_thumb_0, "鱼眼"));
        filterList.add(new Filter(R.id.menu_camera_mag, R.mipmap.filter_thumb_0, "放大镜"));
        return filterList;
    }

    public static List<Filter> getPresetEffect() {
        List<Filter> filterList = new ArrayList<>();
        filterList.add(new Filter(R.id.menu_camera_default, R.drawable.ic_remove, "原图"));
        filterList.add(new Filter(R.id.menu_camera_rainbow3, R.drawable.effect_rainbow, "吐彩虹"));
        filterList.add(new Filter(R.id.menu_camera_fire_eye, R.drawable.effect_fire, "眼睛冒火"));
        filterList.add(new Filter(R.id.menu_camera_ghost, R.drawable.effect_ghost, "恶灵"));
        filterList.add(new Filter(R.id.menu_camera_big_eye, R.drawable.effect_big_eye, "大眼"));
        filterList.add(new Filter(R.id.menu_camera_small_eye, R.drawable.effect_small_eye, "小眼"));
        filterList.add(new Filter(R.id.menu_camera_black_eye, R.drawable.effect_black_eye, "黑眼圈"));
        filterList.add(new Filter(R.id.menu_camera_flush, R.drawable.effect_shy, "红晕"));
        filterList.add(new Filter(R.id.menu_camera_fat_face, R.drawable.effect_fat_face, "胖脸"));
        return filterList;
    }
}
