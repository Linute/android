package com.linute.linute.MainContent.Chat;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.Handler;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.util.TypedValue;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;
import android.widget.TextView;

import com.bumptech.glide.Glide;
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

public class SelectUsersFragment extends Fragment implements UserGroupSearchAdapter2.OnUserClickListener {
    // the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
    private static final String TAG = SelectUsersFragment.class.getSimpleName();

    //    protected UserSelectAdapter mSearchAdapter;
    protected UserSelectAdapter2 mSearchAdapter2;
    protected SelectedUsersAdapter mSelectedAdapter;

    protected ArrayList<User> mSelectedUsers = new ArrayList<>();
    protected List<User> mSearchUserList = new ArrayList<>();

    protected ArrayList<User> mLockedUsers;

    private EditText editText;
    private TextView firstUserName;

    protected Handler mHandler = new Handler();
    protected RecyclerView mSelectedRV;
    protected View mSelectedContainer;
    protected RecyclerView mSearchRV;

    protected final static String KEY_LOCKED_USERS = "locked";
    protected final static String KEY_SELECTED_USERS = "selected";

    /*
    * A focused user appears as a UserListItem at the top of the Search list
    * A user gets focused when their icon is touched from inside the SelectedUsersRV
    * Only one user is focused at a time, if another user is already focused, they're removed
    * If a user is already focused, they'll refocus to draw attention to the user
    * */
    protected User focusedUser = null;
    protected View vEmpty;
    protected View vProgress;

    SharedPreferences mSharedPreferences;

    public static SelectUsersFragment newInstance(ArrayList<User> lockedUsers) {
        Bundle arguments = new Bundle();
        arguments.putParcelableArrayList(KEY_LOCKED_USERS, lockedUsers);
        SelectUsersFragment selectUserFragment = new SelectUsersFragment();
        selectUserFragment.setArguments(arguments);
        return selectUserFragment;
    }

    public static SelectUsersFragment newInstance(ArrayList<User> lockedUsers, ArrayList<User> selectedUsers) {
        Bundle arguments = new Bundle();
        arguments.putParcelableArrayList(KEY_LOCKED_USERS, lockedUsers);
        arguments.putParcelableArrayList(KEY_SELECTED_USERS, selectedUsers);
        SelectUsersFragment selectUserFragment = new SelectUsersFragment();
        selectUserFragment.setArguments(arguments);
        return selectUserFragment;
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Bundle arguments = getArguments();
        if (arguments != null) {
            ArrayList<User> lockedUsersArg = arguments.getParcelableArrayList(KEY_LOCKED_USERS);
            if (lockedUsersArg != null) mLockedUsers = lockedUsersArg;
            ArrayList<User> selectedUsersArg = arguments.getParcelableArrayList(KEY_SELECTED_USERS);
            if (selectedUsersArg != null) mSelectedUsers = selectedUsersArg;
        }
        mSharedPreferences = getContext().getSharedPreferences(LinuteConstants.SHARED_PREF_NAME, Context.MODE_PRIVATE);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
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
                if (getActivity() != null) {
                    getActivity().onBackPressed();
                }
            }
        });


        toolbar.inflateMenu(R.menu.menu_create_chat);

        toolbar.getMenu().findItem(R.id.menu_item_create).setOnMenuItemClickListener(new MenuItem.OnMenuItemClickListener() {
            @Override
            public boolean onMenuItemClick(MenuItem menuItem) {
                if (mSelectedUsers.isEmpty()) return true;

                BaseTaptActivity activity = (BaseTaptActivity) getActivity();

                activity.getSupportFragmentManager().popBackStack();
                if (mUsersSelectedListener != null && !mSelectedUsers.isEmpty()) {
                    mUsersSelectedListener.onUsersSelected(mSelectedUsers);
                }
//                activity.replaceContainerWithFragment(ChatFragment.newInstance(null, mSelectedUsers));
                return true;
            }
        });


        mSearchAdapter2 = createSearchAdapter();
//        mSearchAdapter.setLockedUserList(mLockedUsers);
//        mSearchAdapter.setSelectedUserList(mSelectedUsers);
        mSearchRV = (RecyclerView) view.findViewById(R.id.search_users);
        mSearchRV.setHasFixedSize(true);
        final LinearLayoutManager searchLLM = new LinearLayoutManager(getActivity());
        searchLLM.setOrientation(LinearLayoutManager.VERTICAL);
        mSearchRV.setLayoutManager(searchLLM);
//        mSearchRV.setAdapter(mSearchAdapter);
        mSearchRV.setAdapter(mSearchAdapter2);
//        mSearchAdapter.setOnUserSelectedListener(this);

        mSelectedAdapter = new SelectedUsersAdapter(mSelectedUsers);
        mSelectedAdapter.setRequestManager(Glide.with(this));
        mSelectedRV = (RecyclerView) view.findViewById(R.id.selected_users);
        mSelectedContainer = view.findViewById(R.id.top);
        LinearLayoutManager selectedLLM = new LinearLayoutManager(getActivity());
        selectedLLM.setOrientation(LinearLayoutManager.HORIZONTAL);
        mSelectedRV.setLayoutManager(selectedLLM);
        mSelectedRV.setAdapter(mSelectedAdapter);


        mSearchAdapter2.setSelectedUserList(mSelectedUsers);
        mSearchAdapter2.setLockedUserList(mLockedUsers);

        mSelectedAdapter.setUserSelectedListener(new UserSelectAdapter2.OnUserClickListener() {
            @Override
            public void onUserClick(final User user) {

                mHandler.post(new Runnable() {
                    @Override
                    public void run() {
                        /*int listPosition = User.findUser(mSearchUserList, user);
                        if(listPosition != -1){
                            mSearchUserList.remove(listPosition);
                            mSearchAdapter2.notifyItemRemoved(listPosition);
                        }

                        if(focusedUser != null){
                            mSearchUserList.remove(0);
                            mSearchAdapter2.notifyItemRemoved(0);
                        }

                        mSearchUserList.add(0, user);
                        mSearchAdapter2.notifyItemInserted(0);
                        searchLLM.scrollToPosition(0);
                        focusedUser = user;*/

                        if (user.equals(mSearchAdapter2.getFocusedUser())) {
                            mSearchAdapter2.clearFocusedUser();
                        } else {
                            mSearchAdapter2.setFocusedUser(user);
                            mSearchRV.smoothScrollToPosition(0);
                        }


//                        mSearchAdapter.notifyDataSetChanged();
                    }
                });
            }
        });


        editText = (EditText) toolbar.findViewById(R.id.search_users_entry);
        firstUserName = (TextView) view.findViewById(R.id.text_single_user_name);
        editText.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                search(s.toString());
            }

            @Override
            public void afterTextChanged(Editable s) {

            }
        });

        editText.setOnEditorActionListener(new TextView.OnEditorActionListener() {
            @Override
            public boolean onEditorAction(TextView v, int actionId, KeyEvent event) {
                onSearchPressed();
                return true;
            }
        });

        search("");
        vProgress = view.findViewById(R.id.progressBar);
        vEmpty = view.findViewById(R.id.empty_view);

        mSelectedContainer.setVisibility(mSelectedUsers.size() > 0 ? View.VISIBLE : View.GONE);
    }

    @Override
    public void onResume() {
        super.onResume();
    }

    protected void onSearchPressed() {
        if (getActivity() == null) return;
        InputMethodManager imm = (InputMethodManager) getActivity().getSystemService(Context.INPUT_METHOD_SERVICE);
        if (imm == null) return;
        imm.hideSoftInputFromWindow(editText.getWindowToken(), 0);
    }

    protected UserSelectAdapter2 createSearchAdapter() {
        UserSelectAdapter2 userSelectAdapter = new UserSelectAdapter2(getActivity());
        userSelectAdapter.setUserSelectListener(this);
        userSelectAdapter.setUsers(mSearchUserList);
        return userSelectAdapter;

    }

    @Override
    public void onUserClick(final User user /*final int adapterPosition*/) {
        mHandler.post(new Runnable() {
            @Override
            public void run() {
                int listPosition = User.findUser(mSelectedUsers, user);

                if (listPosition == -1) {
                    mSelectedUsers.add(user);
//                    mSearchUserList.remove(user);
                    mSelectedAdapter.notifyItemInserted(mSelectedUsers.size() - 1);
                    if (mSelectedAdapter.getItemCount() == 2) mSelectedAdapter.notifyItemChanged(0);
                    mSelectedRV.getLayoutManager().scrollToPosition(mSelectedUsers.size() - 1);
                    editText.setText("");
//            mSearchAdapter.notifyDataSetChanged();
                } else {
                    mSelectedUsers.remove(listPosition);
                    mSelectedRV.getLayoutManager().scrollToPosition(listPosition);
                    mSelectedAdapter.notifyItemRemoved(listPosition);
                    if (mSelectedAdapter.getItemCount() == 1) mSelectedAdapter.notifyItemChanged(0);
                    editText.setText("");
                }
//                mSearchAdapter.notifyItemChanged(adapterPosition);
                mSearchAdapter2.clearFocusedUser();
                mSearchAdapter2.notifyDataSetChanged();

                showName(mSelectedUsers.size() == 1);


                mSelectedContainer.setVisibility(mSelectedUsers.size() > 0 ? View.VISIBLE : View.GONE);

                /*mSelectedContainer.clearAnimation();
                if(mSelectedUsers.size() > 0){
                    mSelectedContainer.setVisibility(View.VISIBLE);
                }
                mSelectedContainer.animate().y(mSelectedUsers.size() > 0 ? -mSelectedContainer.getHeight() : 0).withEndAction(new Runnable() {
                    @Override
                    public void run() {
                        mSelectedContainer.setVisibility(mSelectedUsers.size() > 0 ? View.VISIBLE : View.GONE);
                    }
                });*/

            }
        });
    }


    @Override
    public void onStop() {
        super.onStop();
        if (editText.hasFocus() && getActivity() != null) {
            InputMethodManager imm = (InputMethodManager) getActivity().getSystemService(Context.INPUT_METHOD_SERVICE);
            imm.hideSoftInputFromWindow(editText.getWindowToken(), 0);
        }
    }

    protected void search(String searchWord) {
        LSDKChat users = new LSDKChat(getActivity());
        Map<String, Object> newChat = new HashMap<>();
        if (!searchWord.equals("")) {
            newChat.put("fullName", searchWord);
        }

        newChat.put("users", new JSONArray());


        users.getUsersAndRooms(newChat, new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {
                if (getActivity() != null) {
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
                    Log.i(TAG, "onResponseNotSuccessful: " + response.body().string());
                    if (getActivity() != null) {
                        getActivity().runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                Utils.showServerErrorToast(getActivity());
                            }
                        });
                    }
                } else {
//                    mSearchUserList.clear();
                    final ArrayList<User> tempUsers = new ArrayList<>();
                    JSONObject jsonObject;
                    JSONArray friends;
                    try {
                        jsonObject = new JSONObject(response.body().string());
                        //Log.d(TAG, jsonObject.toString(4));
                        friends = jsonObject.getJSONArray("users");
                        JSONObject user;
                        for (int i = 0; i < friends.length(); i++) {
                            user = friends.getJSONObject(i);
                            String college = null;
                            if (!user.getString("college").equals("null")) {
                                college = user.getJSONObject("college").getString("name");
                            }
                            tempUsers.add(new User(
                                    user.getString("id"),
                                    user.getString("firstName"),
                                    user.getString("lastName"),
                                    user.getString("profileImage"),
                                    college
                            ));
                            /*if(findUser(mSelectedUsers, user.getString("id")) != -1) {
                                tempUsers.add(new User(
                                        user.getString("id"),
                                        user.getString("fullName"), user.getString("profileImage")
                                ));
                            }else if(findUser(mLockedUsers, user.getString("id")) != -1){
                                tempUsers.add(new User)
                            }*/

                        }


                        if (getActivity() != null) {
                            getActivity().runOnUiThread(new Runnable() {
                                @Override
                                public void run() {
                                    mHandler.removeCallbacksAndMessages(null);
                                    mHandler.post(new Runnable() {
                                        @Override
                                        public void run() {
                                            mSearchUserList.clear();
                                            mSearchUserList.addAll(tempUsers);
                                            mSearchAdapter2.notifyDataSetChanged();
                                            vEmpty.setVisibility(tempUsers.isEmpty() ? View.VISIBLE : View.GONE);
                                            vProgress.setVisibility(View.GONE);

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
                                    Utils.showServerErrorToast(getActivity());
                                }
                            });
                        }
                    }
                }
            }
        });
    }

    public void showName(boolean show) {
        firstUserName.setVisibility(show ? View.VISIBLE : View.GONE);
        mSelectedRV.animate().x(TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, (show ? 16 : 2), getResources().getDisplayMetrics()));
        if (show) {
            firstUserName.setText(mSelectedUsers.get(0).getFullName());
        }
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
//   TODO     if (mSearchAdapter.getRequestManager() != null) mSearchAdapter.getRequestManager().onDestroy();
        if (mSelectedAdapter.getRequestManager() != null)
            mSelectedAdapter.getRequestManager().onDestroy();
    }

    private OnUsersSelectedListener mUsersSelectedListener;

    public void setOnUsersSelectedListener(OnUsersSelectedListener listener) {
        mUsersSelectedListener = listener;
    }

    interface OnUsersSelectedListener {
        void onUsersSelected(ArrayList<User> users);
    }

}
