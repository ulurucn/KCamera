package vip.frendy.edit.colorful;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.BlurMaskFilter;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.CornerPathEffect;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.PorterDuff;
import android.graphics.PorterDuffXfermode;
import android.graphics.Rect;
import android.support.annotation.Nullable;
import android.util.AttributeSet;
import android.util.Log;
import android.view.MotionEvent;
import android.view.ScaleGestureDetector;
import android.view.View;
import android.view.ViewGroup;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import vip.frendy.edit.R;
import vip.frendy.edit.mosaic.MosaicUtil;

import static android.graphics.Path.FillType.WINDING;

public class ColorfulView extends ViewGroup implements ScaleGestureDetector.OnScaleGestureListener{

    private Context mContext;

    /**
     * 缩放相关属性
     */
    private ScaleGestureDetector mScaleGestureDetector;
    private float scaleFactor = 1.0f;
    private Rect mInitImageRect;


    private List<ColorfulPath> touchPaths;
    //回退操作保存
    private List<ColorfulPath> cachePaths;

    private ColorfulPath touchPath;

    private Paint mColorPaint;
    private int mColorWidth = 30;
    private Rect mImageRect;

    private Paint mEraserPaint;

    //触点
    private boolean isMultiPointer = false;
    private float mLastX;
    private float mLastY;
    private int lastPointerCount = 0;
    private boolean isCanDrag;
    //防误触相关变量
    private long lastCheckDrawTime = 0;
    private boolean isCanDrawPath = false;

    /**
     * 绘画板宽度
     */
    private int mImageWidth;

    /**
     * 绘画板高度
     */
    private int mImageHeight;

    private int mPadding = 0;

    private ColorfulUtils.Type mType = ColorfulUtils.Type.COLOR; //画板类型

    private Bitmap bmBaseLayer;//原图
    private Bitmap bmCoverLayer;//橡皮擦
    private Bitmap bmColorfulLayer;//颜色


    //触点圈圈半径
    private int touchCircleR = 80;
    //触点圈圈
    private Paint touchCirclePaint;

    //是否显示触点圈圈
    private boolean enableTouchCircle = true;
    private boolean showTouchCircle = false;
    private float touchX, touchY;

    private int color = 0x602a5caa;

    private BlurMaskFilter bmf;

    private OnPathColorUpdatedListener mUpdatedListener;

    public ColorfulView(Context context) {
        super(context);
        this.mContext = context;
        initDrawView();
    }

    public ColorfulView(Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
        this.mContext = context;
        initDrawView();
    }

    public ColorfulView(Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        this.mContext = context;
        initDrawView();
    }

    private void initDrawView() {

        touchPaths = new ArrayList<>();
        cachePaths = new ArrayList<>();

        bmf = new BlurMaskFilter(10f, BlurMaskFilter.Blur.NORMAL);

        mColorPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        mColorPaint.setStyle(Paint.Style.STROKE);
        mColorPaint.setAntiAlias(true);
        mColorPaint.setStrokeJoin(Paint.Join.ROUND);
        mColorPaint.setStrokeCap(Paint.Cap.ROUND);
        mColorPaint.setPathEffect(new CornerPathEffect(10));
        mColorPaint.setStrokeWidth(mColorWidth);
        mColorPaint.setXfermode(new PorterDuffXfermode(PorterDuff.Mode.DST_ATOP));

        mEraserPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        mEraserPaint.setStyle(Paint.Style.STROKE);
        mEraserPaint.setAntiAlias(true);
        mEraserPaint.setStrokeJoin(Paint.Join.ROUND);
        mEraserPaint.setStrokeCap(Paint.Cap.ROUND);
        mEraserPaint.setPathEffect(new CornerPathEffect(10));
        mEraserPaint.setStrokeWidth(mColorWidth);
        mEraserPaint.setColor(Color.TRANSPARENT);
        mEraserPaint.setXfermode(new PorterDuffXfermode(PorterDuff.Mode.CLEAR));

        mImageRect = new Rect();
        mInitImageRect = new Rect();
        setWillNotDraw(false);

        touchCirclePaint = new Paint();
        touchCirclePaint.setAntiAlias(true);
        touchCirclePaint.setStyle(Paint.Style.STROKE);
        touchCirclePaint.setStrokeWidth(5);
        touchCirclePaint.setColor(Color.parseColor("#ffffff"));

        mScaleGestureDetector = new ScaleGestureDetector(mContext, this);
    }



    @Override
    public boolean onScale(ScaleGestureDetector detector) {

        float scale = detector.getScaleFactor();
        scaleFactor *= scale;
        if (scaleFactor < 1.0f){
            scaleFactor = 1.0f;
        }
        if (scaleFactor > 2.0f)
            scaleFactor = 2.0f;

        if (mImageRect != null){
            int addWidth =(int) (mInitImageRect.width() * scaleFactor) - mImageRect.width ();
            int addHeight=(int) (mInitImageRect.height() * scaleFactor) - mImageRect.height();
            float centerWidthRatio = (detector.getFocusX() - mImageRect.left) / mImageRect.width();
            float centerHeightRatio = (detector.getFocusY() - mImageRect.left) / mImageRect.height();

            int leftAdd = (int) (addWidth * centerWidthRatio);
            int topAdd = (int) (addHeight * centerHeightRatio);

            mImageRect.left =  mImageRect.left - leftAdd;
            mImageRect.right = mImageRect.right + (addWidth - leftAdd);
            mImageRect.top = mImageRect.top - topAdd;
            mImageRect.bottom = mImageRect.bottom + (addHeight - topAdd);
            checkCenterWhenScale();
        }

        invalidate();
        return true;
    }

    private void checkCenterWhenScale() {
        int deltaX = 0;
        int deltaY = 0;
        if (mImageRect.left > mInitImageRect.left) {
            deltaX = mInitImageRect.left - mImageRect.left;
        }
        if (mImageRect.right < mInitImageRect.right) {
            deltaX = mInitImageRect.right - mImageRect.right;
        }
        if (mImageRect.top > mInitImageRect.top) {
            deltaY = mInitImageRect.top - mImageRect.top;
        }
        if (mImageRect.bottom < mInitImageRect.bottom) {
            deltaY = mInitImageRect.bottom - mImageRect.bottom;
        }
        mImageRect.offset(deltaX,deltaY);
    }

    @Override
    public boolean onScaleBegin(ScaleGestureDetector detector) {
        return true;
    }

    public boolean dispatchTouchEvent(MotionEvent event) {

        super.dispatchTouchEvent(event);

        float pointerX = 0,pointerY = 0;

        int pointerCount = event.getPointerCount();
        //计算多个触摸点的平均值
        for (int i = 0; i < pointerCount; i++){
            pointerX += event.getX(i);
            pointerY += event.getY(i);
        }
        pointerX = pointerX / pointerCount;
        pointerY = pointerY / pointerCount;
        if(pointerCount > 1){
            isMultiPointer = true;
            //在多指模式，防误触变量重置
            isCanDrawPath = false;
            lastCheckDrawTime = 0;
        }
        if(lastPointerCount != pointerCount){
            mLastX = pointerX;
            mLastY = pointerY;
            isCanDrag = false;
            lastPointerCount = pointerCount;
        }
        if(isMultiPointer){
            switch (event.getAction()){
                case MotionEvent.ACTION_MOVE:
                    if (pointerCount == 1)
                        break;
                    if (mImageRect.width() > mInitImageRect.width()){ //仅仅在放大的状态，图片才可移动
                        int dx = (int) (pointerX - mLastX);
                        int dy = (int) (pointerY - mLastY);
                        if (!isCanDrag)
                            isCanDrag = isCanDrag(dx,dy);
                        if (isCanDrag) {
                            if (mImageRect.left + dx > mInitImageRect.left)
                                dx = mInitImageRect.left - mImageRect.left;
                            if (mImageRect.right + dx < mInitImageRect.right)
                                dx = mInitImageRect.right - mImageRect.right;
                            if (mImageRect.top + dy > mInitImageRect.top)
                                dy = mInitImageRect.top - mImageRect.top;
                            if (mImageRect.bottom + dy < mInitImageRect.bottom)
                                dy = mInitImageRect.bottom - mImageRect.bottom;
                            mImageRect.offset(dx, dy);
                        }
                    }
                    mLastX = pointerX;
                    mLastY = pointerY;
                    invalidate();
                    break;
                case MotionEvent.ACTION_UP:
                    lastPointerCount = 0;
                    isMultiPointer = false;
                    break;
            }
            return true;
        }

        int x = (int) event.getX();
        int y = (int) event.getY();
        //防误触
        if(!isCanDrawPath){
            if(lastCheckDrawTime == 0) {
                lastCheckDrawTime = System.currentTimeMillis();
            }
            if(System.currentTimeMillis() - lastCheckDrawTime > 50) { //大于50ms为有效值
                isCanDrawPath = true;
            }
        }
        onPathEvent(event, x, y);
        return true;
    }



    private void onPathEvent(MotionEvent event, int x, int y) {

        if(mImageWidth <= 0 || mImageHeight <= 0) {
            return;
        }

        if(x < mImageRect.left || x > mImageRect.right || y < mImageRect.top || y > mImageRect.bottom) {
            return;
        }

        float ratio = (mImageRect.right - mImageRect.left) / (float) mImageWidth;
        x = (int) ((x - mImageRect.left) / ratio);
        y = (int) ((y - mImageRect.top) / ratio);

        if(event.getAction() == MotionEvent.ACTION_DOWN) {
            touchPath = new ColorfulPath();
            touchPath.drawPath = new Path();
            touchPath.drawPath.moveTo(x, y);
            touchPath.paintWidth = (int) (mColorWidth / scaleFactor);
            touchPath.type = mType;

            if(event.getPointerCount() <= 1) {
                touchPaths.add(touchPath);
                //回调
                if(touchPath.type == ColorfulUtils.Type.ERASER && mUpdatedListener != null) {
                    mUpdatedListener.OnPathEraserApplyed();
                }
            }
        } else if(event.getAction() == MotionEvent.ACTION_MOVE) {
            if(isCanDrawPath) {
                if(touchPath != null && touchPath.drawPath != null)
                    touchPath.drawPath.lineTo(x, y);

                updatePathColor();

                invalidate();
            }
        } else if(event.getAction() == MotionEvent.ACTION_UP) {
            isCanDrawPath = false;
            lastCheckDrawTime = 0;
        }

    }

    private void updatePathColor() {
        if (mImageHeight <= 0 || mImageWidth <= 0) {
            return;
        }

        bmColorfulLayer = Bitmap.createBitmap(mImageWidth, mImageHeight, Bitmap.Config.ARGB_8888);

        Canvas canvas = new Canvas(bmColorfulLayer);

        mColorPaint.setColor(color);
        mColorPaint.setMaskFilter(bmf);
        mEraserPaint.setMaskFilter(bmf);

        for (ColorfulPath path : touchPaths) {
            Path pathTemp = path.drawPath;
            int drawWidth = path.paintWidth;
            mColorPaint.setStrokeWidth(drawWidth);

            if(path.type == ColorfulUtils.Type.COLOR) {
                canvas.drawPath(pathTemp, mColorPaint);
            } else if (path.type == ColorfulUtils.Type.ERASER){
                canvas.drawPath(pathTemp, mEraserPaint);
            }
        }

        //更新回调
        if(mUpdatedListener != null)
            mUpdatedListener.OnPathColorUpdated();

    }

    private boolean isCanDrag(int dx, int dy) {
        return Math.sqrt((dx*dx)+(dy*dy)) >= 5.0f;
    }

    @Override
    protected void onLayout(boolean changed, int left, int top, int right, int bottom) {
        if (mImageWidth <= 0 || mImageHeight <= 0) {
            return;
        }

        int contentWidth = right - left;
        int contentHeight = bottom - top;
        int viewWidth = contentWidth - mPadding * 2;
        int viewHeight = contentHeight - mPadding * 2;
        float widthRatio = viewWidth / ((float) mImageWidth);
        float heightRatio = viewHeight / ((float) mImageHeight);
        float ratio = widthRatio < heightRatio ? widthRatio : heightRatio;
        int realWidth = (int) (mImageWidth * ratio);
        int realHeight = (int) (mImageHeight * ratio);

        int imageLeft = (contentWidth - realWidth) / 2;
        int imageTop = (contentHeight - realHeight) / 2;
        int imageRight = imageLeft + realWidth;
        int imageBottom = imageTop + realHeight;
        mImageRect.set(imageLeft, imageTop, imageRight, imageBottom);
        mInitImageRect.set(imageLeft,imageTop,imageRight,imageBottom);

    }

    @Override
    public void onScaleEnd(ScaleGestureDetector detector) {

    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);

        if(bmBaseLayer != null) {
            canvas.drawBitmap(bmBaseLayer, null, mImageRect, null);
        }
        if(bmColorfulLayer != null) {
            canvas.drawBitmap(bmColorfulLayer, null, mImageRect, null);
        }

        if(enableTouchCircle && showTouchCircle && touchCirclePaint != null) {
            canvas.drawCircle(touchX, touchY, touchCircleR, touchCirclePaint);
        }

    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {

        if(mScaleGestureDetector != null)
            mScaleGestureDetector.onTouchEvent(event);
        switch (event.getAction()) {
            case MotionEvent.ACTION_DOWN:
                touchX = event.getX();
                touchY = event.getY();
                showTouchCircle = true;
                invalidate();
                break;
            case MotionEvent.ACTION_MOVE:
                touchX = event.getX();
                touchY = event.getY();
                break;
            case MotionEvent.ACTION_UP:
                showTouchCircle = false;
                invalidate();
                break;
        }
        return super.onTouchEvent(event);
    }

    public void setBackgroundResource(String imgPath) {
        File file = new File(imgPath);
        if (file == null || !file.exists()) {
            //Log.w(TAG, "setSrcPath invalid file path " + imgPath);
            return;
        }

        //reset();

        Bitmap bitmap = BitmapFactory.decodeFile(imgPath);
        mImageWidth = bitmap.getWidth();
        mImageHeight = bitmap.getHeight();
        bmBaseLayer = bitmap;
        requestLayout();
        invalidate();
    }


    public boolean reset() {
        this.mImageWidth = 0;
        this.mImageHeight = 0;
        if(bmCoverLayer != null) {
            bmCoverLayer.recycle();
            bmCoverLayer = null;
        }
        if(bmBaseLayer != null) {
            bmBaseLayer.recycle();
            bmBaseLayer = null;
        }
        if(bmColorfulLayer != null) {
            bmColorfulLayer.recycle();
            bmColorfulLayer = null;
        }

        touchPaths.clear();
        cachePaths.clear();
        return true;
    }

    @Override
    protected void onDetachedFromWindow() {
        super.onDetachedFromWindow();
        if(bmCoverLayer != null) {
            bmCoverLayer.recycle();
            bmCoverLayer = null;
        }
        if(bmBaseLayer != null) {
            bmBaseLayer.recycle();
            bmBaseLayer = null;
        }
        if(bmColorfulLayer != null) {
            bmColorfulLayer.recycle();
            bmColorfulLayer = null;
        }
    }

    public void setTouchCircleR(int r) {
        touchCircleR = r;
        invalidate();
    }

    public void changeColor(int color) {
        this.color = color;
        updatePathColor();
        invalidate();
    }

    public void setPaintType(ColorfulUtils.Type type) {
        this.mType = type;
    }

    public void backward() {
        if(touchPaths == null || cachePaths == null) return;
        if(touchPaths.size() <= 0) return;

        ColorfulPath lastPath = touchPaths.get(touchPaths.size() - 1);
        touchPaths.remove(lastPath);
        cachePaths.add(lastPath);
        updatePathColor();
        invalidate();
    }

    public void forward() {
        if(touchPaths == null || cachePaths == null) return;
        if(cachePaths.size() <= 0) return;

        ColorfulPath lastPath = cachePaths.get(cachePaths.size() - 1);
        touchPaths.add(lastPath);
        cachePaths.remove(lastPath);

        updatePathColor();
        invalidate();
    }

    public boolean canBackward() {
        if(touchPaths == null || cachePaths == null) return false;
        if(touchPaths.size() <= 0) {
            return false;
        } else {
            return true;
        }
    }

    public boolean canForward() {
        if(touchPaths == null || cachePaths == null) return false;
        if(cachePaths.size() <= 0) {
            return false;
        } else {
            return true;
        }
    }

    public void showTouchCircle(boolean state) {
        this.showTouchCircle = state;
        this.touchX = (float)(this.getWidth() / 2);
        this.touchY = (float)(this.getHeight() / 2);
        invalidate();
    }

    public void setWidth(int width) {
        this.mColorWidth = width;
    }

    public Bitmap getColorBitmap() {
        if (bmColorfulLayer == null) {
            Bitmap bitmap = Bitmap.createBitmap(this.mImageWidth, this.mImageHeight, Bitmap.Config.ARGB_8888);
            Canvas canvas = new Canvas(bitmap);
            canvas.drawBitmap(bmBaseLayer, 0.0F, 0.0F, (Paint)null);
            canvas.save();
            return bitmap;
        } else {
            Bitmap bitmap = Bitmap.createBitmap(this.mImageWidth, this.mImageHeight, Bitmap.Config.ARGB_8888);
            Canvas canvas = new Canvas(bitmap);
            canvas.drawBitmap(bmBaseLayer, 0.0F, 0.0F, (Paint)null);
            canvas.drawBitmap(bmColorfulLayer, 0.0F, 0.0F, (Paint)null);
            canvas.save();
            return bitmap;
        }
    }

    public void clearColorPath() {

        if (touchPaths != null) {
            touchPaths.clear();
        }

        if (cachePaths != null) {
            cachePaths.clear();
        }

        if (bmColorfulLayer != null) {
            bmColorfulLayer.recycle();
            bmColorfulLayer = null;
        }

        invalidate();

    }

    /**
     * 设置画板更新监听
     * @param listener
     */
    public void setOnPathColorUpdatedListener(OnPathColorUpdatedListener listener) {
        mUpdatedListener = listener;
    }

    public interface OnPathColorUpdatedListener {
        void OnPathColorUpdated();
        void OnPathEraserApplyed();
    }


}
