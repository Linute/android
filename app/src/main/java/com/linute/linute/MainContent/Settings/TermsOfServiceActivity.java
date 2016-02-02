package com.linute.linute.MainContent.Settings;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.view.View;
import android.webkit.WebView;
import android.webkit.WebViewClient;

import com.linute.linute.R;
import com.linute.linute.UtilsAndHelpers.Utils;

public class TermsOfServiceActivity extends AppCompatActivity {

    private Toolbar mToolbar;
    private WebView mTermsWebView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_webview);

        setUpToolBar();
        setUpWebView();
    }

    private void setUpToolBar() {
        mToolbar = (Toolbar) findViewById(R.id.webviewactivity_toolbar);
        mToolbar.setTitle("Terms of Services");
        mToolbar.setNavigationIcon(R.drawable.ic_action_navigation_arrow_back_inverted);
        mToolbar.setNavigationOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                onBackPressed();
            }
        });

        setSupportActionBar(mToolbar);
    }

    private void setUpWebView(){
        mTermsWebView = (WebView) findViewById(R.id.webviewactivity_webview);
        mTermsWebView.setWebViewClient(new LinuteBrowser());
        if (Utils.isNetworkAvailable(this)) {
            mTermsWebView.loadUrl("http://www.linute.com/terms-of-service/");
        }else {
            Utils.showBadConnectionToast(this);
        }
    }


    private class LinuteBrowser extends WebViewClient {
        @Override
        public boolean shouldOverrideUrlLoading(WebView view, String url) {
            view.loadUrl(url);
            return true;
        }
    }
}
