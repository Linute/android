package com.linute.linute.MainContent.Chat;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.Handler;
import android.support.v4.app.Fragment;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;

import com.linute.linute.API.LSDKChat;
import com.linute.linute.R;
import com.linute.linute.UtilsAndHelpers.BaseTaptActivity;
import com.linute.linute.UtilsAndHelpers.LinuteConstants;
import com.linute.linute.UtilsAndHelpers.Utils;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.Response;

public class CreateChatFragment extends Fragment implements UserSelectAdapter.OnUserSelectedListener{
    // the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
    private static final String TAG = CreateChatFragment.class.getSimpleName();

    private UserSelectAdapter mSearchAdapter;
    private SelectedUsersAdapter mSelectedAdapter;

    private ArrayList<User> mSelectedUsers = new ArrayList<>();
    private List<User> mSearchUserList = new ArrayList<>();
    private SharedPreferences mSharedPreferences;

    private EditText editText;

    private Handler mHandler = new Handler();
    private RecyclerView mSelectedRV;
    private RecyclerView mSearchRV;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        mSharedPreferences = getContext().getSharedPreferences(LinuteConstants.SHARED_PREF_NAME, Context.MODE_PRIVATE);
        return inflater.inflate(R.layout.fragment_search_users, container, false);
    }

    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        Toolbar toolbar = (Toolbar) view.findViewById(R.id.search_user_toolbar);
        toolbar.setNavigationIcon(R.drawable.ic_action_navigation_arrow_back_inverted);
        toolbar.setNavigationOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (getActivity() != null){
                    getActivity().onBackPressed();
                }
            }
        });

        toolbar.inflateMenu(R.menu.menu_create_chat);
        toolbar.getMenu().findItem(R.id.menu_item_create).setOnMenuItemClickListener(new MenuItem.OnMenuItemClickListener() {
            @Override
            public boolean onMenuItemClick(MenuItem menuItem) {
                BaseTaptActivity activity = (BaseTaptActivity)getActivity();
                activity.replaceContainerWithFragment(ChatFragment.newInstance(null, mSelectedUsers));
                return true;
            }
        });


        mSelectedAdapter = new SelectedUsersAdapter(mSelectedUsers);
        mSelectedRV = (RecyclerView) view.findViewById(R.id.selected_users);
        LinearLayoutManager selectedLLM = new LinearLayoutManager(getActivity());
        selectedLLM.setOrientation(LinearLayoutManager.HORIZONTAL);
        mSelectedRV.setLayoutManager(selectedLLM);
        mSelectedRV.setAdapter(mSelectedAdapter);


        mSearchAdapter = new UserSelectAdapter(getActivity(), mSearchUserList);
        mSearchRV = (RecyclerView) view.findViewById(R.id.search_users);
        mSearchRV.setHasFixedSize(true);
        LinearLayoutManager llm = new LinearLayoutManager(getActivity());
        llm.setOrientation(LinearLayoutManager.VERTICAL);
        mSearchRV.setLayoutManager(llm);
        mSearchRV.setAdapter(mSearchAdapter);


        mSearchAdapter.setOnUserSelectedListener(this);

        editText = (EditText) view.findViewById(R.id.search_users_entry);
        editText.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                getUsers(s.toString());
            }

            @Override
            public void afterTextChanged(Editable s) {

            }
        });

        getUsers("");
    }

    @Override
    public void onUserSelected(User user) {
        if(!mSelectedUsers.contains(user)) {
            mSelectedUsers.add(user);
           // mSearchUserList.remove(user);
            mSelectedAdapter.notifyItemInserted(mSelectedUsers.size()-1);
            mSelectedRV.getLayoutManager().scrollToPosition(mSelectedUsers.size()-1);
            editText.setText("");
//            mSearchAdapter.notifyDataSetChanged();
        }
    }

    @Override
    public void onStop() {
        super.onStop();

        if (editText.hasFocus() && getActivity() != null){
            InputMethodManager imm = (InputMethodManager) getActivity().getSystemService(Context.INPUT_METHOD_SERVICE);
            imm.hideSoftInputFromWindow(editText.getWindowToken(), 0);
        }
    }

    private void getUsers(String searchWord) {
        LSDKChat users = new LSDKChat(getActivity());
        Map<String, String> newChat = new HashMap<>();
        newChat.put("owner", mSharedPreferences.getString("userID", null));

        if (!searchWord.equals("")) {
            newChat.put("fullName", searchWord);
        }

        users.getUsers(newChat, new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {
                if (getActivity() != null){
                    getActivity().runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            Utils.showBadConnectionToast(getActivity());
                        }
                    });
                }
            }

            @Override
            public void onResponse(Call call, Response response) throws IOException {
                if (!response.isSuccessful()) {
                    Log.d(TAG, "onResponseNotSuccessful: " + response.body().string());
                    if (getActivity() != null){
                        getActivity().runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                Utils.showServerErrorToast(getActivity());
                            }
                        });
                    }
                } else {

//                    mSearchUserList.clear();
                    ArrayList<User> tempUsers = new ArrayList<>();
                    JSONObject jsonObject;
                    JSONArray friends;
                    try {
                        jsonObject = new JSONObject(response.body().string());
                        friends = jsonObject.getJSONArray("friends");
                        JSONObject user;
                        for (int i = 0; i < friends.length(); i++) {
                            user = ((JSONObject) friends.get(i)).getJSONObject("user");
                            if(findUser(mSelectedUsers, user.getString("id")) == -1) {
                                tempUsers.add(new User(
                                        user.getString("id"),
                                        user.getString("fullName"), user.getString("profileImage")
                                ));
                            }
                        }

                        mSearchUserList.clear();
                        mSearchUserList.addAll(tempUsers);

                        if (getActivity() != null) {
                            getActivity().runOnUiThread(new Runnable() {
                                @Override
                                public void run() {
                                    mHandler.removeCallbacksAndMessages(null);
                                    mHandler.post(new Runnable() {
                                        @Override
                                        public void run() {
                                            mSearchAdapter.notifyDataSetChanged();
                                        }
                                    });
                                }
                            });
                        }

                    } catch (JSONException e) {
                        e.printStackTrace();
                        if (getActivity() != null){
                            getActivity().runOnUiThread(new Runnable() {
                                @Override
                                public void run() {
                                    Utils.showServerErrorToast(getActivity());
                                }
                            });
                        }
                    }
                }
            }
        });
    }

    protected int findUser(ArrayList<User> users, User searchUser){
        for(int i=0;i<users.size();i++){
            User user = users.get(i);
            if(user.userId.equals(searchUser.userId)){
                return i;
            }
        }
        return -1;
    }

    protected int findUser(ArrayList<User> users, String id){
        for(int i=0;i<users.size();i++){
            User user = users.get(i);
            if(user.userId.equals(id)){
                return i;
            }
        }
        return -1;
    }

}
