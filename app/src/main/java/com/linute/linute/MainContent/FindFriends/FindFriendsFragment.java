package com.linute.linute.MainContent.FindFriends;

import android.Manifest;
import android.content.ContentResolver;
import android.content.Context;
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
import android.support.v7.widget.SearchView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.InputMethodManager;
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
import com.linute.linute.API.API_Methods;
import com.linute.linute.API.LSDKFriendSearch;
import com.linute.linute.R;
import com.linute.linute.UtilsAndHelpers.BaseTaptActivity;
import com.linute.linute.UtilsAndHelpers.DividerItemDecoration;

import com.linute.linute.UtilsAndHelpers.LinuteConstants;
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

    public static final String TAG = FindFriendsFragment.class.getSimpleName();

    public static final String SEARCH_TYPE_KEY = "search_type";

    public static final String FACEBOOK_TOKEN_KEY = "facebook_token";

    private int mSearchType = 0; // 0 for search, 1 for facebook, 2 for contacts

    private RecyclerView mRecyclerView;
    private SearchView mSearchView;
    private FriendSearchAdapter mFriendSearchAdapter;
    private String mQueryString; //what's in the search view
    private TextView mDescriptionText;

    private List<FriendSearchUser> mFriendFoundList = new ArrayList<>();

    private List<FriendSearchUser> mUnfilteredList = new ArrayList<>();
    //private String mFbToken;

    private CallbackManager mCallbackManager;

    private ProgressBar mProgressBar;

    private View mFindFriendsRationale;

    private Button mReloadButton;


    private GetContactsInBackground mGetContactsInBackground;

//    @Override
//    protected void onCreate(Bundle savedInstanceState) {
//        super.onCreate(savedInstanceState);
//
////        setContentView(R.layout.fragment_find_friends);
////
////        if (getIntent() != null) {
////            mSearchType = getIntent().getIntExtra(SEARCH_TYPE_KEY, 0);
////        }
////
////        mDescriptionText = (TextView) findViewById(R.id.findFriends_text);
////        mRecyclerView = (RecyclerView) findViewById(R.id.findFriends_recycler_view);
////        mSearchView = (MaterialSearchView) findViewById(R.id.findFriends_search_view);
////        mProgressBar = (ProgressBar) findViewById(R.id.findFriends_progressbar);
////        mSearchView.showSearch(false);
////
////
//        LinearLayoutManager llm = new LinearLayoutManager(this);
//        llm.setOrientation(LinearLayoutManager.VERTICAL);
//        mRecyclerView.setLayoutManager(llm);
//
//        mFriendSearchAdapter = new FriendSearchAdapter(this, mFriendFoundList, false);
//
//        mRecyclerView.setAdapter(mFriendSearchAdapter);
//        mRecyclerView.addItemDecoration(new DividerItemDecoration(this, null));
//
//
//        setupSearchViewHandler();
//
//        if (mSearchType == 0) { //if search by name, we need init text
//            mDescriptionText.setText("Enter your friends name in the search bar");
//            mDescriptionText.setVisibility(View.VISIBLE);
//        } else if (mSearchType == 1) { //facebook
//            FacebookSdk.sdkInitialize(getApplicationContext());
//
//            //facebook
//            mCallbackManager = CallbackManager.Factory.create();
//
//            setUpFacebookCallback();
//
//            loginFacebook();
//        } else { //contacts. This process takes forever to get emails. We will use async task
//            mGetContactsInBackground = new GetContactsInBackground();
//            setUpContactsList();
//        }
//    }

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

        mDescriptionText = (TextView) rootView.findViewById(R.id.findFriends_text);
        mRecyclerView = (RecyclerView) rootView.findViewById(R.id.findFriends_recycler_view);
        mSearchView = (SearchView) rootView.findViewById(R.id.findFriends_search_view);
        mProgressBar = (ProgressBar) rootView.findViewById(R.id.findFriends_progressbar);
        mFindFriendsRationale = rootView.findViewById(R.id.findFriends_rationale_text);
        mReloadButton = (Button) rootView.findViewById(R.id.findFriends_reload_button);

        mSearchView.setIconified(false);
        mSearchView.setIconifiedByDefault(false);

        mReloadButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                reload();
            }
        });

        LinearLayoutManager llm = new LinearLayoutManager(getActivity());
        llm.setOrientation(LinearLayoutManager.VERTICAL);
        mRecyclerView.setLayoutManager(llm);

        mFriendSearchAdapter = new FriendSearchAdapter(getActivity(), mFriendFoundList, false);

        mRecyclerView.setAdapter(mFriendSearchAdapter);
        mRecyclerView.addItemDecoration(new DividerItemDecoration(getActivity(), null));


        setupSearchViewHandler();

        if (fragmentNeedsUpdating()) {
            if (mSearchType == 0) { //if search by name, we need init text
                mDescriptionText.setText("Enter your friend's name in the search bar");
                mDescriptionText.setVisibility(View.VISIBLE);
            } else if (mSearchType == 1) { //facebook
                FacebookSdk.sdkInitialize(getActivity().getApplicationContext());

                //facebook
                mCallbackManager = CallbackManager.Factory.create();

                setUpFacebookCallback();

                loginFacebook();

            } else { //contacts. This process takes forever to get emails. We will use async task
                mGetContactsInBackground = new GetContactsInBackground();
                setUpContactsList();
            }

            setFragmentNeedUpdating(false);
        }


        return rootView;
    }

    @Override
    public void onResume() {
        super.onResume();

        BaseTaptActivity activity = (BaseTaptActivity) getActivity();
        if (activity != null) {
            activity.setTitle("Find Friends");
            activity.lowerAppBarElevation();
            activity.resetToolbar();
            JSONObject obj = new JSONObject();
            try {
                obj.put("owner", activity.getSharedPreferences(LinuteConstants.SHARED_PREF_NAME, Context.MODE_PRIVATE).getString("userID",""));
                obj.put("action", "active");
                obj.put("screen", "Friend List");
                activity.emitSocket(API_Methods.VERSION + ":users:tracking", obj);
            } catch (JSONException e) {
                e.printStackTrace();
            }

        }
    }

    @Override
    public void onPause() {
        super.onPause();
        BaseTaptActivity activity = (BaseTaptActivity) getActivity();
        if (activity != null){
            JSONObject obj = new JSONObject();
            try {
                obj.put("owner", activity.getSharedPreferences(LinuteConstants.SHARED_PREF_NAME, Context.MODE_PRIVATE).getString("userID",""));
                obj.put("action", "inactive");
                obj.put("screen", "Friend List");
                activity.emitSocket(API_Methods.VERSION + ":users:tracking", obj);
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }
    }

    @Override
    public void onStop() {
        super.onStop();
        if (mGetContactsInBackground != null) { //cancel task
            mGetContactsInBackground.cancel(true);
        }

        BaseTaptActivity activity = (BaseTaptActivity) getActivity();
        if (activity != null) {
            activity.raiseAppBarLayoutElevation();
        }

        if (mSearchView.hasFocus() && getActivity() != null) {
            InputMethodManager imm = (InputMethodManager) getActivity().getSystemService(Context.INPUT_METHOD_SERVICE);
            imm.hideSoftInputFromWindow(mSearchView.getWindowToken(), 0);

        }
    }

    private Handler mSearchHandler; //handles filtering and searchign

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

                            mFriendFoundList.clear();
                            mFriendFoundList.addAll(tempFriends);

                            if (getActivity() == null || mCancelLoad) return;

                            getActivity().runOnUiThread(new Runnable() {
                                @Override
                                public void run() {

                                    if (mFriendFoundList.isEmpty()) { //show no results found if empty
                                        mDescriptionText.setText("No results found");
                                        mDescriptionText.setVisibility(View.VISIBLE);
                                    }
                                    mFriendSearchAdapter.notifyDataSetChanged();

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
                for (FriendSearchUser user : mUnfilteredList)
                    tempFriend.add(user);
            } else {
                for (FriendSearchUser user : mUnfilteredList)
                    if (user.nameContains(mQueryString)) {
                        tempFriend.add(user);
                    }

                if (tempFriend.isEmpty() && mDescriptionText.getVisibility() == View.INVISIBLE) { //empty, show empty text
                        mDescriptionText.setVisibility(View.VISIBLE);
                }
            }
            mFriendFoundList.clear();
            mFriendFoundList.addAll(tempFriend);

            mFriendSearchAdapter.notifyDataSetChanged();
        }
    };


    private boolean mCancelLoad = false;

    //searching by name
    private SearchView.OnQueryTextListener mSearchListener = new SearchView.OnQueryTextListener() {
        @Override
        public boolean onQueryTextSubmit(String query) {
            return false;
        }

        @Override
        public boolean onQueryTextChange(String newText) {
            mQueryString = newText;

            //stop current handler
            //we will wait to see if user inputs more before running search
            mSearchHandler.removeCallbacks(mSearchByNameRunnable);

            if (newText.isEmpty()) { //no text in search bar
                mFriendFoundList.clear();
                mFriendSearchAdapter.notifyDataSetChanged();
                mCancelLoad = true;
                mDescriptionText.setText("Enter your friend's name in the search bar");
                mDescriptionText.setVisibility(View.VISIBLE);
            } else {
                if (mDescriptionText.getVisibility() == View.VISIBLE)
                    mDescriptionText.setVisibility(View.INVISIBLE);

                mCancelLoad = false;
                mSearchHandler.postDelayed(mSearchByNameRunnable, 300);
            }

            return false;
        }
    };

    //Filter or search TextChangeListners
    private SearchView.OnQueryTextListener mFilterListener = new SearchView.OnQueryTextListener() {
        @Override
        public boolean onQueryTextSubmit(String query) {
            return false;
        }

        @Override
        public boolean onQueryTextChange(String newText) {
            if (mInRationalText) return false;

            mQueryString = newText;
            mSearchHandler.removeCallbacks(mFilterFacebookAndContactsRunnable);
            mSearchHandler.postDelayed(mFilterFacebookAndContactsRunnable, 300);
            return false;
        }
    };


    private void setupSearchViewHandler() {

        mSearchHandler = new Handler(); //searchs or filters as you type

        if (mSearchType == 0) { //search
            mSearchView.setOnQueryTextListener(mSearchListener);
        } else //filter
            mSearchView.setOnQueryTextListener(mFilterListener);
    }


    public void showServerError() {
        getActivity().runOnUiThread(new Runnable() {
            @Override
            public void run() {
                Utils.showServerErrorToast(getActivity());
            }
        });
    }


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
                        showRetryButton(true);
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

                        if (getActivity() == null) return;
                        getActivity().runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                showRetryButton(false);
                                mProgressBar.setVisibility(View.GONE);
                                if (mFriendFoundList.isEmpty()) {
                                    mDescriptionText.setText("No results found");
                                    mDescriptionText.setVisibility(View.VISIBLE);
                                }
                                mFriendSearchAdapter.notifyDataSetChanged();
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

    private static final int READ_CONTACTS_PERMISSION = 5;

    private void setUpContactsList() {
        //check for contact permissions
        //no permissions: ask for them
        if (ContextCompat.checkSelfPermission(getActivity(), Manifest.permission.READ_CONTACTS) != PackageManager.PERMISSION_GRANTED) {
            requestPermissions(new String[]{Manifest.permission.READ_CONTACTS}, READ_CONTACTS_PERMISSION);
        }
        //have permissions
        else {
            if (mInRationalText) {
                mFindFriendsRationale.setVisibility(View.GONE);
            }

            mInRationalText = true; //still need to load contacts
            mProgressBar.setVisibility(View.VISIBLE);
            mGetContactsInBackground.execute();
        }
    }


    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
        if (requestCode == READ_CONTACTS_PERMISSION) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                if (mInRationalText) {
                    mFindFriendsRationale.setVisibility(View.GONE);
                }
                mInRationalText = true;
                mProgressBar.setVisibility(View.VISIBLE);
                mGetContactsInBackground.execute();
            } else {
                showRationaleTextContacts();
            }
        } else {
            super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        }
    }

    private boolean mInRationalText = false; //basically determines if we can type in searchbar

    private void showRationaleTextContacts() {
        if (mInRationalText) return; //already in rationale text

        mInRationalText = true;
        mFindFriendsRationale.setVisibility(View.VISIBLE);
        mFindFriendsRationale.findViewById(R.id.findFriends_turn_on).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                setUpContactsList();
            }
        });
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
                        showRetryButton(true);
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

                        if (getActivity() == null) return;
                        getActivity().runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                showRetryButton(false);
                                mProgressBar.setVisibility(View.GONE);
                                mInRationalText = false;
                                if (mFriendFoundList.isEmpty()) {
                                    mDescriptionText.setText("No results found");
                                    mDescriptionText.setVisibility(View.VISIBLE);
                                }
                                mFriendSearchAdapter.notifyDataSetChanged();
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
                Log.i(TAG, "onClick: clicked fb");
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

    public void reload() {
        if (mSearchType == 0) {
            mSearchByNameRunnable.run();
        } else if (mSearchType == 1) {
            loginFacebook();
        } else {
            mGetContactsInBackground.execute();
        }
    }

    public void showRetryButton(boolean show) {
        mReloadButton.setVisibility(show ? View.VISIBLE : View.GONE);
    }

    private void showServerErrorWithRetryButton() {
        mProgressBar.setVisibility(View.GONE);
        getActivity().runOnUiThread(new Runnable() {
            @Override
            public void run() {
                Utils.showServerErrorToast(getActivity());
                showRetryButton(true);
            }
        });
    }
}

