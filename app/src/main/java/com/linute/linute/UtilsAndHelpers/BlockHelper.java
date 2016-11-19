package com.linute.linute.UtilsAndHelpers;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;

import com.linute.linute.API.API_Methods;
import com.linute.linute.MainContent.DiscoverFragment.Post;
import com.linute.linute.MainContent.FeedDetailFragment.Comment;
import com.linute.linute.Socket.TaptSocket;

import org.json.JSONException;
import org.json.JSONObject;

/**
 * Created by mikhail on 11/19/16.
 */

public class BlockHelper {
    public static AlertDialog blockUserFromCommentDialog(Context context, final Comment comment){
        return new AlertDialog.Builder(context)
                .setTitle( "Block "+ (comment.isAnon() ? "this Anon" : comment.getCommentUserName()) + "?")
                .setMessage("You will not see any posts or comments from this user")
                .setNegativeButton("Cancel", null)
                .setPositiveButton("Block", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        if(comment.isAnon()){
                            try {
                                JSONObject object = new JSONObject();
                                object.put("block", true);
                                object.put("comment", comment.getCommentPostId());
                                TaptSocket.getInstance().emit(API_Methods.VERSION +":users:block:anonymous", object);
                            } catch (JSONException e) {
                                e.printStackTrace();
                            }
                        }else{
                            try {
                                JSONObject object = new JSONObject();
                                object.put("block", true);
                                object.put("user", comment.getCommentUserId());
                                TaptSocket.getInstance().emit(API_Methods.VERSION + ":users:block:real", object);
                            } catch (JSONException e) {
                                e.printStackTrace();
                            }
                        }
                    }
                }).create();
    }

    public static AlertDialog blockUserFromPostDialog(Context context, final Post post){
        return new AlertDialog.Builder(context)
                .setTitle( "Block "+ (post.getPrivacy() == 1 ? "this Anon" : post.getUserName()) + "?")
                .setMessage("You will not see any posts or comments from this user")
                .setNegativeButton("Cancel", null)
                .setPositiveButton("Block", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        if(post.getPrivacy() == 1){
                            try {
                                JSONObject object = new JSONObject();
                                object.put("block", true);
                                object.put("post", post.getId());
                                TaptSocket.getInstance().emit(API_Methods.VERSION +":users:block:anonymous", object);
                            } catch (JSONException e) {
                                e.printStackTrace();
                            }
                        }else{
                            try {
                                JSONObject object = new JSONObject();
                                object.put("block", true);
                                object.put("user", post.getUserId());
                                TaptSocket.getInstance().emit(API_Methods.VERSION + ":users:block:real", object);
                            } catch (JSONException e) {
                                e.printStackTrace();
                            }
                        }
                    }
                }).create();
    }
}
