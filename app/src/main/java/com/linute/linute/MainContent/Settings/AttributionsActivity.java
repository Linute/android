package com.linute.linute.MainContent.Settings;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.text.method.ScrollingMovementMethod;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.TextView;

import com.linute.linute.R;

public class AttributionsActivity extends AppCompatActivity {


    private TextView mAttributionsText;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_attributions);

        mAttributionsText = (TextView) findViewById(R.id.attributions_text_view);
        mAttributionsText.setMovementMethod(new ScrollingMovementMethod());

        Toolbar toolbar = (Toolbar) findViewById(R.id.attributions_toolbar);
        toolbar.setTitle("Attributions");
        toolbar.setNavigationIcon(R.drawable.ic_action_navigation_arrow_back_inverted);
        setSupportActionBar(toolbar);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == android.R.id.home){
            onBackPressed();
            return true;
        }else {
            return super.onOptionsItemSelected(item);
        }
    }
}
