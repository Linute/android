package com.linute.linute.UtilsAndHelpers;

import com.linute.linute.R;

/**
 * Created by QiFeng on 2/7/16.
 */
public class PlaceholderStatuses {

    private static int[] PLACEHOLDER_STATUSES =
            {
                    R.string.placeholder_bio0,
                    R.string.placeholder_bio1,
                    R.string.placeholder_bio2,
                    R.string.placeholder_bio3,
                    R.string.placeholder_bio4,
                    R.string.placeholder_bio5,
                    R.string.placeholder_bio6,
                    R.string.placeholder_bio7,
                    R.string.placeholder_bio8,
                    R.string.placeholder_bio9,
                    R.string.placeholder_bio10,
                    R.string.placeholder_bio11,
                    R.string.placeholder_bio12,
                    R.string.placeholder_bio13,
                    R.string.placeholder_bio14,
                    R.string.placeholder_bio15,
                    R.string.placeholder_bio16,
                    R.string.placeholder_bio17,
                    R.string.placeholder_bio18,
                    R.string.placeholder_bio19,
                    R.string.placeholder_bio20,
                    R.string.placeholder_bio21,
                    R.string.placeholder_bio22,
                    R.string.placeholder_bio23,
                    R.string.placeholder_bio24,
                    R.string.placeholder_bio25,
                    R.string.placeholder_bio26,
                    R.string.placeholder_bio27,
                    R.string.placeholder_bio28,
                    R.string.placeholder_bio29,
                    R.string.placeholder_bio30,
                    R.string.placeholder_bio31,
                    R.string.placeholder_bio32,
                    R.string.placeholder_bio33,
                    R.string.placeholder_bio34,
                    R.string.placeholder_bio35,
                    R.string.placeholder_bio36,
                    R.string.placeholder_bio37,
                    R.string.placeholder_bio38,
                    R.string.placeholder_bio39,
                    R.string.placeholder_bio40
            };


    public static int getRandomStringRes(int index) {
       return PLACEHOLDER_STATUSES[index];
    }
}
