package vip.frendy.camdemo.activity;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.hardware.Camera;
import android.net.Uri;
import android.opengl.GLSurfaceView;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.view.View;

import java.io.File;

import vip.frendy.base.Common;
import vip.frendy.camdemo.R;
import vip.frendy.camdemo.extension.HandlerExt;
import vip.frendy.camdemo.presenter.CameraLoader;
import vip.frendy.camdemo.presenter.FilterHelper;
import vip.frendy.camera.settings.SettingFlashMode;
import vip.frendy.fliter.FilterType;
import vip.frendy.fliter.widget.GPUImage;
import vip.frendy.fliter.base.GPUImageFilter;
import vip.frendy.fliter.gpufilters.GPUImageOverlayBlendFilter;
import vip.frendy.fliter.gpufilters.GPUImageTwoInputFilter;

/**
 * Created by frendy on 2018/4/8.
 */

public class CameraActivity extends BaseActivity implements View.OnClickListener, CameraLoader.ILoaderListener, CameraLoader.ICameraListener {
    private Context mContext = this;

    private GLSurfaceView mGLSurfaceView;
    private GPUImage mGPUImage;

    private CameraLoader mCamera;
    private GPUImageFilter mFilter;
    private FilterHelper mFilterHelper;

    private boolean isAnimFilter = false;
    private int[] animFrame = {R.drawable.animationa, R.drawable.animationb, R.drawable.animationc, R.drawable.animationd};
    private int animIndex = 0;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        shouldRequestPermission = true;
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_camera);

        findViewById(R.id.capture).setOnClickListener(this);
        findViewById(R.id.flashlight).setOnClickListener(this);
        findViewById(R.id.blend).setOnClickListener(this);
        findViewById(R.id.anim).setOnClickListener(this);
        findViewById(R.id.beauty).setOnClickListener(this);
        findViewById(R.id.watermark).setOnClickListener(this);

        mGLSurfaceView = findViewById(R.id.surfaceView);

        mGPUImage = new GPUImage(this);
        mGPUImage.setGLSurfaceView(mGLSurfaceView);

        mFilterHelper = new FilterHelper(this);
        mCamera = new CameraLoader(this, this, this);

        View cameraSwitchView = findViewById(R.id.switch_camera);
        cameraSwitchView.setOnClickListener(this);
        if (!mCamera.getCameraHelper().hasFrontCamera() || !mCamera.getCameraHelper().hasBackCamera()) {
            cameraSwitchView.setVisibility(View.GONE);
        }
    }

    @Override
    public void onPermissionsGranted() {
        super.onPermissionsGranted();
        mCamera.onResume();
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
        mGPUImage.setUpCamera(camera, orientation, flipHorizontal, flipVertical);
    }

    @Override
    public void onTakePicture(final Camera camera, final File file) {
        Bitmap bitmap = BitmapFactory.decodeFile(file.getAbsolutePath());

        //mGPUImage.setImage(bitmap);
        mGLSurfaceView.setRenderMode(GLSurfaceView.RENDERMODE_WHEN_DIRTY);

        String path = Common.getOutputMediaDir() + File.separator + System.currentTimeMillis() + ".jpg";
        mGPUImage.saveToPictures(bitmap, path, new GPUImage.OnPictureSavedListener() {
                    @Override
                    public void onPictureSaved(final Uri uri) {
                        file.delete();
                        camera.startPreview();
                        mGLSurfaceView.setRenderMode(GLSurfaceView.RENDERMODE_CONTINUOUSLY);
                    }
                });
    }

    @Override
    public void onSwitching() {
        mGLSurfaceView.requestLayout();
        mGPUImage.deleteImage();
    }

    @Override
    public void onClick(final View v) {
        switch (v.getId()) {
            case R.id.capture:
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
            case R.id.switch_camera:
                mCamera.switchCamera();
                break;
            case R.id.flashlight:
                mCamera.setFlashMode(SettingFlashMode.FLASH_MODE_ON);
                break;

            case R.id.anim:
                isAnimFilter = !isAnimFilter;
            case R.id.blend:
                switchFilterTo(mFilterHelper.createBlendFilter(GPUImageOverlayBlendFilter.class, R.mipmap.fliter));
                break;
            case R.id.beauty:
                switchFilterTo(mFilterHelper.createFilter(FilterType.BEAUTY, true));
                break;
            case R.id.watermark:
                switchFilterTo(mFilterHelper.createFilter(FilterType.WATER_MARK, true));
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
    protected void onDestroy() {
        super.onDestroy();
        if(mFilterHelper != null) mFilterHelper.onDestroy();
    }
}
