package org.adblockplus.libadblockplus.android.webviewapp;

import android.app.Activity;
import android.os.Bundle;
import android.text.TextUtils;
import android.widget.TextView;

import com.google.gson.Gson;

/**
 * Created by max on 21.02.17.
 */

public class DetailInfoActivity extends Activity {

    public void onCreate(Bundle bundle) {
        super.onCreate(bundle);
        setContentView(R.layout.activity_detail_info);
        String str = MemoryCommunicator.getInstance().loadStr(Key.LAST_DETAIL_INFO);
        if ( !TextUtils.isEmpty(str) ) {
                DetailInfo detailInfo = new Gson().fromJson(str, DetailInfo.class);
                showDetailInfo(detailInfo);
        }
    }

    private void showDetailInfo(DetailInfo detailInfo) {
        double adblockAlgoritmWorkTime = (double) detailInfo.adblockAlgoritmWorkTime / 1000000000.0;
        TextView textView = (TextView) findViewById(R.id.text_view);
        String str = "current url=" + detailInfo.currentUrl + "\n" +
                "time for analyzing urls:" + adblockAlgoritmWorkTime + "\n" +
                "blockedUrlCount=" + detailInfo.blockedUrlCount + "\n" +
                     "adblockType=" + detailInfo.adblockType + "\n" +
                "blocked urls:\n" + TextUtils.join("\n///////////\n", detailInfo.blockedUrls);
        textView.setText(str);
    }
}
