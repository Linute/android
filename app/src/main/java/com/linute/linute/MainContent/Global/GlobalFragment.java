package com.linute.linute.MainContent.Global;

import android.content.Context;
import android.os.Bundle;
import android.os.Handler;
import android.support.annotation.Nullable;
import android.support.design.widget.AppBarLayout;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.linute.linute.API.LSDKGlobal;
import com.linute.linute.MainContent.Chat.RoomsActivityFragment;
import com.linute.linute.MainContent.EventBuses.NewMessageBus;
import com.linute.linute.MainContent.EventBuses.NewMessageEvent;
import com.linute.linute.MainContent.EventBuses.NotificationEvent;
import com.linute.linute.MainContent.EventBuses.NotificationEventBus;
import com.linute.linute.MainContent.EventBuses.NotificationsCounterSingleton;
import com.linute.linute.MainContent.Global.Articles.Article;
import com.linute.linute.MainContent.Global.Articles.ArticleFragment;
import com.linute.linute.MainContent.MainActivity;
import com.linute.linute.R;
import com.linute.linute.UtilsAndHelpers.BaseFragment;
import com.linute.linute.UtilsAndHelpers.Utils;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.util.ArrayList;
import java.util.LinkedList;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.Response;
import rx.Subscription;
import rx.android.schedulers.AndroidSchedulers;
import rx.functions.Action1;
import rx.schedulers.Schedulers;

/**
 * Created by QiFeng on 5/14/16.
 */
public class GlobalFragment extends BaseFragment implements GlobalChoicesAdapter.GoToTrend {

    private static GlobalFragment instance;

    public static GlobalFragment getInstance() {
        if (instance == null) {
            instance = new GlobalFragment();
        }
        return instance;
    }

    private static final String TAG = GlobalFragment.class.getSimpleName();

    private RecyclerView vRecycler;
    private SwipeRefreshLayout vSwipe;
    private GlobalChoicesAdapter mGlobalChoicesAdapter;
    private LinkedList<GlobalChoiceItem> mGlobalChoiceItems = new LinkedList<>();
    private Toolbar vToolbar;
    private Handler mHandler;
    private AppBarLayout vAppBarLayout;

    private View vNotificationIndicator;
    private TextView vNotificationCounter;

    private TextView vUpdateCounter;
    private View vUpdateNotification;

    private View vEmpty;

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mHandler = new Handler();
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View root = inflater.inflate(R.layout.fragment_global_choices, container, false);

        if (mGlobalChoicesAdapter == null) {
            mGlobalChoicesAdapter = new GlobalChoicesAdapter(getContext(), mGlobalChoiceItems);
        }
        mGlobalChoicesAdapter.setGoToTrend(this);

        vEmpty = root.findViewById(R.id.empty_view);
        vAppBarLayout = (AppBarLayout) root.findViewById(R.id.appbar_layout);
        vRecycler = (RecyclerView) root.findViewById(R.id.recycler_view);
        vSwipe = (SwipeRefreshLayout) root.findViewById(R.id.swipe_refresh);

        mGlobalChoicesAdapter.setRequestManager(Glide.with(this));

        vRecycler.setAdapter(mGlobalChoicesAdapter);

        GridLayoutManager manager = new GridLayoutManager(getContext(), 2);
        manager.setSpanSizeLookup(new GridLayoutManager.SpanSizeLookup() {
            @Override
            public int getSpanSize(int position) {
                //headers are 1 span
                switch (mGlobalChoiceItems.get(position).type) {
                    case GlobalChoiceItem.TYPE_HEADER_FRIEND:
                    case GlobalChoiceItem.TYPE_HEADER_HOT:
                        return 1;
                    default:
                        return 2;
                }
            }
        });

        vRecycler.setLayoutManager(manager);
        vRecycler.setHasFixedSize(true);

        vToolbar = (Toolbar) root.findViewById(R.id.toolbar);
        vToolbar.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                vRecycler.scrollToPosition(0);
            }
        });
        vToolbar.inflateMenu(R.menu.people_fragment_menu);
        vToolbar.setNavigationOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                MainActivity activity = (MainActivity) getActivity();
                if (activity != null) activity.openDrawer();
            }
        });

        View update = vToolbar.getMenu().findItem(R.id.menu_updates).getActionView();
        update.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                MainActivity activity = (MainActivity) getActivity();
                if (activity != null)
                    activity.addActivityFragment();
            }
        });

        vUpdateNotification = update.findViewById(R.id.notification);
        vUpdateCounter = (TextView) vUpdateNotification.findViewById(R.id.notification_count);

        vToolbar.setNavigationIcon(NotificationsCounterSingleton.getInstance().hasNewPosts() ?
                R.drawable.nav_icon : R.drawable.ic_action_navigation_menu);

        View chat = vToolbar.getMenu().findItem(R.id.menu_chat).getActionView();
        chat.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                MainActivity activity = (MainActivity) getActivity();
                if (activity != null)
                    activity.addFragmentToContainer(RoomsActivityFragment.getInstance(), RoomsActivityFragment.TAG);
            }
        });
        vNotificationIndicator = chat.findViewById(R.id.notification);
        vNotificationCounter = (TextView) chat.findViewById(R.id.notification_count);

        vSwipe.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                mGlobalChoiceItems.clear();
                getArticles();
            }
        });

        return root;
    }


    @Override
    public void onResume() {
        super.onResume();

        if (getFragmentState() == FragmentState.NEEDS_UPDATING) {
            vSwipe.post(new Runnable() {
                @Override
                public void run() {
                    vSwipe.setRefreshing(true);
                }
            });
            //mGlobalChoiceItems.clear();
            getArticles();
        } else if (mGlobalChoiceItems.isEmpty()) {
            vEmpty.setVisibility(View.VISIBLE);
        }


        NotificationsCounterSingleton singleton = NotificationsCounterSingleton.getInstance();

        int count = singleton.getNumOfNewActivities();
        vUpdateNotification.setVisibility(count > 0 ? View.VISIBLE : View.GONE);
        vUpdateCounter.setText(count < 100 ? count + "" : "+");

        count = singleton.getNumMessages();

        vNotificationIndicator.setVisibility(singleton.hasMessage() ? View.VISIBLE : View.GONE);
        vNotificationCounter.setText(count < 100 ? count + "" : "+");

        mChatSubscription = NewMessageBus
                .getInstance()
                .getObservable()
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(mNewMessageSubscriber);

        mNotificationSubscription = NotificationEventBus
                .getInstance()
                .getObservable()
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(mNotificationEventAction1);

    }

    @Override
    public void onPause() {
        super.onPause();
        vAppBarLayout.setExpanded(true, false);

        if (mChatSubscription != null) {
            mChatSubscription.unsubscribe();
        }

        if (mNotificationSubscription != null) {
            mNotificationSubscription.unsubscribe();
        }
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        mGlobalChoiceItems.clear();

        if (vSwipe != null)
            vSwipe.post(new Runnable() {
                @Override
                public void run() {
                    vSwipe.setRefreshing(true);
                }
            });

        getArticles();
    }

    public void getArticles() {
        setFragmentState(FragmentState.LOADING_DATA);

        new LSDKGlobal(getContext()).getArticles(new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {
                if (getActivity() != null) {
                    getActivity().runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            if (isViewLoaded()) vSwipe.setRefreshing(false);
                            Utils.showBadConnectionToast(getActivity());
                        }
                    });
                }
            }

            @Override
            public void onResponse(Call call, Response response) throws IOException {
                if (response.isSuccessful()) {

                    try {
                        JSONArray articles = new JSONObject(response.body().string()).getJSONArray("articles");

                        //Log.d(TAG, "onResponse: " + articles.toString(4));
                        final LinkedList<GlobalChoiceItem> tempList = new LinkedList<>();
                        addHotAndFriends(tempList);
                        JSONObject article;
                        GlobalChoiceItem item;

                        for (int i = 0; i < articles.length(); i++) {
                            try {

                                article = articles.getJSONObject(i);
                                item = new Article(
                                        article
                                );
                                tempList.add(item);

                            } catch (JSONException e) {
                                e.printStackTrace();
                            }
                        }

//                        GlobalChoiceItem.sort(tempList);


                        if (getActivity() != null) {
                            getActivity().runOnUiThread(new Runnable() {
                                @Override
                                public void run() {
                                    if (isViewLoaded()) {
                                        vSwipe.setRefreshing(false);
                                    }
                                    mHandler.removeCallbacksAndMessages(null);
                                    mHandler.post(new Runnable() {
                                        @Override
                                        public void run() {
                                            mGlobalChoiceItems.clear();
                                            mGlobalChoiceItems.addAll(tempList);
                                            if (isViewLoaded()) {
                                                mGlobalChoicesAdapter.notifyDataSetChanged();
                                                vEmpty.setVisibility(mGlobalChoiceItems.isEmpty() ? View.VISIBLE : View.GONE);
                                            }
                                        }
                                    });

                                    setFragmentState(FragmentState.NEEDS_UPDATING);
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
                                    if (vSwipe != null)
                                        vSwipe.setRefreshing(false);
                                }
                            });
                        }
                    }
                } else {
                    if (getActivity() != null) {
                        getActivity().runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                Utils.showServerErrorToast(getActivity());
                                if (vSwipe != null)
                                    vSwipe.setRefreshing(false);
                            }
                        });
                    }
                }
            }
        });
    }

    private boolean isViewLoaded() {
        return getView() != null;
    }

    @Override
    public void resetFragment() {
        vRecycler.scrollToPosition(0);
    }


    private Subscription mChatSubscription;
    private Action1<NewMessageEvent> mNewMessageSubscriber = new Action1<NewMessageEvent>() {
        @Override
        public void call(NewMessageEvent event) {
            int count = NotificationsCounterSingleton.getInstance().getNumMessages();
            vNotificationIndicator.setVisibility(event.hasNewMessage() ? View.VISIBLE : View.GONE);
            vNotificationCounter.setText(count < 100 ? count + "" : "+");
        }
    };


    private Subscription mNotificationSubscription;
    private Action1<NotificationEvent> mNotificationEventAction1 = new Action1<NotificationEvent>() {
        @Override
        public void call(NotificationEvent notificationEvent) {
            if (notificationEvent.getType() == NotificationEvent.DISCOVER) {
                vToolbar.setNavigationIcon(notificationEvent.hasNotification() ? R.drawable.nav_icon : R.drawable.ic_action_navigation_menu);
            } else if (notificationEvent.getType() == NotificationEvent.ACTIVITY) {
                int count = NotificationsCounterSingleton.getInstance().getNumOfNewActivities();
                vUpdateNotification.setVisibility(count > 0 ? View.VISIBLE : View.GONE);
                vUpdateCounter.setText(count < 100 ? count + "" : "+");
            }
        }
    };

    @Override
    public void goToTrend(GlobalChoiceItem item) {
        MainActivity activity = (MainActivity) getActivity();
        if (activity != null) {
            if (item.type == GlobalChoiceItem.TYPE_ARTICLE)
                activity.addFragmentToContainer(ArticleFragment.newInstance((Article) item));
            else
                activity.addFragmentToContainer(TrendingPostsFragment.newInstance(item), "TREND");
        }
    }

    private void addHotAndFriends(LinkedList<GlobalChoiceItem> items) {
        items.addFirst(new GlobalChoiceItem("News", null, GlobalChoiceItem.TYPE_SECTION_TEXT));
        items.addFirst(new GlobalChoiceItem("Friends", null, GlobalChoiceItem.TYPE_HEADER_FRIEND));
        items.addFirst(new GlobalChoiceItem("Hottest", null, GlobalChoiceItem.TYPE_HEADER_HOT));
    }

    private void addTestArticle(ArrayList<GlobalChoiceItem> items) {
        try {
            items.add(new Article(ARTICLE_JSON));
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        if (mGlobalChoicesAdapter.getRequestManager() != null)
            mGlobalChoicesAdapter.getRequestManager().onDestroy();

        mGlobalChoicesAdapter.setRequestManager(null);
    }


    public static JSONObject ARTICLE_JSON;

    static {
        try {
            ARTICLE_JSON = new JSONObject(
                    "{"
                            + "	\"id\" : \"0\","
                            + "	\"date\" : \"04/05/2016\","
                            + "	\"publisher\" : \"Mikhail Foenko\","
                            + "	\"authors\" : [\"Mikhail Foenko\"],"
                            + "	\"color\" : \"FFFFFF\","
                            + "	\"title\" : \"Somethign about CCNY\","
                            + "	\"image\" : \"http://ccnycampus.org/wp-content/blogs.dir/5/files/2016/09/Screen-Shot-2016-09-15-at-3.48.40-PM.png\","
                            + "	\"content\" : ["
                            + "		{"
                            + "			\"type\":\"paragraph\","
                            + "			\"data\":\"lisa stole money or some shit\""
                            + "		},"
                            + "		{"
                            + "			\"type\":\"image\","
                            + "			\"data\":\"http://ccnycampus.org/wp-content/blogs.dir/5/files/2016/09/Screen-Shot-2016-09-15-at-3.48.40-PM.png\""
                            + "		}"
                            + "	]"
                            + "}");
        } catch (JSONException e) {
            e.printStackTrace();
            ARTICLE_JSON = new JSONObject();
        }
    }
}
