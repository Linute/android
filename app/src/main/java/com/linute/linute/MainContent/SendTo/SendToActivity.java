package com.linute.linute.MainContent.SendTo;

import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;

/**
 * Created by mikhail on 11/11/16.
 */

public class SendToActivity extends AppCompatActivity{


    public static final String EXTRA_POST_ID = "post_id";

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        String postId = null;

        Intent intent = getIntent();
        if(intent == null){
            finish();
        }else{
            postId = intent.getStringExtra(EXTRA_POST_ID);
        }

        getSupportFragmentManager().beginTransaction().add(SendToFragment.newInstance(postId),SendToFragment.TAG);
    }

}
