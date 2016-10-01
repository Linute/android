package com.linute.linute.UtilsAndHelpers;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.MenuItem;
import android.view.View;
import android.webkit.WebChromeClient;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.ProgressBar;

import com.linute.linute.R;

/**
 * Created by QiFeng on 4/5/16.
 */


/**
 * Provide this activty with toolbar title and url
 *
 */
public class WebViewActivity extends BaseSocketActivity {

    public static final String LOAD_URL = "url_to_load";

    private ProgressBar mProgressBar;

    private Toolbar mToolbar;

    @Override
    protected void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_webview);

        Bundle b = getIntent().getExtras();

        String url = "";
        if (b != null){
            url = b.getString(LOAD_URL);
        }

        setUpToolBar();
        setUpWebView(url);
        setUpProgressBar();
    }

    private void setUpToolBar() {
        mToolbar = (Toolbar) findViewById(R.id.webviewactivity_toolbar);
        mToolbar.setNavigationIcon(R.drawable.ic_action_navigation_arrow_back_inverted);
        mToolbar.setTitle("Loading");
        setSupportActionBar(mToolbar);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == android.R.id.home) {
            onBackPressed();
            return true;
        }
        return false;
    }

    private void setUpWebView(String url){
        WebView webView = (WebView) findViewById(R.id.webviewactivity_webview);
        webView.setWebViewClient(new LinuteBrowser());

        webView.getSettings().setJavaScriptEnabled(true);
        webView.setWebChromeClient(new LinuteChrome());

        if (Utils.isNetworkAvailable(this)) {
            webView.loadUrl(url);
        }else {
            Utils.showBadConnectionToast(this);
        }
    }

    private void setUpProgressBar(){
        mProgressBar = (ProgressBar) findViewById(R.id.progress_bar);
        mProgressBar.setProgress(0);
        if (mProgressBar.getVisibility() == View.INVISIBLE){
            mProgressBar.setVisibility(View.VISIBLE);
        }
    }

    private class LinuteBrowser extends WebViewClient {
        @Override
        public boolean shouldOverrideUrlLoading(WebView view, String url) {
            mProgressBar.setProgress(0);
            if (mProgressBar.getVisibility() == View.GONE){
                mProgressBar.setVisibility(View.VISIBLE);
            }
            view.loadUrl(url);
            return true;
        }
    }

    private class LinuteChrome extends WebChromeClient {
        @Override
        public void onReceivedTitle(WebView view, String title) {
            super.onReceivedTitle(view, title);
            mToolbar.setTitle(title);
        }

        @Override
        public void onProgressChanged(WebView view, int newProgress) {
            super.onProgressChanged(view, newProgress);
            mProgressBar.setProgress(newProgress);
            if (newProgress >= 100 && mProgressBar.getVisibility() == View.VISIBLE)
                mProgressBar.setVisibility(View.GONE);
        }
    }
}
