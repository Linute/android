package com.linute.linute.MainContent.ProfileFragment;


import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.bumptech.glide.signature.StringSignature;
import com.linute.linute.API.LSDKUser;
import com.linute.linute.MainContent.DiscoverFragment.CheckBoxQuestionAdapter;
import com.linute.linute.MainContent.MainActivity;
import com.linute.linute.MainContent.Settings.ChangeProfileImageActivity;
import com.linute.linute.MainContent.Settings.SettingActivity;
import com.linute.linute.R;
import com.linute.linute.UtilsAndHelpers.DividerItemDecoration;
import com.linute.linute.UtilsAndHelpers.LinuteConstants;
import com.linute.linute.UtilsAndHelpers.LinuteUser;
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

public class Profile extends Fragment {
    public static final String TAG = "ProfileFragment";

    public static final String PARCEL_DATA_KEY = "profileFragmentArrayOfActivities";
    public static final String HAS_UPDATED_KEY = "profileFragmentHasUpdatedFromDB";

    private boolean mHasUpdatedFromDB = false; //if we have gotten information from Database

    private RecyclerView recList;
    private LinearLayoutManager llm;
    private ProfileAdapter mProfileAdapter;

    private SwipeRefreshLayout mSwipeRefreshLayout;

    private ArrayList<UserActivityItem> mUserActivityItems = new ArrayList<>();

    private LSDKUser mUser;
    private SharedPreferences mSharedPreferences;
    private String mProfileImagePath;

    public static final int IMAGE_CHANGED = 1234;

    private Runnable rServerErrorAction = new Runnable() {
        @Override
        public void run() {
            Utils.showServerErrorToast(getContext());
        }
    };

    private Runnable rFailedConnectionAction = new Runnable() {
        @Override
        public void run() {
            Utils.showBadConnectionToast(getContext());
        }
    };
    private LinuteUser user = new LinuteUser();
    public boolean hasSetTitle;


    public Profile() {
        // Required empty public constructor
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View rootView = inflater.inflate(R.layout.fragment_profile2, container, false);

        setHasOptionsMenu(true);

        mUser = new LSDKUser(getContext());
        mSharedPreferences = getContext().getSharedPreferences(LinuteConstants.SHARED_PREF_NAME, Context.MODE_PRIVATE);

//        ((MainActivity) getActivity()).resetToolbar();

        recList = (RecyclerView) rootView.findViewById(R.id.prof_frag_rec);
        recList.setHasFixedSize(true);
        llm = new LinearLayoutManager(getActivity());
        llm.setOrientation(LinearLayoutManager.VERTICAL);
        recList.setLayoutManager(llm);
        recList.addItemDecoration(new DividerItemDecoration(getActivity(), null));

        mProfileAdapter = new ProfileAdapter(mUserActivityItems, user, getContext(), Profile.this);
        recList.setAdapter(mProfileAdapter);

        mSwipeRefreshLayout = (SwipeRefreshLayout) rootView.findViewById(R.id.profilefrag2_swipe_refresh);
        mSwipeRefreshLayout.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                updateAndSetHeader();
                setActivities(); //get activities

            }
        });

        if (!mHasUpdatedFromDB) {
            updateAndSetHeader(); //get information from server to update profile
            setActivities();
        }

        return rootView;
    }

    @Override
    public void onResume() {
        super.onResume();

//        setDefaultHeader(); //set header using cached info

        //we only get info from database first time activity starts
        //afterwards, it only updates when user scrolls to update
    }

    @Override
    public void onSaveInstanceState(Bundle outState) { //saves fragment state
        outState.putBoolean(HAS_UPDATED_KEY, mHasUpdatedFromDB); //so we don't update info again
        outState.putParcelableArrayList(PARCEL_DATA_KEY, mUserActivityItems); //list of activities is saved
        super.onSaveInstanceState(outState);
    }

    @Override
    public void onViewStateRestored(Bundle savedInstanceState) { //gets saved frament state
        if (savedInstanceState != null) {
            mHasUpdatedFromDB = savedInstanceState.getBoolean(HAS_UPDATED_KEY);
            mUserActivityItems = savedInstanceState.getParcelableArrayList(PARCEL_DATA_KEY);
        }

        super.onViewStateRestored(savedInstanceState);
    }

    //get user information from server
    public void updateAndSetHeader() {
        mUser.getProfileInfo(mSharedPreferences.getString("userID", null), new Callback() {
            @Override
            public void onFailure(Request request, IOException e) {
                if (getActivity() != null) {
                    mHasUpdatedFromDB = true;  //successfully updated profile with updated info
                    getActivity().runOnUiThread(rFailedConnectionAction);
                }
            }

            @Override
            public void onResponse(Response response) throws IOException {
                mHasUpdatedFromDB = true;  //successfully updated profile with updated info
                if (response.isSuccessful()) { //attempt to update view with response
                    final String body = response.body().string();
                    if (getActivity() == null) return;
                    JSONObject jsonObject = null;
                    try {
                        jsonObject = new JSONObject(body);
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }
                    user = new LinuteUser(jsonObject); //container for new information
                    Log.d("TAG", body);
                    getActivity().runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            mProfileAdapter = new ProfileAdapter(mUserActivityItems, user, getContext(), Profile.this);
                            recList.setAdapter(mProfileAdapter);
                            mProfileAdapter.notifyDataSetChanged();
                            if (!hasSetTitle) {
                                ((MainActivity) getActivity()).setTitle(user.getFirstName() + " " + user.getLastName());
                                hasSetTitle = true;
                            }
                        }
                    });
                } else {//else something went
                    Log.v(TAG, response.body().string());
                    getActivity().runOnUiThread(rServerErrorAction);
                }
            }
        });
    }

    private void setTextAndImageViewsAndSaveToPrefs(String response) {
//        try { //try to get new information


//            mProfileImagePath = Utils.getImageUrlOfUser(user.getProfileImage());
//            if (mProfileImagePath != null)
//                setProfilePicture();
//
//            //save the new info
//            SharedPreferences.Editor editor = mSharedPreferences.edit();
//            editor.putString("firstName", user.getFirstName());
//            editor.putString("lastName", user.getLastName());
//            editor.putString("status", user.getStatus());
//            editor.putInt("numOfFriends", user.getFriendsNumber());
//            editor.putInt("numOfAttendedEvents", user.getAttendedNumber());
//            editor.putInt("numOfHostedEvents", user.getHostedNumber());
//            editor.apply();

//        } catch (JSONException e) { //apply saved or cached data
//            e.printStackTrace();
//            Log.v(TAG, "Couldn't save info");
//        }
    }

    //set our views with the default or cached data
    //used when there is trouble retrieving data from server
//    private void setDefaultHeader() {
//        mFullNameText.setText(
//                mSharedPreferences.getString("firstName", "") + " " + mSharedPreferences.getString("lastName", ""));
//        mNumOfAttendedEvents.setText(String.valueOf(mSharedPreferences.getInt("numOfAttendedEvents", 0)));
//        mNumOfHostedEvents.setText(String.valueOf(mSharedPreferences.getInt("numOfHostedEvents", 0)));
//        mNumOfFriends.setText(String.valueOf(mSharedPreferences.getInt("numOfFriends", 0)));
//        mStatusText.setText(mSharedPreferences.getString("status", ""));
//
//        setCachedProfilePicture();
//    }

    //try to load image from cache
//    private void setCachedProfilePicture() {
//        //attempt to find profile image in cache
//        mProfileImagePath = Utils.getImageUrlOfUser(mSharedPreferences.getString("profileImage", null));
//        if (mProfileImagePath != null)
//            setProfilePicture();
//    }

    //seting profile image hlper
//    private void setProfilePicture() {
//        int radius = getResources().getDimensionPixelSize(R.dimen.profilefragment_main_profile_image_radius);
//        setImage(mProfileImagePath, mProfilePicture, radius, radius);
//    }

    //take userID
//    private void setImage(String imagePath, ImageView into, int width, int height) {
//        //get image dimenstion
//        //ImageViews don't load in time to get their actual height
//        //without dimensions, we can't grab image from cache
//        Glide.with(this)
//                .load(imagePath)
//                .asBitmap()
//                .signature(new StringSignature(mSharedPreferences.getString("imageSigniture", "000")))
//                .override(width, height) //change image to the size we want
//                .placeholder(R.drawable.profile_picture_placeholder)
//                .diskCacheStrategy(DiskCacheStrategy.RESULT) //only cache the scaled image
//                .into(into);
//    }

    public void setActivities() {
        LSDKUser user = new LSDKUser(getContext());
        user.getUserActivities(new Callback() {
            @Override
            public void onFailure(Request request, IOException e) {
                getActivity().runOnUiThread(new Runnable() { //if refreshing, turn off
                    @Override
                    public void run() {
                        if (mSwipeRefreshLayout.isRefreshing())
                            mSwipeRefreshLayout.setRefreshing(false);
                        Utils.showBadConnectionToast(getContext());
                    }
                });
            }

            @Override
            public void onResponse(Response response) throws IOException {
                if (response.isSuccessful()) { //got response
                    try { //try to grab needed information from response
                        String body = response.body().string();
                        final JSONArray activities = new JSONObject(body).getJSONArray("activities"); //try to get activities from response
                        Log.d("TAG", body);


                        //i only update the list of activities if their are new values
                        //array has problems if I continuously update with new values too quickly
                        if (activities.length() != mUserActivityItems.size()) { //we have an updated set of info

                            mUserActivityItems.clear(); //clear so we don't have duplicates
                            for (int i = 0; i < activities.length(); i++) { //add each activity into our array
                                mUserActivityItems.add(
                                        new UserActivityItem(
                                                activities.getJSONObject(i),
                                                mProfileImagePath,
                                                mSharedPreferences.getString("firstName", "") + " " + mSharedPreferences.getString("lastName", "")
                                        )); //create activity objects and add to array
                            }
                        }
                    } catch (JSONException e) { //unable to grab needed info
                        e.printStackTrace();
                        getActivity().runOnUiThread(rServerErrorAction);
                    }
                } else { //unable to connect with DB
                    Log.v(TAG, response.body().string());
                    getActivity().runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            Utils.showServerErrorToast(getContext());
                        }
                    });
                }

                //turn refresh off if it's on and notify ListView we might have updated information
                getActivity().runOnUiThread(new Runnable() {
                    @Override
                    public void run() { //update view
                        if (mSwipeRefreshLayout.isRefreshing())
                            mSwipeRefreshLayout.setRefreshing(false);

                        //NOTE: maybe move this inside try.
                        //      should we update date text if no connection?
                        mProfileAdapter.notifyDataSetChanged();

                    }
                });

            }
        });
    }

    protected void editProfileImage() {
        Intent i = new Intent(getContext(), ChangeProfileImageActivity.class);
        startActivityForResult(i, IMAGE_CHANGED);
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == IMAGE_CHANGED && resultCode == Activity.RESULT_OK) { //profile image changed
            //tell our items to update
            mHasUpdatedFromDB = false;
            mUserActivityItems.clear();
        }
    }


    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        super.onCreateOptionsMenu(menu, inflater);
        //menu.clear();
        inflater.inflate(R.menu.profile_fragment_menu, menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {

        int id = item.getItemId();

        if (R.id.profile_fragment_menu_settings == id) {
            Intent i = new Intent(getContext(), SettingActivity.class);
            startActivity(i);
            return true;
        }

        return super.onOptionsItemSelected(item);
    }
}
