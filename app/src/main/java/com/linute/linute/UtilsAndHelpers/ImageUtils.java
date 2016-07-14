package com.linute.linute.UtilsAndHelpers;

import android.content.Intent;


public class ImageUtils {

    public static void pickUsing(android.support.v4.app.Fragment fragment, int requestCode){
        fragment.startActivityForResult(
                createChooserIntent(),
                requestCode);
    }

    private static Intent createChooserIntent() {
        Intent intent = new Intent();
        intent.setType("image/*");
        intent.setAction(Intent.ACTION_GET_CONTENT);
        intent.addCategory(Intent.CATEGORY_OPENABLE);

        return Intent.createChooser(intent, null);
    }
}