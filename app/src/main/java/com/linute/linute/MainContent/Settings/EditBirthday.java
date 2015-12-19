package com.linute.linute.MainContent.Settings;

import android.content.SharedPreferences;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.view.View;
import android.widget.Button;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.linute.linute.R;
import com.linute.linute.UtilsAndHelpers.LinuteConstants;
import com.linute.linute.UtilsAndHelpers.Utils;

public class EditBirthday extends AppCompatActivity {

    private ProgressBar mProgressBar;
    private TextView mBirthdayText;
    private Button mSaveButton;
    private Button mCancelButton;
    private Button mEditBirthdayButton;
    private View mButtonLayer;
    private Toolbar mToolBar;

    private SharedPreferences mSharedPreferences;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_edit_birthday);

        mSharedPreferences = getSharedPreferences(LinuteConstants.SHARED_PREF_NAME, MODE_PRIVATE);
        bindViews();
        setUpToolbar();
        setDefaultValues();
    }

    private void bindViews(){
        mProgressBar = (ProgressBar) findViewById(R.id.editbirthday_progressbar);
        mBirthdayText = (TextView) findViewById(R.id.editbirthday_birthday_text);
        mSaveButton = (Button) findViewById(R.id.editbirthday_save_button);
        mCancelButton = (Button) findViewById(R.id.editbirthday_cancel_button);
        mEditBirthdayButton = (Button) findViewById(R.id.editbirthday_edit_button);
        mButtonLayer = findViewById(R.id.editbirthday_buttons);
    }

    private void setUpToolbar(){
        mToolBar = (Toolbar) findViewById(R.id.editname_toolbar);
        setSupportActionBar(mToolBar);

        getSupportActionBar().setTitle("Name");
    }

    private void setDefaultValues(){
        String dob = mSharedPreferences.getString("dob", "");
        mBirthdayText.setText(Utils.formatToReadableString(
                dob.equals("") ? "Jan 1, 2000" : dob));
    }

    public String formatDateFromInts(int year, int month, int day){
        String date = year+"-";
        date += (month < 10 ? "0" + month : month) + "-";
        date += day < 10 ? "0" + day : day;
        return date;
    }
}
