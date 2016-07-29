package com.linute.linute.LoginAndSignup.SignUpFragments;

import android.content.Context;
import android.content.DialogInterface;
import android.os.Bundle;
import android.os.Handler;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v7.app.AlertDialog;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;
import android.widget.Toast;

import com.linute.linute.API.LSDKCollege;
import com.linute.linute.LoginAndSignup.College;
import com.linute.linute.R;
import com.linute.linute.UtilsAndHelpers.Utils;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.Response;

/**
 * Created by QiFeng on 7/28/16.
 */
class SignUpCollegeFragment extends Fragment {

    public static final String TAG = SignUpCollegeFragment.class.getSimpleName();

    private ArrayList<College> mNearbyColleges = new ArrayList<>();
    private ArrayList<College> mFilteredList = new ArrayList<>();

    private View vProgress;
    private View vEmpty;
    private View vNearby;
    private EditText vSearch;
    private RecyclerView vRecyclerView;

    private Call mCall;
    private Handler mSeachHandler = new Handler();
    private Handler mResponseHandler = new Handler();

    private SignUpCollegeAdapter mSignUpCollegeAdapter;

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View root = inflater.inflate(R.layout.fragment_sign_up_college, container, false);

        vProgress = root.findViewById(R.id.progress);
        vEmpty = root.findViewById(R.id.empty_view);
        vNearby = root.findViewById(R.id.nearby);

        vRecyclerView = (RecyclerView) root.findViewById(R.id.recycler_view);
        vRecyclerView.setLayoutManager(new LinearLayoutManager(getContext(), LinearLayoutManager.VERTICAL, false));

        if (mSignUpCollegeAdapter == null) {
            mSignUpCollegeAdapter = new SignUpCollegeAdapter(mFilteredList);
            mSignUpCollegeAdapter.setOnCollegeSelected(new SignUpCollegeAdapter.OnCollegeSelected() {
                @Override
                public void onCollegeSelected(College college) {
                    collegeSelected(college);
                }
            });
        }

        vRecyclerView.setAdapter(mSignUpCollegeAdapter);

        vRecyclerView.addOnScrollListener(new RecyclerView.OnScrollListener() {
            @Override
            public void onScrollStateChanged(RecyclerView recyclerView, int newState) {
                super.onScrollStateChanged(recyclerView, newState);
                if (newState == RecyclerView.SCROLL_STATE_DRAGGING && vSearch.hasFocus())
                    hideKeyboard();
            }
        });

        vSearch = (EditText) root.findViewById(R.id.search);
        vSearch.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
            }

            @Override
            public void afterTextChanged(final Editable s) {
                mSeachHandler.removeCallbacksAndMessages(null);
                if (s.toString().isEmpty()) {
                    mResponseHandler.removeCallbacksAndMessages(null);
                    if (mNearbyColleges.isEmpty()) {
                        showNearby();
                    } else {
                        mResponseHandler.post(new Runnable() {
                            @Override
                            public void run() {
                                mFilteredList.clear();
                                mFilteredList.addAll(mNearbyColleges);
                                mSignUpCollegeAdapter.notifyDataSetChanged();
                                showFiltered();
                            }
                        });
                    }
                } else {
                    mSeachHandler.postDelayed(new Runnable() {
                        @Override
                        public void run() {
                            searchColleges(s.toString());
                        }
                    }, 300);
                }
            }
        });

        vNearby.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //// TODO: 7/28/16  
                Toast.makeText(getContext(), "TO DO", Toast.LENGTH_SHORT).show();
            }
        });


        return root;
    }


    private void showProgress() {
        vProgress.setVisibility(View.VISIBLE);
        vNearby.setVisibility(View.GONE);
        vEmpty.setVisibility(View.GONE);
        vRecyclerView.setVisibility(View.GONE);
    }

    private void showNearby() {
        vProgress.setVisibility(View.GONE);
        vNearby.setVisibility(View.VISIBLE);
        vEmpty.setVisibility(View.GONE);
        vRecyclerView.setVisibility(View.GONE);
    }

    private void showFiltered() {
        vProgress.setVisibility(View.GONE);
        vNearby.setVisibility(View.GONE);
        vEmpty.setVisibility(View.GONE);
        vRecyclerView.setVisibility(View.VISIBLE);
    }

    private void showEmpty() {
        vProgress.setVisibility(View.GONE);
        vNearby.setVisibility(View.GONE);
        vEmpty.setVisibility(View.VISIBLE);
        vRecyclerView.setVisibility(View.GONE);
    }

    private void collegeSelected(final College college) {
        if (getContext() == null) return;

        new AlertDialog.Builder(getContext())
                .setTitle("Confirm")
                .setMessage("Are you sure you want to set " + college.getCollegeName() + " as your school?")
                .setPositiveButton("Yes", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        SignUpParentFragment fragment = (SignUpParentFragment) getParentFragment();

                        if (fragment != null) {
                            fragment.getSignUpInfo().setCollegeId(college.getCollegeId());
                            fragment.getSignUpInfo().setCollegeName(college.getCollegeName());
                            fragment.addFragment(SignUpEmailFragment.newInstance(college), SignUpEmailFragment.TAG);
                        }

                    }
                })
                .setNegativeButton("No", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.dismiss();
                    }
                })
                .show();
    }


    private void searchColleges(String name) {
        if (mCall != null) mCall.cancel();
        if (getContext() == null) return;

        HashMap<String, String> params = new HashMap<>();

        params.put("name", name);
        params.put("limit", "30");

        mCall = new LSDKCollege(getContext()).getColleges(params, new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {
                if (getActivity() != null && !call.isCanceled()) {
                    getActivity().runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            Utils.showBadConnectionToast(getContext());
                            if (mFilteredList.isEmpty()) {
                                showEmpty();
                            } else {
                                showFiltered();
                            }
                        }
                    });
                }
            }

            @Override
            public void onResponse(Call call, Response response) throws IOException {
                if (response.isSuccessful()) {
                    try {
                        JSONArray obj = new JSONObject(response.body().string()).getJSONArray("colleges");

                        final ArrayList<College> tempColleges = new ArrayList<College>();
                        //Log.i(TAG, "onResponse: "+obj.toString(4));
                        JSONObject college;
                        String address;
                        ArrayList<String> excludeList;
                        ArrayList<String> includeList;

                        JSONArray emails;

                        for (int i = 0; i < obj.length(); i++) {
                            try {
                                college = obj.getJSONObject(i);
                                address = "";
                                address += college.getString("address") + ", " + college.getString("city") + ", " + college.getString("state") + " " + college.getString("zip");

                                emails = college.getJSONArray("excludedEmails");
                                excludeList = getEmailsArray(emails);

                                emails = college.getJSONArray("includedEmails");
                                includeList = getEmailsArray(emails);

                                tempColleges.add(
                                        new College(
                                                college.getString("name"),
                                                college.getString("id"),
                                                address,
                                                includeList,
                                                excludeList
                                        )
                                );
                            } catch (JSONException e) {
                                e.printStackTrace();
                            }
                        }

                        if (getActivity() != null) {
                            getActivity().runOnUiThread(new Runnable() {
                                @Override
                                public void run() {
                                    mResponseHandler.post(new Runnable() {
                                        @Override
                                        public void run() {
                                            mFilteredList.clear();
                                            mFilteredList.addAll(tempColleges);
                                            mSignUpCollegeAdapter.notifyDataSetChanged();

                                            if (mFilteredList.isEmpty()) showEmpty();
                                            else showFiltered();
                                        }
                                    });
                                }
                            });
                        }

                    } catch (JSONException e) {
                        e.printStackTrace();
                        if (getActivity() != null) {
                            getActivity().runOnUiThread(new Runnable() {
                                @Override
                                public void run() {
                                    Utils.showServerErrorToast(getContext());
                                    if (mFilteredList.isEmpty()) {
                                        showEmpty();
                                    } else {
                                        showFiltered();
                                    }
                                }
                            });
                        }
                    }
                } else {
                    if (getActivity() != null) {
                        getActivity().runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                Utils.showServerErrorToast(getContext());
                                if (mFilteredList.isEmpty()) {
                                    showEmpty();
                                } else {
                                    showFiltered();
                                }
                            }
                        });
                    }
                }
            }
        });
    }


    public ArrayList<String> getEmailsArray(JSONArray email) throws JSONException {
        ArrayList<String> emailList = new ArrayList<>();
        for (int i = 0; i < email.length(); i++)
            emailList.add(email.getString(i));

        return emailList;
    }

    @Override
    public void onStop() {
        super.onStop();
        hideKeyboard();

    }


    private void hideKeyboard() {
        if (getActivity() == null) return;
        InputMethodManager imm = (InputMethodManager) getActivity().getSystemService(Context.INPUT_METHOD_SERVICE);
        imm.hideSoftInputFromWindow(vSearch.getWindowToken(), 0);
    }
}
