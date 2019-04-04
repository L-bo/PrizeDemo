package com.example.prizedemo.view;

import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Matrix;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.Point;
import android.graphics.PorterDuff;
import android.graphics.PorterDuffXfermode;
import android.graphics.Rect;
import android.graphics.Typeface;
import android.support.annotation.Nullable;
import android.text.TextPaint;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.View;

import com.example.prizedemo.R;
import com.example.prizedemo.utils.Constant;
import com.example.prizedemo.utils.ViewUtils;

/**
 * @作者 liubo
 * @创建时间 2019/4/4 12:07 AM
 * 注: 实现刮刮奖效果
 */
public class PrizeView extends View {
    private Paint mPaint;
    // 背景
    private Context mContext;
    // 默认的宽高
    private int mDefaultWidth, mDefaultHeight;
    // 橡皮擦,用来刮开涂层
    private Paint mEraserPaint;
    private TextPaint mTextPaint;
    private Path mPath;
    private float mX, mY;
    private static final float TOUCH_TOLERANCE = 4;
    private Canvas mCanvas;
    private int mViewWidth;
    private int mViewHeight;
    private Bitmap mBgBitmap, mFgBitmap;
    private Point mCenterPoint;
    private volatile boolean isCompleted = false;//是否已经完成，共享内存，保持多线程的可见性
    // 文字内容
    private String mTextContent;
    // 文字大小
    private float mTextSize;
    // 文字颜色
    private int mTextColor;
    // 背景图
    private int mBagImage;
    // 覆盖层颜色
    private int mCoverColor;
    // 是否文字加粗
    private boolean mTextType;

    public PrizeView(Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
        mContext = context;
        mDefaultWidth = ViewUtils.dipToPx(context, Constant.DEFAULT_WIDTH_SIZE);
        mDefaultHeight = ViewUtils.dipToPx(context, Constant.DEFAULT_HEIGHT_SIZE);
        initAttrs(attrs);
        init();
    }

    private void initAttrs(AttributeSet attrs) {
        TypedArray typedArray = mContext.obtainStyledAttributes(attrs, R.styleable.PrizeView);
        mTextContent = typedArray.getString(R.styleable.PrizeView_prize_text_content);
        mTextSize = typedArray.getDimension(R.styleable.PrizeView_prize_text_size, 0);
        mTextColor = typedArray.getColor(R.styleable.PrizeView_prize_text_color, Color.BLACK);
        mBagImage = typedArray.getResourceId(R.styleable.PrizeView_prize_bag_image, R.mipmap.back);
        mTextType = typedArray.getBoolean(R.styleable.PrizeView_prize_text_type, false);
        mCoverColor = typedArray.getColor(R.styleable.PrizeView_prize_cover_color, Color.BLACK);
        typedArray.recycle();
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        super.onMeasure(widthMeasureSpec, heightMeasureSpec);
        // 获取宽高,
        setMeasuredDimension(ViewUtils.measure(widthMeasureSpec, mDefaultWidth)
                , ViewUtils.measure(heightMeasureSpec, mDefaultHeight));
    }

    private void init() {
        mPath = new Path();
        mCenterPoint = new Point();
        mPaint = new Paint();
        // 透明度 不设置为255 即可
        mPaint.setAlpha(0);
        mPaint.setXfermode(new PorterDuffXfermode(PorterDuff.Mode.SRC_IN));
        mPaint.setStyle(Paint.Style.STROKE);
        // 设置结合处的形状为圆角
        mPaint.setStrokeJoin(Paint.Join.ROUND);
        mPaint.setStrokeWidth(50);
        // 设置结尾处的形状为圆角
        mPaint.setStrokeCap(Paint.Cap.ROUND);

        mTextPaint = new TextPaint();
        mTextPaint.setAntiAlias(true);
        mTextPaint.setTextSize(ViewUtils.dipToPx(mContext, mTextSize));
        mTextPaint.setColor(mTextColor);
        // 设置文字加粗
        if (mTextType) {
            mTextPaint.setTypeface(Typeface.DEFAULT_BOLD);
        }
        mTextPaint.setTextAlign(Paint.Align.CENTER);
        //橡皮擦
        mEraserPaint = new Paint();
        mEraserPaint.setAlpha(0);
        //这个属性是设置paint为橡皮擦重中之重
        //这是重点
        //下面这句代码是橡皮擦设置的重点
        mEraserPaint.setXfermode(new PorterDuffXfermode(PorterDuff.Mode.DST_IN));
        mEraserPaint.setAntiAlias(true);
        mEraserPaint.setDither(true);
        mEraserPaint.setStyle(Paint.Style.STROKE);
        mEraserPaint.setStrokeCap(Paint.Cap.ROUND);
        mEraserPaint.setStrokeWidth(30);
    }

    @Override
    protected void onSizeChanged(int w, int h, int oldw, int oldh) {
        super.onSizeChanged(w, h, oldw, oldh);
        mViewWidth = getWidth();
        mViewHeight = getHeight();
        //获取圆的相关参数
        mCenterPoint.x = w / 2;
        mCenterPoint.y = h / 2;
        // 背景图片也可以用作奖品展示
        mBgBitmap = BitmapFactory.decodeResource(getResources(), mBagImage);
        // 遮盖层
        mFgBitmap = Bitmap.createBitmap(getWidth(), getHeight(), Bitmap.Config.ARGB_8888);
        // bitmap的宽高和view的宽高也许不是等比例的, 这里计算需要缩放的比例
        float scaleWidth = mFgBitmap.getWidth() * 1.0f / mBgBitmap.getWidth();
        float scaleHeight = mFgBitmap.getHeight() * 1.0f / mBgBitmap.getHeight();
        // 为了保证图片能够等比例缩放, 而不是宽/高会被拉伸, 这里要取得相对小的那个值
        float scale = Math.min(scaleWidth, scaleHeight);
        // 通过矩阵进行缩放
        Matrix matrix = new Matrix();
        matrix.postScale(scale, scale);
        // 得到新的图片
        mBgBitmap = Bitmap.createBitmap(mBgBitmap, 0, 0, mBgBitmap.getWidth(), mBgBitmap.getHeight(), matrix, true);
        mCanvas = new Canvas(mFgBitmap);
        mCanvas.drawColor(mCoverColor);

    }

    private float getBaselineOffsetFromY(Paint paint) {
        return ViewUtils.measureTextHeight(paint) / 2;
    }

    @Override
    protected void onDraw(Canvas canvas) {
        // 第二个矩形区域表示bitmap要绘制的区域
//        @SuppressLint("DrawAllocation") Rect bgBitmapStartRect = new Rect(mLeft, mTop,
//                mBgBitmap.getWidth() + mLeft, mBgBitmap.getHeight() + mTop);
        Rect bgBtimapRect = new Rect(0, 0, mViewWidth, mViewHeight);
        canvas.drawBitmap(mBgBitmap, bgBtimapRect, bgBtimapRect, null);
        drawText(canvas);
        super.onDraw(canvas);
        //如果没有刮完的话，需要绘制用户touch的轨迹，以及刮涂层
        if (!isCompleted) {
            drawPath();
            canvas.drawBitmap(mFgBitmap, 0, 0, null);
        }
        mCanvas.drawPath(mPath, mPaint);
    }

    // 绘制文字
    private void drawText(Canvas canvas) {
        canvas.drawText(mTextContent, mCenterPoint.x, mCenterPoint.y + getBaselineOffsetFromY(mTextPaint), mTextPaint);
    }

    private void touch_start(float x, float y) {
        mPath.reset();
        mPath.moveTo(x, y);
        mX = x;
        mY = y;
        mCanvas.drawPath(mPath, mEraserPaint);
    }

    private void touch_move(float x, float y) {
        float dx = Math.abs(x - mX);
        float dy = Math.abs(y - mY);
        if (dx >= TOUCH_TOLERANCE || dy >= TOUCH_TOLERANCE) {
            //源代码是这样写的，可是我没有弄明白，为什么要这样？
            mPath.quadTo(mX, mY, (x + mX) / 2, (y + mY) / 2);
            mX = x;
            mY = y;
            mCanvas.drawPath(mPath, mEraserPaint);
        }
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        float x = event.getX();
        float y = event.getY();
        switch (event.getAction()) {
            case MotionEvent.ACTION_DOWN:
                mPath = new Path();
                touch_start(x, y);
                invalidate(); //清屏
                break;
            case MotionEvent.ACTION_MOVE:
                touch_move(x, y);
                invalidate();
                break;
            case MotionEvent.ACTION_UP:
                //计算已经刮完的像素
                if (!isCompleted)
                    new Thread(mRunnable).start();
                break;
        }
        if (!isCompleted)
            invalidate();
        return true;
    }

    /**
     * 绘制用户手指刮过的路径
     */
    private void drawPath() {
        mEraserPaint.setStyle(Paint.Style.STROKE);
        mEraserPaint.setXfermode(new PorterDuffXfermode(PorterDuff.Mode.DST_OUT));
        mCanvas.drawPath(mPath, mEraserPaint);
    }

    private Runnable mRunnable = new Runnable()
    {
        @Override
        public void run() {
            int w = getWidth();
            int h = getHeight();

            float wipeArea = 0;
            float totalArea = w * h;
            Bitmap bitmap = mFgBitmap;
            int[] mPixels = new int[w * h];

            bitmap.getPixels(mPixels, 0, w, 0, 0, w, h);
            //计算被擦除的区域（也就是像素值为0）的像素数之和
            for (int i = 0; i < w; i++) {
                for (int j = 0; j < h; j++) {
                    int index = i + j * w;
                    if (mPixels[index] == 0) {
                        wipeArea++;
                    }
                }
            }
            //计算擦除的像素数与总像素数的百分比
            if (wipeArea > 0 && totalArea > 0) {
                int percent = (int) (wipeArea * 100 / totalArea);
                if (percent > 60) {
                    isCompleted = true;
                    postInvalidate();
                }
            }
        }
    };



    public Paint getPaint() {
        return mPaint;
    }

    public void setPaint(Paint paint) {
        mPaint = paint;
    }

    public String getTextContent() {
        return mTextContent;
    }

    public void setTextContent(String textContent) {
        mTextContent = textContent;
    }

    public float getTextSize() {
        return mTextSize;
    }

    public void setTextSize(float textSize) {
        mTextSize = textSize;
    }

    public int getTextColor() {
        return mTextColor;
    }

    public void setTextColor(int textColor) {
        mTextColor = textColor;
    }

    public int getBagImage() {
        return mBagImage;
    }

    public void setBagImage(int bagImage) {
        mBagImage = bagImage;
    }

    public int getCoverColor() {
        return mCoverColor;
    }

    public void setCoverColor(int coverColor) {
        mCoverColor = coverColor;
    }

    public boolean isTextType() {
        return mTextType;
    }

    public void setTextType(boolean textType) {
        mTextType = textType;
    }
}
