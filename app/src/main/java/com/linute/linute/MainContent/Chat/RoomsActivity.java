package com.linute.linute.MainContent.Chat;

import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.app.FragmentTransaction;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import com.linute.linute.R;

import org.json.JSONException;
import org.json.JSONObject;

import de.greenrobot.event.EventBus;
import de.greenrobot.event.Subscribe;

public class RoomsActivity extends AppCompatActivity {

    private static final String TAG = RoomsActivity.class.getSimpleName();
    private TextView mTitle;
    Handler mHandler = new Handler(Looper.getMainLooper());
    private EventBus mEventBus = EventBus.getDefault();
    private FloatingActionButton mFab;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_rooms);
        if (!mEventBus.isRegistered(this)) {
            mEventBus.register(this);
        }

        Toolbar toolbar = (Toolbar) findViewById(R.id.rooms_toolbar);
        setSupportActionBar(toolbar);
        mTitle = (TextView) toolbar.findViewById(R.id.toolbar_title);
        mTitle.setText("Rooms");

        mFab = (FloatingActionButton) findViewById(R.id.fab);
        mFab.setImageResource(R.drawable.add_friend);
        mFab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
//                Snackbar.make(view, "Replace with your own action", Snackbar.LENGTH_LONG)
//                        .setAction("Action", null).show();
                // Create fragment and give it an argument specifying the article it should show
                SearchUsers newFragment = SearchUsers.newInstance("Hi", "Hello");
                FragmentTransaction transaction = getSupportFragmentManager().beginTransaction();
                // Replace whatever is in the fragment_container view with this fragment,
                // and add the transaction to the back stack so the user can navigate back
                transaction.replace(R.id.fragment, newFragment);
                transaction.addToBackStack(null);
                // Commit the transaction
                transaction.commit();
                toggleFab(false);
            }
        });

        try {
            getSupportActionBar().setDisplayShowTitleEnabled(false);
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        } catch (NullPointerException ne) {
            ne.printStackTrace();
        }

    }

    @Override
    protected void onDestroy() {
        if (mEventBus.isRegistered(this)) {
            mEventBus.unregister(this);
        }
        super.onDestroy();
    }

    @Subscribe
    public void onEvent(final JSONObject object) {
        mHandler.post(new Runnable() {
            @Override
            public void run() {
                try {
                    Toast.makeText(RoomsActivity.this, object.getString("username") + " " + object.getString("message"), Toast.LENGTH_SHORT).show();
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }
        });
    }

    public void toggleFab(boolean toggleFab) {
        if (!toggleFab && mFab.isShown()) {
            mFab.hide();
        } else if (toggleFab && !mFab.isShown()) {
            mFab.show();
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
    }

    @Override
    public void onBackPressed() {
        int count = getFragmentManager().getBackStackEntryCount();
        if (count == 0) {
            toggleFab(true);
            super.onBackPressed();
        } else {
            getFragmentManager().popBackStack();
        }
    }
}
