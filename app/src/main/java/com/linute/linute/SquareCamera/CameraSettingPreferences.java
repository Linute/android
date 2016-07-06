package com.linute.linute.SquareCamera;

import android.content.Context;
import android.content.SharedPreferences;
import android.support.annotation.NonNull;

/**
 * Created by desmond on 4/10/15.
 */
public class CameraSettingPreferences {

    private static final String FLASH_ON = "squarecamera__flash_on";

    private static SharedPreferences getCameraSettingPreferences(@NonNull final Context context) {
        return context.getSharedPreferences("com.desmond.squarecamera", Context.MODE_PRIVATE);
    }

    public static void saveCameraFlashMode(@NonNull final Context context, final boolean mFlashOn) {
        final SharedPreferences preferences = getCameraSettingPreferences(context);

        if (preferences != null) {
            final SharedPreferences.Editor editor = preferences.edit();
            editor.putBoolean(FLASH_ON, mFlashOn);
            editor.apply();
        }
    }

    public static boolean getCameraFlashMode(@NonNull final Context context) {
        final SharedPreferences preferences = getCameraSettingPreferences(context);

        return preferences != null && preferences.getBoolean(FLASH_ON, false);
    }
}
