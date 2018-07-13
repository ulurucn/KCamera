package vip.frendy.edit.mosaic;

import android.content.Context;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.Bitmap.Config;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.CornerPathEffect;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.PorterDuff;
import android.graphics.PorterDuffXfermode;
import android.graphics.Rect;
import android.util.AttributeSet;
import android.util.Log;
import android.util.TypedValue;
import android.view.MotionEvent;
import android.view.ScaleGestureDetector;
import android.view.ViewGroup;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

/**
 * 马赛克
 * 
 * @author frendy
 * 
 *         使用说明：
 *         1.布局中引用view mosaic = (DrawMosaicView) findViewById(R.id.mosaic);
 *         2.设置所要打马赛克的图片 mosaic.setMosaicBackgroundResource(mBitmap);
 *         3.设置马赛克资源样式 mosaic.setMosaicResource(bit);
 *         4.设置绘画粗细 mosaic.setMosaicBrushWidth(10);
 *         5.设置马赛克类型(马赛克，橡皮擦) mosaic.setMosaicType(MosaicType.ERASER);
 */

public class ScaleMosaicView extends ViewGroup implements ScaleGestureDetector.OnScaleGestureListener {
	public static final String TAG = "MosaicView";

	// default image inner padding, in dip pixels
	private static final int INNER_PADDING = 0;

	/**
	 * 马赛克粗细 可以按 5，10，15，20, 30
	 **/
	private static final int PATH_WIDTH = 30;

	/**
	 * 绘画板宽度
	 */
	private int mImageWidth;

	/**
	 * 绘画板高度
	 */
	private int mImageHeight;

	/**
	 * 绘画底层图片资源
	 */
	private Bitmap bmBaseLayer;

	/**
	 * 橡皮擦图层
	 *
	 */
	private Bitmap bmCoverLayer;

	private Bitmap bmMosaicLayer;

	/**
	 * 画笔
	 */
	private int mBrushWidth;

	private Rect mImageRect;

	private Paint mPaint;

	private int mPadding;

	/**
	 * 触摸路径数据
	 */
	private List<MosaicPath> touchPaths;
	//回退操作保存
	private List<MosaicPath> cachePaths;

	private MosaicPath touchPath;

	//是否显示触点圈圈
	private boolean enableTouchCircle = true;
	private boolean showTouchCircle = false;
	private float touchX, touchY;
	//触点圈圈半径
	private int touchCircleR = 80;
	//触点圈圈
	private Paint touchCirclePaint;

	/**
	 * 马赛克类型 Mosaic: 打码 erase: 橡皮擦
	 */
	private MosaicUtil.MosaicType mMosaicType = MosaicUtil.MosaicType.MOSAIC;

	private Context mContext;

	private OnPathMosaicUpdatedListener mUpdatedListener;

	/**
	 * 缩放相关属性
	 */
	private ScaleGestureDetector mScaleGestureDetector;
	private float scaleFactor = 1.0f;
	private Rect mInitImageRect;
	//触点
	private boolean isMultiPointer = false;
	private float mLastX;
	private float mLastY;
	private int lastPointerCount = 0;
	private boolean isCanDrag;
	//防误触相关变量
	private long lastCheckDrawTime = 0;
	private boolean isCanDrawPath = false;


	public ScaleMosaicView(Context context) {
		super(context);
		this.mContext = context;
		initDrawView();
	}

	public ScaleMosaicView(Context context, AttributeSet attrs) {
		super(context, attrs);
		this.mContext = context;
		initDrawView();
	}

	/**
	 * 初始化绘画板 默认的情况下是马赛克模式
	 */
	private void initDrawView() {
		touchPaths = new ArrayList<>();
		cachePaths = new ArrayList<>();

		mPadding = dp2px(INNER_PADDING);
		mBrushWidth = dp2px(PATH_WIDTH);

		mPaint = new Paint();
		mPaint.setAntiAlias(true);
		mPaint.setStyle(Paint.Style.STROKE);
		mPaint.setStrokeWidth(6);
		mPaint.setColor(0xff2a5caa);
		mImageRect = new Rect();
		mInitImageRect = new Rect();
		setWillNotDraw(false);
		setMosaicType(MosaicUtil.MosaicType.MOSAIC);

		touchCirclePaint = new Paint();
		touchCirclePaint.setStyle(Paint.Style.STROKE);
		touchCirclePaint.setStrokeWidth(5);
		touchCirclePaint.setColor(Color.parseColor("#ffffff"));

		mScaleGestureDetector = new ScaleGestureDetector(mContext, this);
	}

	/**
	 * 设置画刷的宽度
	 * @param brushWidth 画刷宽度大小
	 */
	public void setMosaicBrushWidth(int brushWidth) {
		this.mBrushWidth = dp2px(brushWidth);
	}

	/**
	 * 设置马赛克类型
	 * @param type 类型
	 */
	public void setMosaicType(MosaicUtil.MosaicType type) {
		this.mMosaicType = type;
	}

	/**
	 * 设置所要打码的图片资源
	 * @param imgPath 图片路径
	 */
	public void setMosaicBackgroundResource(String imgPath) {
		File file = new File(imgPath);
		if (file == null || !file.exists()) {
			Log.w(TAG, "setSrcPath invalid file path " + imgPath);
			return;
		}

		reset();

		Bitmap bitmap = BitmapFactory.decodeFile(imgPath);
		mImageWidth = bitmap.getWidth();
		mImageHeight = bitmap.getHeight();
		bmBaseLayer = bitmap;
		requestLayout();
		invalidate();
	}

	/**
	 * 设置马赛克样式资源
	 * @param imgPath 样式图片路径
	 */
	public void setMosaicResource(String imgPath) {

		File file = new File(imgPath);
		if (file == null || !file.exists()) {
			Log.w(TAG, "setSrcPath invalid file path " + imgPath);
			setMosaicType(MosaicUtil.MosaicType.ERASER);
			return;
		}

		Bitmap bitmap = BitmapFactory.decodeFile(imgPath);
		if (bitmap != null) {
			setMosaicType(MosaicUtil.MosaicType.MOSAIC);

			if (bmCoverLayer != null)
			{
				bmCoverLayer.recycle();
			}
			bmCoverLayer = bitmap;
		} else {
			Log.i("jarlen", " setMosaicResource bitmap = null ");
			return;
		}

		updatePathMosaic();
		invalidate();
	}

	/**
	 * 设置所要打码的资源图片
	 * @param bitmap 资源图片路径
	 */
	public void setMosaicBackgroundResource(Bitmap bitmap) {
		if (bitmap == null) {
			Log.e("jarlen", "setMosaicBackgroundResource : bitmap == null");
			return;
		}

		reset();
		mImageWidth = bitmap.getWidth();
		mImageHeight = bitmap.getHeight();

		bmBaseLayer = bitmap;

		requestLayout();
		invalidate();
	}

	/**
	 * 设置马赛克样式资源
	 * @param bitmap 样式图片资源
	 */
	public void setMosaicResource(Bitmap bitmap) {
		setMosaicType(MosaicUtil.MosaicType.MOSAIC);

		if (bmCoverLayer != null) {
			bmCoverLayer.recycle();
		}
		touchPaths.clear();
		cachePaths.clear();

		bmCoverLayer = getBitmap(bitmap);
		updatePathMosaic();

		invalidate();
	}

	private Bitmap getBitmap(Bitmap bit) {
		Bitmap bitmap = Bitmap.createBitmap(mImageWidth, mImageHeight, Config.ARGB_8888);
		Canvas canvas = new Canvas(bitmap);
		canvas.drawBitmap(bit, 0, 0, null);
		canvas.save();
		return bitmap;
	}

	//是否显示触点圈圈
	public void enableTouchCircle(boolean enable) {
		enableTouchCircle = enable;
	}

	//主动显示触点圈圈
	public void showTouchCircle(boolean show) {
		touchX = getWidth() / 2;
		touchY = getHeight() / 2;
		showTouchCircle = show;
		invalidate();
	}

	//设置触点圈圈颜色
	public void setTouchCirclePaintColor(int color) {
		if(touchCirclePaint != null) {
			touchCirclePaint.setColor(color);
			invalidate();
		}
	}

	//设置触点圈圈粗细
	public void setTouchCirclePaintStrokeWidth(float width) {
		if(touchCirclePaint != null) {
			touchCirclePaint.setStrokeWidth(width);
			invalidate();
		}
	}

	//设置触点圈圈半径
	public void setTouchCircleR(int r) {
		touchCircleR = r;
		invalidate();
	}

	/**
	 * 更新画板
	 */
	public void update() {
		updatePathMosaic();
		invalidate();
	}

	public void backward() {
		if(touchPaths == null || cachePaths == null) return;
		if(touchPaths.size() <= 0) return;

		MosaicPath lastPath = touchPaths.get(touchPaths.size() - 1);
		touchPaths.remove(lastPath);
		cachePaths.add(lastPath);

		update();
	}

	public void forward() {
		if(touchPaths == null || cachePaths == null) return;
		if(cachePaths.size() <= 0) return;

		MosaicPath lastPath = cachePaths.get(cachePaths.size() - 1);
		touchPaths.add(lastPath);
		cachePaths.remove(lastPath);

		update();
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

	/**
	 * 清除绘画数据
	 */
	public void clear() {
		touchPaths.clear();
		cachePaths.clear();

		if (bmMosaicLayer != null) {
			bmMosaicLayer.recycle();
			bmMosaicLayer = null;
		}

		invalidate();
	}

	/**
	 * 重置绘画版
	 * @return
	 */
	public boolean reset() {
		this.mImageWidth = 0;
		this.mImageHeight = 0;
		if (bmCoverLayer != null) {
			bmCoverLayer.recycle();
			bmCoverLayer = null;
		}
		if (bmBaseLayer != null) {
			bmBaseLayer.recycle();
			bmBaseLayer = null;
		}
		if (bmMosaicLayer != null) {
			bmMosaicLayer.recycle();
			bmMosaicLayer = null;
		}

		touchPaths.clear();
		cachePaths.clear();
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
			touchPath = new MosaicPath();
			touchPath.drawPath = new Path();
			touchPath.drawPath.moveTo(x, y);
			touchPath.paintWidth = mBrushWidth;
			touchPath.type = mMosaicType;

			if(event.getPointerCount() <= 1) {
				touchPaths.add(touchPath);
				//回调
				if(touchPath.type == MosaicUtil.MosaicType.ERASER && mUpdatedListener != null) {
					mUpdatedListener.OnPathEraserApplyed();
				}
			}
		} else if(event.getAction() == MotionEvent.ACTION_MOVE) {
			if(isCanDrawPath) {
				if(touchPath != null && touchPath.drawPath != null)
					touchPath.drawPath.lineTo(x, y);

				updatePathMosaic();
				invalidate();
			}
		} else if(event.getAction() == MotionEvent.ACTION_UP) {
			isCanDrawPath = false;
			lastCheckDrawTime = 0;
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

	/**
	 * 刷新绘画板
	 */
	private void updatePathMosaic() {
		if(mImageWidth <= 0 || mImageHeight <= 0) {
			return;
		}

		if(bmMosaicLayer != null) {
			bmMosaicLayer.recycle();
		}
		bmMosaicLayer = Bitmap.createBitmap(mImageWidth, mImageHeight, Config.ARGB_8888);

		Bitmap bmTouchLayer = Bitmap.createBitmap(mImageWidth, mImageHeight, Config.ARGB_8888);

		Paint paint = new Paint(Paint.ANTI_ALIAS_FLAG);
		paint.setStyle(Paint.Style.STROKE);
		paint.setAntiAlias(true);
		paint.setStrokeJoin(Paint.Join.ROUND);
		paint.setStrokeCap(Paint.Cap.ROUND);
		paint.setPathEffect(new CornerPathEffect(10));
		paint.setStrokeWidth(mBrushWidth);
		paint.setColor(Color.BLUE);

		Paint paintEraser = new Paint(Paint.ANTI_ALIAS_FLAG);
		paintEraser.setStyle(Paint.Style.STROKE);
		paintEraser.setAntiAlias(true);
		paintEraser.setStrokeJoin(Paint.Join.ROUND);
		paintEraser.setStrokeCap(Paint.Cap.ROUND);
		paintEraser.setPathEffect(new CornerPathEffect(10));
		paintEraser.setStrokeWidth(mBrushWidth);
		paintEraser.setColor(Color.TRANSPARENT);
		paintEraser.setXfermode(new PorterDuffXfermode(PorterDuff.Mode.CLEAR));

		Canvas canvas = new Canvas(bmTouchLayer);

		for (MosaicPath path : touchPaths) {
			Path pathTemp = path.drawPath;
			int drawWidth = path.paintWidth;
			paint.setStrokeWidth(drawWidth);

			if(path.type == MosaicUtil.MosaicType.MOSAIC) {
				canvas.drawPath(pathTemp, paint);
			} else {
				canvas.drawPath(pathTemp, paintEraser);
			}
		}

		canvas.setBitmap(bmMosaicLayer);
		canvas.drawARGB(0, 0, 0, 0);
		canvas.drawBitmap(bmCoverLayer, 0, 0, null);

		paint.reset();
		paint.setAntiAlias(true);
		paint.setXfermode(new PorterDuffXfermode(PorterDuff.Mode.DST_IN));
		canvas.drawBitmap(bmTouchLayer, 0, 0, paint);
		paint.setXfermode(null);
		canvas.save();

		bmTouchLayer.recycle();

		//更新回调
		if(mUpdatedListener != null)
			mUpdatedListener.OnPathMosaicUpdated();
	}

	@Override
	public void onDraw(Canvas canvas) {
		super.onDraw(canvas);

		if(bmBaseLayer != null) {
			canvas.drawBitmap(bmBaseLayer, null, mImageRect, null);
		}
		if(bmMosaicLayer != null) {
			canvas.drawBitmap(bmMosaicLayer, null, mImageRect, null);
		}
		if(enableTouchCircle && showTouchCircle && touchCirclePaint != null) {
			canvas.drawCircle(touchX, touchY, touchCircleR, touchCirclePaint);
		}
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

	/**
	 * 返回马赛克最终结果
	 * @return 马赛克最终结果
	 */
	public Bitmap getMosaicBitmap() {
		if (bmMosaicLayer == null) {
			return null;
		}

		Bitmap bitmap = Bitmap.createBitmap(mImageWidth, mImageHeight,
				Config.ARGB_8888);
		Canvas canvas = new Canvas(bitmap);
		canvas.drawBitmap(bmBaseLayer, 0, 0, null);
		canvas.drawBitmap(bmMosaicLayer, 0, 0, null);
		canvas.save();
		return bitmap;
	}

	private int dp2px(int dip) {
		Context context = this.getContext();
		Resources resources = context.getResources();
		int px = Math.round(TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, dip, resources.getDisplayMetrics()));
		return px;
	}

	private boolean isCanDrag(int dx, int dy) {
		return Math.sqrt((dx*dx)+(dy*dy)) >= 5.0f;
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
			int addHeight=(int) (mInitImageRect.height()*scaleFactor) - mImageRect.height();
			float centerWidthRatio = (detector.getFocusX()-mImageRect.left)/mImageRect.width();
			float centerHeightRatio = (detector.getFocusY() - mImageRect.left)/mImageRect.height();

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

	@Override
	public void onScaleEnd(ScaleGestureDetector detector) {}

	/**
	 * 设置画板更新监听
	 * @param listener
	 */
	public void setOnPathMosaicUpdatedListener(OnPathMosaicUpdatedListener listener) {
		mUpdatedListener = listener;
	}

	public interface OnPathMosaicUpdatedListener {
		void OnPathMosaicUpdated();
		void OnPathEraserApplyed();
	}
}
