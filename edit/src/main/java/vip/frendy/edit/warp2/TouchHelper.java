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

    public TouchHelper() {
        //触点
        touchCirclePaint = new Paint();
        touchCirclePaint.setStyle(Paint.Style.STROKE);
        touchCirclePaint.setStrokeWidth(5);
        touchCirclePaint.setColor(Color.parseColor("#d75372"));

        touchCrossPaint = new Paint();
        touchCrossPaint.setStyle(Paint.Style.STROKE);
        touchCrossPaint.setStrokeWidth(5);
        touchCrossPaint.setColor(Color.parseColor("#d75372"));

        //虚线效果
        DashPathEffect pathEffect = new DashPathEffect(new float[] { 20, 10 }, 1);

        touchCirclePaint2 = new Paint();
        touchCirclePaint2.setStyle(Paint.Style.STROKE);
        touchCirclePaint2.setStrokeWidth(5);
        touchCirclePaint2.setColor(Color.parseColor("#ffffff"));
        touchCirclePaint2.setPathEffect(pathEffect);

        touchLinePaint = new Paint();
        touchLinePaint.setStyle(Paint.Style.STROKE);
        touchLinePaint.setStrokeWidth(5);
        touchLinePaint.setColor(Color.parseColor("#ffffff"));
        touchLinePaint.setPathEffect(pathEffect);

        touchCrossPaint2 = new Paint();
        touchCrossPaint2.setStyle(Paint.Style.STROKE);
        touchCrossPaint2.setStrokeWidth(5);
        touchCrossPaint2.setColor(Color.parseColor("#ffffff"));
    }

    public void onDraw(Canvas canvas) {
        if(enableTouchCircle && showTouchCircle && touchCirclePaint != null) {
            Path path = new Path();
            path.moveTo(startX, startY);
            path.lineTo(touchX, touchY);
            canvas.drawPath(path, touchLinePaint);

            Path pathCross1 = new Path();
            pathCross1.moveTo(startX - touchCrossR, startY);
            pathCross1.lineTo(startX + touchCrossR, startY);
            pathCross1.moveTo(startX, startY - touchCrossR);
            pathCross1.lineTo(startX, startY + touchCrossR);
            canvas.drawPath(pathCross1, touchCrossPaint);
            canvas.drawCircle(startX, startY, touchCircleR, touchCirclePaint);

            Path pathCross2 = new Path();
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
