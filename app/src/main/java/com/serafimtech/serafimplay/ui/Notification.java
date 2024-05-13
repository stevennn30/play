package com.serafimtech.serafimplay.ui;

import android.content.Context;
import android.graphics.Bitmap;
import android.net.ConnectivityManager;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.webkit.WebChromeClient;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.fragment.app.Fragment;
import androidx.navigation.Navigation;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import com.serafimtech.serafimplay.MainActivity;
import com.serafimtech.serafimplay.R;
import com.serafimtech.serafimplay.file.value.SerafimInfo;

import static com.serafimtech.serafimplay.App.getApp;

public class Notification extends Fragment {
    //<editor-fold desc="<Variable>">
    private WebView webView;
    private ImageView returnBtn;
    private SwipeRefreshLayout swipeRefreshLayout;
    private TextView updateText;
    private Handler mHandler;
    private ProgressBar progressBar;
    //</editor-fold>

    //<editor-fold desc="<LifeCycle>">
//    @Override
//    public void onWindowFocusChanged(boolean hasFocus) {
//        super.onWindowFocusChanged(hasFocus);
//        if (hasFocus) {
//            int flag =  View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY
//                    | View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN
//                    | View.SYSTEM_UI_FLAG_HIDE_NAVIGATION;
//            getWindow().getDecorView().setSystemUiVisibility(flag);
//        }
//    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View root = inflater.inflate(R.layout.notification, container, false);
        updateText = root.findViewById(R.id.update_text);
        swipeRefreshLayout = root.findViewById(R.id.refresh_layout);
        webView = root.findViewById(R.id.web_view);
        returnBtn = root.findViewById(R.id.return_btn);
        progressBar = root.findViewById(R.id.progress_bar);
        mHandler = new Handler();
        return root;
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        swipeRefreshLayout.setOnRefreshListener(() -> {
            loadUrl();
            mHandler.postDelayed(() -> swipeRefreshLayout.setRefreshing(false), 1000);
        });
        returnBtn.setOnClickListener((View v) -> Navigation.findNavController(requireView()).navigateUp());
        loadUrl();
    }

    //</editor-fold>

    private void loadUrl() {
        if (isConnected()) {
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
                public void onPageFinished(WebView view, String url) {
                    super.onPageFinished(view, url);
                    progressBar.setVisibility(View.GONE);
                    webView.setVisibility(View.VISIBLE);
                }

                @Override
                public void onPageStarted(WebView view, String url, Bitmap favicon) {
                    super.onPageStarted(view, url, favicon);
                    progressBar.setVisibility(View.VISIBLE);
                }
            }); //不調用系統瀏覽器
//        Toast.makeText(this, locale.getCountry(), Toast.LENGTH_SHORT).show();
            switch (getApp().localeCountryCode) {
                case "TW":
                    webView.loadUrl(SerafimInfo.official_website_tw);
                    break;
                case "CN":
                    webView.loadUrl(SerafimInfo.official_website_cn);
                    break;
                case "JP":
                    webView.loadUrl(SerafimInfo.official_website_jp);
                    break;
                case "KR":
                    webView.loadUrl(SerafimInfo.official_website_kr);
                    break;
                default:
                    webView.loadUrl(SerafimInfo.official_website_us);
                    break;
            }
        } else {
            updateText.setVisibility(View.VISIBLE);
            webView.setVisibility(View.INVISIBLE);
            Toast.makeText(getActivity(), R.string.no_internet_connected, Toast.LENGTH_SHORT).show();
        }
    }

    public boolean isConnected() {
        return ((ConnectivityManager) getActivity().getSystemService(Context.CONNECTIVITY_SERVICE)).getActiveNetworkInfo() != null;
    }

    public void setDefaultWebSettings(WebView webView) {
        WebSettings webSettings = webView.getSettings();
        //5.0以上开启混合模式加载
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            webSettings.setMixedContentMode(WebSettings.MIXED_CONTENT_ALWAYS_ALLOW);
        }
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

//    @Override
//    public boolean onKeyDown(int keyCode, KeyEvent event) {
//        if (keyCode == KeyEvent.KEYCODE_BACK && webView.canGoBack()) {
//            webView.goBack();
//            return true;
//        }
//        return super.onKeyDown(keyCode, event);
//    }
}
