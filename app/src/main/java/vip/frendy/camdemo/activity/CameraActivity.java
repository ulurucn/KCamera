package vip.frendy.camdemo.activity;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.hardware.Camera;
import android.net.Uri;
import android.opengl.GLSurfaceView;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.util.Log;
import android.view.View;
import android.widget.SeekBar;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;

import jp.co.cyberagent.android.gpuimage.GPUImage;
import jp.co.cyberagent.android.gpuimage.GPUImageFilter;
import jp.co.cyberagent.android.gpuimage.GPUImageOverlayBlendFilter;
import jp.co.cyberagent.android.gpuimage.GPUImageTwoInputFilter;
import vip.frendy.camdemo.R;
import vip.frendy.camdemo.presenter.CameraLoader;
import vip.frendy.camera.Common;

import static vip.frendy.camera.Common.MEDIA_TYPE_IMAGE;

/**
 * Created by frendy on 2018/4/8.
 */

public class CameraActivity extends BaseActivity implements SeekBar.OnSeekBarChangeListener, View.OnClickListener, CameraLoader.ILoaderListener {
    private Context mContext = this;

    private GPUImage mGPUImage;

    private CameraLoader mCamera;
    private GPUImageFilter mFilter;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_camera);

        ((SeekBar) findViewById(R.id.seekBar)).setOnSeekBarChangeListener(this);
        findViewById(R.id.button_choose_filter).setOnClickListener(this);
        findViewById(R.id.button_capture).setOnClickListener(this);

        mGPUImage = new GPUImage(this);
        mGPUImage.setGLSurfaceView((GLSurfaceView) findViewById(R.id.surfaceView));

        mCamera = new CameraLoader(this, this);

        View cameraSwitchView = findViewById(R.id.img_switch_camera);
        cameraSwitchView.setOnClickListener(this);
        if (!mCamera.getCameraHelper().hasFrontCamera() || !mCamera.getCameraHelper().hasBackCamera()) {
            cameraSwitchView.setVisibility(View.GONE);
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        mCamera.onResume();
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
    public void onClick(final View v) {
        switch (v.getId()) {
            case R.id.button_choose_filter:
                switchFilterTo(createBlendFilter(mContext, GPUImageOverlayBlendFilter.class));
                break;
            case R.id.button_capture:
                if (mCamera.getCameraInstance().getParameters().getFocusMode()
                        .equals(Camera.Parameters.FOCUS_MODE_CONTINUOUS_PICTURE)) {
                    takePicture();
                } else {
                    mCamera.getCameraInstance().autoFocus(new Camera.AutoFocusCallback() {
                        @Override
                        public void onAutoFocus(final boolean success, final Camera camera) {
                            takePicture();
                        }
                    });
                }
                break;
            case R.id.img_switch_camera:
                mCamera.switchCamera();
                break;
        }
    }

    private void takePicture() {
        Camera.Parameters params = mCamera.getCameraInstance().getParameters();
        params.setRotation(90);
        mCamera.getCameraInstance().setParameters(params);
        for (Camera.Size size : params.getSupportedPictureSizes()) {
            Log.i("cam", "Supported: " + size.width + "x" + size.height);
        }
        mCamera.getCameraInstance().takePicture(null, null, new Camera.PictureCallback() {
                    @Override
                    public void onPictureTaken(byte[] data, final Camera camera) {
                        final File pictureFile = Common.getOutputMediaFile(MEDIA_TYPE_IMAGE, "tmp");
                        if (pictureFile == null) {
                            Log.d("cam", "Error creating media file, check storage permissions");
                            return;
                        }

                        try {
                            FileOutputStream fos = new FileOutputStream(pictureFile);
                            fos.write(data);
                            fos.close();
                        } catch (FileNotFoundException e) {
                            Log.d("cam", "File not found: " + e.getMessage());
                        } catch (IOException e) {
                            Log.d("cam", "Error accessing file: " + e.getMessage());
                        }
                        data = null;

                        Bitmap bitmap = BitmapFactory.decodeFile(pictureFile.getAbsolutePath());
                        // mGPUImage.setImage(bitmap);
                        final GLSurfaceView view = (GLSurfaceView) findViewById(R.id.surfaceView);
                        view.setRenderMode(GLSurfaceView.RENDERMODE_WHEN_DIRTY);
                        mGPUImage.saveToPictures(bitmap, "GPUImage", System.currentTimeMillis() + ".jpg",
                                new GPUImage.OnPictureSavedListener() {
                                    @Override
                                    public void onPictureSaved(final Uri uri) {
                                        pictureFile.delete();
                                        camera.startPreview();
                                        view.setRenderMode(GLSurfaceView.RENDERMODE_CONTINUOUSLY);
                                    }
                                });
                    }
                });
    }


    private void switchFilterTo(final GPUImageFilter filter) {
        if (mFilter == null
                || (filter != null && !mFilter.getClass().equals(filter.getClass()))) {
            mFilter = filter;
            mGPUImage.setFilter(mFilter);
        }
    }

    @Override
    public void onProgressChanged(final SeekBar seekBar, final int progress, final boolean fromUser) {}

    @Override
    public void onStartTrackingTouch(final SeekBar seekBar) {}

    @Override
    public void onStopTrackingTouch(final SeekBar seekBar) {}

    private static GPUImageFilter createBlendFilter(Context context, Class<? extends GPUImageTwoInputFilter> filterClass) {
        try {
            GPUImageTwoInputFilter filter = filterClass.newInstance();
            filter.setBitmap(BitmapFactory.decodeResource(context.getResources(), R.mipmap.fliter));
            return filter;
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }
}
