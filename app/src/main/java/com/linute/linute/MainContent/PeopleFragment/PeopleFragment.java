package com.linute.linute.MainContent.PeopleFragment;

import android.content.Context;
import android.content.pm.PackageManager;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Bundle;
import android.os.Handler;
import android.os.SystemClock;
import android.support.v4.app.Fragment;
import android.support.v4.content.ContextCompat;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.Toast;

import com.linute.linute.API.LSDKPeople;
import com.linute.linute.API.LSDKUser;
import com.linute.linute.R;
import com.linute.linute.UtilsAndHelpers.CustomLinearLayoutManager;
import com.linute.linute.UtilsAndHelpers.SpaceItemDecoration;
import com.linute.linute.UtilsAndHelpers.UpdatableFragment;
import com.linute.linute.UtilsAndHelpers.Utils;

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
    private LinearLayoutManager llm;
    private SwipeRefreshLayout mSwipeRefreshLayout;
    private PeopleAdapter mPeopleAdapter;

    private View mRationaleLayer;

    private boolean mNearMe = false;

    private List<People> mPeopleList = new ArrayList<>();

    private LocationManager mLocationManager;

    public static final long TIME_DIFFERENCE = 600000; //10 minutes

    public PeopleFragment() {
        // Required empty public constructor
    }

    public static PeopleFragment newInstance(boolean nearMe) {
        PeopleFragment people = new PeopleFragment();
        Bundle args = new Bundle();
        args.putBoolean("NEAR_ME", nearMe);
        people.setArguments(args);
        return people;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            mNearMe = getArguments().getBoolean("NEAR_ME");
        }
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putBoolean("NEAR_ME", mNearMe);
    }

    @Override
    public void onViewStateRestored(Bundle savedInstanceState) {
        super.onViewStateRestored(savedInstanceState);
        if (savedInstanceState != null) {
            mNearMe = savedInstanceState.getBoolean("NEAR_ME");
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View rootView = inflater.inflate(R.layout.fragment_people, container, false);

        mRationaleLayer = rootView.findViewById(R.id.peopleFragment_rationale_view);

        setHasOptionsMenu(true);

        recList = (RecyclerView) rootView.findViewById(R.id.people_frag_rec);
        recList.setHasFixedSize(true);
        llm = new CustomLinearLayoutManager(getActivity());
        llm.setOrientation(LinearLayoutManager.VERTICAL);
        recList.setLayoutManager(llm);
        recList.addItemDecoration(new SpaceItemDecoration(getActivity(), R.dimen.list_space,
                true, true));


        mSwipeRefreshLayout = (SwipeRefreshLayout) rootView.findViewById(R.id.peoplefrag_swipe_refresh);
        mSwipeRefreshLayout.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                //mSwipeRefreshLayout.setRefreshing(true);
                if (!mNearMe)
                    getPeople();
                else {
                    getPeopleNearMe();
                }
            }
        });

        mPeopleAdapter = new PeopleAdapter(mPeopleList, getActivity(), mNearMe);
        recList.setAdapter(mPeopleAdapter);

        mLocationManager = (LocationManager) getActivity().getSystemService(Context.LOCATION_SERVICE);

        return rootView;
    }

    @Override
    public void onResume() {
        super.onResume();
//        MainActivity mainActivity = (MainActivity) getActivity();
//        if (mainActivity != null) {
//            mainActivity.setTitle("People");
//            mainActivity.resetToolbar();
//        }


        PeopleFragmentsHolder fragment = (PeopleFragmentsHolder) getParentFragment();

        if (fragment == null) return;

        //initially presented fragment by discoverHolderFragment doesn't get loaded by discoverholderfragment
        //do it in on resume
        //if initial fragment was campus feed, we are in campus feed, and it needs to be updated
        if (fragment.getInitiallyPresentedFragmentWasNearby()
                && mNearMe
                && fragment.nearMeFragmentNeedsUpdating()) {
            getPeopleNearMe();
            fragment.setNearMeNeedsUpdating(false);
        } else if (!fragment.getInitiallyPresentedFragmentWasNearby()
                && !mNearMe
                && fragment.activeNeedsUpdating()) {
            getPeople();
            fragment.setActiveNeedsUpdating(false);
        }
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

    public void getPeople() {

        if (!mSwipeRefreshLayout.isRefreshing()) {
            mSwipeRefreshLayout.post(new Runnable() {
                @Override
                public void run() {
                    mSwipeRefreshLayout.setRefreshing(true);
                }
            });
        }

        LSDKPeople people = new LSDKPeople(getActivity());
        people.getPeople(new HashMap<String, String>(), new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {
                if (getActivity() == null) return;
                getActivity().runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        mSwipeRefreshLayout.setRefreshing(false);
                        Utils.showBadConnectionToast(getActivity());
                    }
                });
            }

            @Override
            public void onResponse(Call call, Response response) throws IOException {

                String responsString = response.body().string();

                if (!response.isSuccessful()) {
                    Log.d("TAG", responsString);
                    return;
                }

                //Log.i(TAG, "onResponse: " + responsString);
                JSONObject jsonObject;
                JSONArray jsonArray;
                try {
                    jsonObject = new JSONObject(responsString);
                    jsonArray = jsonObject.getJSONArray("people");


                    People people;
                    String friend;
                    SimpleDateFormat simpleDateFormat = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'");
                    Date myDate;
                    String dateString;

                    JSONObject userOwner;

                    ArrayList<People> tempPeople = new ArrayList<>();

                    for (int i = 0; i < jsonArray.length(); i++) {
                        jsonObject = (JSONObject) jsonArray.get(i);
                        friend = jsonObject.getString("friend");


                        userOwner = jsonObject.getJSONObject("owner");

                        //Start
                        boolean areFriends = false;
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

                        //End

                        myDate = simpleDateFormat.parse(jsonObject.getString("date"));
                        dateString = Utils.getTimeAgoString(myDate.getTime());

                        people = new People(
                                userOwner.getString("profileImage"),
                                userOwner.getString("fullName"),
                                personId,
                                dateString,
                                areFriends,
                                userOwner.getString("status")
                        );

                        tempPeople.add(people);
                    }

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

                } catch (JSONException | ParseException e) {
                    e.printStackTrace();
                    if (getActivity() == null) {
                        return;
                    }
                    getActivity().runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            mSwipeRefreshLayout.setRefreshing(false);

                            Utils.showServerErrorToast(getActivity());

                        }
                    });
                }
            }
        });
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
                            distanceString = "right next to you";
                        } else {
                            distanceString = twoDForm.format(distance) + " miles away";
                        }

                        //End

                        people = new People(
                                userOwner.getString("profileImage"),
                                userOwner.getString("fullName"),
                                personId,
                                distanceString,
                                areFriends,
                                userOwner.getString("status"));
                        tempPeople.add(people);
                    }


                    mPeopleList.clear();
                    mPeopleList.addAll(tempPeople);

                    if (getActivity() == null) {
                        Log.d("TAG", "Null");
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
        PeopleFragmentsHolder holder = (PeopleFragmentsHolder) getParentFragment();
        if (holder == null) return;
        if (holder.hasLocationPermissions()) {
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

    public void scrollUp() {
        if (recList != null) {
            recList.smoothScrollToPosition(0);
        }
    }
}
