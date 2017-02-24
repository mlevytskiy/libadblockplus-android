/*
 * This file is part of Adblock Plus <https://adblockplus.org/>,
 * Copyright (C) 2006-2016 Eyeo GmbH
 *
 * Adblock Plus is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License version 3 as
 * published by the Free Software Foundation.
 *
 * Adblock Plus is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with Adblock Plus.  If not, see <http://www.gnu.org/licenses/>.
 */

package org.adblockplus.libadblockplus.android.webview;

import android.content.Context;
import android.graphics.Bitmap;
import android.util.AttributeSet;
import android.util.Log;
import android.webkit.WebView;
import android.webkit.WebViewClient;

import org.adblockplus.libadblockplus.FilterEngine;
import org.adblockplus.libadblockplus.android.AdblockEngine;

import java.util.HashMap;
import java.util.Map;
import java.util.regex.Pattern;

/**
 * WebView with ad blocking
 */
public class AdblockWebView extends WebView {

  public static final AdblockType DEFAULT_ADBLOCK_TYPE = AdblockType.EASY_LIST_NATIVE_CODE;
  private static final Pattern RE_IMAGE = Pattern.compile("\\.(?:gif|png|jpe?g|bmp|ico)$", Pattern.CASE_INSENSITIVE);
  private static final Pattern RE_FONT = Pattern.compile("\\.(?:ttf|woff)$", Pattern.CASE_INSENSITIVE);

  private AdBlocker adBlocker;
  private AdblockType adblockType = DEFAULT_ADBLOCK_TYPE;
  private AdblockEngine adblockEngine;
  private WebViewPageLoadFinishing listener;

  public AdblockWebView(Context context) {
    super(context);
    init(context);
  }

  public AdblockWebView(Context context, AttributeSet attrs) {
    super(context, attrs);
    init(context);
  }

  public AdblockWebView(Context context, AttributeSet attrs, int defStyleAttr) {
    super(context, attrs, defStyleAttr);
    init(context);
  }

  public void setListener(WebViewPageLoadFinishing listener) {
    this.listener = listener;
  }

  private void init(Context context) {
    adBlocker = AdBlocker.instance;
    adBlocker.init(context);
    getSettings().setJavaScriptEnabled(true);
    getSettings().setJavaScriptCanOpenWindowsAutomatically(true);
    getSettings().setDomStorageEnabled(true);
    getSettings().setLoadWithOverviewMode(true);
    getSettings().setUseWideViewPort(true);
  }

  @Override
  public void setWebViewClient(final WebViewClient client) {

    WebViewClient newWebViewClient = new WebViewClient() {

      @Override
      public void onPageStarted(WebView view, String url, Bitmap favicon) {
        client.onPageStarted(view, url, favicon);
        Log.i("assets", "page started=" + url);
      }

      @Override
      public void onPageFinished(WebView view, String url) {
        client.onPageFinished(view, url);
        String next = adBlocker.getNext();
        if (next == null) {
          adBlocker.saveResult();
        }
        AdblockWebView.this.loadUrl(next);
//        long timeDiff = 0;
//        for (Map.Entry<String, Long> entry : times.entrySet()) {
//          timeDiff = entry.getValue() + timeDiff;
//        }
//        times.clear();
//        double seconds2 = (double)timeDiff / 1000000000.0;
//
//        int blockedUrlCount = 0;
//        List<String> blockedUrls = new ArrayList<>();
//        for (Map.Entry<String, Boolean> entry : loadedUrls.entrySet()) {
//          if (entry.getValue()) {
//            blockedUrls.add(entry.getKey());
//            blockedUrlCount++;
//          }
//        }
//        loadedUrls.clear();
//
//        listener.loadFinishing(url, adblockType, timeDiff, blockedUrlCount, blockedUrls);
//
//        Log.d("test", "time for analyzing url:" + " domain list impl:" +  seconds2 + " blockedUrlCount=" + blockedUrlCount);
      }

//      @Override
//      public void onReceivedError(WebView view, WebResourceRequest request, WebResourceError error) {
//        super.onReceivedError(view, request, error);
//      }

      @Override
      public void onReceivedError(WebView view, int errorCode, String description, String failingUrl) {
        client.onReceivedError(view, errorCode, description, failingUrl);
        if (errorCode == -5) {
          Log.i("assets2", "rejected by proxy=" + failingUrl);
          adBlocker.putResult(failingUrl);
        } else {

        }
        String next = adBlocker.getNext();
        if (next == null) {
          adBlocker.saveResult();
        }
        AdblockWebView.this.loadUrl(next);
      }

      private Map<String, Boolean> loadedUrls = new HashMap<>();
      private Map<String, Long> times = new HashMap<>();

//      @Override
//      public WebResourceResponse shouldInterceptRequest(WebView view, String url) {
//        boolean result;
//        if (!loadedUrls.containsKey(url)) {
//          long time1 = System.nanoTime();
//          result = filter(view, url, adblockType, loadedUrls);
//          times.put(url, System.nanoTime() - time1);
//        } else {
//          result = loadedUrls.get(url);
//        }
//
//        return result ? AdBlocker.createEmptyResource() : super.shouldInterceptRequest(view, url);
//      }

    };

    super.setWebViewClient(newWebViewClient);
  }

  private boolean filter(WebView view, String url, AdblockType type, Map<String, Boolean> loadedUrls) {
    boolean result;
    if (!loadedUrls.containsKey(url)) {
      if (type == AdblockType.SIMPLE_DOMAIN_LIST) {
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

  public void setAblockType(AdblockType type) {
    adblockType = type;
  }

}
