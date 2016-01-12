package com.linute.linute.MainContent;


import android.app.Dialog;
import android.app.DialogFragment;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;
import android.widget.RadioGroup;
import android.widget.Switch;

import com.linute.linute.API.LSDKEvents;
import com.linute.linute.MainContent.DiscoverFragment.DiscoverFragment;
import com.linute.linute.R;
import com.linute.linute.SquareCamera.CameraActivity;
import com.squareup.okhttp.Callback;
import com.squareup.okhttp.Request;
import com.squareup.okhttp.Response;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import info.hoang8f.android.segmented.SegmentedGroup;

/**
 * A simple {@link Fragment} subclass.
 * Use the {@link PostContentPage#newInstance} factory method to
 * create an instance of this fragment.
 */
public class PostContentPage extends DialogFragment {
    // TODO: Rename parameter arguments, choose names that match
    // the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
    private static final String ARG_PARAM1 = "param1";

    // TODO: Rename and change types of parameters
    private int mCurrentPage;
    private String mParam2;
    private EditText textContent;
    private Switch anonymousSwitch;


    public PostContentPage() {
        // Required empty public constructor
    }

    /**
     * Use this factory method to create a new instance of
     * this fragment using the provided parameters.
     *
     * @return A new instance of fragment PostContentPage.
     */
    // TODO: Rename and change types and number of parameters
    public static PostContentPage newInstance(int currentPage) {
        PostContentPage fragment = new PostContentPage();
        Bundle args = new Bundle();
        args.putInt(ARG_PARAM1, currentPage);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            mCurrentPage = getArguments().getInt(ARG_PARAM1);
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        final View rootView = inflater.inflate(R.layout.fragment_post_content_page, container, false);

        final InputMethodManager imm = (InputMethodManager) getActivity().getSystemService(Context.INPUT_METHOD_SERVICE);

        // Hide keyboard and clear focus from edittext when empty area touched
        rootView.findViewById(R.id.postContentPageBottomBorder).setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                rootView.findViewById(R.id.postContentPageEditText).clearFocus();
                imm.hideSoftInputFromWindow(rootView.findViewById(R.id.postContentPageEditText).getWindowToken(), 0);
                return false;
            }
        });

        textContent = (EditText) rootView.findViewById(R.id.postContentPageEditText);
        anonymousSwitch = (Switch) rootView.findViewById(R.id.anonymous);


        rootView.findViewById(R.id.cancelPost).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                PostContentPage.this.dismiss();
            }
        });

        rootView.findViewById(R.id.submitPost).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Map<String, Object> postData = new HashMap<>();

                JSONArray coord = new JSONArray();
                JSONObject jsonObject = new JSONObject();
                try {
                    coord.put(0);
                    coord.put(0);
                    jsonObject.put("coordinates", coord);
                } catch (JSONException e) {
                    e.printStackTrace();
                }


                Log.d("TAG", jsonObject.toString());

                postData.put("college", "564a46ff8ac4a559174248d9");//TODO: add college
                postData.put("privacy", (anonymousSwitch.isChecked() ? 1 : 0) + "");
                postData.put("title", textContent.getText().toString());
                postData.put("type", "0");
                postData.put("geo", jsonObject);


                new LSDKEvents(getActivity()).postEvent(postData, new Callback() {
                    @Override
                    public void onFailure(Request request, IOException e) {
                        e.printStackTrace();
                    }

                    @Override
                    public void onResponse(Response response) throws IOException {
                        if (!response.isSuccessful()) {
                            Log.d("TAG", response.body().string());
                        } else {
                            Log.i("TAG", "onResponse: ");
                        }
                        getActivity().runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                PostContentPage.this.dismiss();
                                imm.hideSoftInputFromWindow(rootView.findViewById(R.id.postContentPageEditText).getWindowToken(), 0);
                            }
                        });
                    }
                });
            }
        });


        return rootView;
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

    public void onBackPressed() {
        this.dismiss();
    }
}
