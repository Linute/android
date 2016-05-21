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

import com.facebook.AccessToken;
import com.facebook.CallbackManager;
import com.facebook.FacebookCallback;
import com.facebook.FacebookException;
import com.facebook.FacebookSdk;
import com.facebook.login.LoginManager;
import com.facebook.login.LoginResult;
import com.linute.linute.API.LSDKFriendSearch;
import com.linute.linute.R;
import com.linute.linute.UtilsAndHelpers.DividerItemDecoration;

import com.linute.linute.UtilsAndHelpers.UpdatableFragment;
import com.linute.linute.UtilsAndHelpers.Utils;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.Response;

/**
 * Created by QiFeng on 1/16/16.
 */


//TODO: ADD RELOAD BUTTON

public class FindFriendsFragment extends UpdatableFragment {

    public static final int SEARCH_TYPE_NAME = 0;
    public static final int SEARCH_TYPE_FACEBOOK = 1;

    public static final String TAG = FindFriendsFragment.class.getSimpleName();

    public static final String SEARCH_TYPE_KEY = "search_type";

    private int mSearchType = 0; // 0 for search, 1 for facebook, 2 for contacts

    private FriendSearchAdapter mFriendSearchAdapter;
    private String mQueryString; //what's in the search view


    private List<FriendSearchUser> mFriendFoundList = new ArrayList<>();

    private List<FriendSearchUser> mUnfilteredList = new ArrayList<>();
    //private String mFbToken;

    private CallbackManager mCallbackManager;
    private ProgressBar mProgressBar;
    private View mFindFriendsRationale;

    private View mEmptyText;

    private GetContactsInBackground mGetContactsInBackground;

    private Handler mMainHandler = new Handler();

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
        RecyclerView recyclerView = (RecyclerView) rootView.findViewById(R.id.findFriends_recycler_view);
        mProgressBar = (ProgressBar) rootView.findViewById(R.id.findFriends_progressbar);
        mFindFriendsRationale = rootView.findViewById(R.id.findFriends_rationale_text);

        Button reloadButton = (Button) rootView.findViewById(R.id.findFriends_turn_on);

        mEmptyText = rootView.findViewById(R.id.findFriends_text);

        LinearLayoutManager llm = new LinearLayoutManager(getActivity());
        llm.setOrientation(LinearLayoutManager.VERTICAL);
        recyclerView.setLayoutManager(llm);

        mFriendSearchAdapter = new FriendSearchAdapter(getActivity(), mFriendFoundList, false);

        recyclerView.setAdapter(mFriendSearchAdapter);
        recyclerView.addItemDecoration(new DividerItemDecoration(getActivity(), null));


        if (mSearchType == SEARCH_TYPE_NAME) { //if search by name, we need init text
            rationaleText.setText("Enter your friend's name in the search bar");
            reloadButton.setVisibility(View.GONE);
            mRecievedList = true;
            if (mFriendFoundList.isEmpty()) {
                if ((mQueryString == null || mQueryString.equals(""))) {
                    mFindFriendsRationale.setVisibility(View.VISIBLE);
                } else {
                    mEmptyText.setVisibility(View.VISIBLE);
                    mFindFriendsRationale.setVisibility(View.GONE);
                }
            } else {
                mFindFriendsRationale.setVisibility(View.GONE);
            }
            setFragmentNeedUpdating(false);
        } else if (mSearchType == SEARCH_TYPE_FACEBOOK) { //facebook
            rationaleText.setText(R.string.facebook_rationale);
            reloadButton.setBackgroundColor(ContextCompat.getColor(getActivity(), R.color.facebook_blue));
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

            if (mFriendFoundList.isEmpty() && !mUnfilteredList.isEmpty()) {
                mFindFriendsRationale.setVisibility(View.GONE);
                mEmptyText.setVisibility(View.VISIBLE);
            } else if (mUnfilteredList.isEmpty()) {
                mFindFriendsRationale.setVisibility(View.VISIBLE);
            }
        } else { //contacts. This process takes forever to get emails. We will use async task
            rationaleText.setText(R.string.need_contant_permission);
            reloadButton.setBackgroundColor(ContextCompat.getColor(getActivity(), R.color.yellow_color));
            reloadButton.setText("Search contacts");
            reloadButton.setOnClickListener(new View.OnClickListener() {

                @Override
                public void onClick(View v) {
                    mGetContactsInBackground = new GetContactsInBackground();
                    setUpContactsList();
                }
            });

            if (mFriendFoundList.isEmpty() && !mUnfilteredList.isEmpty()) {
                mFindFriendsRationale.setVisibility(View.GONE);
                mEmptyText.setVisibility(View.VISIBLE);
            } else if (mUnfilteredList.isEmpty()) {
                mFindFriendsRationale.setVisibility(View.VISIBLE);
            }
        }
        return rootView;
    }


    @Override
    public void onResume() {
        super.onResume();
    }

    @Override
    public void onStop() {
        super.onStop();
        if (mGetContactsInBackground != null) { //cancel task
            mGetContactsInBackground.cancel(true);
        }
    }

    private Handler mSearchHandler = new Handler(); //handles filtering and searchign

    //Search my name inputted in searchbar
    private Runnable mSearchByNameRunnable = new Runnable() {
        @Override
        public void run() {

            if (getActivity() == null) return;

            new LSDKFriendSearch(getActivity()).searchFriendByName(mQueryString, new Callback() {
                @Override
                public void onFailure(Call call, IOException e) {
                    if (getActivity() == null) return;
                    getActivity().runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            Utils.showBadConnectionToast(getActivity());
                        }
                    });
                }

                @Override
                public void onResponse(Call call, Response response) throws IOException {
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

                            if (mCancelLoad) return;

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
            ArrayList<FriendSearchUser> tempFriend = new ArrayList<>();

            if (mQueryString.trim().isEmpty()) {
                tempFriend.addAll(mUnfilteredList);
            } else {
                for (FriendSearchUser user : mUnfilteredList)
                    if (user.nameContains(mQueryString)) {
                        tempFriend.add(user);
                    }

                if (tempFriend.isEmpty() && mEmptyText.getVisibility() == View.GONE) { //empty, show empty text
                    mEmptyText.setVisibility(View.VISIBLE);
                }
            }
            mFriendFoundList.clear();
            mFriendFoundList.addAll(tempFriend);

            if (mFriendFoundList.isEmpty()) {
                mEmptyText.setVisibility(View.VISIBLE);
            } else if (mEmptyText.getVisibility() == View.VISIBLE) {
                mEmptyText.setVisibility(View.GONE);
            }

            mMainHandler.removeCallbacks(null);
            mMainHandler.post(new Runnable() {
                @Override
                public void run() {
                    mFriendSearchAdapter.notifyDataSetChanged();
                }
            });
        }
    };


    private boolean mCancelLoad = false;

    //searching by name
    private void searchByName(String newText) {
        mQueryString = newText;

        //stop current handler
        //we will wait to see if user inputs more before running search
        mSearchHandler.removeCallbacks(mSearchByNameRunnable);
        if (newText.isEmpty()) { //no text in search bar
            mFriendFoundList.clear();

            mMainHandler.removeCallbacksAndMessages(null);
            mMainHandler.post(new Runnable() {
                @Override
                public void run() {
                    mFriendSearchAdapter.notifyDataSetChanged();
                }
            });

            mCancelLoad = true;
            mFindFriendsRationale.setVisibility(View.VISIBLE);
            if (mEmptyText.getVisibility() == View.VISIBLE) mEmptyText.setVisibility(View.GONE);
        } else {
            if (mFindFriendsRationale.getVisibility() == View.VISIBLE)
                mFindFriendsRationale.setVisibility(View.GONE);

            mCancelLoad = false;
            mSearchHandler.postDelayed(mSearchByNameRunnable, 300);
        }
    }

    //Filter or search TextChangeListners
    private void filterList(String newText) {
        if (mInRationalText) return;
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


    private boolean mRecievedList = false;

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

                        JSONArray friends = json.getJSONArray("friends");

                        if (friends != null) {
                            mUnfilteredList.clear();
                            for (int i = 0; i < friends.length(); i++) {
                                FriendSearchUser user = new FriendSearchUser(friends.getJSONObject(i));
                                mFriendFoundList.add(user);
                                mUnfilteredList.add(user);
                            }
                        }
                        setFragmentNeedUpdating(false);

                        if (getActivity() == null) return;
                        getActivity().runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                //showRetryButton(false);
                                mProgressBar.setVisibility(View.GONE);
                                if (mFriendFoundList.isEmpty()) {
                                    mEmptyText.setVisibility(View.VISIBLE);
                                }

                                mMainHandler.removeCallbacksAndMessages(null);
                                mMainHandler.post(new Runnable() {
                                    @Override
                                    public void run() {
                                        mFriendSearchAdapter.notifyDataSetChanged();
                                    }
                                });

                                mRecievedList = true;
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
            mInRationalText = true; //still need to load contacts
            mProgressBar.setVisibility(View.VISIBLE);
            mGetContactsInBackground.execute();
        }
    }

    private boolean mInRationalText = false; //basically determines if we can type in searchbar

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
                        if (mGetContactsInBackground.isCancelled()) break;
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
                        if (mGetContactsInBackground.isCancelled()) break;
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
                                mFriendFoundList.add(user);
                                mUnfilteredList.add(user);
                            }
                        }

                        setFragmentNeedUpdating(false);

                        if (getActivity() == null) return;
                        getActivity().runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                //showRetryButton(false);
                                mProgressBar.setVisibility(View.GONE);
                                mInRationalText = false;
                                if (mFriendFoundList.isEmpty()) {
                                    mEmptyText.setVisibility(View.VISIBLE);
                                }

                                mMainHandler.removeCallbacksAndMessages(null);
                                mMainHandler.post(new Runnable() {
                                    @Override
                                    public void run() {
                                        mFriendSearchAdapter.notifyDataSetChanged();
                                    }
                                });

                                mRecievedList = true;
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

    //TODO: get rid of progress bar mIn = false, hide text

    private void setUpFacebookCallback() {

        LoginManager.getInstance().registerCallback(mCallbackManager, new FacebookCallback<LoginResult>() {
            @Override
            public void onSuccess(LoginResult loginResult) {
                if (!loginResult.getRecentlyDeniedPermissions().isEmpty()) {
                    showRationalTextFacebook();
                    return;
                }
                if (mInRationalText) {
                    mInRationalText = false;
                    mFindFriendsRationale.setVisibility(View.GONE);
                }
                setUpFacebookList(loginResult.getAccessToken().getToken());
            }

            @Override
            public void onCancel() {
                if (!mInRationalText)
                    showRationalTextFacebook();
            }

            @Override
            public void onError(FacebookException error) {
                error.printStackTrace();
                showFacebookErrorToast();

                if (!mInRationalText)
                    showRationalTextFacebook();
            }
        });

    }

    private void showRationalTextFacebook() {
        if (mInRationalText) return; //already in rationale text

        mInRationalText = true;
        mFindFriendsRationale.setVisibility(View.VISIBLE);

        ((TextView) mFindFriendsRationale.findViewById(R.id.findFriends_rat_text)).setText(R.string.facebook_rationale);

        TextView mLogin = (TextView) mFindFriendsRationale.findViewById(R.id.findFriends_turn_on);
        mLogin.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                loginFacebook();
                mFindFriendsRationale.setVisibility(View.GONE);
                mInRationalText = false;
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
        if (!mRecievedList) return;
        if (mSearchType == SEARCH_TYPE_NAME) {
            searchByName(query);
        } else {
            filterList(query);
        }
    }
}

