package vip.frendy.edit.interfaces;

/**
 * Created by frendy on 2018/4/8.
 */

public interface IPictureEditListener {
    void onPictureEditApply(int id, String path);
    void onPictureEditCancel(int id);
}
