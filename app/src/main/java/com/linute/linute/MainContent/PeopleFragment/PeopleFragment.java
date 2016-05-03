package com.linute.linute.MainContent.PeopleFragment;

import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Bundle;
import android.os.Handler;
import android.os.SystemClock;
import android.support.annotation.NonNull;
import android.support.design.widget.AppBarLayout;
import android.support.v4.app.Fragment;
import android.support.v4.content.ContextCompat;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.Toast;

import com.linute.linute.API.LSDKPeople;
import com.linute.linute.API.LSDKUser;
import com.linute.linute.MainContent.Chat.NewChatEvent;
import com.linute.linute.MainContent.Chat.RoomsActivity;
import com.linute.linute.MainContent.FindFriends.FindFriendsChoiceFragment;
import com.linute.linute.MainContent.MainActivity;
import com.linute.linute.Manifest;
import com.linute.linute.R;
import com.linute.linute.UtilsAndHelpers.CustomLinearLayoutManager;
import com.linute.linute.UtilsAndHelpers.SpaceItemDecoration;
import com.linute.linute.UtilsAndHelpers.UpdatableFragment;
import com.linute.linute.UtilsAndHelpers.Utils;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.text.DecimalFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.Response;

/**
 * A simple {@link Fragment} subclass.
 */
public class PeopleFragment extends UpdatableFragment {
    private static final String TAG = PeopleFragment.class.getSimpleName();
    private RecyclerView recList;
    private SwipeRefreshLayout mSwipeRefreshLayout;
    private PeopleAdapter mPeopleAdapter;

    private View mRationaleLayer;

    private List<People> mPeopleList = new ArrayList<>();

    private LocationManager mLocationManager;

    public static final long TIME_DIFFERENCE = 600000; //10 minutes

    private AppBarLayout mAppBarLayout;
    private Toolbar mToolbar;
    private boolean mHasMessage;

    public PeopleFragment() {
        // Required empty public constructor
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View rootView = inflater.inflate(R.layout.fragment_people, container, false);

        mRationaleLayer = rootView.findViewById(R.id.peopleFragment_rationale_view);

        setHasOptionsMenu(true);

        //recList = (RecyclerView) rootView.findViewById(R.id.people_action_icon);
        recList.setHasFixedSize(true);
        LinearLayoutManager llm = new CustomLinearLayoutManager(getActivity());
        llm.setOrientation(LinearLayoutManager.VERTICAL);
        recList.setLayoutManager(llm);


        mAppBarLayout = (AppBarLayout) rootView.findViewById(R.id.appbar_layout);
        mToolbar = (Toolbar) rootView.findViewById(R.id.toolbar);
        mToolbar.setNavigationOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                MainActivity activity = (MainActivity) getActivity();
                if (activity != null){
                    activity.openDrawer();
                }
            }
        });
        mToolbar.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                recList.scrollToPosition(0);
            }
        });

        MainActivity activity = (MainActivity) getActivity();
        mHasMessage = activity.hasMessage();
        mToolbar.inflateMenu(activity.hasMessage() ? R.menu.people_fragment_menu_noti : R.menu.people_fragment_menu);
        mToolbar.setOnMenuItemClickListener(new Toolbar.OnMenuItemClickListener() {
            @Override
            public boolean onMenuItemClick(MenuItem item) {
                switch (item.getItemId()) {
                    case R.id.menu_find_friends:
                        MainActivity activity = (MainActivity) getActivity();
                        if (activity != null){
                            activity.addFragmentToContainer(new FindFriendsChoiceFragment());
                        }
                        return true;
                    case R.id.people_fragment_menu_chat:
                        Intent enterRooms = new Intent(getActivity(), RoomsActivity.class);
                        startActivity(enterRooms);
                        return true;
                    default:
                        return false;
                }
            }
        });

        recList.addItemDecoration(new SpaceItemDecoration(getActivity(), R.dimen.list_space,
                true, true));

        mSwipeRefreshLayout = (SwipeRefreshLayout) rootView.findViewById(R.id.peoplefrag_swipe_refresh);
        mSwipeRefreshLayout.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                getPeopleNearMe();
            }
        });

        mPeopleAdapter = new PeopleAdapter(mPeopleList, getActivity());
        recList.setAdapter(mPeopleAdapter);

        mLocationManager = (LocationManager) getActivity().getSystemService(Context.LOCATION_SERVICE);

        return rootView;
    }

    @Override
    public void onResume() {
        super.onResume();
        if (fragmentNeedsUpdating()) {
            getPeopleNearMe();
            setFragmentNeedUpdating(false);
        }
        EventBus.getDefault().register(this);
    }

    @Override
    public void onPause() {
        super.onPause();
        EventBus.getDefault().unregister(this);
        mAppBarLayout.setExpanded(true,false);
    }

    public void getPeopleNearMe() {
        if (getActivity() == null) return;
        if (ContextCompat.checkSelfPermission(getActivity(),
                android.Manifest.permission.ACCESS_FINE_LOCATION)
                != PackageManager.PERMISSION_GRANTED) {
            showRationalLayer();
        } else {
            if (Utils.isNetworkAvailable(getActivity())) {

                if (!checkLocationOn()) { //location service not enabled
                    return;
                }

                if (mRationaleLayer.getVisibility() == View.VISIBLE)
                    mRationaleLayer.setVisibility(View.GONE);

                mSwipeRefreshLayout.post(new Runnable() {
                    @Override
                    public void run() {
                        mSwipeRefreshLayout.setRefreshing(true);
                    }
                });

                //get last known location
                Location loca = mLocationManager.getLastKnownLocation(LocationManager.NETWORK_PROVIDER);
                if (loca != null) {
                    //if older than 10 minutes
                    if (SystemClock.currentThreadTimeMillis() - loca.getTime() > TIME_DIFFERENCE) {
                        mLocationManager.requestSingleUpdate(LocationManager.NETWORK_PROVIDER, mLocationListener, null);

                        mTimeoutHandler = new Handler();
                        mTimeoutRunnable = new Runnable() {
                            @SuppressWarnings("MissingPermission")
                            @Override
                            public void run() {
                                mLocationManager.removeUpdates(mLocationListener);
                                showRationalLayer();
                            }
                        };

                        mTimeoutHandler.postDelayed(mTimeoutRunnable, 10000);
                    } else {
                        updateLocationAndGetPeople(loca);
                    }
                }
                //no known last known location
                else {
                    mLocationManager.requestSingleUpdate(LocationManager.NETWORK_PROVIDER, mLocationListener, null);

                    mTimeoutHandler = new Handler();

                    mTimeoutRunnable = new Runnable() {
                        @SuppressWarnings("MissingPermission")
                        @Override
                        public void run() {
                            mLocationManager.removeUpdates(mLocationListener);
                            showRationalLayer();
                        }
                    };
                    mTimeoutHandler.postDelayed(mTimeoutRunnable, 10000);
                }

            } else {
                Utils.showBadConnectionToast(getActivity());
            }
        }

    }


    private Handler mTimeoutHandler;
    private Runnable mTimeoutRunnable;

    private boolean checkLocationOn() {
        if (!mLocationManager.isProviderEnabled(LocationManager.NETWORK_PROVIDER)) {
            mRationaleLayer.setVisibility(View.VISIBLE);

            mPeopleList.clear();
            mPeopleAdapter.notifyDataSetChanged();

            if (mSwipeRefreshLayout.isRefreshing()) {
                mSwipeRefreshLayout.setRefreshing(false);
            }

            Button b = (Button) mRationaleLayer.findViewById(R.id.peopleFragment_get_location_permissions_button);
            b.setText("Search now");
            b.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    getPeopleNearMe();
                }
            });

            if (getActivity() == null) return false;

            Toast.makeText(getActivity(), "Please make sure your location service is turned on", Toast.LENGTH_SHORT).show();

            return false;
        }
        return true;
    }

    private void showRationalLayer() {
        mRationaleLayer.setVisibility(View.VISIBLE);
        mPeopleList.clear();
        mPeopleAdapter.notifyDataSetChanged();

        if (mSwipeRefreshLayout.isRefreshing()) {
            mSwipeRefreshLayout.setRefreshing(false);
        }

        Button getPermissions = (Button) mRationaleLayer.findViewById(R.id.peopleFragment_get_location_permissions_button);
        getPermissions.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                requestLocation();
            }
        });
        getPermissions.setText("Turn on");
    }


    LocationListener mLocationListener = new LocationListener() {
        @Override
        public void onLocationChanged(Location location) {
            //Log.i(TAG, "onLocationChanged: " + location.toString());
            if (mTimeoutHandler != null) {
                mTimeoutHandler.removeCallbacks(mTimeoutRunnable);
            }
            updateLocationAndGetPeople(location);
        }

        @Override
        public void onStatusChanged(String provider, int status, Bundle extras) {
        }

        @Override
        public void onProviderEnabled(String provider) {
        }

        @Override
        public void onProviderDisabled(String provider) {
            if (mTimeoutHandler != null) {
                mTimeoutHandler.removeCallbacks(mTimeoutRunnable);
            }
        }
    };


    private void updateLocationAndGetPeople(Location location) {
        JSONArray coord = new JSONArray();
        try {
            coord.put(location.getLatitude());
            coord.put(location.getLongitude());

            JSONObject coordinates = new JSONObject();
            coordinates.put("coordinates", coord);

            Map<String, Object> params = new HashMap<>();
            params.put("geo", coordinates);

            if (getActivity() == null) return;

            new LSDKUser(getActivity()).updateLocation(params, new Callback() {
                @Override
                public void onFailure(Call call, IOException e) {
                    if (getActivity() != null) {
                        getActivity().runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                Utils.showBadConnectionToast(getActivity());
                                mSwipeRefreshLayout.setRefreshing(false);
                            }
                        });
                    }
                }

                @Override
                public void onResponse(Call call, Response response) throws IOException {
                    response.body().string();
                    if (response.isSuccessful()) {
                        getPeopleAfterCoordSent();
                    } else {
                        if (getActivity() == null) return;
                        getActivity().runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                Utils.showBadConnectionToast(getActivity());
                                mSwipeRefreshLayout.setRefreshing(false);
                            }
                        });
                    }
                }
            });
//
        } catch (JSONException e) {
            e.printStackTrace();
            if (getActivity() == null) return;
            getActivity().runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    Utils.showBadConnectionToast(getActivity());
                    mSwipeRefreshLayout.setRefreshing(false);
                }
            });
        }
    }


    public void getPeopleAfterCoordSent() {

        if (getActivity() == null) return;
        new LSDKPeople(getActivity()).getPeoplNearMe(new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {
                if (getActivity() != null) {
                    getActivity().runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            Utils.showBadConnectionToast(getActivity());
                            mSwipeRefreshLayout.setRefreshing(false);
                        }
                    });
                }
            }

            @Override
            public void onResponse(Call call, Response response) throws IOException {
                String responseString = response.body().string();
                try {
                    JSONObject obj = new JSONObject(responseString);
                    JSONArray jsonArray = obj.getJSONArray("geo");
                    People people;
                    JSONObject jsonObject;
                    JSONArray posts;
                    JSONObject post;
                    JSONArray images;

                    String friend;

                    DecimalFormat twoDForm = new DecimalFormat("#.#");
                    JSONObject userOwner;

                    ArrayList<People> tempPeople = new ArrayList<>();

                    for (int i = 0; i < jsonArray.length(); i++) {
                        jsonObject = (JSONObject) jsonArray.get(i);
                        friend = jsonObject.getString("friend");

                        //Start
                        boolean areFriends = false;

                        userOwner = jsonObject.getJSONObject("owner");

                        String personId = userOwner.getString("id");

                        if (!friend.equals("")) {
                            JSONObject friendsJson = jsonObject.getJSONObject("friend");
                            String owner = friendsJson.getString("owner");
                            if (owner.equals(personId)) {
                                if (friendsJson.getBoolean("followedBack"))
                                    areFriends = true;
                            } else {
                                areFriends = true;
                            }
                        }

                        String distanceString;
                        double distance = jsonObject.getDouble("disInMiles");

                        if (distance < 0.05) {
                            distanceString = "NEARBY";
                        } else {
                            distanceString = twoDForm.format(distance) + " Mi AWAY";
                        }

                        people = new People(
                                userOwner.getString("profileImage"),
                                userOwner.getString("fullName"),
                                personId,
                                distanceString,
                                areFriends,
                                userOwner.getString("status"));

                        try {
                            people.setSchoolName(userOwner.getJSONObject("college").getString("name"));
                        } catch (JSONException e) {
                            e.printStackTrace();
                            people.setSchoolName("");
                        }

                        ArrayList<People.PersonRecentPost> recentPosts = new ArrayList<>();

                        posts = jsonObject.getJSONArray("posts");
                        for (int k = 0; k < posts.length(); k++) {
                            post = posts.getJSONObject(k); //look at each post
                            images = post.getJSONArray("images"); //get image array

                            if (images.length() > 0) //if image array has image
                                recentPosts.add(new People.PersonRecentPost(images.getString(0), post.getString("id")));
                        }
                        people.setPersonRecentPosts(recentPosts);

                        //set up ratings
                        post = jsonObject.getJSONObject("rates");
                        people.setAlreadyRated(!post.isNull("userChoice"));
                        ArrayList<People.RatingObject> ratingObjs = new ArrayList<>();
                        ratingObjs.add(getObjectFromJSON(post.getJSONObject("rateOne")));
                        ratingObjs.add(getObjectFromJSON(post.getJSONObject("rateTwo")));
                        ratingObjs.add(getObjectFromJSON(post.getJSONObject("rateThree")));
                        ratingObjs.add(getObjectFromJSON(post.getJSONObject("rateFour")));
                        people.setRatingObjects(ratingObjs);

                        tempPeople.add(people);
                    }

                    //Log.i(TAG, "onResponse: size -- "+ tempPeople.size());

                    mPeopleList.clear();
                    mPeopleList.addAll(tempPeople);

                    if (getActivity() == null) {
                        return;
                    }
                    getActivity().runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            if (mSwipeRefreshLayout.isRefreshing()) {
                                mSwipeRefreshLayout.setRefreshing(false);
                            }

                            mPeopleAdapter.notifyDataSetChanged();

                        }
                    });

                } catch (JSONException e) {
                    e.printStackTrace();
                    if (getActivity() == null) return;

                    getActivity().runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            Utils.showServerErrorToast(getActivity());
                            mSwipeRefreshLayout.setRefreshing(false);
                        }
                    });
                }
            }
        });
    }

    public static final int LOCATION_REQUEST = 1;

    private void requestLocation() {
        if (ContextCompat.checkSelfPermission(getActivity(), android.Manifest.permission.ACCESS_FINE_LOCATION)
                != PackageManager.PERMISSION_GRANTED) {
            requestPermissions(new String[]{android.Manifest.permission.ACCESS_FINE_LOCATION}, LOCATION_REQUEST);
        }else {
            gotPermissionResults();
        }
    }

    public void gotPermissionResults() {
        try {
            if (Utils.isNetworkAvailable(getActivity())) {
                mLocationManager.requestSingleUpdate(LocationManager.NETWORK_PROVIDER, mLocationListener, null);
                mSwipeRefreshLayout.post(new Runnable() {
                    @Override
                    public void run() {
                        mSwipeRefreshLayout.setRefreshing(true);
                    }
                });
                mRationaleLayer.setVisibility(View.GONE);

                mTimeoutHandler = new Handler();

                mTimeoutRunnable = new Runnable() {
                    @SuppressWarnings("MissingPermission")
                    @Override
                    public void run() {
                        mLocationManager.removeUpdates(mLocationListener);
                        showRationalLayer();
                    }
                };
                mTimeoutHandler.postDelayed(mTimeoutRunnable, 10000);

            } else {
                Utils.showBadConnectionToast(getActivity());
            }
        } catch (SecurityException e) {
            e.printStackTrace();
        }
    }

    private People.RatingObject getObjectFromJSON(JSONObject object) throws JSONException {
        return new People.RatingObject(object.getString("key"), object.getString("name"), object.getInt("value"));
    }


    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        if (requestCode == LOCATION_REQUEST ){
            if (grantResults.length > 0
                    && grantResults[0] == PackageManager.PERMISSION_GRANTED){
                gotPermissionResults();
            }else {
                showRationalLayer();
            }
        }
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
    }

    @Override
    public void onStop() {
        super.onStop();
        if (mRationaleLayer.getVisibility() == View.VISIBLE) {
            setFragmentNeedUpdating(true);
        }
    }

    @Subscribe
    public void onEvent(NewChatEvent event){
        // your implementation
        if (event.hasNewMessage() != mHasMessage){
            mToolbar.getMenu().findItem(R.id.people_fragment_menu_chat).setIcon(event.hasNewMessage() ?
                    R.drawable.notify_mess_icon :
                    R.drawable.ic_chat81
            );
            mHasMessage = event.hasNewMessage();
        }
    }
}
