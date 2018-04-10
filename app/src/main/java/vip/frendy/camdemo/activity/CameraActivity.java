package vip.frendy.camdemo.activity;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.hardware.Camera;
import android.net.Uri;
import android.opengl.GLSurfaceView;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.view.View;
import android.widget.SeekBar;

import java.io.File;

import jp.co.cyberagent.android.gpuimage.GPUImage;
import jp.co.cyberagent.android.gpuimage.GPUImageFilter;
import jp.co.cyberagent.android.gpuimage.GPUImageOverlayBlendFilter;
import jp.co.cyberagent.android.gpuimage.GPUImageTwoInputFilter;
import vip.frendy.camdemo.R;
import vip.frendy.camdemo.extension.HandlerExt;
import vip.frendy.camdemo.presenter.CameraLoader;
import vip.frendy.camdemo.presenter.FilterHelper;
import vip.frendy.camera.Permission;
import vip.frendy.camera.settings.SettingISO;

/**
 * Created by frendy on 2018/4/8.
 */

public class CameraActivity extends BaseActivity implements SeekBar.OnSeekBarChangeListener, View.OnClickListener,
        CameraLoader.ILoaderListener, CameraLoader.ICameraListener {
    private Context mContext = this;
    private Permission mPermission;

    private GPUImage mGPUImage;

    private CameraLoader mCamera;
    private GPUImageFilter mFilter;
    private FilterHelper mFilterHelper;

    private boolean isAnimFilter = false;
    private int[] animFrame = {R.drawable.animationa, R.drawable.animationb, R.drawable.animationc, R.drawable.animationd};
    private int animIndex = 0;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_camera);

        ((SeekBar) findViewById(R.id.seekBar)).setOnSeekBarChangeListener(this);
        findViewById(R.id.button_choose_filter).setOnClickListener(this);
        findViewById(R.id.button_anim_filter).setOnClickListener(this);
        findViewById(R.id.button_capture).setOnClickListener(this);

        mGPUImage = new GPUImage(this);
        mGPUImage.setGLSurfaceView((GLSurfaceView) findViewById(R.id.surfaceView));

        mFilterHelper = new FilterHelper(this);
        mCamera = new CameraLoader(this, this, this);

        View cameraSwitchView = findViewById(R.id.img_switch_camera);
        cameraSwitchView.setOnClickListener(this);
        if (!mCamera.getCameraHelper().hasFrontCamera() || !mCamera.getCameraHelper().hasBackCamera()) {
            cameraSwitchView.setVisibility(View.GONE);
        }

        //权限申请
        mPermission = new Permission(this);
        mPermission.requestPermissions();
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if(mPermission != null)
            mPermission.onRequestPermissionsResult(requestCode, permissions, grantResults, new Permission.IPermissionsListener() {
                @Override
                public void onPermissionsGranted() {
                    mCamera.onResume();
                }
                @Override
                public void onPermissionsDenied() {}
            });
    }

    @Override
    protected void onResume() {
        super.onResume();
    }

    @Override
    protected void onPause() {
        mCamera.onPause();
        super.onPause();
    }

    @Override
    public void onCameraSetUp(Camera camera, int orientation, boolean flipHorizontal, boolean flipVertical) {
        mGPUImage.setUpCamera(camera, orientation, flipHorizontal, false);
    }

    @Override
    public void onTakePicture(final Camera camera, final File file) {
        Bitmap bitmap = BitmapFactory.decodeFile(file.getAbsolutePath());

        //mGPUImage.setImage(bitmap);
        final GLSurfaceView view = findViewById(R.id.surfaceView);
        view.setRenderMode(GLSurfaceView.RENDERMODE_WHEN_DIRTY);
        mGPUImage.saveToPictures(bitmap, "GPUImage", System.currentTimeMillis() + ".jpg", new GPUImage.OnPictureSavedListener() {
                    @Override
                    public void onPictureSaved(final Uri uri) {
                        file.delete();
                        camera.startPreview();
                        view.setRenderMode(GLSurfaceView.RENDERMODE_CONTINUOUSLY);
                    }
                });
    }

    @Override
    public void onClick(final View v) {
        switch (v.getId()) {
            case R.id.button_anim_filter:
                isAnimFilter = !isAnimFilter;
            case R.id.button_choose_filter:
                switchFilterTo(mFilterHelper.createBlendFilter(GPUImageOverlayBlendFilter.class, R.mipmap.fliter));
                break;
            case R.id.button_capture:
                if (mCamera.getCameraInstance().getParameters().getFocusMode().equals(Camera.Parameters.FOCUS_MODE_CONTINUOUS_PICTURE)) {
                    mCamera.takePicture();
                } else {
                    mCamera.getCameraInstance().autoFocus(new Camera.AutoFocusCallback() {
                        @Override
                        public void onAutoFocus(final boolean success, final Camera camera) {
                            mCamera.takePicture();
                        }
                    });
                }
                break;
            case R.id.img_switch_camera:
                mCamera.switchCamera();
                break;
        }
    }

    private void switchFilterTo(final GPUImageFilter filter) {
        if(filter != null && mFilter != null && !mFilter.getClass().equals(filter.getClass())) {
            mFilter.destroy();
            mFilter = null;
        }
        if(filter != null) {
            mFilter = filter;
            updateFilter();
        }
    }

    private void updateFilter() {
        mGPUImage.setFilter(mFilter);

        if(!isAnimFilter) return;

        HandlerExt.postDelayToUI(new Runnable() {
            @Override
            public void run() {
                if(mFilter instanceof GPUImageTwoInputFilter) {
                    ((GPUImageTwoInputFilter) mFilter).setBitmap(BitmapFactory.decodeResource(
                            mContext.getResources(), animFrame[animIndex++ % animFrame.length]));
                    updateFilter();
                }
            }
        }, 300L);
    }

    @Override
    public void onProgressChanged(final SeekBar seekBar, final int progress, final boolean fromUser) {}

    @Override
    public void onStartTrackingTouch(final SeekBar seekBar) {}

    @Override
    public void onStopTrackingTouch(final SeekBar seekBar) {
        if(mCamera != null) mCamera.setISO(SettingISO.DEFAULT_VALUES[seekBar.getProgress() % 6]);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if(mFilterHelper != null) mFilterHelper.onDestroy();
    }
}
