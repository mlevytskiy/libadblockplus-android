package org.adblockplus.libadblockplus.android.webview;

import android.content.Context;
import android.graphics.Bitmap;
import android.util.AttributeSet;
import android.util.Log;
import android.webkit.WebResourceResponse;
import android.webkit.WebView;
import android.webkit.WebViewClient;

import org.adblockplus.libadblockplus.android.AdblockEngine;

import java.util.HashMap;
import java.util.Map;

/**
 * Created by max on 20.02.17.
 */

public class AdblockWebView2 extends WebView {

    private AdBlocker adBlocker;

    public AdblockWebView2(Context context) {
        super(context);
        init(context);
    }

    public AdblockWebView2(Context context, AttributeSet attrs) {
        super(context, attrs);
        init(context);
    }

    public AdblockWebView2(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init(context);
    }

    public AdblockWebView2(Context context, AttributeSet attrs, int defStyleAttr, int defStyleRes) {
        super(context, attrs, defStyleAttr, defStyleRes);
        init(context);
    }

    private void init(Context context) {
        adBlocker = new AdBlocker();
        adBlocker.init(context);
        getSettings().setJavaScriptEnabled(true);
        getSettings().setJavaScriptCanOpenWindowsAutomatically(true);
        getSettings().setDomStorageEnabled(true);
    }

    @Override
    public void setWebViewClient(final WebViewClient client) {

        WebViewClient newWebViewClient = new WebViewClient() {
            @Override
            public void onPageStarted(WebView view, String url, Bitmap favicon) {
                client.onPageStarted(view, url, favicon);
            }

            @Override
            public void onPageFinished(WebView view, String url) {
                client.onPageFinished(view, url);
                long nativeTimeDiff = 0;
                for (Map.Entry<String, Long> entry : times1.entrySet()) {
                    nativeTimeDiff = entry.getValue() + nativeTimeDiff;
                }
                times1.clear();
                double seconds2 = (double)nativeTimeDiff / 1000000000.0;

                int blockedUrlCount = 0;
                for (Map.Entry<String, Boolean> entry : loadedUrls.entrySet()) {
                    if (entry.getValue()) {
                        blockedUrlCount++;
                    }
                }

                Log.d("test", "time for analyzing url:" + " domain list impl:" +  seconds2 + " blockedUrlCount=" + blockedUrlCount);
            }

            @Override
            public void onReceivedError(WebView view, int errorCode, String description, String failingUrl) {
                client.onReceivedError(view, errorCode, description, failingUrl);
            }

            private Map<String, Boolean> loadedUrls = new HashMap<>();
            private Map<String, Long> times1 = new HashMap<>();

            @Override
            public WebResourceResponse shouldInterceptRequest(WebView view, String url) {
                boolean result;
                if (!loadedUrls.containsKey(url)) {
                    long time1 = System.nanoTime();
                    result = adBlocker.isAd(url);
                    times1.put(url, System.nanoTime() - time1);
                    loadedUrls.put(url, result);
                } else {
                    result = loadedUrls.get(url);
                }

                return result ? AdBlocker.createEmptyResource() : super.shouldInterceptRequest(view, url);
            }

        };

        super.setWebViewClient(newWebViewClient);
    }

    public void setAdblockEngine(final AdblockEngine adblockEngine) {

    }
}
