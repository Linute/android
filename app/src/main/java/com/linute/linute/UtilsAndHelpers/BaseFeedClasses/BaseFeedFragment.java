package com.linute.linute.UtilsAndHelpers.BaseFeedClasses;

import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.Handler;
import android.support.annotation.Nullable;
import android.support.v7.app.AlertDialog;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.linute.linute.API.API_Methods;
import com.linute.linute.API.LSDKEvents;
import com.linute.linute.MainContent.DiscoverFragment.Post;
import com.linute.linute.MainContent.MainActivity;
import com.linute.linute.MainContent.SendTo.SendToFragment;
import com.linute.linute.R;
import com.linute.linute.UtilsAndHelpers.BaseFragment;
import com.linute.linute.UtilsAndHelpers.BaseTaptActivity;
import com.linute.linute.UtilsAndHelpers.LinuteConstants;
import com.linute.linute.UtilsAndHelpers.LoadMoreViewHolder;
import com.linute.linute.UtilsAndHelpers.SpaceItemDecoration;
import com.linute.linute.UtilsAndHelpers.Utils;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.util.ArrayList;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.Response;

/**
 * Created by QiFeng on 9/2/16.
 */
public abstract class BaseFeedFragment extends BaseFragment {

    public final static String TAG = BaseFeedFragment.class.getSimpleName();

    protected RecyclerView vRecyclerView;
    protected View vEmptyView;

    protected BaseFeedAdapter mFeedAdapter;

    //for loading more feed
    protected boolean mFeedDone;
    protected int mSkip = 0;
    protected boolean mLoadingMore = false;

    protected String mCollegeId;
    protected String mUserId;

    protected AlertDialog mAlertDialog; //for delete, hide, reveal options


    protected Handler mHandler = new Handler(); //handler for recview

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        SharedPreferences sharedPreferences = getActivity().getSharedPreferences(LinuteConstants.SHARED_PREF_NAME, Context.MODE_PRIVATE);
        mCollegeId = sharedPreferences.getString("collegeId", "");
        mUserId = sharedPreferences.getString("userID", null);

    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        final View rootView = inflater.inflate(getLayout(), container, false); //setContent
        initAdapter();
        mFeedAdapter.setRequestManager(Glide.with(this));
        mFeedAdapter.setPostAction(new BaseFeedAdapter.PostAction() {
            @Override
            public void clickedOptions(final Post p, final int position) {
                if (getContext() == null || mUserId == null) return;

                final boolean isOwner = p.getUserId().equals(mUserId);
                String[] options;
                if (isOwner) {
                    options = new String[]{"Delete post", p.getPrivacy() == 1 ? "Reveal identity" : "Make anonymous", "Share post"};
                } else {
                    options = new String[]{"Report post", p.isPostHidden() ? "Unhide post" : "Hide post", "Share post"};
                }
                mAlertDialog = new AlertDialog.Builder(getContext())
                        .setItems(options, new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                switch (which) {
                                    case 0:
                                        if (isOwner) confirmDeletePost(p, position);
                                        else confirmReportPost(p);
                                        return;
                                    case 1:
                                        if (isOwner) confirmToggleAnon(p, position);
                                        else confirmToggleHidden(p, position);
                                        return;
                                    case 2:
                                        sharePost(p);
                                }
                            }
                        }).show();
            }
        });

        vEmptyView = rootView.findViewById(R.id.empty_view);
//        inflater.inflate(getEmptyLayout(), (ViewGroup)vEmptyView, true);

        vRecyclerView = (RecyclerView) rootView.findViewById(R.id.recycler_view);

        vRecyclerView.addItemDecoration(new SpaceItemDecoration(getActivity(), R.dimen.list_space,
                true, true));

        LinearLayoutManager llm = new LinearLayoutManager(getActivity());
        llm.setOrientation(LinearLayoutManager.VERTICAL);
        vRecyclerView.setLayoutManager(llm);

      /*  recList.addItemDecoration(new SpaceItemDecoration(getActivity(), R.dimen.list_space,
                true, true));*/

        mFeedAdapter.setGetMoreFeed(new LoadMoreViewHolder.OnLoadMore() {
            @Override
            public void loadMore() {
                if (getFragmentState() == FragmentState.LOADING_DATA || mFeedDone)
                    return;
                if (mFeedAdapter.getLoadState() == LoadMoreViewHolder.STATE_LOADING) {
                    getMorePosts();
                }
            }
        });

        vRecyclerView.setAdapter(mFeedAdapter);
        return rootView;
    }


    @Override
    public void onStop() {
        super.onStop();
        if (mAlertDialog != null) {
            mAlertDialog.dismiss();
            mAlertDialog = null;
        }
    }

    protected abstract void initAdapter();

    protected abstract int getLayout();
//    protected abstract int getEmptyLayout();

    protected abstract void getPosts();

    protected abstract void getMorePosts();

    private void confirmDeletePost(final Post p, final int position) {
        mAlertDialog = new AlertDialog.Builder(getActivity())
                .setTitle("Delete your post")
                .setMessage("Are you sure you want to delete what you've created?")
                .setNegativeButton("No", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.dismiss();
                    }
                })
                .setPositiveButton("Yes", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        deletePost(p, position);
                    }
                })
                .show();
    }

    private void deletePost(final Post p, final int pos) {
        if (getActivity() == null || !mUserId.equals(p.getUserId())) return;
        final ProgressDialog progressDialog = ProgressDialog.show(getActivity(), "", "Deleting", true, false);

        new LSDKEvents(getActivity()).deleteEvent(p.getPostId(), new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {
                progressDialog.dismiss();
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
                if (response.isSuccessful()) {
                    response.body().close();

                    final BaseTaptActivity activity = (BaseTaptActivity) getActivity();

                    if (activity == null) return;

                    if (notifyFeedNeedsUpdating())
                        activity.setFragmentOfIndexNeedsUpdating(FragmentState.NEEDS_UPDATING,MainActivity.FRAGMENT_INDEXES.FEED);

                    activity.setFragmentOfIndexNeedsUpdating(FragmentState.NEEDS_UPDATING,MainActivity.FRAGMENT_INDEXES.PROFILE);

                    activity.runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            Toast.makeText(activity, "Post deleted", Toast.LENGTH_SHORT).show();
                            mHandler.post(new Runnable() {
                                @Override
                                public void run() {
                                    int position = pos;
                                    if (!getPostsArray().get(pos).equals(p)) { //check this is correct post
                                        position = getPostsArray().indexOf(p);
                                    }

                                    if (position >= 0) {
                                        getPostsArray().remove(pos);
                                        mFeedAdapter.notifyItemRemoved(pos);
                                    }
                                }
                            });
                        }
                    });

                } else {
                    if (getActivity() != null) {
                        getActivity().runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                Utils.showServerErrorToast(getActivity());
                            }
                        });
                    }
                }
                progressDialog.dismiss();
            }
        });
    }


    private void confirmReportPost(final Post p) {
        if (getActivity() == null) return;
        final CharSequence options[] = new CharSequence[]{"Spam", "Inappropriate", "Harassment"};
        mAlertDialog = new AlertDialog.Builder(getActivity())
                .setTitle("Report As")
                .setItems(options, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        reportPost(p, which);
                    }
                })
                .create();
        mAlertDialog.show();
    }

    private void reportPost(Post p, int reason) {
        if (getActivity() == null) return;
        new LSDKEvents(getActivity()).reportEvent(reason, p.getPostId(), new Callback() {
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
                if (response.isSuccessful()) {
                    response.body().close();
                    if (getActivity() == null) return;
                    getActivity().runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            Toast.makeText(getActivity(), "Post reported", Toast.LENGTH_SHORT).show();
                        }
                    });
                } else {
                    Log.e(TAG, "onResponse: " + response.body().string());
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
        });
    }

    private void confirmToggleAnon(final Post p, final int pos) {
        if (getActivity() == null) return;

        boolean isAnon = p.getPrivacy() == 1;
        mAlertDialog = new AlertDialog.Builder(getActivity())
                .setTitle(isAnon ? "Reveal" : "Wear a mask")
                .setMessage(isAnon ? "Are you sure you want to turn anonymous off for this post?" : "Are you sure you want to make this post anonymous?")
                .setNegativeButton("No", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.dismiss();
                    }
                })
                .setPositiveButton("Yes", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        toggleAnon(p, pos);
                    }
                })
                .show();
    }

    private void toggleAnon(final Post p, final int position) {
        if (getActivity() == null || !mUserId.equals(p.getUserId())) return;
        final boolean isAnon = p.getPrivacy() == 1;
        final ProgressDialog progressDialog = ProgressDialog.show(getActivity(), null, isAnon ? "Revealing post..." : "Making post anonymous...", true, false);
        new LSDKEvents(getActivity()).revealEvent(p.getPostId(), !isAnon, new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {
                progressDialog.dismiss();
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
                String res = response.body().string();

                if (response.isSuccessful()) {
                    try {

                        if (!isAnon) {
                            JSONObject obj = new JSONObject(res);
                            p.setAnonImage(Utils.getAnonImageUrl(obj.getString("anonymousImage")));
                        }

                        BaseTaptActivity act = (BaseTaptActivity) getActivity();

                        if (act != null) {
                            if (notifyFeedNeedsUpdating())
                                act.setFragmentOfIndexNeedsUpdating(FragmentState.NEEDS_UPDATING,MainActivity.FRAGMENT_INDEXES.FEED);
                            act.setFragmentOfIndexNeedsUpdating(FragmentState.NEEDS_UPDATING,MainActivity.FRAGMENT_INDEXES.PROFILE);

                            act.runOnUiThread(new Runnable() {
                                @Override
                                public void run() {
                                    p.setPostPrivacy(isAnon ? 0 : 1);
                                    mHandler.post(new Runnable() {
                                        @Override
                                        public void run() {
                                            int pos = position;
                                            if (!getPostsArray().get(position).equals(p)){
                                                pos = getPostsArray().indexOf(p);
                                            }

                                            if (pos >= 0){
                                                mFeedAdapter.notifyItemChanged(pos);
                                            }
                                        }

                                    });
                                    Toast.makeText(getActivity(), isAnon ? "Post revealed" : "Post made anonymous", Toast.LENGTH_SHORT).show();
                                }
                            });
                        }
                    } catch (JSONException e) {
                        e.printStackTrace();
                        Log.e(TAG, "onResponse: " + res);
                        if (getActivity() != null) {
                            getActivity().runOnUiThread(new Runnable() {
                                @Override
                                public void run() {
                                    Utils.showServerErrorToast(getActivity());
                                }
                            });
                        }
                    }
                } else {
                    Log.e(TAG, "onResponse: " + res);
                    if (getActivity() != null) {
                        getActivity().runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                Utils.showServerErrorToast(getActivity());
                            }
                        });
                    }
                }
                progressDialog.dismiss();
            }
        });
    }

    private void confirmToggleHidden(final Post p, final int pos) {
        if (getActivity() == null) return;
        mAlertDialog = new AlertDialog.Builder(getActivity())
                .setTitle(p.isPostHidden() ? "Unhide post" : "Hide it")
                .setMessage(p.isPostHidden() ? "This will make this post viewable on your feed. Still want to go ahead with it?" : "This will remove this post from your feed, go ahead with it?")
                .setPositiveButton("let's do it!", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        toggleHidden(p, pos);
                    }
                })
                .setNegativeButton("no, thanks", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.dismiss();
                    }
                })
                .show();
    }

    private void toggleHidden(Post p, int position) {
        BaseTaptActivity activity = (BaseTaptActivity) getActivity();
        if (activity == null) return;

        if (!activity.socketConnected()){
            Utils.showBadConnectionToast(activity);
            return;
        }

        int pos = position;
        if (!getPostsArray().get(position).equals(p)){
            pos = getPostsArray().indexOf(p);
        }

        final int pos1 = pos;
        if (pos >= 0){
            mHandler.post(new Runnable() {
                @Override
                public void run() {
                    getPostsArray().remove(pos1);
                    mFeedAdapter.notifyItemRemoved(pos1);
                }
            });
        }

        Toast.makeText(activity,
                p.isPostHidden() ? "Post unhidden" : "Post hidden",
                Toast.LENGTH_SHORT).show();

        if (notifyFeedNeedsUpdating())
            activity.setFragmentOfIndexNeedsUpdating(FragmentState.NEEDS_UPDATING, MainActivity.FRAGMENT_INDEXES.FEED);

        activity.setFragmentOfIndexNeedsUpdating(FragmentState.NEEDS_UPDATING,MainActivity.FRAGMENT_INDEXES.PROFILE);

        JSONObject emit = new JSONObject();
        try {
            emit.put("hide", !p.isPostHidden());
            emit.put("room", p.getPostId());
            activity.emitSocket(API_Methods.VERSION + ":posts:hide", emit);
        } catch (JSONException e) {
            Utils.showServerErrorToast(activity);
            e.printStackTrace();
        }
    }


    //if discover feed needs to be updated
    protected boolean notifyFeedNeedsUpdating(){
        return true;
    }


    private void sharePost(Post p) {
        BaseTaptActivity activity = (BaseTaptActivity) getActivity();
        if (activity != null)
            activity.addFragmentOnTop(SendToFragment.newInstance(p.getPostId()), "send_to");
    }


    public abstract ArrayList<Post> getPostsArray();

    @Override
    public void onDestroyView() {
        super.onDestroyView();

        if (mFeedAdapter.getRequestManager() != null)
            mFeedAdapter.getRequestManager().onDestroy();

        mFeedAdapter.setRequestManager(null);
    }
}
