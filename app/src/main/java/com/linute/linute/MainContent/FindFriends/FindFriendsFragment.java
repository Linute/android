package com.linute.linute.MainContent.FindFriends;

import android.Manifest;
import android.content.ContentResolver;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.provider.ContactsContract;
import android.support.annotation.Nullable;
import android.support.v4.content.ContextCompat;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.facebook.AccessToken;
import com.facebook.CallbackManager;
import com.facebook.FacebookCallback;
import com.facebook.FacebookException;
import com.facebook.FacebookSdk;
import com.facebook.login.LoginManager;
import com.facebook.login.LoginResult;
import com.linute.linute.API.LSDKFriendSearch;
import com.linute.linute.API.LSDKPeople;
import com.linute.linute.R;
import com.linute.linute.UtilsAndHelpers.DividerItemDecoration;

import com.linute.linute.UtilsAndHelpers.BaseFragment;
import com.linute.linute.UtilsAndHelpers.Utils;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.Response;

/**
 * Created by QiFeng on 1/16/16.
 */


//TODO: ADD RELOAD BUTTON

public class FindFriendsFragment extends BaseFragment {

    public static final int SEARCH_TYPE_NAME = 0;
    public static final int SEARCH_TYPE_FACEBOOK = 1;

    public static final String TAG = FindFriendsFragment.class.getSimpleName();

    public static final String SEARCH_TYPE_KEY = "search_type";

    private int mSearchType = 0; // 0 for search, 1 for facebook, 2 for contacts

    private FriendSearchAdapter mFriendSearchAdapter;
    private String mQueryString; //what's in the search view

    // filtered list of users
    private List<FriendSearchUser> mFriendFoundList = new ArrayList<>();

    //for facebook and contacts, this will contain the list of unfiltered users
    //for search by name, it will contain the most popular people
    private List<FriendSearchUser> mUnfilteredList = new ArrayList<>();

    private CallbackManager mCallbackManager;
    private ProgressBar mProgressBar;
    private View mFindFriendsRationale;

    private View mEmptyText;

    private Handler mMainHandler = new Handler();

    private Call mSearchByNameCall;

    public static FindFriendsFragment newInstance(int searchType) {
        FindFriendsFragment fragment = new FindFriendsFragment();
        Bundle args = new Bundle();
        args.putInt(SEARCH_TYPE_KEY, searchType);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            mSearchType = getArguments().getInt(SEARCH_TYPE_KEY);
        }
    }


    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.fragment_find_friends, container, false);

        TextView rationaleText = (TextView) rootView.findViewById(R.id.findFriends_rat_text);
        final RecyclerView recyclerView = (RecyclerView) rootView.findViewById(R.id.findFriends_recycler_view);
        mProgressBar = (ProgressBar) rootView.findViewById(R.id.findFriends_progressbar);
        mFindFriendsRationale = rootView.findViewById(R.id.findFriends_rationale_text);

        Button reloadButton = (Button) rootView.findViewById(R.id.findFriends_turn_on);

        mEmptyText = rootView.findViewById(R.id.findFriends_text);

        LinearLayoutManager llm = new LinearLayoutManager(getActivity());
        llm.setOrientation(LinearLayoutManager.VERTICAL);
        recyclerView.setLayoutManager(llm);

        mFriendSearchAdapter = new FriendSearchAdapter(getActivity(), mFriendFoundList);
        mFriendSearchAdapter.setRequestManager(Glide.with(this));

        recyclerView.setAdapter(mFriendSearchAdapter);
        //recyclerView.addItemDecoration(new DividerItemDecoration(getActivity(), null));

        recyclerView.addOnScrollListener(new RecyclerView.OnScrollListener() {
            @Override
            public void onScrollStateChanged(RecyclerView recyclerView, int newState) {
                super.onScrollStateChanged(recyclerView, newState);
                if (newState == RecyclerView.SCROLL_STATE_DRAGGING) {
                    FindFriendsChoiceFragment fragment = (FindFriendsChoiceFragment) getParentFragment();
                    if (fragment != null) fragment.hideKeyboard();
                }
            }
        });

        if (mSearchType == SEARCH_TYPE_NAME) { //if search by name, we need init text

            if (getFragmentState() == FragmentState.NEEDS_UPDATING) {
                rationaleText.setVisibility(View.GONE);
                mEmptyText.setVisibility(View.GONE);
                mProgressBar.setVisibility(View.VISIBLE);
                new LSDKPeople(rootView.getContext()).getPeople(new HashMap<String, String>(), new Callback() {

                    @Override
                    public void onResponse(Call call, Response response) throws IOException {
//                    Log.i(TAG, response.body().string());
                        try {
                            JSONObject json = new JSONObject(response.body().string());
                            //Log.i(TAG, "onResponse: "+json.toString(4));
                            JSONArray events = json.getJSONArray("people");
                            ArrayList<FriendSearchUser> tempFriends = new ArrayList<>();
                            JSONObject friend;
                            if (events != null) {
                                for (int i = 0; i < events.length(); i++) {
                                    try{
                                       friend = events.getJSONObject(i).getJSONObject("friend");
                                    }catch (JSONException e){
                                        friend = null;
                                    }

                                    tempFriends.add(new FriendSearchUser(events.getJSONObject(i).getJSONObject("owner"), friend));
                                }
                            }

                            mUnfilteredList.clear();
                            mUnfilteredList.addAll(tempFriends);

                            if (getActivity() == null) return;

                            setFragmentState(FragmentState.FINISHED_UPDATING);

                            if (mFriendFoundList.isEmpty()) {
                                getActivity().runOnUiThread(new Runnable() {
                                    @Override
                                    public void run() {
                                        if (mUnfilteredList.isEmpty()) { //show no results found if empty
                                            mEmptyText.setVisibility(View.VISIBLE);
                                        } else if (mEmptyText.getVisibility() == View.VISIBLE) {
                                            mEmptyText.setVisibility(View.GONE);
                                        }

                                        //so notify isn't called repeatedly if they don't have to be
                                        mMainHandler.removeCallbacks(null);

                                        mMainHandler.post(new Runnable() {
                                            @Override
                                            public void run() {
                                                mFriendFoundList.addAll(mUnfilteredList);
                                                mFriendSearchAdapter.notifyDataSetChanged();
                                            }
                                        });

                                        mProgressBar.setVisibility(View.GONE);
                                    }
                                });
                            }
                        } catch (JSONException e) {
                            e.printStackTrace();

                            if (getActivity() != null) {
                                getActivity().runOnUiThread(new Runnable() {
                                    @Override
                                    public void run() {
                                        mProgressBar.setVisibility(View.GONE);
                                    }
                                });
                            }
                        }

                    }

                    @Override
                    public void onFailure(Call call, IOException e) {
                        if (getActivity() == null) return;
                        getActivity().runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                Utils.showBadConnectionToast(getActivity());
                                mProgressBar.setVisibility(View.GONE);
                            }
                        });
                    }
                });
            } else {
                mEmptyText.setVisibility(mFriendFoundList.isEmpty() ? View.VISIBLE : View.GONE);
            }

            reloadButton.setVisibility(View.GONE);
            mFindFriendsRationale.setVisibility(View.GONE);

        } else if (mSearchType == SEARCH_TYPE_FACEBOOK) { //facebook

            rationaleText.setText(R.string.facebook_rationale);
            reloadButton.setBackgroundResource(R.drawable.fb_button);

            if (getFragmentState() == FragmentState.NEEDS_UPDATING) {
                mFindFriendsRationale.setVisibility(View.VISIBLE);
                reloadButton.setOnClickListener(new View.OnClickListener() {
                    private boolean loaded = false;

                    @Override
                    public void onClick(View v) {

                        if (!loaded) {
                            FacebookSdk.sdkInitialize(getActivity().getApplicationContext());
                            mCallbackManager = CallbackManager.Factory.create();
                            setUpFacebookCallback();
                            loaded = true;
                        }

                        mFindFriendsRationale.setVisibility(View.GONE);
                        loginFacebook();
                    }
                });
            } else {
                mFindFriendsRationale.setVisibility(View.GONE);
                if (mFriendFoundList.isEmpty()) {
                    mEmptyText.setVisibility(View.VISIBLE);
                }
            }

        } else { //contacts. This process takes forever to get emails. We will use async task
            rationaleText.setText(R.string.need_contant_permission);
            reloadButton.setBackgroundResource(R.drawable.yellow_button);
            reloadButton.setText("Search contacts");

            if (getFragmentState() == FragmentState.NEEDS_UPDATING) {
                mFindFriendsRationale.setVisibility(View.VISIBLE);
                reloadButton.setOnClickListener(new View.OnClickListener() {

                    @Override
                    public void onClick(View v) {
                        setUpContactsList();
                    }
                });
            } else {
                mFindFriendsRationale.setVisibility(View.GONE);
                if (mFriendFoundList.isEmpty()) {
                    mEmptyText.setVisibility(View.VISIBLE);
                }
            }
        }
        return rootView;
    }

    private Handler mSearchHandler = new Handler(); //handles filtering and searchign

    //Search my name inputted in searchbar
    private Runnable mSearchByNameRunnable = new Runnable() {
        @Override
        public void run() {

            if (getActivity() == null) return;

            if (mSearchByNameCall != null) mSearchByNameCall.cancel();

            mSearchByNameCall = new LSDKFriendSearch(getActivity()).searchFriendByName(mQueryString, new Callback() {
                @Override
                public void onFailure(Call call, IOException e) {
                    if (getActivity() == null || call.isCanceled()) return;
                    getActivity().runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            Utils.showBadConnectionToast(getActivity());
                        }
                    });
                }

                @Override
                public void onResponse(Call call, Response response) throws IOException {
                    if (call.isCanceled()) return;

                    String resString = response.body().string();
                    if (response.isSuccessful()) {
                        try {
                            JSONObject json = new JSONObject(resString);

                            JSONArray friends = json.getJSONArray("friends");

                            ArrayList<FriendSearchUser> tempFriends = new ArrayList<>();

                            if (friends != null) {
                                for (int i = 0; i < friends.length(); i++) {
                                    tempFriends.add(new FriendSearchUser(friends.getJSONObject(i)));
                                }
                            }

                            mFriendFoundList.clear();
                            mFriendFoundList.addAll(tempFriends);

                            if (getActivity() == null) return;

                            getActivity().runOnUiThread(new Runnable() {
                                @Override
                                public void run() {

                                    if (mFriendFoundList.isEmpty()) { //show no results found if empty
                                        mEmptyText.setVisibility(View.VISIBLE);
                                    } else if (mEmptyText.getVisibility() == View.VISIBLE) {
                                        mEmptyText.setVisibility(View.GONE);
                                    }

                                    //so notify isn't called repeadedly if they don't have to be
                                    mMainHandler.removeCallbacks(null);
                                    mMainHandler.post(new Runnable() {
                                        @Override
                                        public void run() {
                                            mFriendSearchAdapter.notifyDataSetChanged();
                                        }
                                    });
                                }
                            });

                        } catch (JSONException e) {
                            e.printStackTrace();
                            if (getActivity() == null) return;
                            showServerError();

                        }
                    } else {
                        Log.e(TAG, "onResponse: " + resString);
                        if (getActivity() == null) return;
                        showServerError();
                    }
                }
            });
        }
    };

    //filters list
    private Runnable mFilterFacebookAndContactsRunnable = new Runnable() {
        @Override
        public void run() {
            mMainHandler.removeCallbacksAndMessages(null);
            mMainHandler.post(new Runnable() {
                @Override
                public void run() {
                    if (mQueryString.trim().isEmpty()) {
                        mFriendFoundList.clear();
                        mFriendFoundList.addAll(mUnfilteredList);
                    } else {
                        ArrayList<FriendSearchUser> tempFriend = new ArrayList<>();

                        for (FriendSearchUser user : mUnfilteredList)
                            if (user.nameContains(mQueryString)) {
                                tempFriend.add(user);
                            }

                        mFriendFoundList.clear();
                        mFriendFoundList.addAll(tempFriend);
                    }


                    if (mFriendFoundList.isEmpty()) {
                        mEmptyText.setVisibility(View.VISIBLE);
                    } else {
                        mEmptyText.setVisibility(View.GONE);
                    }


                    mFriendSearchAdapter.notifyDataSetChanged();
                }
            });
        }
    };

    //searching by name
    private void searchByName(String newText) {
        mQueryString = newText;

        //stop current handler
        //we will wait to see if user inputs more before running search
        mSearchHandler.removeCallbacks(mSearchByNameRunnable);
        if (newText.isEmpty()) { //no text in search bar
            mMainHandler.removeCallbacksAndMessages(null);
            if (mSearchByNameCall != null) mSearchByNameCall.cancel();

            mMainHandler.post(new Runnable() {
                @Override
                public void run() {
                    mFriendFoundList.clear();
                    mFriendFoundList.addAll(mUnfilteredList);
                    mFriendSearchAdapter.notifyDataSetChanged();
                }
            });

            if (mEmptyText.getVisibility() == View.VISIBLE) mEmptyText.setVisibility(View.GONE);

        } else {
            if (mFindFriendsRationale.getVisibility() == View.VISIBLE)
                mFindFriendsRationale.setVisibility(View.GONE);

            mSearchHandler.postDelayed(mSearchByNameRunnable, 300);
        }
    }

    //Filter or search TextChangeListners
    private void filterList(String newText) {
        if (getFragmentState() == FragmentState.NEEDS_UPDATING) return;
        mQueryString = newText;
        mSearchHandler.removeCallbacks(mFilterFacebookAndContactsRunnable);
        mSearchHandler.postDelayed(mFilterFacebookAndContactsRunnable, 300);
    }


    public void showServerError() {
        getActivity().runOnUiThread(new Runnable() {
            @Override
            public void run() {
                Utils.showServerErrorToast(getActivity());
            }
        });
    }


    //private boolean mRecievedList = false;

    private void setUpFacebookList(String token) {

        mProgressBar.setVisibility(View.VISIBLE);
        if (getActivity() == null) return;

        new LSDKFriendSearch(getActivity()).searchFriendByFacebook(token, new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {
                if (getActivity() == null) return;
                getActivity().runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        Utils.showBadConnectionToast(getActivity());
                        //showRetryButton(true);
                    }
                });
            }

            @Override
            public void onResponse(Call call, Response response) throws IOException {
                if (response.isSuccessful()) {
                    try {
                        JSONObject json = new JSONObject(response.body().string());

                        //Log.i(TAG, "onResponse: "+json.toString(4));
                        JSONArray friends = json.getJSONArray("friends");

                        if (friends != null) {
                            mUnfilteredList.clear();
                            for (int i = 0; i < friends.length(); i++) {
                                FriendSearchUser user = new FriendSearchUser(friends.getJSONObject(i));
                                mUnfilteredList.add(user);
                            }
                        }

                        setFragmentState(FragmentState.FINISHED_UPDATING);

                        if (getActivity() == null) return;

                        getActivity().runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                //showRetryButton(false);
                                mProgressBar.setVisibility(View.GONE);


                                mMainHandler.removeCallbacksAndMessages(null);
                                mMainHandler.post(new Runnable() {
                                    @Override
                                    public void run() {
                                        mFriendFoundList.clear();
                                        mFriendFoundList.addAll(mUnfilteredList);
                                        mFriendSearchAdapter.notifyDataSetChanged();

                                        if (mFriendFoundList.isEmpty()) {
                                            mEmptyText.setVisibility(View.VISIBLE);
                                        } else {
                                            mEmptyText.setVisibility(View.GONE);
                                        }
                                    }
                                });

                                //mRecievedList = true;
                            }
                        });
                    } catch (JSONException e) {
                        e.printStackTrace();
                        if (getActivity() == null) return;
                        showServerErrorWithRetryButton();

                    }
                } else {
                    Log.e(TAG, "onResponse: " + response.body().string());
                    if (getActivity() == null) return;
                    showServerErrorWithRetryButton();
                }
            }
        });
    }

    private static final int READ_CONTACTS_PERMISSION = 19;

    private void setUpContactsList() {
        //check for contact permissions
        //no permissions: ask for them
        if (ContextCompat.checkSelfPermission(getActivity(), Manifest.permission.READ_CONTACTS) != PackageManager.PERMISSION_GRANTED) {
            requestPermissions(new String[]{Manifest.permission.READ_CONTACTS}, READ_CONTACTS_PERMISSION);
        }
        //have permissions
        else {
            mFindFriendsRationale.setVisibility(View.GONE);
            mProgressBar.setVisibility(View.VISIBLE);
            new GetContactsInBackground().execute();
        }
    }

    private void loadContacts() {
        JSONArray phoneNumbers = new JSONArray();
        JSONArray emails = new JSONArray();

        if (getActivity() == null) return;

        ContentResolver cr = getActivity().getContentResolver();

        if (cr == null) return;

        Cursor cur = cr.query(ContactsContract.Contacts.CONTENT_URI,
                null, null, null, null);

        if (cur == null) return;

        if (cur.getCount() > 0) {
            while (cur.moveToNext()) {
                String id = cur.getString(cur.getColumnIndex(ContactsContract.Contacts._ID));
                if (Integer.parseInt(cur.getString(cur.getColumnIndex(ContactsContract.Contacts.HAS_PHONE_NUMBER))) > 0) {

                    // get the phone number
                    Cursor pCur = cr.query(ContactsContract.CommonDataKinds.Phone.CONTENT_URI, null,
                            ContactsContract.CommonDataKinds.Phone.CONTACT_ID + " = ?",
                            new String[]{id}, null);

                    if (pCur == null) continue;

                    while (pCur.moveToNext()) {
                        phoneNumbers.put(pCur.getString(
                                pCur.getColumnIndex(ContactsContract.CommonDataKinds.Phone.NUMBER)));
                    }
                    pCur.close();


                    // get email and type

                    Cursor emailCur = cr.query(
                            ContactsContract.CommonDataKinds.Email.CONTENT_URI,
                            null,
                            ContactsContract.CommonDataKinds.Email.CONTACT_ID + " = ?",
                            new String[]{id}, null);

                    if (emailCur == null) continue;

                    while (emailCur.moveToNext()) {
                        // This would allow you get several email addresses
                        // if the email addresses were stored in an array

                        emails.put(emailCur.getString(
                                emailCur.getColumnIndex(ContactsContract.CommonDataKinds.Email.DATA)));
                    }
                    emailCur.close();
                }
            }
        }

        cur.close();

        //send info to backend
        new LSDKFriendSearch(getActivity()).searchFriendByContacts(phoneNumbers, emails, new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {
                if (getActivity() == null) return;
                getActivity().runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        Utils.showBadConnectionToast(getActivity());
                        mProgressBar.setVisibility(View.GONE);
                        mFindFriendsRationale.setVisibility(View.VISIBLE);
                        //showRetryButton(true);
                    }
                });

            }

            @Override
            public void onResponse(Call call, Response response) throws IOException {
                if (response.isSuccessful()) {

                    try {
                        JSONObject json = new JSONObject(response.body().string());
                        JSONArray friends = json.getJSONArray("friends");

                        if (friends != null) {
                            mUnfilteredList.clear();
                            for (int i = 0; i < friends.length(); i++) {
                                FriendSearchUser user = new FriendSearchUser(friends.getJSONObject(i));
                                mUnfilteredList.add(user);
                            }
                        }

                        setFragmentState(FragmentState.FINISHED_UPDATING);

                        if (getActivity() == null) return;
                        getActivity().runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                mProgressBar.setVisibility(View.GONE);
                                if (mFriendFoundList.isEmpty()) {
                                    mEmptyText.setVisibility(View.VISIBLE);
                                }

                                mMainHandler.removeCallbacksAndMessages(null);
                                mMainHandler.post(new Runnable() {
                                    @Override
                                    public void run() {
                                        mFriendFoundList.clear();
                                        mFriendFoundList.addAll(mUnfilteredList);
                                        mFriendSearchAdapter.notifyDataSetChanged();
                                    }
                                });
                            }
                        });

                        //TODO: need to implement invite

                    } catch (JSONException e) {
                        e.printStackTrace();
                        if (getActivity() == null) return;
                        showServerErrorWithRetryButton();
                    }

                } else {
                    Log.e(TAG, "onResponse: " + response.body().string());
                    if (getActivity() == null) return;
                    showServerErrorWithRetryButton();
                }
            }

        });
    }

    private void setUpFacebookCallback() {

        LoginManager.getInstance().registerCallback(mCallbackManager, new FacebookCallback<LoginResult>() {
            @Override
            public void onSuccess(LoginResult loginResult) {
                if (!loginResult.getRecentlyDeniedPermissions().isEmpty()) {
                    showRationalTextFacebook();
                    return;
                }

                mFindFriendsRationale.setVisibility(View.GONE);

                setUpFacebookList(loginResult.getAccessToken().getToken());
            }

            @Override
            public void onCancel() {
                showRationalTextFacebook();
            }

            @Override
            public void onError(FacebookException error) {
                error.printStackTrace();
                showFacebookErrorToast();
                showRationalTextFacebook();
            }
        });

    }

    private void showRationalTextFacebook() {
        mFindFriendsRationale.setVisibility(View.VISIBLE);

        ((TextView) mFindFriendsRationale.findViewById(R.id.findFriends_rat_text)).setText(R.string.facebook_rationale);

        TextView mLogin = (TextView) mFindFriendsRationale.findViewById(R.id.findFriends_turn_on);
        mLogin.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                loginFacebook();
                mFindFriendsRationale.setVisibility(View.GONE);
            }
        });
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        mCallbackManager.onActivityResult(requestCode, resultCode, data);
    }

    private void showFacebookErrorToast() {
        Toast.makeText(getActivity(), R.string.bad_connection_text, Toast.LENGTH_SHORT).show();
    }


    public void loginFacebook() {
        AccessToken token = AccessToken.getCurrentAccessToken();

        if (token != null && token.getPermissions().contains("user_friends") && !token.isExpired())
            setUpFacebookList(token.getToken());

        else
            LoginManager.getInstance().logInWithReadPermissions(FindFriendsFragment.this, Collections.singletonList("user_friends"));
    }


    public class GetContactsInBackground extends AsyncTask<Void, Void, Void> {
        @Override
        protected Void doInBackground(Void... params) {
            loadContacts();
            return null;
        }
    }

    private void showServerErrorWithRetryButton() {
        getActivity().runOnUiThread(new Runnable() {
            @Override
            public void run() {
                mProgressBar.setVisibility(View.GONE);
                Utils.showServerErrorToast(getActivity());
                mFindFriendsRationale.setVisibility(View.VISIBLE);
                //showRetryButton(true);
            }
        });
    }

    public void searchWithQuery(String query) {
        if (mSearchType == SEARCH_TYPE_NAME) {
            searchByName(query);
        } else {
            if (mUnfilteredList.isEmpty()) return;
            filterList(query);
        }
    }



    @Override
    public void onDestroyView() {
        super.onDestroyView();

        if (mFriendSearchAdapter.getRequestManager() != null)
            mFriendSearchAdapter.getRequestManager().onDestroy();

        mFriendSearchAdapter.setRequestManager(null);
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        if (mFriendSearchAdapter != null) mFriendSearchAdapter.clearContext();
    }
}

