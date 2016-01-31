package com.linute.linute.MainContent.Chat;

import android.app.Dialog;
import android.app.DialogFragment;
import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.support.v4.app.FragmentTransaction;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.bumptech.glide.signature.StringSignature;
import com.linute.linute.API.API_Methods;
import com.linute.linute.API.LSDKChat;
import com.linute.linute.R;
import com.linute.linute.UtilsAndHelpers.LinuteConstants;
import com.linute.linute.UtilsAndHelpers.Utils;
import com.mikhaellopez.circularimageview.CircularImageView;
import com.squareup.okhttp.Callback;
import com.squareup.okhttp.Request;
import com.squareup.okhttp.Response;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

/**
 * Created by Arman on 1/19/16.
 */
public class NewChatDialog extends DialogFragment {

    private static final String TAG = NewChatDialog.class.getSimpleName();
    private SearchUser mSearchUser;
    private SharedPreferences mSharedPreferences;
    private String mRoomIsEmpty = "";

    public NewChatDialog() {
        // Required empty public constructor
    }

    /**
     * Use this factory method to create a new instance of
     * this fragment using the provided parameters.
     *
     * @return A new instance of fragment PostCreatePage.
     */
    // TODO: Rename and change types and number of parameters
    public static NewChatDialog newInstance(SearchUser user) {
        NewChatDialog fragment = new NewChatDialog();
        Bundle args = new Bundle();
        args.putString("ID", user.getUserId());
        args.putString("IMAGE", user.getUserImage());
        args.putString("NAME", user.getUserName());
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
//        setStyle(DialogFragment.STYLE_NO_TITLE, R.style.NewChatDialog);
        if (getArguments() != null) {
            mSearchUser = new SearchUser(
                    getArguments().getString("ID"),
                    getArguments().getString("IMAGE"),
                    getArguments().getString("NAME"));
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        final View rootView = inflater.inflate(R.layout.fragment_new_chat_dialog, container, false);

        final InputMethodManager imm = (InputMethodManager) getActivity().getSystemService(Context.INPUT_METHOD_SERVICE);
        mSharedPreferences = getActivity().getSharedPreferences(LinuteConstants.SHARED_PREF_NAME, Context.MODE_PRIVATE);

        ImageView cancelChat = (ImageView) rootView.findViewById(R.id.new_chat_check_cancel);
        ImageView submitChat = (ImageView) rootView.findViewById(R.id.new_chat_check_submit);
        CircularImageView userImage = (CircularImageView) rootView.findViewById(R.id.new_chat_user_image);
        TextView userName = (TextView) rootView.findViewById(R.id.new_chat_user_name);
        final EditText userChatMessage = (EditText) rootView.findViewById(R.id.new_chat_text_entry);

        cancelChat.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                getActivity().getFragmentManager().popBackStack();
                dismiss();
            }
        });

        submitChat.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                final String userText = userChatMessage.getText().toString();
                LSDKChat newChat = new LSDKChat(getActivity());
                final Map<String, String> user = new HashMap<>();
                user.put("users[0]", mSharedPreferences.getString("userID", null));
                user.put("users[1]", mSearchUser.getUserId());
                newChat.checkUserConvo(user, new Callback() {
                    @Override
                    public void onFailure(Request request, IOException e) {
                        Log.d(TAG, "onFailureCheckChat: " + request.body().toString());
                        e.printStackTrace();
                    }

                    @Override
                    public void onResponse(Response response) throws IOException {
                        if (!response.isSuccessful()) {
                            Log.d(TAG, "onResponseCheckChat: " + response.body().string());
                        } else {
                            JSONObject jsonObject = null;
                            try {
                                jsonObject = new JSONObject(response.body().string());
                                Log.d(TAG, "onResponseCheckChat: " + jsonObject.toString(4));

                                if (jsonObject.getJSONArray("rooms").length() == 0) {
                                    getActivity().runOnUiThread(new Runnable() {
                                        @Override
                                        public void run() {
                                            LSDKChat newChat = new LSDKChat(getActivity());
                                            Map<String, Object> users = new HashMap<String, Object>();
                                            JSONArray usersArray = new JSONArray();
                                            usersArray.put(mSharedPreferences.getString("userID", null));
                                            usersArray.put(mSearchUser.getUserId());
                                            users.put("users", usersArray);
                                            users.put("text", userText);
                                            newChat.newChat(users, new Callback() {
                                                @Override
                                                public void onFailure(Request request, IOException e) {
                                                    e.printStackTrace();
                                                    Log.d(TAG, "onFailureNewChat: " + request.body().toString());
                                                }

                                                @Override
                                                public void onResponse(Response response) throws IOException {
                                                    if (!response.isSuccessful()) {
                                                        JSONObject jsonObject = null;
                                                        try {
                                                            jsonObject = new JSONObject(response.body().string());
                                                            Log.d(TAG, "onResponseNotSuccessfulNewChat: " + jsonObject.toString(4));
                                                        } catch (JSONException e) {
                                                            e.printStackTrace();
                                                        }
                                                    } else {
//                                                        Log.d(TAG, "onResponse: " + response.body().string());
                                                        JSONObject jsonObject = null;
                                                        String roomId = "";
                                                        String ownerName = "";
                                                        String ownerId = "";
                                                        int usersCount = 1;
                                                        try {
                                                            jsonObject = new JSONObject(response.body().string());
//                                                            Log.d(TAG, "onResponseSuccesfullNewChat: " + jsonObject.toString(4));
                                                            roomId = jsonObject.getString("room");
                                                            ownerName = jsonObject.getJSONObject("owner").getString("fullName");
                                                            ownerId = jsonObject.getJSONObject("owner").getString("id");
//                                                            usersCount = jsonObject.getJSONArray("users").length();
                                                            // uncomment above when server adds user and delete below line
                                                            usersCount = 2;
                                                        } catch (JSONException e) {
                                                            e.printStackTrace();
                                                        }
                                                        // startChat Frag
                                                        final String finalRoomId = roomId;
                                                        final String finalOwnerName = ownerName;
                                                        final String finalOwnerId = ownerId;
                                                        final int finalUsersCount = usersCount;
                                                        getActivity().runOnUiThread(new Runnable() {
                                                            @Override
                                                            public void run() {
                                                                ChatFragment newFragment = ChatFragment.newInstance(
                                                                        finalRoomId, finalOwnerName, finalOwnerId,
                                                                        finalUsersCount, new ArrayList<ChatHead>());
                                                                FragmentTransaction transaction = ((RoomsActivity) getActivity()).getSupportFragmentManager().beginTransaction();
                                                                // Replace whatever is in the fragment_container view with this fragment,
                                                                // and add the transaction to the back stack so the user can navigate back
                                                                transaction.replace(R.id.chat_container, newFragment);
                                                                transaction.addToBackStack(null);
                                                                // Commit the transaction
                                                                ((RoomsActivity) getActivity()).getSupportFragmentManager().popBackStack();
                                                                dismiss();
                                                                ((RoomsActivity) getActivity()).toggleFab(false);
                                                                transaction.commit();
                                                            }
                                                        });
                                                    }
                                                }
                                            });
                                        }
                                    });
                                } else {
                                    getActivity().runOnUiThread(new Runnable() {
                                        @Override
                                        public void run() {
                                            Toast.makeText(getActivity(), "Looks like you've already got this chat going!", Toast.LENGTH_SHORT).show();
                                        }
                                    });
                                }
                            } catch (JSONException e) {
                                e.printStackTrace();
                            }
                        }
                    }
                });
            }
        });

        Glide.with(getActivity())
                .load(Utils.getImageUrlOfUser(mSearchUser.getUserImage()))
                .asBitmap()
                .signature(new StringSignature(mSharedPreferences.getString("imageSigniture", "000")))
                .placeholder(R.drawable.profile_picture_placeholder)
                .diskCacheStrategy(DiskCacheStrategy.RESULT) //only cache the scaled image
                .into(userImage);

        userName.setText(mSearchUser.getUserName());
        return rootView;
    }

    @Override
    public void onStart() {
        super.onStart();
    }

    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        // The only reason you might override this method when using onCreateView() is
        // to modify any dialog characteristics. For example, the dialog includes a
        // title by default, but your custom layout might not need it. So here you can
        // remove the dialog title, but you must call the superclass to get the Dialog.
        Dialog dialog = super.onCreateDialog(savedInstanceState);
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
        return dialog;
    }
}
