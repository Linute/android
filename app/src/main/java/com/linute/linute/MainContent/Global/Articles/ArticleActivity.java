package com.linute.linute.MainContent.Global.Articles;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;

import com.linute.linute.R;

import java.util.List;

/**
*   Used for deeplinked articles
* */
public class ArticleActivity extends AppCompatActivity {

    private static final String TAG = ArticleActivity.class.getSimpleName();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        Intent intent = getIntent();
        Uri data = intent.getData();
        List<String> pathSegments = data.getPathSegments();
        Log.d(TAG, pathSegments.toString());
        String articleId = pathSegments.get(pathSegments.size()-1);

        setContentView(R.layout.activity_article);
        getSupportFragmentManager().beginTransaction().replace(R.id.activity_article, ArticleFragment.newInstance(articleId)).commit();
    }
}
