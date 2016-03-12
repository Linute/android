package com.linute.linute.MainContent.DiscoverFragment;

import android.content.Context;
import android.content.SharedPreferences;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.View;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.TextView;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.bumptech.glide.signature.StringSignature;
import com.linute.linute.API.API_Methods;
import com.linute.linute.API.LSDKEvents;
import com.linute.linute.MainContent.FeedDetailFragment.FeedDetailPage;
import com.linute.linute.MainContent.TaptUser.TaptUserProfileFragment;
import com.linute.linute.R;
import com.linute.linute.UtilsAndHelpers.BaseTaptActivity;
import com.linute.linute.UtilsAndHelpers.DoubleClickListener;
import com.linute.linute.UtilsAndHelpers.LinuteConstants;
import com.linute.linute.UtilsAndHelpers.RecyclerViewChoiceAdapters.ChoiceCapableAdapter;
import com.linute.linute.UtilsAndHelpers.Utils;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import de.hdodenhof.circleimageview.CircleImageView;
import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.Response;

/**
 * Created by QiFeng on 2/3/16.
 */
public class StatusFeedHolder extends BaseFeedHolder {


    public static final String TAG = StatusFeedHolder.class.getSimpleName();



    protected TextView vStatus;
    protected View vStatusContainer; //so status is easier to press




    public StatusFeedHolder( View itemView, Context context) {
        super(itemView, context);

        vStatus = (TextView) itemView.findViewById(R.id.feedDetail_status_post);

        vStatusContainer = itemView.findViewById(R.id.feedDetail_status_container);

        vStatusContainer.setOnClickListener(new DoubleClickListener() {
            @Override
            public void onDoubleClick(View v) {
                vLikesHeart.toggle();
            }
        });
    }


    @Override
    public void bindModel(Post post) {
        super.bindModel(post);

        vStatus.setText(post.getTitle());
    }

}
