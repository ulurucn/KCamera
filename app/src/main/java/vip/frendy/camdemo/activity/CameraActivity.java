package vip.frendy.camdemo.activity;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.SurfaceTexture;
import android.hardware.Camera;
import android.net.Uri;
import android.opengl.GLSurfaceView;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.view.View;
import android.widget.SeekBar;

import java.io.File;
import java.io.IOException;

import javax.microedition.khronos.egl.EGLConfig;
import javax.microedition.khronos.opengles.GL10;

import vip.frendy.camdemo.R;
import vip.frendy.camdemo.extension.HandlerExt;
import vip.frendy.camdemo.presenter.CameraLoader;
import vip.frendy.camdemo.presenter.FilterHelper;
import vip.frendy.base.Permission;
import vip.frendy.camera.settings.SettingFlashMode;
import vip.frendy.camera.settings.SettingISO;
import vip.frendy.camera.view.FrameCallback;
import vip.frendy.camera.view.Renderer;
import vip.frendy.camera.view.TextureController;
import vip.frendy.fliter.FilterType;
import vip.frendy.fliter.GPUImage;
import vip.frendy.fliter.GPUImageFilter;
import vip.frendy.fliter.aiyafilters.WaterMarkFilter;
import vip.frendy.fliter.gpufilters.GPUImageOverlayBlendFilter;
import vip.frendy.fliter.gpufilters.GPUImageTwoInputFilter;

/**
 * Created by frendy on 2018/4/8.
 */

public class CameraActivity extends BaseActivity implements SeekBar.OnSeekBarChangeListener, View.OnClickListener,
        CameraLoader.ILoaderListener, CameraLoader.ICameraListener, FrameCallback {
    private Context mContext = this;
    private Permission mPermission;

    private GPUImage mGPUImage;
    private GLSurfaceView mSurfaceView;

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
        findViewById(R.id.button_water_mark).setOnClickListener(this);
        findViewById(R.id.button_capture).setOnClickListener(this);
        findViewById(R.id.button_flashlight).setOnClickListener(this);

        mSurfaceView = findViewById(R.id.surfaceView);
        mGPUImage = new GPUImage(this);
        mGPUImage.setGLSurfaceView(mSurfaceView);

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
                    mRenderer = new Camera1Renderer();
                    mGPUImage.setRenderer(mRenderer);

                    mController = new TextureController(CameraActivity.this);
                    WaterMarkFilter filter=new WaterMarkFilter(getResources());
                    filter.setWaterMark(BitmapFactory.decodeResource(getResources(),R.mipmap.ic_launcher_round));
                    filter.setPosition(800,500,300,150);
                    mController.addFilter(filter);
                    mController.setFrameCallback(720, 1280, CameraActivity.this);
                    mSurfaceView.getHolder().addCallback(new SurfaceHolder.Callback() {
                        @Override
                        public void surfaceCreated(SurfaceHolder holder) {
                            mController.surfaceCreated(holder);
                            mController.setRenderer(mRenderer);
                        }

                        @Override
                        public void surfaceChanged(SurfaceHolder holder, int format, int width, int height) {
                            mController.surfaceChanged(width, height);
                        }

                        @Override
                        public void surfaceDestroyed(SurfaceHolder holder) {
                            mController.surfaceDestroyed();
                        }
                    });
                    if (mController != null) {
                        mController.onResume();
                    }
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
        if (mController != null) {
            mController.onPause();
        }
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
            case R.id.button_water_mark:
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
            case R.id.button_flashlight:
                mCamera.setFlashMode(SettingFlashMode.FLASH_MODE_ON);
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


    private TextureController mController;
    private Renderer mRenderer;
    private int cameraId = 1;

    @Override
    public void onFrame(byte[] bytes, long time) {

    }

    private class Camera1Renderer implements Renderer {

        @Override
        public void onDestroy() {
            if (mCamera != null) {
                mCamera = null;
            }
        }

        @Override
        public void onSurfaceCreated(GL10 gl, EGLConfig config) {
            mController.setImageDirection(cameraId);
            Camera.Size size = mCamera.getCameraInstance().getParameters().getPreviewSize();
            mController.setDataSize(size.height, size.width);
            try {
                mCamera.getCameraInstance().setPreviewTexture(mController.getTexture());
                mController.getTexture().setOnFrameAvailableListener(new SurfaceTexture.OnFrameAvailableListener() {
                    @Override
                    public void onFrameAvailable(SurfaceTexture surfaceTexture) {
                        mController.requestRender();
                    }
                });
            } catch (IOException e) {
                e.printStackTrace();
            }
            mCamera.getCameraInstance().startPreview();
        }

        @Override
        public void onSurfaceChanged(GL10 gl, int width, int height) {

        }

        @Override
        public void onDrawFrame(GL10 gl) {

        }

    }
}
