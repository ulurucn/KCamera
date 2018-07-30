package vip.frendy.edit.operate;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Point;
import android.graphics.Rect;
import android.view.MotionEvent;
import android.view.ScaleGestureDetector;
import android.view.View;

import java.util.ArrayList;
import java.util.List;

import vip.frendy.edit.interfaces.ISettingListener;

/**
 * @author jarlen
 * @modified frendy
 */
public class OperateView extends View implements ScaleGestureDetector.OnScaleGestureListener {
    private List<ImageObject> imgLists = new ArrayList<ImageObject>();
    private Rect mCanvasLimits;
    private Bitmap bgBmp;
    private Paint paint = new Paint();
    //true 代表可以添加多个水印图片（或文字），false 代表只可添加单个水印图片（或文字）
    private boolean isMultiAdd;
    private float mObjScale = 0.4f;
    private ISettingListener iSettingListener;
    //是否开启双指触控
    public static int TYPE_MULTI_TOUCH_1 = 1;
    public static int TYPE_MULTI_TOUCH_2 = 2;
    private int mMultiTouchType = TYPE_MULTI_TOUCH_2;

    /**
     * 缩放相关属性
     */
    private ScaleGestureDetector mScaleGestureDetector;
    private float mScaleFactor = 1.0f;
    private Rect mInitImageRect;
    private Rect mImageRect;
    //触点
    private float mLastX;
    private float mLastY;
    private int lastPointerCount = 0;
    private boolean isCanDrag;


    public OperateView(Context context, Bitmap resizeBmp, ISettingListener iSettingListener) {
        super(context);
        this.bgBmp = resizeBmp;
        this.iSettingListener = iSettingListener;
        int width = bgBmp.getWidth();
        int height = bgBmp.getHeight();
        mCanvasLimits = new Rect(0, 0, width, height);

        mImageRect = new Rect();
        mInitImageRect = new Rect();
        mScaleGestureDetector = new ScaleGestureDetector(context, this);
    }

    /**
     * 设置水印图片初始化大小
     */
    public void setObjScale(float scale) {
        mObjScale = scale;
    }

    /**
     * 设置是否可以添加多个图片或者文字对象
     * true 代表可以添加多个水印图片（或文字），false 代表只可添加单个水印图片（或文字）
     */
    public void setMultiAdd(boolean isMultiAdd) {
        this.isMultiAdd = isMultiAdd;
    }

    public void setmMultiTouchType(int type) {
        mMultiTouchType = type;
    }

    /**
     * 将图片对象添加到View中
     * @param imgObj 图片对象
     */
    public void addItem(ImageObject imgObj) {
        if (imgObj == null) {
            return;
        }
        if (!isMultiAdd && imgLists != null) {
            imgLists.clear();
        }
        imgObj.setSelected(true);
        if (!imgObj.isTextObject) {
            imgObj.setScale(mObjScale);
        }
        ImageObject tempImgObj = null;
        for (int i = 0; i < imgLists.size(); i++) {
            tempImgObj = imgLists.get(i);
            tempImgObj.setSelected(false);
        }
        imgLists.add(imgObj);
        invalidate();
    }

    /**
     * 画出容器内所有的图像
     */
    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        int sc = canvas.save();
        canvas.clipRect(mCanvasLimits);
        canvas.drawBitmap(bgBmp, null, mImageRect, paint);
        drawImages(canvas);
        canvas.restoreToCount(sc);
        for (ImageObject ad : imgLists) {
            if (ad != null && ad.isSelected()) {
                ad.drawIcon(canvas);
            }
        }
    }

    /**
     * 循环画图像
     */
    private void drawImages(Canvas canvas) {
        for (ImageObject ad : imgLists) {
            if (ad != null) {
                ad.draw(canvas);
            }
        }
    }

    @Override
    protected void onLayout(boolean changed, int left, int top, int right, int bottom) {
        mImageRect.set(left, top, right, bottom);
        mInitImageRect.set(left, top, right, bottom);
    }

    public void save() {
        ImageObject io = getSelected();
        if (io != null) {
            io.setSelected(false);
        }
        invalidate();
    }

    /**
     * 触摸事件分发
     */
    @Override
    public boolean dispatchTouchEvent(MotionEvent event) {

        return super.dispatchTouchEvent(event);
    }

    /**
     * 根据触控点重绘View
     */
    @Override
    public boolean onTouchEvent(MotionEvent event) {
        if(event.getPointerCount() == 1) {
            handleSingleTouchManipulateEvent(event);
        } else {
            if(mMultiTouchType == TYPE_MULTI_TOUCH_1)
                handleMultiTouchManipulateEvent(event);
            if(mMultiTouchType == TYPE_MULTI_TOUCH_2 && mScaleGestureDetector != null)
                mScaleGestureDetector.onTouchEvent(event);
        }
        if(mMultiTouchType == TYPE_MULTI_TOUCH_2 && !isInObjectArea(event)) {
            //放大后移动图片
            handlerScaleGesture(event);
        }
        invalidate();

        super.onTouchEvent(event);
        return true;
    }

    private boolean mMovedSinceDown = false;
    private boolean mMovedImageSinceDown = false;
    private boolean mResizeAndRotateSinceDown = false;
    private boolean mResizeXSinceDown = false;
    private boolean mResizeYSinceDown = false;
    private float mStartDistance = 0.0f;
    private float mStartScale = 0.0f;
    private float mStartRot = 0.0f;
    private float mPrevRot = 0.0f;
    static public final double ROTATION_STEP = 2.0;
    static public final double ZOOM_STEP = 0.01;
    static public final float CANVAS_SCALE_MIN = 0.25f;
    static public final float CANVAS_SCALE_MAX = 3.0f;
    private Point mPreviousPos = new Point(0, 0); // single touch events
    float diff;
    float rot;

    /**
     * 多点触控操作
     */
    private void handleMultiTouchManipulateEvent(MotionEvent event) {
        switch (event.getAction() & MotionEvent.ACTION_MASK) {
            case MotionEvent.ACTION_POINTER_UP :
                break;
            case MotionEvent.ACTION_POINTER_DOWN :
                float x1 = event.getX(0);
                float x2 = event.getX(1);
                float y1 = event.getY(0);
                float y2 = event.getY(1);
                float delX = (x2 - x1);
                float delY = (y2 - y1);
                diff = (float) Math.sqrt((delX * delX + delY * delY));
                mStartDistance = diff;
                mPrevRot = (float) Math.toDegrees(Math.atan2(delX, delY));
                for (ImageObject io : imgLists) {
                    if (io.isSelected()) {
                        mStartScale = io.getScale();
                        mStartRot = io.getRotation();
                        break;
                    }
                }
                break;

            case MotionEvent.ACTION_MOVE :
                x1 = event.getX(0);
                x2 = event.getX(1);
                y1 = event.getY(0);
                y2 = event.getY(1);
                delX = (x2 - x1);
                delY = (y2 - y1);
                diff = (float) Math.sqrt((delX * delX + delY * delY));
                float scale = diff / mStartDistance;
                float newscale = mStartScale * scale;
                rot = (float) Math.toDegrees(Math.atan2(delX, delY));
                float rotdiff = mPrevRot - rot;
                for (ImageObject io : imgLists) {
                    if (io.isSelected() && newscale < 10.0f && newscale > 0.1f) {
                        float newrot = Math.round((mStartRot + rotdiff) / 1.0f);
                        if (Math.abs((newscale - io.getScale()) * ROTATION_STEP) > Math
                                .abs(newrot - io.getRotation())) {
                            io.setScale(newscale);
                        } else {
                            io.setRotation(newrot % 360);
                        }
                        break;
                    }
                }

                break;
        }
    }

    private long selectTime = 0;
    /**
     * 单点触控操作
     */
    private void handleSingleTouchManipulateEvent(MotionEvent event) {
        long currentTime = 0;

        switch (event.getAction()) {
            case MotionEvent.ACTION_DOWN:
                //拦截触摸事件
                if(mTouchListener != null && mTouchListener.onOperateViewTouch(event)) {
                    break;
                }

                mMovedSinceDown = false;
                mMovedImageSinceDown = false;
                mResizeAndRotateSinceDown = false;
                int selectedId = -1;

                for(int i = imgLists.size() - 1; i >= 0; --i) {
                    ImageObject io = imgLists.get(i);
                    if(io.contains(event.getX(), event.getY())
                            || (io.pointOnCorner(event.getX(), event.getY(), OperateConstants.RIGHTBOTTOM) && io.getRotateBm() != null)
                            || (io.pointOnCorner(event.getX(), event.getY(), OperateConstants.LEFTTOP) && io.getFlipBm() != null)
                            || (io.pointOnCorner(event.getX(), event.getY(), OperateConstants.RIGHTTOP) && io.getDeleteBm() != null)
                            || (io.pointOnCorner(event.getX(), event.getY(), OperateConstants.LEFTBOTTOM) && io.getSettingBm() != null)
                            || (io.pointOnCorner(event.getX(), event.getY(), OperateConstants.LEFTCENTER) && io.getLeftBm() != null)
                            || (io.pointOnCorner(event.getX(), event.getY(), OperateConstants.TOPCENTER) && io.getTopBm() != null)
                            || (io.pointOnCorner(event.getX(), event.getY(), OperateConstants.RIGHTCENTER) && io.getRightBm() != null)
                            || (io.pointOnCorner(event.getX(), event.getY(), OperateConstants.BOTTOMCENTER) && io.getBottomBm() != null)) {
                        io.setSelected(true);
                        imgLists.remove(i);
                        imgLists.add(io);
                        selectedId = imgLists.size() - 1;
                        currentTime = System.currentTimeMillis();
                        if(currentTime - selectTime < 300) {
                            if(myListener != null) {
                                if(getSelected().isTextObject()) {
                                    myListener.onClick((TextObject) getSelected());
                                }
                            }
                        }
                        selectTime = currentTime;
                        break;
                    }
                }
                if(selectedId < 0) {
                    for(int i = imgLists.size() - 1; i >= 0; --i) {
                        ImageObject io = imgLists.get(i);
                        if (io.contains(event.getX(), event.getY())
                                || (io.pointOnCorner(event.getX(), event.getY(), OperateConstants.RIGHTBOTTOM) && io.getRotateBm() != null)
                                || (io.pointOnCorner(event.getX(), event.getY(), OperateConstants.LEFTTOP) && io.getFlipBm() != null)
                                || (io.pointOnCorner(event.getX(), event.getY(), OperateConstants.RIGHTTOP) && io.getDeleteBm() != null)
                                || (io.pointOnCorner(event.getX(), event.getY(), OperateConstants.LEFTBOTTOM) && io.getSettingBm() != null)
                                || (io.pointOnCorner(event.getX(), event.getY(), OperateConstants.LEFTCENTER) && io.getLeftBm() != null)
                                || (io.pointOnCorner(event.getX(), event.getY(), OperateConstants.TOPCENTER) && io.getTopBm() != null)
                                || (io.pointOnCorner(event.getX(), event.getY(), OperateConstants.RIGHTCENTER) && io.getRightBm() != null)
                                || (io.pointOnCorner(event.getX(), event.getY(), OperateConstants.BOTTOMCENTER) && io.getBottomBm() != null)) {
                            io.setSelected(true);
                            imgLists.remove(i);
                            imgLists.add(io);
                            selectedId = imgLists.size() - 1;
                            break;
                        }
                    }
                }
                for(int i = 0; i < imgLists.size(); ++i) {
                    ImageObject io = imgLists.get(i);
                    if(i != selectedId) {
                        io.setSelected(false);
                    }
                }

                ImageObject io = getSelected();
                if(io != null) {
                    if(io.pointOnCorner(event.getX(), event.getY(), OperateConstants.RIGHTTOP) && io.getDeleteBm() != null) {
                        imgLists.remove(io);
                    } else if(io.pointOnCorner(event.getX(), event.getY(), OperateConstants.RIGHTBOTTOM) && io.getRotateBm() != null) {
                        mResizeAndRotateSinceDown = true;
                        float x = event.getX();
                        float y = event.getY();
                        float delX = x - io.getPoint().x;
                        float delY = y - io.getPoint().y;
                        diff = (float) Math.sqrt((delX * delX + delY * delY));
                        mStartDistance = diff;
                        mPrevRot = (float) Math.toDegrees(Math.atan2(delX, delY));
                        mStartScale = io.getScale();
                        mStartRot = io.getRotation();
                    } else if(io.pointOnCorner(event.getX(), event.getY(), OperateConstants.LEFTTOP) && io.getFlipBm() != null) {
                        io.horizontalFlip();
                    } else if(io.pointOnCorner(event.getX(), event.getY(), OperateConstants.LEFTBOTTOM) && io.getSettingBm() != null) {
                        iSettingListener.showSettingBar();
                    } else if((io.pointOnCorner(event.getX(), event.getY(), OperateConstants.LEFTCENTER) && io.getLeftBm() != null)
                            || (io.pointOnCorner(event.getX(), event.getY(), OperateConstants.RIGHTCENTER) && io.getRightBm() != null)) {
                        mResizeXSinceDown = true;
                        float x = event.getX();
                        float y = event.getY();
                        float delX = x - io.getPoint().x;
                        float delY = y - io.getPoint().y;
                        diff = (float) Math.sqrt((delX * delX + delY * delY));
                        mStartDistance = diff;
                        mStartScale = io.getScaleX();
                    } else if((io.pointOnCorner(event.getX(), event.getY(), OperateConstants.TOPCENTER) && io.getTopBm() != null)
                            || (io.pointOnCorner(event.getX(), event.getY(), OperateConstants.BOTTOMCENTER) && io.getBottomBm() != null)) {
                        mResizeYSinceDown = true;
                        float x = event.getX();
                        float y = event.getY();
                        float delX = x - io.getPoint().x;
                        float delY = y - io.getPoint().y;
                        diff = (float) Math.sqrt((delX * delX + delY * delY));
                        mStartDistance = diff;
                        mStartScale = io.getScaleY();
                    } else if (io.contains(event.getX(), event.getY())) {
                        mMovedSinceDown = true;
                        mPreviousPos.x = (int) event.getX();
                        mPreviousPos.y = (int) event.getY();
                    }
                }
                break;

            case MotionEvent.ACTION_UP :
                mMovedSinceDown = false;
                mResizeAndRotateSinceDown = false;
                mResizeXSinceDown = false;
                mResizeYSinceDown = false;
                break;

            case MotionEvent.ACTION_MOVE :
                io = getSelected();
                if(io == null) break;
                //移动
                if(mMovedSinceDown) {
                    int curX = (int) event.getX();
                    int curY = (int) event.getY();
                    int diffX = curX - mPreviousPos.x;
                    int diffY = curY - mPreviousPos.y;
                    mPreviousPos.x = curX;
                    mPreviousPos.y = curY;
                    Point p = io.getPosition();
                    if (p.x + diffX >= mCanvasLimits.left
                            && p.x + diffX <= mCanvasLimits.right
                            && p.y + diffY >= mCanvasLimits.top
                            && p.y + diffY <= mCanvasLimits.bottom)
                        io.moveBy(diffX, diffY);
                }
                //旋转和缩放
                if(mResizeAndRotateSinceDown) {
                    float x = event.getX();
                    float y = event.getY();
                    float delX = x - io.getPoint().x;
                    float delY = y - io.getPoint().y;
                    diff = (float) Math.sqrt((delX * delX + delY * delY));
                    float scale = diff / mStartDistance;
                    float newscale = mStartScale * scale;
                    rot = (float) Math.toDegrees(Math.atan2(delX, delY));
                    float rotdiff = mPrevRot - rot;
                    if(newscale < 10.0f && newscale > 0.1f) {
                        float newrot = Math.round((mStartRot + rotdiff) / 1.0f);
                        if (Math.abs((newscale - io.getScale()) * ROTATION_STEP) > Math.abs(newrot - io.getRotation())) {
                            io.setScale(newscale);
                        } else {
                            io.setRotation(newrot % 360);
                        }
                    }
                }
                //拉伸
                if(mResizeXSinceDown) {
                    float x = event.getX();
                    float y = event.getY();
                    float delX = x - io.getPoint().x;
                    float delY = y - io.getPoint().y;
                    diff = (float) Math.sqrt((delX * delX + delY * delY));
                    float scale = diff / mStartDistance;
                    float newscale = mStartScale * scale;
                    if(newscale < 10.0f && newscale > 0.1f) {
                        io.setScale(newscale, io.getScaleY());
                    }
                }
                if(mResizeYSinceDown) {
                    float x = event.getX();
                    float y = event.getY();
                    float delX = x - io.getPoint().x;
                    float delY = y - io.getPoint().y;
                    diff = (float) Math.sqrt((delX * delX + delY * delY));
                    float scale = diff / mStartDistance;
                    float newscale = mStartScale * scale;
                    if(newscale < 10.0f && newscale > 0.1f) {
                        io.setScale(io.getScaleX(), newscale);
                    }
                }
                break;
        }

        cancelLongPress();
    }

    private void handlerScaleGesture(MotionEvent event) {
        float pointerX = 0,pointerY = 0;
        int pointerCount = event.getPointerCount();
        //计算多个触摸点的平均值
        for(int i = 0; i < pointerCount; i++){
            pointerX += event.getX(i);
            pointerY += event.getY(i);
        }
        pointerX = pointerX / pointerCount;
        pointerY = pointerY / pointerCount;
        if(lastPointerCount != pointerCount){
            mLastX = pointerX;
            mLastY = pointerY;
            isCanDrag = false;
            lastPointerCount = pointerCount;
        }

        ImageObject io = getSelected();
        if(io == null && mMultiTouchType == TYPE_MULTI_TOUCH_2 && event.getAction() == MotionEvent.ACTION_DOWN) {
            mMovedImageSinceDown = true;
        }

        if(mMovedImageSinceDown) {
            switch (event.getAction()) {
                case MotionEvent.ACTION_MOVE:
                    //仅仅在放大的状态，图片才可移动
                    if(mImageRect.width() > mInitImageRect.width()){
                        int dx = (int) (pointerX - mLastX);
                        int dy = (int) (pointerY - mLastY);
                        if(!isCanDrag)
                            isCanDrag = isCanDrag(dx, dy);
                        if(isCanDrag) {
                            if (mImageRect.left + dx > mInitImageRect.left)
                                dx = mInitImageRect.left - mImageRect.left;
                            if (mImageRect.right + dx < mInitImageRect.right)
                                dx = mInitImageRect.right - mImageRect.right;
                            if (mImageRect.top + dy > mInitImageRect.top)
                                dy = mInitImageRect.top - mImageRect.top;
                            if (mImageRect.bottom + dy < mInitImageRect.bottom)
                                dy = mInitImageRect.bottom - mImageRect.bottom;
                            mImageRect.offset(dx, dy);
                            //更新贴纸位置
                            updateObjectPosition(dx, dy);
                        }
                    }
                    mLastX = pointerX;
                    mLastY = pointerY;
                    invalidate();
                    break;
                case MotionEvent.ACTION_UP:
                    mMovedImageSinceDown = false;
                    lastPointerCount = 0;
                    break;
            }
        }
    }

    @Override
    public boolean onScale(ScaleGestureDetector detector) {
        float scale = detector.getScaleFactor();
        mScaleFactor *= scale;
        if(mScaleFactor < 1.0f) mScaleFactor = 1.0f;
        if(mScaleFactor > 3.0f) mScaleFactor = 3.0f;

        if(mImageRect != null){
            int addWidth =(int) (mInitImageRect.width() * mScaleFactor) - mImageRect.width ();
            int addHeight=(int) (mInitImageRect.height() * mScaleFactor) - mImageRect.height();
            float centerWidthRatio = (detector.getFocusX() - mImageRect.left) / mImageRect.width();
            float centerHeightRatio = (detector.getFocusY() - mImageRect.left) / mImageRect.height();

            int leftAdd = (int) (addWidth * centerWidthRatio);
            int topAdd = (int) (addHeight * centerHeightRatio);

            mImageRect.left =  mImageRect.left - leftAdd;
            mImageRect.right = mImageRect.right + (addWidth - leftAdd);
            mImageRect.top = mImageRect.top - topAdd;
            mImageRect.bottom = mImageRect.bottom + (addHeight - topAdd);
            checkCenterWhenScale();
            updateObjectScale();
        }

        invalidate();
        return true;
    }

    private void checkCenterWhenScale() {
        int deltaX = 0;
        int deltaY = 0;
        if(mImageRect.left > mInitImageRect.left) {
            deltaX = mInitImageRect.left - mImageRect.left;
        }
        if(mImageRect.right < mInitImageRect.right) {
            deltaX = mInitImageRect.right - mImageRect.right;
        }
        if(mImageRect.top > mInitImageRect.top) {
            deltaY = mInitImageRect.top - mImageRect.top;
        }
        if(mImageRect.bottom < mInitImageRect.bottom) {
            deltaY = mInitImageRect.bottom - mImageRect.bottom;
        }
        mImageRect.offset(deltaX,deltaY);
    }

    @Override
    public boolean onScaleBegin(ScaleGestureDetector detector) {
        return true;
    }

    @Override
    public void onScaleEnd(ScaleGestureDetector detector) {}

    private boolean isCanDrag(int dx, int dy) {
        return Math.sqrt((dx * dx) + (dy * dy)) >= 5.0f;
    }

    /**
     * 获取选中的对象ImageObject
     */
    private ImageObject getSelected() {
        for(ImageObject obj : imgLists) {
            if(obj.isSelected()) {
                return obj;
            }
        }
        return null;
    }

    private void updateObjectScale() {
        for(ImageObject obj : imgLists) {
            if(obj != null) obj.setScaleZoom(mScaleFactor);
        }
    }

    private void updateObjectPosition(int x, int y) {
        for(ImageObject obj : imgLists) {
            obj.moveBy(x, y);
        }
    }

    private boolean isInObjectArea(MotionEvent event) {
        for(int i = imgLists.size() - 1; i >= 0; --i) {
            ImageObject io = imgLists.get(i);
            if(io.contains(event.getX(), event.getY())
                    || (io.pointOnCorner(event.getX(), event.getY(), OperateConstants.RIGHTBOTTOM) && io.getRotateBm() != null)
                    || (io.pointOnCorner(event.getX(), event.getY(), OperateConstants.LEFTTOP) && io.getFlipBm() != null)
                    || (io.pointOnCorner(event.getX(), event.getY(), OperateConstants.RIGHTTOP) && io.getDeleteBm() != null)
                    || (io.pointOnCorner(event.getX(), event.getY(), OperateConstants.LEFTBOTTOM) && io.getSettingBm() != null)
                    || (io.pointOnCorner(event.getX(), event.getY(), OperateConstants.LEFTCENTER) && io.getLeftBm() != null)
                    || (io.pointOnCorner(event.getX(), event.getY(), OperateConstants.TOPCENTER) && io.getTopBm() != null)
                    || (io.pointOnCorner(event.getX(), event.getY(), OperateConstants.RIGHTCENTER) && io.getRightBm() != null)
                    || (io.pointOnCorner(event.getX(), event.getY(), OperateConstants.BOTTOMCENTER) && io.getBottomBm() != null)) {
                return true;
            }
        }
        return false;
    }

    /**
     * 向外部提供双击监听事件（双击弹出自定义对话框编辑文字）
     */
    MyListener myListener;

    public void setOnListener(MyListener myListener) {
        this.myListener = myListener;
    }

    public interface MyListener {
        public void onClick(TextObject tObject);
    }

    /**
     * 监听触摸事件
     */
    private OnOperateTouchListener mTouchListener;

    public void setOnOperateViewTouchListener(OnOperateTouchListener listener) {
        mTouchListener = listener;
    }

    public interface OnOperateTouchListener {
        boolean onOperateViewTouch(MotionEvent event);
    }

    /**
     * 设置透明度
     */
    public void setStickerTransparency(int progress){
        ImageObject io = getSelected();
        if (io != null) {
            io.setTransparency(progress);
            invalidate();
        }
    }

    public int getStickerTransparency() {
        ImageObject io = getSelected();
        if (io != null) {
            return io.getTransparencyProgress();
        }
        return 0;
    }
}
