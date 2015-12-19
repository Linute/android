package com.linute.linute.MainContent.ProfileFragment;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.Preference;
import android.support.v4.app.ListFragment;
import android.support.v4.widget.SwipeRefreshLayout;
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
import com.linute.linute.API.LSDKUser;
import com.linute.linute.MainContent.Settings.SettingActivity;
import com.linute.linute.R;
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
import org.w3c.dom.Text;

import java.io.IOException;
import java.util.ArrayList;


public class ProfileFragment extends ListFragment {

    public static final String TAG = "ProfileFragment";

    public static final String PARCEL_DATA_KEY = "profileFragmentArrayOfActivities";
    public static final String HAS_UPDATED_KEY = "profileFragmentHasUpdatedFromDB";
    public static final String SHOW_EMPTY_LIST = "profileFragmentShowEmptyList";

    private boolean mHasUpdatedFromDB = false; //if we have gotten information from Database

    private ProfileActivityListAdapter mAdapter;
    private CircularImageView mProfilePicture;
    private TextView mNumOfFriends;
    private TextView mNumOfHostedEvents;
    private TextView mNumOfAttendedEvents;
    private TextView mFullNameText;
    private TextView mStatusText;
    private SwipeRefreshLayout mSwipeRefreshLayout;

    private View mEmptyListView;
    private boolean mShowEmptyView = false; //determines if empty view should be shown

    //array of our activities
    private ArrayList<UserActivityItem> mUserActivityItems = new ArrayList<UserActivityItem>();

    private LSDKUser mUser;
    private SharedPreferences mSharedPreferences;
    private String mProfileImagePath;

    public ProfileFragment() {
        // Required empty public constructor
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.fragment_profile, container, false);

        mUser = new LSDKUser(getContext());

        mSharedPreferences = getContext().getSharedPreferences(LinuteConstants.SHARED_PREF_NAME , Context.MODE_PRIVATE);

        mEmptyListView = (View) rootView.findViewById(R.id.profilefrag_empty_list);

        //set up swipe refresh
        mSwipeRefreshLayout = (SwipeRefreshLayout) rootView.findViewById(R.id.profilefrag_swipe_refresh);
        mSwipeRefreshLayout.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                updateAndSetHeader();
                setActivities(); //get activities

            }
        });

        setHasOptionsMenu(true); //so we can set different action buttons for his tab

        return rootView;
    }


    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);

        //add header
        ViewGroup header = (ViewGroup) LayoutInflater.from(getContext()).inflate(R.layout.fragment_profile_header, getListView(), false);
        getListView().addHeaderView(header, null, false);

        //bind header views
        mNumOfAttendedEvents = (TextView) header.findViewById(R.id.profilefrag_num_attended);
        mNumOfHostedEvents = (TextView) header.findViewById(R.id.profilefrag_num_hosted);
        mNumOfFriends = (TextView) header.findViewById(R.id.profilefrag_num_friends);
        mFullNameText = (TextView) header.findViewById(R.id.profilefrag_fullname);
        mProfilePicture = (CircularImageView) header.findViewById(R.id.profilefrag_prof_image);
        mStatusText = (TextView) header.findViewById(R.id.profilefrag_status);

        //set listview adapter
        mAdapter = new ProfileActivityListAdapter(getContext(), mUserActivityItems);
        setListAdapter(mAdapter);
    }

    @Override
    public void onResume() {
        super.onResume();

        setDefaultHeader(); //set header using cached info

        //we only get info from database first time activity starts
        //afterwards, it only updates when user scrolls to update
        if(!mHasUpdatedFromDB){
            updateAndSetHeader(); //get information from server to update profile
            setActivities();
        }
    }

    @Override
    public void onSaveInstanceState(Bundle outState) { //saves fragment state
        outState.putBoolean(HAS_UPDATED_KEY, mHasUpdatedFromDB); //so we don't update info again
        outState.putParcelableArrayList(PARCEL_DATA_KEY, mUserActivityItems); //list of activities is saved
        outState.putBoolean(SHOW_EMPTY_LIST, mShowEmptyView);
        super.onSaveInstanceState(outState);
    }

    @Override
    public void onViewStateRestored(Bundle savedInstanceState) { //gets saved frament state
        if (savedInstanceState != null) {
            mHasUpdatedFromDB = savedInstanceState.getBoolean(HAS_UPDATED_KEY);
            mUserActivityItems = savedInstanceState.getParcelableArrayList(PARCEL_DATA_KEY);
            mShowEmptyView = savedInstanceState.getBoolean(SHOW_EMPTY_LIST);
        }

        //if we come back and list still empty, show mUserActivityView. else hide
        showEmptyView(mShowEmptyView);

        super.onViewStateRestored(savedInstanceState);
    }

    //get user information from server
    private void updateAndSetHeader(){
        mUser.getProfileInfo(mSharedPreferences.getString("userID", null), new Callback() {
            @Override
            public void onFailure(Request request, IOException e) {
                getActivity().runOnUiThread(rFailedConnectionAction);
            }

            @Override
            public void onResponse(Response response) throws IOException {
                if (response.isSuccessful()) { //attempt to update view with response
                    mHasUpdatedFromDB = true;  //successfully updated profile with updated info
                    final String body = response.body().string();
                    getActivity().runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            setTextAndImageViewsAndSaveToPrefs(body);
                        }
                    });
                } else {//else something went
                    Log.v(TAG, response.body().string());
                    getActivity().runOnUiThread(rServerErrorAction);
                }
            }
        });
    }

    //set views and save information retrieved from server
    private void setTextAndImageViewsAndSaveToPrefs(String response){
        try { //try to get new information
            JSONObject body = new JSONObject(response);
            LinuteUser user = new LinuteUser(body); //container for new information

            //update our views with new info
            mNumOfFriends.setText(String.valueOf(user.getFriendsNumber()));
            mNumOfAttendedEvents.setText(String.valueOf(user.getAttendedNumber()));
            mNumOfHostedEvents.setText(String.valueOf(user.getHostedNumber()));
            mFullNameText.setText(user.getFirstName() + " " + user.getLastName());
            mStatusText.setText(user.getStatus());
            mProfileImagePath = Utils.getImageUrlOfUser(user.getProfileImage());
            if (mProfileImagePath != null)
                setProfilePicture();

            //save the new info
            SharedPreferences.Editor editor = mSharedPreferences.edit();
            editor.putString("firstName", user.getFirstName());
            editor.putString("lastName", user.getLastName());
            editor.putString("status", user.getStatus());
            editor.putInt("numOfFriends", user.getFriendsNumber());
            editor.putInt("numOfAttendedEvents", user.getAttendedNumber());
            editor.putInt("numOfHostedEvents", user.getHostedNumber());
            editor.apply();

        } catch (JSONException e) { //apply saved or cached data
            e.printStackTrace();
            Log.v(TAG, "Couldn't save info");
        }
    }

    //set our views with the default or cached data
    //used when there is trouble retrieving data from server
    private void setDefaultHeader(){
        mFullNameText.setText(
                mSharedPreferences.getString("firstName", "") + " " + mSharedPreferences.getString("lastName", ""));
        mNumOfAttendedEvents.setText(String.valueOf(mSharedPreferences.getInt("numOfAttendedEvents", 0)));
        mNumOfHostedEvents.setText(String.valueOf(mSharedPreferences.getInt("numOfHostedEvents", 0)));
        mNumOfFriends.setText(String.valueOf(mSharedPreferences.getInt("numOfFriends", 0)));
        mStatusText.setText(mSharedPreferences.getString("status", ""));

        setCachedProfilePicture();
    }

    //try to load image from cache
    private void setCachedProfilePicture(){
        //attempt to find profile image in cache
        mProfileImagePath = Utils.getImageUrlOfUser(mSharedPreferences.getString("profileImage", null));
        if(mProfileImagePath != null)
            setProfilePicture();
    }

    //get activities from server
    private void setActivities(){
        LSDKUser user = new LSDKUser(getContext());
        user.getUserActivities(new Callback() {
            @Override
            public void onFailure(Request request, IOException e) {
                getActivity().runOnUiThread(new Runnable() { //if refreshing, turn off
                    @Override
                    public void run() {
                        if (mSwipeRefreshLayout.isRefreshing()) mSwipeRefreshLayout.setRefreshing(false);
                        Utils.showBadConnectionToast(getContext());
                    }
                });
            }

            @Override
            public void onResponse(Response response) throws IOException {
                if (response.isSuccessful()) { //got response
                    try { //try to grab needed information from response
                        final JSONArray activities = new JSONObject(response.body().string()).getJSONArray("activities"); //try to get activities from response

                        //checks to see if we should show the empty list view
                        getActivity().runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                mShowEmptyView = activities.length() == 0 ? true : false; //determine if list empty
                                showEmptyView(mShowEmptyView);  //if empty, show empty view. if not, hide empty view
                            }
                        });

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
                    Utils.showServerErrorToast(getContext());
                }

                //turn refresh off if it's on and notify ListView we might have updated information
                getActivity().runOnUiThread(new Runnable() {
                    @Override
                    public void run() { //update view
                        if (mSwipeRefreshLayout.isRefreshing())
                            mSwipeRefreshLayout.setRefreshing(false);

                        //NOTE: maybe move this inside try.
                        //      should we update date text if no connection?
                        mAdapter.notifyDataSetChanged();

                    }
                });

            }
        });
    }

    private void showEmptyView(boolean showEmpty) {
        //if it't not visible and we want to show it, make it visible
        if (showEmpty && mEmptyListView.getVisibility() == View.GONE)
            mEmptyListView.setVisibility(View.VISIBLE);

        //if it's not visisble and we don't want to show it, make it dissappear
        else if (!showEmpty && mEmptyListView.getVisibility() == View.VISIBLE)
            mEmptyListView.setVisibility(View.GONE);

    }

    //seting profile image hlper
    private void setProfilePicture(){
        int radius = getResources().getDimensionPixelSize(R.dimen.profilefragment_main_profile_image_radius);
        setImage(mProfileImagePath, mProfilePicture, radius, radius);
    }

    //take userID
    private void setImage(String imagePath, ImageView into, int width, int height){
        //get image dimenstion
        //ImageViews don't load in time to get their actual height
        //without dimensions, we can't grab image from cache
        Glide.with(this)
                .load(imagePath)
                .asBitmap()
                .override(width, height) //change image to the size we want
                .placeholder(R.drawable.profile_picture_placeholder)
                .diskCacheStrategy(DiskCacheStrategy.RESULT) //only cache the scaled image
                .into(into);
    }

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


    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        super.onCreateOptionsMenu(menu, inflater);
        //menu.clear();
        inflater.inflate(R.menu.profile_fragment_menu, menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {

        int id = item.getItemId();

        if (R.id.profile_fragment_menu_settings == id){
            Intent i = new Intent(getContext(), SettingActivity.class);
            i.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            startActivity(i);
            return true;
        }

        return super.onOptionsItemSelected(item);
    }
}
