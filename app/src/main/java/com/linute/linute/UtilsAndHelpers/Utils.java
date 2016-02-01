package com.linute.linute.UtilsAndHelpers;

import android.content.Context;
import android.content.SharedPreferences;
import android.content.res.TypedArray;
import android.graphics.Bitmap;
import android.net.ConnectivityManager;
import android.text.format.DateUtils;
import android.util.Base64;
import android.util.Log;
import android.widget.Toast;

import com.linute.linute.R;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.ByteArrayOutputStream;
import java.io.UnsupportedEncodingException;
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.TimeZone;

import io.socket.emitter.Emitter;

/**
 * Created by QiFeng on 11/28/15.
 */
public class Utils {
    public static String CONTENT_TYPE = "application/json";

    //encodes input String
    //returns empty if can't encode (should never happen)
    public static String encode_base64(String input) {
        try {
            return Base64.encodeToString(input.getBytes("UTF-8"), Base64.NO_WRAP);
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
            return "";
        }
    }

    public static String encodeImageBase64(Bitmap bitmap) {
        ByteArrayOutputStream stream = new ByteArrayOutputStream();
        bitmap.compress(Bitmap.CompressFormat.JPEG, 60, stream); //NOTE: Change Compression as needed
        byte[] byteFormat = stream.toByteArray();
        // get the base 64 string
        return Base64.encodeToString(byteFormat, Base64.NO_WRAP);
        //return Base64.encodeToString(byteFormat, Base64.URL_SAFE);
    }


    public static boolean isNetworkAvailable(final Context context) {
        final ConnectivityManager connectivityManager = ((ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE));
        return connectivityManager.getActiveNetworkInfo() != null && connectivityManager.getActiveNetworkInfo().isConnected();
    }

    public static String getTimeZone() {
        return TimeZone.getDefault().getID();
    }

    //toast saying bad connection
    public static void showBadConnectionToast(Context context) {
        Toast.makeText(context, R.string.bad_connection_text, Toast.LENGTH_SHORT).show();
    }

    //problem occured communicating with server
    public static void showServerErrorToast(Context context) {
        Toast.makeText(context, R.string.error_communicating_server, Toast.LENGTH_SHORT).show();
    }

    public static void showSavedToast(Context context) {
        Toast.makeText(context, R.string.data_saved, Toast.LENGTH_SHORT).show();
    }

    public static void testLog(Context context, String TAG) {
        //Test
        SharedPreferences sharedPreferences = context.getSharedPreferences(LinuteConstants.SHARED_PREF_NAME, Context.MODE_PRIVATE);
        Log.v(TAG, "image: " + sharedPreferences.getString("profileImage", "nothing"));
        Log.v(TAG, "uID: " + sharedPreferences.getString("userID", "nothing"));
        Log.v(TAG, "first name: " + sharedPreferences.getString("firstName", "nothing"));
        Log.v(TAG, "last name: " + sharedPreferences.getString("lastName", "nothing"));
        Log.v(TAG, "email: " + sharedPreferences.getString("email", "nothing"));
        Log.v(TAG, "status: " + sharedPreferences.getString("status", "nothing"));
        Log.v(TAG, "facebook: " + sharedPreferences.getString("socialFacebook", "nothing"));
        Log.v(TAG, "logged in: " + (sharedPreferences.getBoolean("isLoggedIn", false) ? "true" : "false"));
        Log.v(TAG, "college name: " + sharedPreferences.getString("collegeName", "no college"));
        Log.v(TAG, "college id: " + sharedPreferences.getString("collegeId", "no college"));
    }

    //clears user information
    //NOTE: RESET OTHER THINGS WHEN THEY COME UP
    public static void resetUserInformation(SharedPreferences.Editor pref) {
        pref.putString("profileImage", null);
        pref.putString("userID", null);
        pref.putString("firstName", null);
        pref.putString("lastName", null);
        pref.putString("email", null);
        pref.putString("status", null);
        pref.putString("socialFacebook", null);
        pref.putString("dob", null);

        pref.putString("fbToken", null);

        pref.putInt("sex", 0);

        pref.putBoolean("isLoggedIn", false);

        pref.putInt("posts", 0);
        pref.putInt("followers", 0);
        pref.putInt("following", 0);
        pref.putString("collegeName", null);
        pref.putString("collegeId", null);
        pref.putString("campus", null);
        pref.putString("passwordFacebook", null);
        pref.commit();
    }

    public static void deleteTempSharedPreference(SharedPreferences.Editor temp) {
        temp.putString("tempCode", null);
        temp.putString("tempPhone", null);



        temp.putString("userID", null);
        temp.putString("password", null);
        temp.putString("socialFacebook", null);
        temp.putInt("sex", 0);
        temp.putString("dob", null);
        temp.putString("registrationType", null);
        temp.putString("profileImage", null);
        temp.putString("firstName", null);
        temp.putString("lastName", null);
        temp.putString("passwordFacebook", null);
        temp.putString("email", null);


        temp.apply();
    }

    public static String getEventImageURL(String jpegName) {
        return "http://images.linute.com/events/original/" + jpegName;
    }

    //return url to a profile image of user
    public static String getImageUrlOfUser(String userImage) {
        return "http://images.linute.com/profiles/original/" + userImage;
    }


    public static String formatDateToReadableString(String date) {
        SimpleDateFormat fm = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'");
        try {
            return SimpleDateFormat.getDateInstance().format(fm.parse(date));
        } catch (ParseException e) {
            e.printStackTrace();
            return "";
        }
    }

    public static int getToolbarHeight(Context context) {
        final TypedArray styledAttributes = context.getTheme().obtainStyledAttributes(
                new int[]{R.attr.actionBarSize});
        int toolbarHeight = (int) styledAttributes.getDimension(0, 0);
        styledAttributes.recycle();

        return toolbarHeight;
    }

    //returns a nicely formated string about when event occurred
    public static String getTimeAgoString(long beforeTime) {
        if (beforeTime == 0) return "";
        DateFormat df = DateFormat.getTimeInstance();
        df.setTimeZone(TimeZone.getTimeZone("gmt"));


        String date = DateUtils.getRelativeTimeSpanString(beforeTime , getUTCdatetimeAsDate().getTime(), DateUtils.SECOND_IN_MILLIS).toString();
        String abrev = getAbrev(date);
        return date.replaceAll("\\D+","") + abrev;
    }

    private static String getAbrev(String date){
        if (date.contains("second")){
            return "s";
        }

        if (date.contains("min")){
            return "m";
        }

        if (date.contains("hour")){
            return "h";
        }

        if (date.contains("day")){
            return "d";
        }
        if (date.contains("week")){
            return "w";
        }
        if (date.contains("month")){
            return "mon";
        }

        if (date.contains("year")){
            return "y";
        }

        return "-";
    }





    //returns millisecond of date
    public static long getTimeFromString(String date) {
        SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'");
        try {
            return dateFormat.parse(date).getTime();
        } catch (ParseException e) {
            e.printStackTrace();
            return 0;
        }

    }


    static final String DATEFORMAT = "yyyy-MM-dd HH:mm:ss";

    public static Date getUTCdatetimeAsDate()
    {
        return StringDateToDate(GetUTCdatetimeAsString());
    }

    public static String GetUTCdatetimeAsString()
    {
        final SimpleDateFormat sdf = new SimpleDateFormat(DATEFORMAT);
        sdf.setTimeZone(TimeZone.getTimeZone("UTC"));

        return sdf.format(new Date());
    }

    public static Date StringDateToDate(String StrDate)
    {
        Date dateToReturn = null;
        SimpleDateFormat dateFormat = new SimpleDateFormat(DATEFORMAT);

        try
        {
            dateToReturn = (Date)dateFormat.parse(StrDate);
        }
        catch (ParseException e)
        {
            e.printStackTrace();
        }

        return dateToReturn;
    }
}
