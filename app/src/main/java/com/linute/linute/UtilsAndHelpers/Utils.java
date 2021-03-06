package com.linute.linute.UtilsAndHelpers;

import android.content.Context;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.ConnectivityManager;
import android.os.Build;
import android.text.format.DateUtils;
import android.util.Base64;
import android.util.Log;
import android.widget.Toast;

import com.bumptech.glide.signature.StringSignature;
import com.linute.linute.R;

import org.apache.commons.io.FileUtils;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.TimeZone;
import java.util.regex.Pattern;


/**
 * Created by QiFeng on 11/28/15.
 */
public class Utils {

    public static final Pattern VALID_EMAIL_ADDRESS_REGEX =
            Pattern.compile("^[A-Z0-9._%+-]+@[A-Z0-9.-]+\\.edu", Pattern.CASE_INSENSITIVE);

    public static String CONTENT_TYPE = "application/json";

    public static SimpleDateFormat getDateFormat(){
        SimpleDateFormat f = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'");
        f.setTimeZone(TimeZone.getTimeZone("UTC"));
        return f;
    }


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

    public static String encodeImageBase64HighRes(Bitmap bitmap){
        ByteArrayOutputStream stream = new ByteArrayOutputStream();
        bitmap.compress(Bitmap.CompressFormat.PNG, 100, stream); //NOTE: Change Compression as needed
        byte[] byteFormat = stream.toByteArray();
        // get the base 64 string
        return Base64.encodeToString(byteFormat, Base64.NO_WRAP);
    }

    public static String encodeFileBase64(File file) throws IOException {
        byte[] filebyte = FileUtils.readFileToByteArray(file);
        return Base64.encodeToString(filebyte, Base64.NO_WRAP);
        //return Base64.encodeToString(byteFormat, Base64.URL_SAFE);
    }

    public static Bitmap decodeImageBase64(String base64){
        if(base64 == null) return null;
        if(base64.contains(","))
            base64 = base64.substring(base64.indexOf(",") + 1);
        byte[] bytes = Base64.decode(base64, Base64.DEFAULT);
        return BitmapFactory.decodeByteArray(bytes,0,bytes.length);
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
        if (context == null) return;
        Toast.makeText(context, R.string.bad_connection_text, Toast.LENGTH_SHORT).show();
    }

    //problem occured communicating with server
    public static void showServerErrorToast(Context context) {
        if (context == null) return;
        Toast.makeText(context, R.string.error_communicating_server, Toast.LENGTH_SHORT).show();
    }

    public static void showSavedToast(Context context) {
        if (context == null) return;
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
        Log.i(TAG, "password: " + sharedPreferences.getString("password", "nothing"));
        Log.v(TAG, "status: " + sharedPreferences.getString("status", "nothing"));
        Log.v(TAG, "facebook: " + sharedPreferences.getString("socialFacebook", "nothing"));
        Log.v(TAG, "logged in: " + (sharedPreferences.getBoolean("isLoggedIn", false) ? "true" : "false"));
        Log.v(TAG, "college name: " + sharedPreferences.getString("collegeName", "no college"));
        Log.v(TAG, "college id: " + sharedPreferences.getString("collegeId", "no college"));
    }

    //clears user information
    //NOTE: RESET OTHER THINGS WHEN THEY COME UP
    public static void resetUserInformation(SharedPreferences pref1) {
        SharedPreferences.Editor pref = pref1.edit();
        pref.putString("profileImage", null);
        pref.putString("userID", null);
        pref.putString("firstName", null);
        pref.putString("lastName", null);
        pref.putString("status", null);
        pref.putString("socialFacebook", null);
        pref.putString("dob", null);

        pref.putString("email", null);
        pref.putString("password", null);

        pref.putInt("sex", 0);

        pref.putBoolean("isLoggedIn", false);

        pref.putInt("posts", 0);
        pref.putInt("followers", 0);
        pref.putInt("following", 0);
        pref.putString("collegeName", null);
        pref.putString("collegeId", null);
        pref.putString("campus", null);

        pref.putString("userToken", null);
        pref.putString("points", "0");
        pref.putString("userName", null);
        pref.putLong("timestamp", 0);
        pref.apply();
    }

    public static void deleteTempSharedPreference(SharedPreferences temp1) {
        SharedPreferences.Editor temp = temp1.edit();
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

    public static String getTrendsImageURL(String jpegName) {
        return "http://images.linute.com/trends/" + jpegName;
    }

    public static String getMessageImageURL(String jpegName) {
        return "http://images.linute.com/messages/original/" + jpegName;
    }

    public static String getMessageVideoURL(String jpegName) {
        return "http://images.linute.com/messages/video/" + jpegName;
    }

    public static String getVideoURL(String videoEnd){
        return "http://images.linute.com/events/video/" + videoEnd;
    }

    //return url to a profile image of user
    public static String getImageUrlOfUser(String userImage) {
        return "http://images.linute.com/profiles/original/" + userImage;
    }

    public static String getAnonImageUrl(String image){
        return "http://images.linute.com/profiles/anonymous/"+image;
    }

    public static String getChatImageUrl(String image){
        return "http://images.linute.com/rooms/original/"+image;
    }

    public static String getChatThumbnailUrl(String image){
        return "http://images.linute.com/rooms/thumbnail/"+image;
    }

    public static String getFilterImageUrl(String image){
        return "http://images.linute.com/filters/"+image;
    }

    public static String getMemeImageUrl(String image){
        return "http://images.linute.com/memes/"+image;
    }

    public static String getCommentImageUrl(String image){
        return "http://images.linute.com/comments/original/"+image;
    }


    public static String formatDateToReadableString(String date) {
        try {
            return SimpleDateFormat.getDateInstance().format(getDateFormat().parse(date));
        } catch (ParseException e) {
            e.printStackTrace();
            return "";
        }
    }

    public static String getRoomDateFormat(long before){
        if (before == 0) return  "";
        SimpleDateFormat simpleDateformat;

        long diff = new Date().getTime() - before;

        if (diff < DateUtils.DAY_IN_MILLIS){
            simpleDateformat = new SimpleDateFormat("h:mm a");
        }else if(diff < DateUtils.WEEK_IN_MILLIS){
            simpleDateformat = new SimpleDateFormat("EEE");
        }else if (diff < DateUtils.YEAR_IN_MILLIS){
            simpleDateformat = new SimpleDateFormat("MMM d");
        }else {
            simpleDateformat = new SimpleDateFormat("YYYY");
        }

        return simpleDateformat.format(new Date(before));
    }

    //returns a nicely formated string about when event occurred
    public static String getTimeAgoString(long beforeTime) {
        if (beforeTime == 0) return "";

        long timeDifference = new Date().getTime() - beforeTime;

        if (timeDifference < 0){ //time less than 0
            return 0 + "s";
        }
        else if (timeDifference > DateUtils.YEAR_IN_MILLIS) //years
            return ((int)(timeDifference / DateUtils.YEAR_IN_MILLIS))+"y";

        //NOTE: skipped months - number of milli in a month changes

        else if (timeDifference > DateUtils.WEEK_IN_MILLIS)
            return ((int) (timeDifference / DateUtils.WEEK_IN_MILLIS))+"w";
        else if (timeDifference > DateUtils.DAY_IN_MILLIS)
            return ((int) (timeDifference / DateUtils.DAY_IN_MILLIS))+"d";
        else if (timeDifference > DateUtils.HOUR_IN_MILLIS)
            return ((int) (timeDifference / DateUtils.HOUR_IN_MILLIS))+"h";
        else if (timeDifference > DateUtils.MINUTE_IN_MILLIS)
            return ((int) (timeDifference / DateUtils.MINUTE_IN_MILLIS)) + "m";
        else
            return ((int) (timeDifference / DateUtils.SECOND_IN_MILLIS))+"s";
    }

    //returns millisecond of date
    public static long getTimeFromString(String date) {
        try {
            return getDateFormat().parse(date).getTime();
        } catch (ParseException e) {
            e.printStackTrace();
            return 0;
        }
    }

    private static StringSignature mGlideSignature;

    public static StringSignature getGlideSignature(Context context){
        if(mGlideSignature == null) {
            mGlideSignature = new StringSignature(context.getSharedPreferences(LinuteConstants.SHARED_PREF_NAME, Context.MODE_PRIVATE).getString("imageSigniture", "000"));
        }
        return mGlideSignature;
    }


    public static String getFBImage(String id){
        return String.format("https://graph.facebook.com/%s/picture?width=720&height=720", id);
    }


    public static String getMyId(Context context){
        return context.getSharedPreferences(LinuteConstants.SHARED_PREF_NAME, Context.MODE_PRIVATE).getString("userID", null);
    }

    public static String stripUnsupportedCharacters(String in){
        /*24 = Nougat, didn't have Build.VERSION_CODE for it*/
        if(Build.VERSION.SDK_INT >= 24 || in == null){
            return in;
        }

        String out = in.replaceAll("[(\\uD83C\\udffb)" +
                "(\\uD83C\\uDFFc)" +
                "(\\uD83C\\uDFFd)" +
                "(\\uD83C\\uDFFe)" +
                "(\\uD83C\\uDFFF)]", "");

        return out;
    }
}
