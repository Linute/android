package com.linute.linute.LoginAndSignup;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.Handler;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.SearchView;
import android.text.InputType;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.linute.linute.API.LSDKCollege;
import com.linute.linute.API.LSDKUser;
import com.linute.linute.MainContent.MainActivity;
import com.linute.linute.R;
import com.linute.linute.UtilsAndHelpers.DividerItemDecoration;
import com.linute.linute.UtilsAndHelpers.LinuteConstants;
import com.linute.linute.UtilsAndHelpers.LinuteUser;
import com.linute.linute.UtilsAndHelpers.Utils;
import com.squareup.okhttp.Callback;
import com.squareup.okhttp.Request;
import com.squareup.okhttp.Response;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class CollegePickerActivity extends AppCompatActivity implements SearchView.OnQueryTextListener{

    public static final String TAG = CollegePickerActivity.class.getSimpleName();

    private RecyclerView mRecyclerView;
    private TextView mEmptyView;
    private List<College> mColleges = new ArrayList<>();
    private CollegeListAdapter mCollegeListAdapter;
    private SearchView mSearchView;
    private ProgressBar mProgressBar;

    private static final String INIT_STRING = "Search for your college by entering your college's name into the search bar";
    private static final String ERROR_STRING = "Error communicating to the server";
    private static final String NO_INTERNET = "No internet connection found";
    private static final String NO_COLLEGES = "No colleges found";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_college_picker);

        mRecyclerView = (RecyclerView) findViewById(R.id.collegePicker_recycler_view);
        mEmptyView = (TextView) findViewById(R.id.collegePicker_empty_results);
        mSearchView = (SearchView) findViewById(R.id.collegePicker_search_view);
        mProgressBar = (ProgressBar) findViewById(R.id.collegePicker_progress_bar);

        mEmptyView.setText(INIT_STRING);

        LinearLayoutManager llm = new LinearLayoutManager(this);
        llm.setOrientation(LinearLayoutManager.VERTICAL);
        mRecyclerView.setLayoutManager(llm);

        mCollegeListAdapter = new CollegeListAdapter(this, mColleges);

        mRecyclerView.setAdapter(mCollegeListAdapter);
        mRecyclerView.addItemDecoration(new DividerItemDecoration(this, null));

        setupSearchView();
        Utils.testLog(this, TAG);
    }

    private void setupSearchView() {
        mSearchView.setOnQueryTextListener(this);
        mSearchView.setIconifiedByDefault(false);
        mSearchView.setInputType(InputType.TYPE_CLASS_TEXT);
        mSearchView.setIconified(false);
        mSearchView.setQueryHint("Search College");
    }


    @Override
    public boolean onQueryTextSubmit(String query) {
        return false;
    }

    //this will run if user stops typing
    final Handler mSearchHandler = new Handler();

    @Override
    public boolean onQueryTextChange(String newText) {

        mSearchHandler.removeCallbacks(getColleges); //don't search

        if (TextUtils.isEmpty(newText)){
            mColleges.clear();
            if (mEmptyView.getVisibility() == View.GONE){
                mEmptyView.setVisibility(View.VISIBLE);
                mEmptyView.setText(INIT_STRING);
                mCollegeListAdapter.notifyDataSetChanged();
            }
        }else {
            if (mEmptyView.getVisibility() == View.VISIBLE){
                mEmptyView.setVisibility(View.GONE);
            }
            mSearchHandler.postDelayed(getColleges, 350);
        }
        return true;
    }

    @Override
    protected void onPause() {
        super.onPause();
        mSearchHandler.removeCallbacks(getColleges);
    }

    private Runnable getColleges = new Runnable() {
        @Override
        public void run() {
            retrieveColleges();
        }
    };

    private void retrieveColleges(){
        Map<String, String> params = new HashMap<>();
        params.put("name", mSearchView.getQuery().toString());

        new LSDKCollege(this).getColleges(params, new Callback() {
            @Override
            public void onFailure(Request request, IOException e) {

                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        if (mColleges.isEmpty()) {
                            mEmptyView.setText(NO_INTERNET);

                            if (mEmptyView.getVisibility() == View.GONE)
                                mEmptyView.setVisibility(View.VISIBLE);

                        } else {
                            Utils.showBadConnectionToast(CollegePickerActivity.this);
                        }
                    }
                });
            }

            @Override
            public void onResponse(Response response) throws IOException {

                if (response.isSuccessful()) {
                    try {
                        JSONArray colleges = (new JSONObject(response.body().string())).getJSONArray("colleges");

                        if (colleges == null) { //shouldn't be null
                            Log.e(TAG, "SHIT! Colleges JSONArray was null");
                            return;
                        }

                        mColleges.clear(); //clear colleges

                        for (int i = 0; i < colleges.length(); i++) { //add new college results
                            mColleges.add(new College(colleges.getJSONObject(i)));
                        }

                        if (mColleges.isEmpty()) { //if empty, tell user no result found
                            runOnUiThread(new Runnable() {
                                @Override
                                public void run() {
                                    mEmptyView.setText(NO_COLLEGES);
                                    if (mEmptyView.getVisibility() == View.GONE)
                                        mEmptyView.setVisibility(View.VISIBLE);
                                }
                            });
                        }

                        notifyChange();

                    } catch (JSONException e) {
                        e.printStackTrace();
                        showServerErrorToast();
                    }
                } else {
                    Log.e(TAG, "onResponse: " + response.body().string());
                    showServerErrorToast();
                }
            }
        });
    }

    private void showServerErrorToast(){
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                Utils.showServerErrorToast(CollegePickerActivity.this);
            }
        });
    }

    private void notifyChange(){
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                mCollegeListAdapter.notifyDataSetChanged();
            }
        });
    }

    public void showConfirmationDialog(String collegName, final String collegeId){
        new AlertDialog.Builder(CollegePickerActivity.this)
                .setTitle("Confirm College")
                .setMessage("Set your school to "+collegName+"?")
                .setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.cancel();
                    }
                })
                .setPositiveButton("Confirm", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        updateCollege(collegeId);
                    }
                })
                .create()
                .show();
    }

    private void updateCollege(String collegeId){
        Map<String, Object> newInfo = new HashMap<>();
        newInfo.put("college", collegeId);

        showProgressBar(true);

        new LSDKUser(this).updateUserInfo(newInfo, null, new Callback() {
            @Override
            public void onFailure(Request request, IOException e) {
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        Utils.showBadConnectionToast(CollegePickerActivity.this);
                        showProgressBar(false);
                    }
                });
            }

            @Override
            public void onResponse(Response response) throws IOException {
                if (response.isSuccessful()) {
                    String responseString = response.body().string();
                    Log.i(TAG, "onResponse: " + responseString);
                    try {

                        boolean saved = saveCollege(LinuteUser.getCollegeFromJson(new JSONObject(responseString)));

                        if (saved) //we saved college
                            goToMainActivity();
                        else //problem saving college
                            showServerErrorAndHideProgressBar();

                    } catch (JSONException e) {
                        e.printStackTrace();
                        showServerErrorAndHideProgressBar();
                    }
                } else {
                    showServerErrorAndHideProgressBar();
                    Log.e(TAG, response.body().string());
                }
            }
        });
    }

    private void showProgressBar(boolean show){
        mProgressBar.setVisibility(show ? View.VISIBLE : View.GONE);
        mRecyclerView.setVisibility(show ? View.INVISIBLE : View.VISIBLE);
    }

    private boolean saveCollege(LinuteUser.CollegeNameAndID college){
        if (college == null) return false;

        SharedPreferences.Editor sharedPref = getSharedPreferences(LinuteConstants.SHARED_PREF_NAME, MODE_PRIVATE).edit();
        sharedPref.putString("collegeName", college.getCollegeName());
        sharedPref.putString("collegeId", college.getCollegeId());
        sharedPref.apply();
        return true;
    }

    private void showServerErrorAndHideProgressBar(){
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                Utils.showServerErrorToast(CollegePickerActivity.this);
                showProgressBar(false);
            }
        });
    }

    private void goToMainActivity(){
        Intent i = new Intent(this, MainActivity.class);
        i.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK | Intent.FLAG_ACTIVITY_NEW_TASK);
        startActivity(i);
        overridePendingTransition(0, 0); //no transition effects
        this.finish();
    }


}
