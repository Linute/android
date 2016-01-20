package com.linute.linute.MainContent.PeopleFragment;


import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import com.linute.linute.API.LSDKPeople;
import com.linute.linute.R;
import com.linute.linute.UtilsAndHelpers.Utils;
import com.squareup.okhttp.Callback;
import com.squareup.okhttp.Request;
import com.squareup.okhttp.Response;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.TimeZone;

/**
 * A simple {@link Fragment} subclass.
 */
public class PeopleFragment extends Fragment {
    private static final String TAG = PeopleFragment.class.getSimpleName();
    private RecyclerView recList;
    private LinearLayoutManager llm;
    private SwipeRefreshLayout mSwipeRefreshLayout;
    private PeopleAdapter mPeopleAdapter;

    private List<People> mPeopleList;

    public PeopleFragment() {
        // Required empty public constructor
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View rootView = inflater.inflate(R.layout.fragment_people, container, false);
        setHasOptionsMenu(true);

        mPeopleList = new ArrayList<>();

        recList = (RecyclerView) rootView.findViewById(R.id.people_frag_rec);
        recList.setHasFixedSize(true);
        llm = new LinearLayoutManager(getActivity());
        llm.setOrientation(LinearLayoutManager.VERTICAL);
        recList.setLayoutManager(llm);
//        recList.addItemDecoration(new DividerItemDecoration(getActivity(), null));


        mSwipeRefreshLayout = (SwipeRefreshLayout) rootView.findViewById(R.id.peoplefrag_swipe_refresh);
        mSwipeRefreshLayout.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                mSwipeRefreshLayout.setRefreshing(true);
                getPeople();
            }
        });

        mPeopleAdapter = new PeopleAdapter(mPeopleList, getContext());
        recList.setAdapter(mPeopleAdapter);

        getPeople();


        return rootView;
    }

    private void getPeople() {
        LSDKPeople people = new LSDKPeople(getActivity());
        people.getPeople(new HashMap<String, String>(), new Callback() {
            @Override
            public void onFailure(Request request, IOException e) {
                Log.d("TAG", "FAILED " + request.body());
            }

            @Override
            public void onResponse(Response response) throws IOException {

                String responsString = response.body().string();
                if (!response.isSuccessful()) {
                    Log.d("TAG", responsString);
                    return;
                }
                JSONObject jsonObject = null;
                JSONArray jsonArray = null;
                try {
                    jsonObject = new JSONObject(responsString);
                    jsonArray = jsonObject.getJSONArray("people");


                    People people;
                    String friend = "";
                    SimpleDateFormat simpleDateFormat = new SimpleDateFormat("yyyy-MM-dd'T'hh:mm:ss", Locale.US);
                    simpleDateFormat.setTimeZone(TimeZone.getTimeZone("UTC"));
                    Date myDate;
                    String dateString;

                    mPeopleList.clear();
                    for (int i = 0; i < jsonArray.length(); i++) {
                        jsonObject = (JSONObject) jsonArray.get(i);
                        friend = jsonObject.getString("friend");


                        myDate = simpleDateFormat.parse(jsonObject.getString("date"));
                        dateString = Utils.getEventTime(myDate);
                        Log.d(TAG, "getPeople " + jsonObject);

                        jsonObject = jsonObject.getJSONObject("owner");
                        people = new People(
                                jsonObject.getString("profileImage"),
                                jsonObject.getString("fullName"),
                                jsonObject.getString("id"),
                                dateString,
                                !friend.equals(""));
                        mPeopleList.add(people);
                    }
                } catch (JSONException | ParseException e) {
                    e.printStackTrace();
                }

                if (getActivity() == null) {
                    Log.d("TAG", "Null");
                    return;
                }
                getActivity().runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        if (mSwipeRefreshLayout.isRefreshing()) {
                            mSwipeRefreshLayout.setRefreshing(false);

                            mPeopleAdapter.notifyDataSetChanged();
                        }
                    }
                });
            }
        });
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        super.onCreateOptionsMenu(menu, inflater);
        //menu.clear();
        inflater.inflate(R.menu.people_fragment_menu, menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {

        int id = item.getItemId();

        if (R.id.people_fragment_menu_chat == id) {
            Toast.makeText(getActivity(), "Chat Coming Soon! WOOHOO", Toast.LENGTH_SHORT).show();
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

}
