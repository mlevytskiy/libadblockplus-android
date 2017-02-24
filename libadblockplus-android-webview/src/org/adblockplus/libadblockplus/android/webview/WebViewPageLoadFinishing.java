package org.adblockplus.libadblockplus.android.webview;

import java.util.List;

/**
 * Created by max on 21.02.17.
 */

public interface WebViewPageLoadFinishing {

    void loadFinishing(String url, AdblockType currentAdblockType, long adblockAlgoritmWorkTime, int blockedUrlCount, List<String> blockedUrls);

}
