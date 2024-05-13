package com.serafimtech.serafimplay.ui;

import android.graphics.Bitmap;
import android.os.Bundle;
import android.os.Handler;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.webkit.WebChromeClient;
import android.webkit.WebResourceError;
import android.webkit.WebResourceRequest;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.ProgressBar;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import com.serafimtech.serafimplay.R;
import com.serafimtech.serafimplay.file.value.SerafimInfo;

import static com.serafimtech.serafimplay.App.getApp;

public class Navigation_Mall extends Fragment {
    private WebView webView;
    private TextView updateText;
    private SwipeRefreshLayout swipeRefreshLayout;
    private Handler mHandler;
    private ProgressBar progressBar;

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View root = inflater.inflate(R.layout.navigation_mall, container, false);

        swipeRefreshLayout = root.findViewById(R.id.refresh_layout);
        updateText = root.findViewById(R.id.update_text);
        webView = root.findViewById(R.id.web_view);
        progressBar = root.findViewById(R.id.progress_bar);
        mHandler = new Handler();
        swipeRefreshLayout.setOnRefreshListener(() -> {
            loadUrl();
            mHandler.postDelayed(() -> swipeRefreshLayout.setRefreshing(false), 1000);
        });
        loadUrl();
        return root;
    }

    private void loadUrl() {
        updateText.setVisibility(View.INVISIBLE);
        webView.setVisibility(View.VISIBLE);
        setDefaultWebSettings(webView);
        webView.setWebChromeClient(new WebChromeClient() {
            @Override
            public void onProgressChanged(WebView view, int newProgress) {
                super.onProgressChanged(view, newProgress);
                progressBar.setProgress(newProgress);
            }
        });
        webView.setWebViewClient(new WebViewClient() {

            @Override
            public void onPageStarted(WebView view, String url, Bitmap favicon) {
                super.onPageStarted(view, url, favicon);
                progressBar.setVisibility(View.VISIBLE);
                view.setVisibility(View.INVISIBLE);
//                    progressDialogUtil.showProgressDialogWithMessage(fragmentActivity, getResources().getString(R.string.connecting));
            }

            @Override
            public void onPageFinished(WebView view, String url) {
                progressBar.setVisibility(View.GONE);
                view.setVisibility(View.VISIBLE);
            }

            @Override
            public void onReceivedError(WebView view, WebResourceRequest request, WebResourceError error) {
                super.onReceivedError(view, request, error);
            }
        });
        webView.setScrollBarStyle(View.SCROLLBARS_INSIDE_OVERLAY);
        //TODO 各國籍網址
//        webView.loadUrl("https://www.serafim.cc/");
            switch (getApp().ipCountryCode) {
                case "TW":
                    webView.loadUrl(SerafimInfo.shop_url_tw);
                    break;
                case "CN":
                    webView.loadUrl(SerafimInfo.shop_url_cn);
                    break;
                case "JP":
                    webView.loadUrl(SerafimInfo.shop_url_jp);
                    break;
                case "KR":
                    webView.loadUrl(SerafimInfo.shop_url_kr);
                    break;
                default:
                    webView.loadUrl(SerafimInfo.shop_url_us);
                    break;
            }
    }

    public void setDefaultWebSettings(WebView webView) {
        WebSettings webSettings = webView.getSettings();
        //5.0以上开启混合模式加载
        webSettings.setMixedContentMode(WebSettings.MIXED_CONTENT_ALWAYS_ALLOW);
        webSettings.setLoadWithOverviewMode(true);
        webSettings.setUseWideViewPort(true);
        //允许js代码
        webSettings.setJavaScriptEnabled(true);
        //允许SessionStorage/LocalStorage存储
        webSettings.setDomStorageEnabled(true);
        //禁用放缩
        webSettings.setDisplayZoomControls(false);
        webSettings.setBuiltInZoomControls(false);
        //禁用文字缩放
        webSettings.setTextZoom(100);
        //允许缓存，设置缓存位置
//        webSettings.setAppCacheEnabled(true);
//        webSettings.setAppCachePath(getActivity().getDir("appcache", 0).getPath());
        //允许WebView使用File协议
        webSettings.setAllowFileAccess(true);
        //自动加载图片
        webSettings.setLoadsImagesAutomatically(true);
    }
}