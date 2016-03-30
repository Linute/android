package com.linute.linute.SquareCamera;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;

import com.linute.linute.R;
import com.lyft.android.scissors.CropView;

/**
 * Created by QiFeng on 3/28/16.
 */
public class CropActivity extends AppCompatActivity {

    private CropView mCropView;
    private boolean mImageSelected = false;
    public final int CROP_REQUEST = 5;

    public static final int START_CROP_REQUEST = 2;


    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_crop_image);

        mCropView = (CropView) findViewById(R.id.crop_view_scis);
    }


    @Override
    protected void onResume() {
        super.onResume();
        if (!mImageSelected){
            mImageSelected = true;
            mCropView.extensions().pickUsing(this, CROP_REQUEST);
        }
    }


    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {

        super.onActivityResult(requestCode, resultCode, data);

        if (resultCode == RESULT_CANCELED){
            setResult(RESULT_CANCELED);
            finish();
        }

        else if (resultCode == RESULT_OK && requestCode == CROP_REQUEST){
            if (data != null){
                mCropView.extensions().load(data.getData());
            }
        }
    }
}
