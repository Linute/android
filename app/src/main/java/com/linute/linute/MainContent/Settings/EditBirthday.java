package com.linute.linute.MainContent.Settings;

import android.app.DatePickerDialog;
import android.content.SharedPreferences;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.view.View;
import android.widget.Button;
import android.widget.DatePicker;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.linute.linute.R;
import com.linute.linute.UtilsAndHelpers.LinuteConstants;
import com.linute.linute.UtilsAndHelpers.Utils;
import com.squareup.okhttp.Call;

import java.util.Calendar;

public class EditBirthday extends AppCompatActivity implements DatePickerDialog.OnDateSetListener {

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

        getSupportActionBar().setTitle("Birthday");
    }

    private void setDefaultValues(){
        String dob = mSharedPreferences.getString("dob", "");
        mBirthdayText.setText(Utils.formatToReadableString(
                dob.equals("") ? "Jan 1, 2000" : dob));
    }


    private void setUpOnClickListeners(){
        mSaveButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                //saveBirthday();
            }
        });

        mCancelButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                onBackPressed();
                overridePendingTransition(0, 0);
            }
        });

        mEditBirthdayButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showDatePicker();
            }
        });
    }

    public String formatDateFromInts(int year, int month, int day){
        String date = year+"-";
        date += (month < 10 ? "0" + month : month) + "-";
        date += day < 10 ? "0" + day : day;
        return date;
    }


    private void showDatePicker(){
        final Calendar c = Calendar.getInstance();

        new DatePickerDialog(this, this, c.get(Calendar.YEAR), c.get(Calendar.MONTH), c.get(Calendar.DAY_OF_MONTH))
                .show();
    }

    @Override
    public void onDateSet(DatePicker view, int year, int monthOfYear, int dayOfMonth) {

    }
}
