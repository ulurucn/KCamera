package vip.frendy.edit.operate;

import android.graphics.Bitmap;
import android.graphics.Bitmap.Config;
import android.graphics.BitmapFactory;
import android.graphics.BitmapRegionDecoder;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Matrix;
import android.graphics.Paint;
import android.graphics.Point;
import android.graphics.PointF;

import java.util.ArrayList;
import java.util.List;

import vip.frendy.base.BitmapExt;

/**
 * @author jarlen
 * @modified frendy
 */
public class ImageObject {
	public String tag;
	protected Point mPoint = new Point();
	protected float mRotation;
	protected float mScale = 1.0f, mScaleX = 1.0f, mScaleY = 1.0f, mScaleZoom = 1.0f;
	protected boolean mSelected, mSelectedDrawed = false;
	protected boolean flipVertical;
	protected boolean flipHorizontal;
	protected final int resizeBoxSize = 100;
	protected boolean isTextObject;
	protected Bitmap srcBm, _srcBm;
	protected Bitmap rotateBm, deleteBm, flipBm, settingBm;
	protected Bitmap leftBm, rightBm, topBm, bottomBm;
	protected Paint paint = new Paint();
	protected int mTransparencyProgress = 0;

	private Canvas canvas = null;

	/**
	 * 构造方法
	 */
	public ImageObject() {}

	public ImageObject(String text) {}

	/**
	 * 构造方法
	 * @param srcBm 源图片
	 * @param rotateBm 	旋转图片
	 * @param deleteBm	删除图片
	 * @param flipBm	翻转图片
	 * @param settingBm	设置图片
	 */
	public ImageObject(Bitmap srcBm, Bitmap rotateBm, Bitmap deleteBm, Bitmap flipBm, Bitmap settingBm) {
		this.srcBm = Bitmap.createBitmap(srcBm.getWidth(), srcBm.getHeight(), Config.ARGB_8888);
		_srcBm = Bitmap.createBitmap(srcBm);
		canvas = new Canvas(this.srcBm);
		canvas.drawBitmap(srcBm, 0, 0, paint);
		this.rotateBm = rotateBm;
		this.deleteBm = deleteBm;
		this.flipBm = flipBm;
		this.settingBm = settingBm;
		paint.setColor(Color.WHITE);
		paint.setAntiAlias(true); //去掉边缘锯齿
		paint.setStrokeWidth(2); //设置线宽
	}

	/**
	 * 构造方法
	 * @param srcBm	源图片
	 * @param x 图片初始化x坐标
	 * @param y	图片初始化y坐标
	 * @param rotateBm	旋转图片
	 * @param deleteBm 	删除图片
	 * @param flipBm	翻转图片
	 * @param settingBm	设置图片
	 */
	public ImageObject(Bitmap srcBm, int x, int y, Bitmap rotateBm, Bitmap deleteBm,Bitmap flipBm,Bitmap settingBm) {
		this.srcBm = Bitmap.createBitmap(srcBm.getWidth(), srcBm.getHeight(), Config.ARGB_8888);
		_srcBm = Bitmap.createBitmap(srcBm);
		canvas = new Canvas(this.srcBm);
		canvas.drawBitmap(srcBm, 0, 0, paint);
		mPoint.x = x;
		mPoint.y = y;
		this.rotateBm = rotateBm;
		this.deleteBm = deleteBm;
		this.flipBm = flipBm;
		this.settingBm = settingBm;
		paint.setColor(Color.WHITE);
		paint.setAntiAlias(true); //去掉边缘锯齿
		paint.setStrokeWidth(2); //设置线宽
	}

	public void setOpBitmap(Bitmap left, Bitmap top, Bitmap right, Bitmap bottom) {
		leftBm = left;
		topBm = top;
		rightBm = right;
		bottomBm = bottom;
	}

	public void setPoint(Point mPoint) {
		setCenter();
	}

	/**
	 * 获取显示图片的宽
	 */
	public int getWidth() {
		if (srcBm != null)
			return srcBm.getWidth();
		else
			return 0;
	}
	/**
	 * 获取显示图片的高
	 */
	public int getHeight() {
		if (srcBm != null)
			return srcBm.getHeight();
		else
			return 0;
	}

	public void moveBy(int x, int y) {
		mPoint.x += x;
		mPoint.y += y;
		setCenter();
	}

	public void draw(Canvas canvas) {
		int sc = canvas.save();
		try {
			canvas.translate(mPoint.x, mPoint.y);
			canvas.scale(mScale, mScale);
			canvas.scale(mScaleX, mScaleY);
			canvas.scale(mScaleZoom, mScaleZoom);
			int sc2 = canvas.save();
			canvas.rotate(mRotation);
			canvas.scale((flipHorizontal ? -1 : 1), (flipVertical ? -1 : 1));
			canvas.drawBitmap(srcBm, -getWidth() / 2, -getHeight() / 2, paint);
			canvas.restoreToCount(sc2);
		} catch (Exception e) {
			e.printStackTrace();
		}
		canvas.restoreToCount(sc);
	}

	/**
	 * 判断点是否在多边形内
	 * @param pointx
	 * @param pointy
	 * @return
	 */
	public boolean contains(float pointx, float pointy) {
		Lasso lasso = null;
		List<PointF> listPoints = new ArrayList<PointF>();
		listPoints.add(getPointLeftTop());
		listPoints.add(getPointRightTop());
		listPoints.add(getPointRightBottom());
		listPoints.add(getPointLeftBottom());
		lasso = new Lasso(listPoints);
		return lasso.contains(pointx, pointy);
	}

	/**
	 * 获取矩形图片左上角的点
	 */
	protected PointF getPointLeftTop() {
		return getPointByRotation(centerRotation - 180);
	}

	/**
	 * 获取矩形图片左上角在画布中的点
	 */
	protected PointF getPointLeftTopInCanvas() {
		return getPointByRotationInCanvas(centerRotation - 180);
	}

	/**
	 * 获取矩形图片右上角的点
	 */
	protected PointF getPointRightTop() {
		return getPointByRotation(-centerRotation);
	}

	/**
	 * 获取矩形图片右上角在画布中的点
	 */
	protected PointF getPointRightTopInCanvas() {
		return getPointByRotationInCanvas(-centerRotation);
	}

	/**
	 * 获取矩形图片右下角的点
	 */
	protected PointF getPointRightBottom() {
		return getPointByRotation(centerRotation);
	}

	/**
	 * 获取矩形图片右下角在画布中的点
	 */
	protected PointF getPointRightBottomInCanvas() {
		return getPointByRotationInCanvas(centerRotation);
	}

	/**
	 * 获取矩形图片左下角的点
	 */
	protected PointF getPointLeftBottom() {
		return getPointByRotation(-centerRotation + 180);
	}

	/**
	 * 获取矩形图片左下角在画布中的点
	 */
	protected PointF getPointLeftBottomInCanvas() {
		return getPointByRotationInCanvas(-centerRotation + 180);
	}

	/**
	 * 获取矩形图片边上中点
	 */
	protected PointF getPointEdgeLeftCenter() {
		PointF top = getPointLeftTop();
		PointF bottom = getPointLeftBottom();
		PointF center = new PointF();
		center.x = (top.x + bottom.x) / 2;
		center.y = (top.y + bottom.y) / 2;
		return center;
	}

	protected PointF getPointEdgeTopCenter() {
		PointF left = getPointLeftTop();
		PointF right = getPointRightTop();
		PointF center = new PointF();
		center.x = (left.x + right.x) / 2;
		center.y = (left.y + right.y) / 2;
		return center;
	}

	protected PointF getPointEdgeRightCenter() {
		PointF top = getPointRightTop();
		PointF bottom = getPointRightBottom();
		PointF center = new PointF();
		center.x = (top.x + bottom.x) / 2;
		center.y = (top.y + bottom.y) / 2;
		return center;
	}

	protected PointF getPointEdgeBottomCenter() {
		PointF left = getPointLeftBottom();
		PointF right = getPointRightBottom();
		PointF center = new PointF();
		center.x = (left.x + right.x) / 2;
		center.y = (left.y + right.y) / 2;
		return center;
	}

	/**
	 * 获取缩放和旋转点
	 */
	protected PointF getResizeAndRotatePoint() {
		PointF pointF = new PointF();
		double h = getHeight();
		double w = getWidth();
		double r = (float) Math.sqrt(w * w + h * h) / 2 * mScale;
		double rotatetemp = (float) Math.toDegrees(Math.atan(h / w));
		double rotate = (mRotation + rotatetemp) * Math.PI / 180;
		pointF.x = (float) (r * Math.cos(rotate));
		pointF.y = (float) (r * Math.sin(rotate));
		return pointF;
	}

	/**
	 * 判断点击是否在边角按钮上
	 * @param x 触点的横坐标
	 * @param y 触点得纵坐标
	 * @param type 四角的位置
	 * @return
	 */
	public boolean pointOnCorner(float x, float y, int type) {
		PointF point = null;
		float delX = 0;
		float delY = 0;
		if(OperateConstants.LEFTTOP == type && deleteBm != null) {
			point = getPointLeftTop();
			delX = x - (point.x - deleteBm.getWidth() / 2);
			delY = y - (point.y - deleteBm.getHeight() / 2);
		} else if(OperateConstants.RIGHTBOTTOM == type && rotateBm != null) {
			point = getPointRightBottom();
			delX = x - (point.x + rotateBm.getWidth() / 2);
			delY = y - (point.y + rotateBm.getHeight() / 2);
		} else if(OperateConstants.RIGHTTOP == type && flipBm != null) {
			point = getPointRightTop();
			delX = x - (point.x + flipBm.getWidth() / 2);
			delY = y - (point.y + flipBm.getHeight() / 2);
		} else if(OperateConstants.LEFTBOTTOM == type && settingBm != null) {
			point = getPointLeftBottom();
			delX = x - (point.x - settingBm.getWidth() / 2);
			delY = y - (point.y - settingBm.getHeight() / 2);
		} else if(OperateConstants.LEFTCENTER == type && leftBm != null) {
			point = getPointEdgeLeftCenter();
			delX = x - (point.x - leftBm.getWidth() / 2);
			delY = y - (point.y - leftBm.getHeight() / 2);
		} else if(OperateConstants.TOPCENTER == type && topBm != null) {
			point = getPointEdgeTopCenter();
			delX = x - (point.x - topBm.getWidth() / 2);
			delY = y - (point.y - topBm.getHeight() / 2);
		} else if(OperateConstants.RIGHTCENTER == type && rightBm != null) {
			point = getPointEdgeRightCenter();
			delX = x - (point.x + rightBm.getWidth() / 2);
			delY = y - (point.y + rightBm.getHeight() / 2);
		} else if(OperateConstants.BOTTOMCENTER == type && bottomBm != null) {
			point = getPointEdgeBottomCenter();
			delX = x - (point.x + bottomBm.getWidth() / 2);
			delY = y - (point.y + bottomBm.getHeight() / 2);
		}
		float diff = (float) Math.sqrt((delX * delX + delY * delY));
		if(Math.abs(diff) <= resizeBoxSize) {
			return true;
		}
		return false;
	}

	private float centerRotation;
	private float R;

	/**
	 * 计算中心点的坐标
	 */
	protected void setCenter() {
		double delX = getWidth() * mScale * mScaleX * mScaleZoom / 2;
		double delY = getHeight() * mScale * mScaleY * mScaleZoom / 2;
		R = (float) Math.sqrt((delX * delX + delY * delY));
		centerRotation = (float) Math.toDegrees(Math.atan(delY / delX));
	}

	/**
	 * 根据旋转角度获取定点坐标
	 */
	private PointF getPointByRotation(float rotation) {
		PointF pointF = new PointF();
		double rot = (mRotation + rotation) * Math.PI / 180;
		pointF.x = getPoint().x + (float) (R * Math.cos(rot));
		pointF.y = getPoint().y + (float) (R * Math.sin(rot));
		return pointF;
	}

	/**
	 * 获取矩形图片边上中点
	 */
	private PointF getPonitEdgeCenterByRotation(float rotation, int _r) {
		float r = _r * mScale / 2;
		PointF pointF = new PointF();
		double rot = (mRotation + rotation) * Math.PI / 180;
		pointF.x = getPoint().x + (float) (r * Math.cos(rot));
		pointF.y = getPoint().y + (float) (r * Math.sin(rot));
		return pointF;
	}

	public PointF getPointByRotationInCanvas(float rotation) {
		PointF pointF = new PointF();
		double rot = (mRotation + rotation) * Math.PI / 180;
		pointF.x = (float) (R * Math.cos(rot));
		pointF.y = (float) (R * Math.sin(rot));
		return pointF;
	}

	public void setScale(float scale) {
		if (getWidth() * scale * mScaleX * mScaleZoom >= resizeBoxSize / 2
				&& getHeight() * scale * mScaleY * mScaleZoom >= resizeBoxSize / 2) {
			mScale = scale;
			setCenter();
		}
	}

	public void setScale(float scaleX, float scaleY) {
		if (getWidth() * mScale * scaleX * mScaleZoom >= resizeBoxSize / 2
				&& getHeight() * mScale * scaleY * mScaleZoom >= resizeBoxSize / 2) {
			mScaleX = scaleX;
			mScaleY = scaleY;
			setCenter();
		}
	}

	public void setScaleZoom(float scaleZoom) {
		if (getWidth() * mScale * mScaleX * scaleZoom >= resizeBoxSize / 2
				&& getHeight() * mScale * mScaleY * scaleZoom >= resizeBoxSize / 2) {
			mScaleZoom = scaleZoom;
			setCenter();
		}
	}

	/**
	 * 绘画选中的图标
	 */
	public void drawIcon(Canvas canvas) {
		PointF deletePF = getPointRightTop();
		PointF rotatePF = getPointRightBottom();
		PointF flipPF = getPointLeftTop();
		PointF settingPF = getPointLeftBottom();

		canvas.drawLine(flipPF.x, flipPF.y, deletePF.x, deletePF.y, paint);
		canvas.drawLine(deletePF.x, deletePF.y, rotatePF.x, rotatePF.y, paint);
		canvas.drawLine(rotatePF.x, rotatePF.y, settingPF.x, settingPF.y, paint);
		canvas.drawLine(settingPF.x, settingPF.y, flipPF.x, flipPF.y, paint);

		if(deleteBm != null) {
			canvas.drawBitmap(deleteBm, deletePF.x - deleteBm.getWidth() / 2,
					deletePF.y - deleteBm.getHeight() / 2, paint);
		}
		if(rotateBm != null) {
			canvas.drawBitmap(rotateBm, rotatePF.x - rotateBm.getWidth() / 2,
					rotatePF.y - rotateBm.getHeight() / 2, paint);
		}
		if(flipBm != null) {
			canvas.drawBitmap(flipBm,flipPF.x - flipBm.getWidth() / 2,
					flipPF.y - flipBm.getHeight() / 2, paint);
		}
		if(settingBm != null) {
			canvas.drawBitmap(settingBm,settingPF.x - settingBm.getWidth() / 2,
					settingPF.y - settingBm.getHeight() / 2, paint);
		}

		PointF leftPF = getPointEdgeLeftCenter();
		PointF topPF = getPointEdgeTopCenter();
		PointF rightPF = getPointEdgeRightCenter();
		PointF bottomPF = getPointEdgeBottomCenter();

		if(leftBm != null) {
			canvas.drawBitmap(leftBm,leftPF.x - leftBm.getWidth() / 2,
					leftPF.y - leftBm.getHeight() / 2, paint);
		}
		if(topBm != null) {
			canvas.drawBitmap(topBm,topPF.x - topBm.getWidth() / 2,
					topPF.y - topBm.getHeight() / 2, paint);
		}
		if(rightBm != null) {
			canvas.drawBitmap(rightBm,rightPF.x - rightBm.getWidth() / 2,
					rightPF.y - rightBm.getHeight() / 2, paint);
		}
		if(bottomBm != null) {
			canvas.drawBitmap(bottomBm,bottomPF.x - bottomBm.getWidth() / 2,
					bottomPF.y - bottomBm.getHeight() / 2, paint);
		}
	}

	/**
	 * 水平翻转方法
	 */
	public void horizontalFlip() {
		srcBm = BitmapExt.reverseImage(srcBm, -1, 1);
	}

	/**
	 * 设置透明度
	 */
	public void setTransparency(int progress) {
		mTransparencyProgress = progress;
		int[] argb = new int[_srcBm.getWidth() * _srcBm.getHeight()];

		//获得图片的ARGB值
		_srcBm.getPixels(argb, 0, _srcBm.getWidth(), 0, 0, _srcBm.getWidth(), _srcBm.getHeight());

		for (int i = 0; i < argb.length-1; i++) {
			int a = (argb[i] >> 24) & 0xff;
			a = a * (100 - progress) / 100;

			argb[i] = (a << 24) | (argb[i] & 0x00FFFFFF);
		}

		srcBm = Bitmap.createBitmap(argb, _srcBm.getWidth(), _srcBm.getHeight(), Config.ARGB_8888);
	}

	/**
	 * get、set方法
	 */
	public boolean isSelected() {
		return mSelected;
	}

	public void setSelected(boolean Selected) {
		this.mSelected = Selected;
	}

	public boolean isSelectedDrawed() {
		return mSelectedDrawed;
	}

	public void setSelectedDrawed(boolean drawed) {
		mSelectedDrawed = drawed;
	}

	public boolean isFlipVertical() {
		return flipVertical;
	}

	public void setFlipVertical(boolean flipVertical) {
		this.flipVertical = flipVertical;
	}

	public boolean isFlipHorizontal() {
		return flipHorizontal;
	}

	public void setFlipHorizontal(boolean flipHorizontal) {
		this.flipHorizontal = flipHorizontal;
	}

	public Bitmap getSrcBm() {
		return srcBm;
	}

	public void setSrcBm(Bitmap srcBm) {
		this.srcBm = srcBm;
	}

	public Bitmap getRotateBm() {
		return rotateBm;
	}

	public void setRotateBm(Bitmap rotateBm) {
		this.rotateBm = rotateBm;
	}

	public Bitmap getDeleteBm() {
		return deleteBm;
	}

	public void setDeleteBm(Bitmap deleteBm) {
		this.deleteBm = deleteBm;
	}

    public Bitmap getFlipBm() {
        return flipBm;
    }

    public void setFlipBm(Bitmap flipBm) {
        this.flipBm = flipBm;
    }

    public Bitmap getSettingBm(){
		return settingBm;
	}

	public void setSettingBm(Bitmap settingBm){
		this.settingBm = settingBm;
	}

	public Bitmap getLeftBm() {
		return leftBm;
	}

	public void setLeftBm(Bitmap bitmap) {
		leftBm = bitmap;
	}

	public Bitmap getTopBm() {
		return topBm;
	}

	public void setTopBm(Bitmap bitmap) {
		topBm = bitmap;
	}

	public Bitmap getRightBm() {
		return rightBm;
	}

	public void setRightBm(Bitmap bitmap) {
		rightBm = bitmap;
	}

	public Bitmap getBottomBm() {
		return bottomBm;
	}

	public void setBottomBm(Bitmap bitmap) {
		bottomBm = bitmap;
	}

	public Point getPosition() {
		return mPoint;
	}

	public void setPosition(Point Position) {
		this.mPoint = Position;
	}

	public Point getPoint() {
		return mPoint;
	}

	public float getRotation() {
		return mRotation;
	}

	public void setRotation(float Rotation) {
		this.mRotation = Rotation;
	}

	public float getScale() {
		return mScale;
	}

	public float getScaleX() {
		return mScaleX;
	}

	public float getScaleY() {
		return mScaleY;
	}

	public float getScaleZoom() {
		return mScaleZoom;
	}

	public void setTextObject(boolean isTextObject) {
		this.isTextObject = isTextObject;
	}

	public boolean isTextObject() {
		return isTextObject;
	}

	public int getTransparencyProgress() {
		return mTransparencyProgress;
	}
}
