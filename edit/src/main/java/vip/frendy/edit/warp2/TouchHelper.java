package vip.frendy.edit.warp2;

import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.DashPathEffect;
import android.graphics.Paint;
import android.graphics.Path;
import android.view.MotionEvent;

/**
 * Created by frendy on 2018/7/17.
 */

public class TouchHelper {

    //是否显示触点圈圈
    private boolean enableTouchCircle = true;
    private boolean showTouchCircle = false;
    private float touchX, touchY, startX, startY;
    //触点圈圈半径
    private int touchCircleR = 60;
    private int touchCrossR = 15;
    //触点圈圈
    private Paint touchCirclePaint;
    private Paint touchCirclePaint2;
    private Paint touchLinePaint;
    private Paint touchCrossPaint;
    private Paint touchCrossPaint2;
    private int strokeWidth = 5;

    private Path path = new Path();
    private Path pathCross1 = new Path();
    private Path pathCross2 = new Path();

    public TouchHelper() {
        //触点
        touchCirclePaint = new Paint();
        touchCirclePaint.setAntiAlias(true);
        touchCirclePaint.setStyle(Paint.Style.STROKE);
        touchCirclePaint.setStrokeWidth(strokeWidth);
        touchCirclePaint.setColor(Color.parseColor("#d75372"));

        touchCrossPaint = new Paint();
        touchCrossPaint.setAntiAlias(true);
        touchCrossPaint.setStyle(Paint.Style.STROKE);
        touchCrossPaint.setStrokeWidth(strokeWidth);
        touchCrossPaint.setColor(Color.parseColor("#d75372"));

        //虚线效果
        DashPathEffect pathEffect = new DashPathEffect(new float[] { 20, 10 }, 1);

        touchCirclePaint2 = new Paint();
        touchCirclePaint2.setAntiAlias(true);
        touchCirclePaint2.setStyle(Paint.Style.STROKE);
        touchCirclePaint2.setStrokeWidth(strokeWidth);
        touchCirclePaint2.setColor(Color.parseColor("#ffffff"));
        touchCirclePaint2.setPathEffect(pathEffect);

        touchLinePaint = new Paint();
        touchLinePaint.setAntiAlias(true);
        touchLinePaint.setStyle(Paint.Style.STROKE);
        touchLinePaint.setStrokeWidth(strokeWidth);
        touchLinePaint.setColor(Color.parseColor("#ffffff"));
        touchLinePaint.setPathEffect(pathEffect);

        touchCrossPaint2 = new Paint();
        touchCrossPaint2.setAntiAlias(true);
        touchCrossPaint2.setStyle(Paint.Style.STROKE);
        touchCrossPaint2.setStrokeWidth(strokeWidth);
        touchCrossPaint2.setColor(Color.parseColor("#ffffff"));
    }

    public void setTouchCirclePaint(Paint paint) {
        touchCirclePaint = paint;
    }

    public void setTouchCirclePaint2(Paint paint) {
        touchCirclePaint2 = paint;
    }

    public void setTouchCrossPaint(Paint paint) {
        touchCrossPaint = paint;
    }

    public void setTouchCrossPaint2(Paint paint) {
        touchCrossPaint2 = paint;
    }

    public void setTouchLinePaint(Paint paint) {
        touchLinePaint = paint;
    }

    public void setStrokeWidth(int width) {
        strokeWidth = width;
        //更新paint
        if(touchCirclePaint != null) touchCirclePaint.setStrokeWidth(strokeWidth);
        if(touchCirclePaint2 != null) touchCirclePaint2.setStrokeWidth(strokeWidth);
        if(touchCrossPaint != null) touchCrossPaint.setStrokeWidth(strokeWidth);
        if(touchCrossPaint2 != null) touchCrossPaint2.setStrokeWidth(strokeWidth);
        if(touchLinePaint != null) touchLinePaint.setStrokeWidth(strokeWidth);
    }

    public void onDraw(Canvas canvas) {
        if(enableTouchCircle && showTouchCircle && touchCirclePaint != null) {
            path.reset();
            path.moveTo(startX, startY);
            path.lineTo(touchX, touchY);
            canvas.drawPath(path, touchLinePaint);

            pathCross1.reset();
            pathCross1.moveTo(startX - touchCrossR, startY);
            pathCross1.lineTo(startX + touchCrossR, startY);
            pathCross1.moveTo(startX, startY - touchCrossR);
            pathCross1.lineTo(startX, startY + touchCrossR);
            canvas.drawPath(pathCross1, touchCrossPaint);
            canvas.drawCircle(startX, startY, touchCircleR, touchCirclePaint);

            pathCross2.reset();
            pathCross2.moveTo(touchX - touchCrossR, touchY);
            pathCross2.lineTo(touchX + touchCrossR, touchY);
            pathCross2.moveTo(touchX, touchY - touchCrossR);
            pathCross2.lineTo(touchX, touchY + touchCrossR);
            canvas.drawPath(pathCross2, touchCrossPaint2);
            canvas.drawCircle(touchX, touchY, touchCircleR, touchCirclePaint2);
        }
    }

    public void onTouchEvent(CanvasView view, MotionEvent event) {
        switch (event.getAction()) {
            case MotionEvent.ACTION_DOWN:
                startX = event.getX();
                startY = event.getY();
                touchX = event.getX();
                touchY = event.getY();
                showTouchCircle = true;
                invalidate(view);
                break;
            case MotionEvent.ACTION_MOVE:
                touchX = event.getX();
                touchY = event.getY();
                break;
            case MotionEvent.ACTION_UP:
                showTouchCircle = false;
                invalidate(view);
                break;
        }
    }

    public void invalidate(CanvasView view) {
        if (view != null) {
            view.invalidate();
        }
    }
}
