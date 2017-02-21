package org.adblockplus.libadblockplus.android.webview;

import android.content.Context;
import android.graphics.Bitmap;
import android.util.AttributeSet;
import android.util.Log;
import android.webkit.WebResourceResponse;
import android.webkit.WebView;
import android.webkit.WebViewClient;

import org.adblockplus.libadblockplus.FilterEngine;
import org.adblockplus.libadblockplus.android.AdblockEngine;

import java.util.HashMap;
import java.util.Map;
import java.util.regex.Pattern;

/**
 * Created by max on 20.02.17.
 */

public class AdblockWebView4 extends WebView {

    private static final Pattern RE_IMAGE = Pattern.compile("\\.(?:gif|png|jpe?g|bmp|ico)$", Pattern.CASE_INSENSITIVE);
    private static final Pattern RE_FONT = Pattern.compile("\\.(?:ttf|woff)$", Pattern.CASE_INSENSITIVE);

    private AdBlocker adBlocker;
    private AdblockEngine adblockEngine;

    public AdblockWebView4(Context context) {
        super(context);
        init(context);
    }

    public AdblockWebView4(Context context, AttributeSet attrs) {
        super(context, attrs);
        init(context);
    }

    public AdblockWebView4(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init(context);
    }

    public AdblockWebView4(Context context, AttributeSet attrs, int defStyleAttr, int defStyleRes) {
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
                long contentTypeAnalyzingSum = 0;
                for (Map.Entry<String, Long> entry : times1.entrySet()) {
                    contentTypeAnalyzingSum = entry.getValue() + contentTypeAnalyzingSum;
                }
                long nativeTimeDiff = 0;
                for (Map.Entry<String, Long> entry : times2.entrySet()) {
                    nativeTimeDiff = entry.getValue() + nativeTimeDiff;
                }
                int blockedUrlCount = 0;
                for (Map.Entry<String, Boolean> entry : loadedUrls.entrySet()) {
                    if (entry.getValue()) {
                        blockedUrlCount++;
                    }
                }
                times1.clear();
                times2.clear();
                StringBuilder stringBuilder = new StringBuilder();
                for (Map.Entry<String, Boolean> entry : loadedUrls.entrySet()) {
                    stringBuilder.append(entry.getValue() + entry.getKey());
                    stringBuilder.append("\n");
                }
                System.out.print(stringBuilder.toString());
                loadedUrls.clear();
                double seconds1 = (double)contentTypeAnalyzingSum / 1000000000.0;
                double seconds2 = (double)nativeTimeDiff / 1000000000.0;
                Log.d("test", "time for analyzing url: contentType:" + seconds1 + " native code:" +  seconds2 + "blocked urls=" + blockedUrlCount);
            }

            @Override
            public void onReceivedError(WebView view, int errorCode, String description, String failingUrl) {
                client.onReceivedError(view, errorCode, description, failingUrl);
            }

            private Map<String, Boolean> loadedUrls = new HashMap<>();
            private Map<String, Long> times1 = new HashMap<>();
            private Map<String, Long> times2 = new HashMap<>();

            @Override
            public WebResourceResponse shouldInterceptRequest(WebView view, String url) {
                boolean result;
                if (!loadedUrls.containsKey(url)) {
                    long time1 = System.nanoTime();
                    FilterEngine.ContentType contentType = getContentType(url);
                    long time2 = System.nanoTime();
                    times1.put(url, time2 - time1);
                    result = adblockEngine.matches(url, contentType, new String[]{} );
                    long time3Diff = System.nanoTime() - time2;
                    times2.put(url, time3Diff);
                    loadedUrls.put(url, result);
                } else {
                    result = loadedUrls.get(url);
                }

                return result ? AdBlocker.createEmptyResource() : super.shouldInterceptRequest(view, url);
            }

            @Override
            public boolean shouldOverrideUrlLoading(WebView view, String url) {
                Log.i("test", url);
                return super.shouldOverrideUrlLoading(view, url);
            }

        };

        super.setWebViewClient(newWebViewClient);
    }

    private FilterEngine.ContentType getContentType(String url) {
        if (url.lastIndexOf(".css") != -1) {
            return FilterEngine.ContentType.STYLESHEET;
        } else if (url.lastIndexOf(".js") != -1) {
            return FilterEngine.ContentType.SCRIPT;
        } else if (url.lastIndexOf(".html") != -1) {
            return FilterEngine.ContentType.SUBDOCUMENT;
        } else if (RE_IMAGE.matcher(url).find()) {
            return FilterEngine.ContentType.IMAGE;
        } else if (RE_FONT.matcher(url).find()) {
            return FilterEngine.ContentType.FONT;
        } else {
            return FilterEngine.ContentType.OTHER;
        }

    }

    public void setAdblockEngine(AdblockEngine adblockEngine) {
        this.adblockEngine = adblockEngine;
    }

}
