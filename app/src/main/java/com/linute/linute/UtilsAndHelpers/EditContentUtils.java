package com.linute.linute.UtilsAndHelpers;

import android.content.Context;
import android.os.Environment;

import com.bumptech.glide.Glide;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.concurrent.ExecutionException;

/**
 * Created by mikhail on 11/18/16.
 */

public class EditContentUtils {

    public static void saveStickers(Context context, JSONObject body) throws JSONException {
        JSONArray memes = body.getJSONArray("memes");

        File memeDir = new File(context.getExternalFilesDir(Environment.DIRECTORY_PICTURES), "memes/");
        memeDir.mkdirs();


        if (memes == null) return;

        for (File f : memeDir.listFiles()) {
            for (int i = 0; i < memes.length(); i++) {
                String fileName = memes.getJSONObject(i).getString("name")+".png";
                if (fileName.equals(f.getName())) break;
                if (i == memes.length() - 1)
                    f.delete();
            }
        }

        for (int i = 0; i < memes.length(); i++) {
            final JSONObject meme = memes.getJSONObject(i);
            final String fileName = meme.getString("name")+".png";
            final String imageUrl = meme.getString("image");
            final File file = new File(memeDir, fileName);
            if (!file.exists()) {
                try {
                    file.createNewFile();
                    File res = Glide.with(context)
                            .load(Utils.getMemeImageUrl(imageUrl))
                            .downloadOnly(200, 200).get();

                    FileOutputStream fos = new FileOutputStream(file);
                    FileInputStream fis = new FileInputStream(res);
                    byte[] buf = new byte[1024];
                    int len;
                    while ((len = fis.read(buf)) > 0) {
                        fos.write(buf, 0, len);
                    }
                    fis.close();
                    fos.close();
                } catch (IOException | InterruptedException | ExecutionException ioe) {
                    ioe.printStackTrace();
                }

            }
        }
    }

    public static void saveFilters(Context context, JSONObject body) throws JSONException {
        JSONArray filters = body.getJSONArray("filters");

        File filtersDir = new File(context.getExternalFilesDir(Environment.DIRECTORY_PICTURES), "filters/");
        filtersDir.mkdirs();

        if (filters == null) return;

        for (File f : filtersDir.listFiles()) {
            for (int i = 0; i < filters.length(); i++) {
                String fileName = filters.getJSONObject(i).getString("name")+".png";
                if (fileName.equals(f.getName())) break;
                if (i == filters.length() - 1)
                    f.delete();
            }
        }

        for (int i = 0; i < filters.length(); i++) {
            final JSONObject filter = filters.getJSONObject(i);
            String fileName = filter.getString("name")+".png";
            String imageUrl = filter.getString("image");

            final File file = new File(filtersDir, fileName);
            if (!file.exists()) {
                try {
                    file.createNewFile();
                    File res = Glide.with(context)
                            .load(Utils.getFilterImageUrl(imageUrl))
                            .downloadOnly(1080, 1920).get();

                    FileOutputStream fos = new FileOutputStream(file);
                    FileInputStream fis = new FileInputStream(res);
                    byte[] buf = new byte[1024];
                    int len;
                    while ((len = fis.read(buf)) > 0) {
                        fos.write(buf, 0, len);
                    }
                    fis.close();
                    fos.close();
                } catch (IOException | ExecutionException ioe) {
                    ioe.printStackTrace();
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }

            }
        }
    }
}
