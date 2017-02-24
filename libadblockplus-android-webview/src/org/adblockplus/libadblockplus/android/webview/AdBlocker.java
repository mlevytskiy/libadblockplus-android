package org.adblockplus.libadblockplus.android.webview;

import android.content.Context;
import android.os.AsyncTask;
import android.text.TextUtils;
import android.util.Log;
import android.webkit.WebResourceResponse;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.MalformedURLException;
import java.util.ArrayList;
import java.util.List;

public class AdBlocker {
    private static final String AD_HOSTS_FILE = "hosts.txt";
    private static final List<String> AD_HOSTS = new ArrayList<>();
    private static final String TAG = AdBlocker.class.getSimpleName();
    public static int currentIndex = 0;
    public static AdBlocker instance = new AdBlocker();
    public static File file = new File("sdcard/adblock.txt");
    FileWriter logWriter;
    BufferedWriter out;

    private AdBlocker() {

    }

    public static void init(final Context context) {
        new AsyncTask<Void, Void, Void>() {
            @Override
            protected Void doInBackground(Void... params) {
                try {
                    loadFromAssets(context);
                    Log.i("assets", "assets loaded");
                } catch (IOException e) {
                    // noop
                }
                return null;
            }
        }.execute();
    }

    private static void loadFromAssets(Context context) throws IOException {
        InputStream stream = context.getAssets().open(AD_HOSTS_FILE);
        InputStreamReader inputStreamReader = new InputStreamReader(stream);
        BufferedReader bufferedReader = new BufferedReader(inputStreamReader);
        String line;
        while ((line = bufferedReader.readLine()) != null) AD_HOSTS.add(line);
        bufferedReader.close();
        inputStreamReader.close();
        stream.close();
    }

    private List<String> resultList = new ArrayList<>();

    public void putResult(String str) {
        int index1 = "http://".length();
        str = str.substring(index1, str.length()-1);
        resultList.add(str);
        try {
            out.write(str);
            out.newLine();
            out.flush();
        } catch (Exception e) {

        }
    }

    public void saveResult() throws IOException {
        System.out.print(resultList);
        out.flush();
        out.close();
    }

    public String getLastResult() {
            return resultList.get(resultList.size()-1);
    }

    public String getFirst() {
        if (!file.exists()) {
            try {
                file.createNewFile();
            } catch (IOException e) {
                Log.e(TAG, e.getMessage());
            }
        }
        try {
            BufferedReader input = new BufferedReader(new FileReader(file));
            String last = "", line = "";

            while ((line = input.readLine()) != null) {
                last = line;
            }
            for (int i = 0; i < AD_HOSTS.size(); i++) {
                if (TextUtils.equals(AD_HOSTS.get(i), last)) {
                    currentIndex = i;
                }
            }

        } catch (Exception e) {
            Log.e(TAG, e.getMessage());
        }
        try {
            logWriter = new FileWriter(file, true);
        } catch (IOException e) {
            Log.e(TAG, e.getMessage());
        }
        out = new BufferedWriter(logWriter);
        return AD_HOSTS.get(currentIndex);
    }

    public String getNext() {
        currentIndex++;
        if (AD_HOSTS.size() == currentIndex) {
            return null;
        }
        return AD_HOSTS.get(currentIndex);
    }

    public static boolean isAd(String url) {
        try {
            return isAdHost(UrlUtils.getHost(url));
        } catch (MalformedURLException e) {
            Log.d("AmniX", e.toString());
            return false;
        }
    }

    private static boolean isAdHost(String host) {
        if (TextUtils.isEmpty(host)) {
            return false;
        }
        int index = host.indexOf(".");
        return index >= 0 && (AD_HOSTS.contains(host) ||
                index + 1 < host.length() && isAdHost(host.substring(index + 1)));
    }

    public static WebResourceResponse createEmptyResource() {
        return new WebResourceResponse("text/plain", "utf-8", new ByteArrayInputStream("".getBytes()));
    }

}