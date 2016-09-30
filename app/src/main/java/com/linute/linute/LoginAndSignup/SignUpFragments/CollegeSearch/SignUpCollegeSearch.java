package com.linute.linute.LoginAndSignup.SignUpFragments.CollegeSearch;

import android.content.Context;
import android.os.Bundle;
import android.os.Handler;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.InputMethodManager;

import com.linute.linute.API.LSDKCollege;
import com.linute.linute.LoginAndSignup.College;
import com.linute.linute.LoginAndSignup.PreLoginActivity;
import com.linute.linute.LoginAndSignup.SignUpFragments.SignUpParentFragment;
import com.linute.linute.R;
import com.linute.linute.UtilsAndHelpers.MaterialSearchToolbar;
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
 * Created by QiFeng on 7/30/16.
 */
public class SignUpCollegeSearch extends Fragment {

    public static final String TAG = SignUpCollegeSearch.class.getSimpleName();
    public static final String LONGITUDE_KEY = "long_key";
    public static final String LATITUDE_KEY = "lat_key";

    private double mLongitude;
    private double mLatitude;

    private short mType;

    private SignUpCollegeAdapter mCollegeAdapter;

    public static final short TYPE_SEARCH = 0;
    public static final short TYPE_NEARBY = 1;

    private CollegeSelected mCollegeSelected;

    private ArrayList<College> mColleges = new ArrayList<>();
    private ArrayList<College> mUnfilteredColleges = new ArrayList<>();

    private Call mCall;
    private Handler mSeachHandler = new Handler();
    private Handler mResponseHandler = new Handler();

    private boolean mUpdated = false;


    private RecyclerView vRecyclerView;
    private MaterialSearchToolbar vMaterialSearch;
    private View vEmpty;
    private View vProgress;


    public static SignUpCollegeSearch newInstance(double longitude, double latitude) {

        SignUpCollegeSearch search = new SignUpCollegeSearch();
        Bundle args = new Bundle();
        args.putDouble(LONGITUDE_KEY, longitude);
        args.putDouble(LATITUDE_KEY, latitude);
        search.setArguments(args);

        return search;
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        if (getArguments() != null) {
            mLongitude = getArguments().getDouble(LONGITUDE_KEY);
            mLatitude = getArguments().getDouble(LATITUDE_KEY);
            mType = TYPE_NEARBY;
        } else mType = TYPE_SEARCH;
    }


    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View root = LayoutInflater.from(getContext()).inflate(R.layout.fragment_sign_up_search_college, container, false);

        vMaterialSearch = (MaterialSearchToolbar) root.findViewById(R.id.toolbar);

        vMaterialSearch.setNavigationOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                getFragmentManager().popBackStack();
            }
        });

        vMaterialSearch.setSearchActions(new MaterialSearchToolbar.SearchActions() {
            @Override
            public void search(final String query) {
                mSeachHandler.removeCallbacksAndMessages(null);
                mSeachHandler.postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        if (query.isEmpty())
                            mResponseHandler.post(new Runnable() {
                                @Override
                                public void run() {
                                    mColleges.clear();
                                    mColleges.addAll(mUnfilteredColleges);
                                    mCollegeAdapter.notifyDataSetChanged();
                                    showList();
                                }
                            });
                        else
                            searchCollege(query);
                    }
                }, 300);
            }
        });

        if (mCollegeAdapter == null) {
            mCollegeAdapter = new SignUpCollegeAdapter(mColleges);
        }

        mCollegeAdapter.setOnCollegeSelected(new SignUpCollegeAdapter.OnCollegeSelected() {
            @Override
            public void onCollegeSelected(College college) {
                if (mCollegeSelected != null) {
                    mCollegeSelected.onCollegeSelected(college);
                    getFragmentManager().popBackStack();
                }
            }
        });

        vRecyclerView = (RecyclerView) root.findViewById(R.id.collegePicker_recycler_view);
        vRecyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
        vRecyclerView.setAdapter(mCollegeAdapter);

        vRecyclerView.addOnScrollListener(new RecyclerView.OnScrollListener() {
            @Override
            public void onScrollStateChanged(RecyclerView recyclerView, int newState) {
                super.onScrollStateChanged(recyclerView, newState);
                if (newState == RecyclerView.SCROLL_STATE_DRAGGING && vMaterialSearch.hasFocus())
                    hideKeyboard();
            }
        });


        vProgress = root.findViewById(R.id.collegePicker_progress_bar);
        vEmpty = root.findViewById(R.id.collegePicker_empty_results);

        if (!mUpdated)
            getInitialList();


        vMaterialSearch.requestFocus();

        InputMethodManager imm = (InputMethodManager) getActivity().getSystemService(Context.INPUT_METHOD_SERVICE);
        imm.toggleSoftInput(InputMethodManager.SHOW_FORCED,0);

        return root;
    }


    private void getInitialList() {
        if (mType == TYPE_SEARCH)
            getSearchList();
        else
            getNearbyColleges();

    }


    private void getNearbyColleges() {
        if (getContext() == null) return;

        mUpdated = true;
        showProgress();
        HashMap<String, Object> param = new HashMap<>();

        param.put("latitude", mLatitude + "");
        param.put("longitude", mLongitude + "");

        new LSDKCollege(getContext()).getColleges(param, new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {
                if (getActivity() != null) {
                    getActivity().runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            Utils.showBadConnectionToast(getContext());
                        }
                    });
                }
            }

            @Override
            public void onResponse(Call call, Response response) throws IOException {
                if (response.isSuccessful()) {
                    try {
                        JSONArray obj = new JSONObject(response.body().string()).getJSONArray("colleges");

                        final ArrayList<College> tempColleges = new ArrayList<>();
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
                                            mUnfilteredColleges.clear();
                                            mUnfilteredColleges.addAll(tempColleges);

                                            if (vMaterialSearch.getText().isEmpty()) {
                                                mColleges.clear();
                                                mColleges.addAll(mUnfilteredColleges);
                                                mCollegeAdapter.notifyDataSetChanged();
                                                showList();
                                            }
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
                                }
                            });
                        }
                    }
                } else {
                    Log.i(TAG, "onResponse: " + response.body().string());
                    if (getActivity() != null) {
                        getActivity().runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                Utils.showServerErrorToast(getContext());
                            }
                        });
                    }
                }
            }
        });
    }


    private void getSearchList() {
        if (getContext() == null) return;

        mUpdated = true;

        showProgress();

        HashMap<String, Object> params = new HashMap<>();

        params.put("name", "");
        params.put("limit", "30");

        new LSDKCollege(getContext()).getColleges(params, new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {
                if (getActivity() != null && !call.isCanceled()) {
                    getActivity().runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            Utils.showBadConnectionToast(getContext());
                        }
                    });
                }
            }

            @Override
            public void onResponse(Call call, Response response) throws IOException {
                if (response.isSuccessful()) {
                    try {
                        JSONArray obj = new JSONObject(response.body().string()).getJSONArray("colleges");

                        final ArrayList<College> tempColleges = new ArrayList<>();
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
                                            mUnfilteredColleges.clear();
                                            mUnfilteredColleges.addAll(tempColleges);

                                            if (vMaterialSearch.getText().isEmpty()) {
                                                mColleges.clear();
                                                mColleges.addAll(mUnfilteredColleges);
                                                mCollegeAdapter.notifyDataSetChanged();
                                                showList();
                                            }
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
                            }
                        });
                    }
                }
            }
        });

    }

    private void searchCollege(String name) {

        if (mCall != null) mCall.cancel();
        if (getContext() == null) return;

        showProgress();

        HashMap<String, Object> params = new HashMap<>();

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
                        }
                    });
                }
            }

            @Override
            public void onResponse(Call call, Response response) throws IOException {
                if (response.isSuccessful()) {
                    try {
                        JSONArray obj = new JSONObject(response.body().string()).getJSONArray("colleges");

                        final ArrayList<College> tempColleges = new ArrayList<>();
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
                                            mColleges.clear();
                                            mColleges.addAll(tempColleges);
                                            mCollegeAdapter.notifyDataSetChanged();

                                            if (mColleges.isEmpty()) showEmpty();
                                            else showList();
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
                            }
                        });
                    }
                }
            }
        });
    }


    public void showEmpty() {
        vRecyclerView.setVisibility(View.GONE);
        vEmpty.setVisibility(View.VISIBLE);
        vProgress.setVisibility(View.GONE);
    }

    public void showProgress() {
        vRecyclerView.setVisibility(View.GONE);
        vEmpty.setVisibility(View.GONE);
        vProgress.setVisibility(View.VISIBLE);
    }

    public void showList() {
        vRecyclerView.setVisibility(View.VISIBLE);
        vEmpty.setVisibility(View.GONE);
        vProgress.setVisibility(View.GONE);
    }


    public ArrayList<String> getEmailsArray(JSONArray email) throws JSONException {
        ArrayList<String> emailList = new ArrayList<>();
        for (int i = 0; i < email.length(); i++)
            emailList.add(email.getString(i));

        return emailList;
    }

    public void setCollegeSelected(CollegeSelected collegeSelected) {
        mCollegeSelected = collegeSelected;
    }

    public interface CollegeSelected {
        void onCollegeSelected(College college);

    }


    @Override
    public void onStart() {
        super.onStart();
        SignUpParentFragment frag = (SignUpParentFragment) getParentFragment();
        if (frag != null) {
            frag.showToolbar(false);
        }


        PreLoginActivity act = (PreLoginActivity) getActivity();
        if (act != null) {
            act.showChat(false);
        }
    }

    @Override
    public void onStop() {
        super.onStop();
        hideKeyboard();

        SignUpParentFragment frag = (SignUpParentFragment) getParentFragment();
        if (frag != null) {
            frag.showToolbar(true);
        }

        PreLoginActivity act = (PreLoginActivity) getActivity();
        if (act != null) {
            act.showChat(true);
        }
    }

    private void hideKeyboard() {
        if (getActivity() == null) return;
        InputMethodManager imm = (InputMethodManager) getActivity().getSystemService(Context.INPUT_METHOD_SERVICE);
        imm.hideSoftInputFromWindow(vMaterialSearch.getWindowToken(), 0);
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        mCollegeSelected = null;
    }
}
