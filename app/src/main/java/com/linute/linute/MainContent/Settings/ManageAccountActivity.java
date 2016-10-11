package com.linute.linute.MainContent.Settings;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.view.View;

import com.linute.linute.R;
import com.linute.linute.UtilsAndHelpers.BaseSocketActivity;

public class ManageAccountActivity extends BaseSocketActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_manage_account);

        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        if (toolbar != null)
            toolbar.setNavigationOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    onBackPressed();
                }
            });
    }


    public void goToNextActivity(View view) {
        Class target = view.getId() == R.id.deactivate ? DeactivateAccountActivity.class : BlockedUsersActivity.class;
        Intent intent = new Intent(ManageAccountActivity.this, target);
        startActivity(intent);
    }

}
