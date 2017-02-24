package org.adblockplus.libadblockplus.android.webviewapp;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by max on 21.02.17.
 */

public class DetailInfo {

    public String currentUrl;
    public long adblockAlgoritmWorkTime;
    public int blockedUrlCount;
    public List<String> blockedUrls = new ArrayList<>();
    public String adblockType;
    public long time;

}
