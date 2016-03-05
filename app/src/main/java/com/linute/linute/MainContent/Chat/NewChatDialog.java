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

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.bumptech.glide.signature.StringSignature;
import com.linute.linute.API.LSDKChat;
import com.linute.linute.R;
import com.linute.linute.UtilsAndHelpers.LinuteConstants;
import com.linute.linute.UtilsAndHelpers.Utils;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

import de.hdodenhof.circleimageview.CircleImageView;
import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.Response;

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
        CircleImageView userImage = (CircleImageView) rootView.findViewById(R.id.new_chat_user_image);
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

                if (getActivity() == null) return;
                LSDKChat newChat = new LSDKChat(getActivity());

                JSONArray users = new JSONArray();
                users.put(mSearchUser.getUserId());
                users.put(mSharedPreferences.getString("userID", null));

                newChat.getPastMessages(users, new Callback() {
                    @Override
                    public void onFailure(Call call, IOException e) {
                        e.printStackTrace();
                    }

                    @Override
                    public void onResponse(Call call, Response response) throws IOException {
                        Log.i(TAG, "onResponse: "+response.body().string());
                    }
                });
            }
        });

        Glide.with(getActivity())
                .load(Utils.getImageUrlOfUser(mSearchUser.getUserImage()))
                .asBitmap()
                .signature(new StringSignature(mSharedPreferences.getString("imageSigniture", "000")))
                .placeholder(R.drawable.image_loading_background)
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
