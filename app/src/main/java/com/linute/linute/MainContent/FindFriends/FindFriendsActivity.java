package com.linute.linute.MainContent.FindFriends;

import android.Manifest;
import android.app.DialogFragment;
import android.content.ContentResolver;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.provider.ContactsContract;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.FragmentTransaction;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.View;
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

import com.linute.linute.UtilsAndHelpers.MaterialSearchView.MaterialSearchView;
import com.linute.linute.UtilsAndHelpers.Utils;
import com.squareup.okhttp.Callback;
import com.squareup.okhttp.Request;
import com.squareup.okhttp.Response;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.security.Permission;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Created by QiFeng on 1/16/16.
 */


//TODO: ADD RELOAD BUTTON

public class FindFriendsActivity extends AppCompatActivity {

    public static final String TAG = FindFriendsActivity.class.getSimpleName();

    public static final String SEARCH_TYPE_KEY = "search_type";

    public static final String FACEBOOK_TOKEN_KEY = "facebook_token";

    private int mSearchType = 0; // 0 for search, 1 for facebook, 2 for contacts

    private RecyclerView mRecyclerView;
    private MaterialSearchView mSearchView;
    private FriendSearchAdapter mFriendSearchAdapter;
    private String mQueryString; //what's in the search view
    private TextView mDescriptionText;

    private List<FriendSearchUser> mFriendFoundList = new ArrayList<>();

    private List<FriendSearchUser> mUnfilteredList = new ArrayList<>();
    //private String mFbToken;

    private CallbackManager mCallbackManager;

    private ProgressBar mProgressBar;


    private GetContactsInBackground mGetContactsInBackground;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_find_friends);

        if (getIntent() != null) {
            mSearchType = getIntent().getIntExtra(SEARCH_TYPE_KEY, 0);
        }

        mDescriptionText = (TextView) findViewById(R.id.findFriends_text);
        mRecyclerView = (RecyclerView) findViewById(R.id.findFriends_recycler_view);
        mSearchView = (MaterialSearchView) findViewById(R.id.findFriends_search_view);
        mProgressBar = (ProgressBar) findViewById(R.id.findFriends_progressbar);
        mSearchView.showSearch(false);


        LinearLayoutManager llm = new LinearLayoutManager(this);
        llm.setOrientation(LinearLayoutManager.VERTICAL);
        mRecyclerView.setLayoutManager(llm);

        mFriendSearchAdapter = new FriendSearchAdapter(this, mFriendFoundList, false);

        mRecyclerView.setAdapter(mFriendSearchAdapter);
        mRecyclerView.addItemDecoration(new DividerItemDecoration(this, null));


        setupSearchViewHandler();

        if (mSearchType == 0) { //if search by name, we need init text
            mDescriptionText.setText("Enter your friends name in the search bar");
            mDescriptionText.setVisibility(View.VISIBLE);
        } else if (mSearchType == 1) { //facebook
            FacebookSdk.sdkInitialize(getApplicationContext());

            //facebook
            mCallbackManager = CallbackManager.Factory.create();

            setUpFacebookCallback();

            loginFacebook();
        } else { //contacts. This process takes forever to get emails. We will use async task
            mGetContactsInBackground = new GetContactsInBackground();
            setUpContactsList();
        }
    }


    @Override
    protected void onSaveInstanceState(Bundle outState) {
        outState.putInt(SEARCH_TYPE_KEY, mSearchType);

        //TODO: add parcelable
        super.onSaveInstanceState(outState);
    }

    @Override
    protected void onRestoreInstanceState(Bundle savedInstanceState) {
        super.onRestoreInstanceState(savedInstanceState);
        mSearchType = savedInstanceState.getInt(SEARCH_TYPE_KEY);

        //TODO: get parcelable
    }

    @Override
    protected void onResume() {
        super.onResume();
    }

    @Override
    protected void onPause() {
        super.onPause();
        //remove callbacks? flaw: won't load then
    }

    @Override
    protected void onStop() {
        super.onStop();
        if (mGetContactsInBackground != null){ //cancel task
            mGetContactsInBackground.cancel(true);
        }
    }

    private Handler mSearchHandler; //handles filtering and searchign

    //Search my name inputted in searchbar
    private Runnable mSearchByNameRunnable = new Runnable() {
        @Override
        public void run() {

            new LSDKFriendSearch(FindFriendsActivity.this).searchFriendByName(mQueryString, new Callback() {
                @Override
                public void onFailure(Request request, IOException e) {
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            Utils.showBadConnectionToast(FindFriendsActivity.this);
                        }
                    });
                }

                @Override
                public void onResponse(Response response) throws IOException {
                    String resString = response.body().string();
                    if (response.isSuccessful()) {
                        try {
                            JSONObject json = new JSONObject(resString);

                            JSONArray friends = json.getJSONArray("friends");

                            if (friends != null) {
                                mFriendFoundList.clear();

                                for (int i = 0; i < friends.length(); i++) {
                                    mFriendFoundList.add(new FriendSearchUser(friends.getJSONObject(i)));
                                }
                            }

                            runOnUiThread(new Runnable() {
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
                            showServerError();

                        }
                    } else {
                        Log.e(TAG, "onResponse: " + resString);
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
            mFriendFoundList.clear();
            if (mQueryString.trim().isEmpty()) {
                for (FriendSearchUser user : mUnfilteredList)
                    mFriendFoundList.add(user);
            } else {
                for (FriendSearchUser user : mUnfilteredList)
                    if (user.nameContains(mQueryString)) {
                        mFriendFoundList.add(user);
                    }
                if (mFriendFoundList.isEmpty()) { //empty, show empty text
                    if (mDescriptionText.getVisibility() == View.INVISIBLE) {
                        mDescriptionText.setVisibility(View.VISIBLE);
                    }
                }
            }

            mFriendSearchAdapter.notifyDataSetChanged();
        }
    };

    //searching by name
    private MaterialSearchView.OnQueryTextListener mSearchListener = new MaterialSearchView.OnQueryTextListener() {
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
                mDescriptionText.setText("Enter your friends name in the search bar");
                mDescriptionText.setVisibility(View.VISIBLE);
            } else {
                if (mDescriptionText.getVisibility() == View.VISIBLE)
                    mDescriptionText.setVisibility(View.INVISIBLE);

                if (newText.length() > 2) { //will wait longer when less than 2 characters
                    mSearchHandler.postDelayed(mSearchByNameRunnable, 1000);
                } else { //searches more often else
                    mSearchHandler.postDelayed(mSearchByNameRunnable, 400);
                }
            }
            return false;
        }
    };

    //Filter or search TextChangeListners
    private MaterialSearchView.OnQueryTextListener mFilterListener = new MaterialSearchView.OnQueryTextListener() {
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
        //quits activity
        mSearchView.setOnBackPressedListener(new MaterialSearchView.OnBackPressedListener() {
            @Override
            public void onBackPressed() {
                FindFriendsActivity.this.onBackPressed();
            }
        });

        mSearchHandler = new Handler(); //searchs or filters as you type

        if (mSearchType == 0) { //search
            mSearchView.setOnQueryTextListener(mSearchListener);
        } else //filter
            mSearchView.setOnQueryTextListener(mFilterListener);
    }


    @Override
    public void onBackPressed() {
        if (getSupportFragmentManager().getBackStackEntryCount() > 0) {
            getSupportFragmentManager().popBackStack();
        } else {
            super.onBackPressed();
        }
    }

    public void addFragment(DialogFragment frag) {
        getFragmentManager().beginTransaction()
                .setTransition(FragmentTransaction.TRANSIT_FRAGMENT_OPEN)
                .add(R.id.postContainer, frag)
                .addToBackStack(null)
                .commit();
    }

    public void showServerError() {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                Utils.showServerErrorToast(FindFriendsActivity.this);
            }
        });
    }


    private void setUpFacebookList(String token) {
        new LSDKFriendSearch(this).searchFriendByFacebook(token, new Callback() {
            @Override
            public void onFailure(Request request, IOException e) {
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        Utils.showBadConnectionToast(FindFriendsActivity.this);
                        showRetryButton(true);
                    }
                });
            }

            @Override
            public void onResponse(Response response) throws IOException {
                if (response.isSuccessful()) {
                    try {
                        JSONObject json = new JSONObject(response.body().string());

                        JSONArray friends = json.getJSONArray("friends");

                        if (friends != null) {
                            for (int i = 0; i < friends.length(); i++) {
                                FriendSearchUser user = new FriendSearchUser(friends.getJSONObject(i));
                                mFriendFoundList.add(user);
                                mUnfilteredList.add(user);
                            }
                        }

                        runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                showRetryButton(false);

                                if (mFriendFoundList.isEmpty()) {
                                    mDescriptionText.setText("No results found");
                                    mDescriptionText.setVisibility(View.VISIBLE);
                                }
                                mFriendSearchAdapter.notifyDataSetChanged();
                            }
                        });

                    } catch (JSONException e) {
                        e.printStackTrace();
                        showServerErrorWithRetryButton();

                    }
                } else {
                    Log.e(TAG, "onResponse: " + response.body().string());
                    showServerErrorWithRetryButton();
                }
            }
        });
    }

    private static final int READ_CONTACTS_PERMISSION = 5;

    private void setUpContactsList() {
        //check for contact permissions
        //no permissions: ask for them
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.READ_CONTACTS) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.READ_CONTACTS}, READ_CONTACTS_PERMISSION);
        }
        //have permissions
        else {
            if (mInRationalText) {
                findViewById(R.id.findFriends_rationale_text).setVisibility(View.GONE);
            }

            mInRationalText = true;
            mProgressBar.setVisibility(View.VISIBLE);
            mGetContactsInBackground.execute();
        }
    }


    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
        if (requestCode == READ_CONTACTS_PERMISSION) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                if (mInRationalText) {
                    findViewById(R.id.findFriends_rationale_text).setVisibility(View.GONE);
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
        findViewById(R.id.findFriends_rationale_text).setVisibility(View.VISIBLE);
        findViewById(R.id.findFriends_turn_on).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                setUpContactsList();
            }
        });
    }

    private void loadContacts() {
        JSONArray phoneNumbers = new JSONArray();
        JSONArray emails = new JSONArray();
        
        ContentResolver cr = getContentResolver();
        
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
        new LSDKFriendSearch(this).searchFriendByContacts(phoneNumbers, emails, new Callback() {
            @Override
            public void onFailure(Request request, IOException e) {
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        Utils.showBadConnectionToast(FindFriendsActivity.this);
                        mProgressBar.setVisibility(View.GONE);
                        showRetryButton(true);
                    }
                });

            }

            @Override
            public void onResponse(Response response) throws IOException {
                if (response.isSuccessful()) {

                    try {
                        JSONObject json = new JSONObject(response.body().string());
                        JSONArray friends = json.getJSONArray("friends");

                        if (friends != null && friends.length() > 0){
                            for (int i = 0; i < friends.length(); i++) {
                                FriendSearchUser user = new FriendSearchUser(friends.getJSONObject(i));
                                mFriendFoundList.add(user);
                                mUnfilteredList.add(user);
                            }
                        }

                        runOnUiThread(new Runnable() {
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
                        showServerErrorWithRetryButton();
                    }

                } else {
                    Log.e(TAG, "onResponse: "+response.body().string() );
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
                    findViewById(R.id.findFriends_rationale_text).setVisibility(View.GONE);
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
        findViewById(R.id.findFriends_rationale_text).setVisibility(View.VISIBLE);

        ((TextView) findViewById(R.id.findFriends_text)).setText("Tapt needs access to you're facebook friends to find friends.");

        TextView mLogin = (TextView) findViewById(R.id.findFriends_turn_on);
        mLogin.setText("Log In");
        mLogin.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                loginFacebook();
            }
        });
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        mCallbackManager.onActivityResult(requestCode, resultCode, data);
    }

    private void showFacebookErrorToast() {
        Toast.makeText(this, R.string.bad_connection_text, Toast.LENGTH_SHORT).show();
    }


    public void loginFacebook() {
        AccessToken token = AccessToken.getCurrentAccessToken();

        if (token != null && token.getPermissions().contains("user_friends") && !token.isExpired())
            setUpFacebookList(token.getToken());

        else
            LoginManager.getInstance().logInWithReadPermissions(FindFriendsActivity.this, Collections.singletonList("user_friends"));
    }


    public class GetContactsInBackground extends AsyncTask<Void, Void ,Void>{
        @Override
        protected Void doInBackground(Void... params) {
            loadContacts();
            return null;
        }
    }

    public void reload(View v){
        if(mSearchType == 0){
            mSearchByNameRunnable.run();
        }else if(mSearchType == 1){
            loginFacebook();
        }else {
            mGetContactsInBackground.execute();
        }
    }

    public void showRetryButton(boolean show){
        findViewById(R.id.findFriends_reload_button).setVisibility(show? View.VISIBLE : View.GONE);
    }

    private void showServerErrorWithRetryButton(){
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                Utils.showServerErrorToast(FindFriendsActivity.this);
                showRetryButton(true);
            }
        });
    }
}

