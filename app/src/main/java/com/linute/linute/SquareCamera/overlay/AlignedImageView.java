package com.linute.linute.SquareCamera.overlay;

import android.content.Context;
import android.graphics.Matrix;
import android.graphics.drawable.Drawable;
import android.util.AttributeSet;
import android.widget.ImageView;

/**
 * Created by mikhail on 6/25/16.
 */
public class AlignedImageView extends ImageView {

    public AlignedImageView(Context context) {
        super(context);
    }

    public AlignedImageView(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public AlignedImageView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
    }

    public void setAlignLeft(boolean alignLeft) {
        this.alignLeft = alignLeft;
    }

    private boolean alignLeft = true;


    @Override
    protected boolean setFrame(int l, int t, int r, int b) {
        boolean hasChanged = super.setFrame(l, t, r, b);
        Matrix matrix = getImageMatrix();
        float scaleFactor = getHeight() / (float) getDrawable().getIntrinsicHeight();
        matrix.setScale(scaleFactor, scaleFactor, 0, 0);

        // The Important Bit
        Drawable drawable = getDrawable();
        float heightD = drawable.getIntrinsicWidth();
        float height = getWidth();
        if(alignLeft) {
            matrix.setTranslate(height - heightD, 0);
        }else{
            matrix.setTranslate(heightD - height, 0);
        }
        setImageMatrix(matrix);
        return hasChanged;
    }
}
