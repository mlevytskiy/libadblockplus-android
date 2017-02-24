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

import static org.adblockplus.libadblockplus.android.webview.AdblockType.SIMPLE_DOMAIN_LIST;

/**
 * Created by max on 20.02.17.
 */

public class AdblockWebView2 extends WebView {

    private static final Pattern RE_IMAGE = Pattern.compile("\\.(?:gif|png|jpe?g|bmp|ico)$", Pattern.CASE_INSENSITIVE);
    private static final Pattern RE_FONT = Pattern.compile("\\.(?:ttf|woff)$", Pattern.CASE_INSENSITIVE);

    private AdBlocker adBlocker;
    private AdblockType adblockType = SIMPLE_DOMAIN_LIST;
    private AdblockEngine adblockEngine;

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
        adBlocker = AdBlocker.instance;
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
                    result = filter(view, url, adblockType, loadedUrls);
                    times1.put(url, System.nanoTime() - time1);
                } else {
                    result = loadedUrls.get(url);
                }

                return result ? AdBlocker.createEmptyResource() : super.shouldInterceptRequest(view, url);
            }

        };

        super.setWebViewClient(newWebViewClient);
    }

    private boolean filter(WebView view, String url, AdblockType type, Map<String, Boolean> loadedUrls) {
        boolean result;
        if (!loadedUrls.containsKey(url)) {
            if (type == SIMPLE_DOMAIN_LIST) {
                result = adBlocker.isAd(url);
            } else {
                FilterEngine.ContentType contentType = getContentType(url);
                result = adblockEngine.matches(url, contentType, new String[]{} );
            }
            loadedUrls.put(url, result);
        } else {
            result = loadedUrls.get(url);
        }
        return result;
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

    public void setAdblockEngine(final AdblockEngine adblockEngine) {
        this.adblockEngine = adblockEngine;
    }

    public void chooseAblockType(AdblockType type) {
        adblockType = type;
    }

}
