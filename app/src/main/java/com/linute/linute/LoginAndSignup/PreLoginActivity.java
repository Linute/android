package com.linute.linute.LoginAndSignup;

import android.content.Intent;
import android.support.v7.app.AppCompatActivity;

import android.support.v4.app.FragmentPagerAdapter;
import android.os.Bundle;
import android.view.View;
import android.view.View.OnClickListener;

import android.view.animation.AnimationUtils;
import android.widget.Button;
import android.widget.ImageSwitcher;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.ViewSwitcher;

import com.linute.linute.R;

public class PreLoginActivity extends AppCompatActivity {

    /**
     * The {@link android.support.v4.view.PagerAdapter} that will provide
     * fragments for each of the sections. We use a
     * {@link FragmentPagerAdapter} derivative, which will keep every
     * loaded fragment in memory. If this becomes too memory intensive, it
     * may be best to switch to a
     * {@link android.support.v4.app.FragmentStatePagerAdapter}.
     */

    public static String TAG = "PreLogin";

    private Button mLinuteLoginButton;
    private Button mFacebookLoginButton;
    private TextView mSignupText;

    private ImageSwitcher mImageSwitcher;

    //list of images we will use
    private int[] mBackgroundImageResID = {
            R.drawable.college_walk1,
            R.drawable.college_walk2,
            R.drawable.college_walk3
    };

    private int mImageCount = mBackgroundImageResID.length;
    private int mCurrentImageIndex = 0;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_pre_login);


        //switches background images
        mImageSwitcher = (ImageSwitcher) findViewById(R.id.background_image_switcher);

        mLinuteLoginButton = (Button) findViewById(R.id.prelogin_linute_login);
        mLinuteLoginButton.setOnClickListener(mLinuteOnClickListener);

        mSignupText = (TextView)findViewById(R.id.linute_signup);
        mSignupText.setOnClickListener(mLinuteSignUpClicked);


        mFacebookLoginButton = (Button) findViewById(R.id.prelogin_facebook_login);
        //TODO: Set this button

        setUpImageSwitcher();
    }


    private void setUpImageSwitcher(){

        mImageSwitcher.setFactory(new ViewSwitcher.ViewFactory() {
            @Override
            public View makeView() {
                ImageView imageView = new ImageView(getApplicationContext());
                imageView.setScaleType(ImageView.ScaleType.CENTER_CROP);

                mImageSwitcher.setLayoutParams(new RelativeLayout.LayoutParams(RelativeLayout.LayoutParams.MATCH_PARENT, RelativeLayout.LayoutParams.MATCH_PARENT));
                return imageView;
            }
        });

        mImageSwitcher.setImageResource(mBackgroundImageResID[0]); //set first image

        //set in and out animations
        mImageSwitcher.setInAnimation(AnimationUtils.loadAnimation(this, R.anim.fade_in));
        mImageSwitcher.setOutAnimation(AnimationUtils.loadAnimation(this, R.anim.fade_out));

        //set so it automatically switches every 1 sec
        mImageSwitcher.postDelayed(new Runnable() {
            @Override
            public void run() {
                //cycle through the images
                mImageSwitcher.setImageResource(mBackgroundImageResID[getNextBackgroundImageIndex()]);
                mImageSwitcher.postDelayed(this, 6000);
            }
        }, 5000);
    }

    private int getNextBackgroundImageIndex(){
        //cycles to beginning image when at the end
        return ++mCurrentImageIndex == mImageCount ? mCurrentImageIndex = 0 : mCurrentImageIndex;
    }


    //go to SignIn Actvity
    private OnClickListener mLinuteOnClickListener = new OnClickListener() {
        @Override
        public void onClick(View v) {
            Intent goToLinuteLogin = new Intent(getApplicationContext(), LinuteLoginActivity.class);
            startActivity(goToLinuteLogin);
        }
    };

    //takes you to signup activity
    private OnClickListener mLinuteSignUpClicked = new OnClickListener() {
        @Override
        public void onClick(View v) {
            Intent goToLinuteLogin = new Intent(getApplicationContext(), LinuteSignUpActivity.class);
            goToLinuteLogin.putExtra("CURR_BACKGROUND_INDEX", mCurrentImageIndex);
            startActivity(goToLinuteLogin);
        }
    };

}
