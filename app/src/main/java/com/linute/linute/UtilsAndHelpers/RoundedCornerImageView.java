package com.linute.linute.UtilsAndHelpers;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapShader;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Rect;
import android.graphics.RectF;
import android.graphics.Shader;
import android.util.AttributeSet;
import android.util.Log;
import android.widget.ImageView;

/**
 * Created by mikhail on 7/19/16.
 */
public class RoundedCornerImageView extends ImageView {

    Bitmap mBitmap;

    public RoundedCornerImageView(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public RoundedCornerImageView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
    }

    public RoundedCornerImageView(Context context) {
        super(context);
    }

    @Override
    public void setImageBitmap(Bitmap bm) {
//        super.setImageBitmap(bm);
        Log.i("AAA","bmp set");
        mBitmap = bm;
        mShader = new BitmapShader(mBitmap, Shader.TileMode.CLAMP, Shader.TileMode.CLAMP);
        mPaint.setShader(mShader);
        invalidate();
    }

    Rect mDrawingRect = new Rect();
    RectF mBounds = new RectF();

    Paint mPaint = new Paint();
    Shader mShader;

    @Override
    protected void onDraw(Canvas canvas) {
        getDrawingRect(mDrawingRect);
        mBounds.set(mDrawingRect);
        canvas.drawRoundRect(mBounds, 16, 16, mPaint);
    }
}
