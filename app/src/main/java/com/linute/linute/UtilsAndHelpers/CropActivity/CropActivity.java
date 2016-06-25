package com.linute.linute.UtilsAndHelpers.CropActivity;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.MenuItem;
import android.view.View;

import com.linute.linute.R;
import com.lyft.android.scissors.CropView;

import java.io.File;
import java.util.Date;

import rx.Observable;
import rx.Subscription;
import rx.functions.Action1;

import static android.graphics.Bitmap.CompressFormat.JPEG;
import static rx.android.schedulers.AndroidSchedulers.mainThread;
import static rx.schedulers.Schedulers.io;

/**
 * Created by QiFeng on 6/17/16.
 */
public class CropActivity extends AppCompatActivity {

    private CropView vCropView;
    private Subscription mSubscription;
    public static final String IMAGE_URI = "image";


    /**
     * needs an intent
     * key: IMAGE_URI
     */
    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_crop);

        vCropView = (CropView) findViewById(R.id.crop_image);
        Toolbar t = (Toolbar) findViewById(R.id.toolbar);

        if (t != null) {
            t.inflateMenu(R.menu.crop_menu);
            t.setNavigationOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    setResult(RESULT_CANCELED);
                    finish();
                }
            });

            t.setOnMenuItemClickListener(new Toolbar.OnMenuItemClickListener() {
                @Override
                public boolean onMenuItemClick(MenuItem item) {
                    if (item.getItemId() == R.id.check) {
                        cropView();
                        return true;
                    }
                    return false;
                }
            });
        }

        Uri image = getIntent().getParcelableExtra(IMAGE_URI);


        if (image != null) {
            vCropView.extensions().load(image);
        }
    }


    private void cropView() {
        final File croppedFile = new File(getCacheDir(), new Date().getTime()+"");

        mSubscription = Observable
                .from(vCropView.extensions()
                        .crop()
                        .quality(100)
                        .format(JPEG)
                        .into(croppedFile))
                .subscribeOn(io())
                .observeOn(mainThread())
                .subscribe(new Action1<Void>() {
                    @Override
                    public void call(Void nothing) {
                        Intent i = new Intent();
                        i.setData(Uri.fromFile(croppedFile));
                        setResult(RESULT_OK, i);
                        finish();
                    }
                });
    }


    @Override
    public void onBackPressed() {
        setResult(RESULT_CANCELED);
        super.onBackPressed();
    }


    @Override
    protected void onPause() {
        super.onPause();
        if (mSubscription != null) mSubscription.unsubscribe();
    }
}
