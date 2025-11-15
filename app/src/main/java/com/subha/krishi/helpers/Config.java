package com.subha.krishi.helpers;

import android.content.pm.ApplicationInfo;
import android.content.pm.PackageManager;

public class Config {
    public static final String OPEN_WEATHER_API_KEY;
    public static final String NEWS_API;
    public static final String IMGBB_API;
    public static final String AGRO_API;

    static {
        String weatherKey = "", newsKey = "", imgbbKey = "", agroKey = "";
        try {
            ApplicationInfo appInfo = Krishi.getInstance()
                    .getPackageManager()
                    .getApplicationInfo(Krishi.getInstance().getPackageName(), PackageManager.GET_META_DATA);
            weatherKey = appInfo.metaData.getString("OPEN_WEATHER_API_KEY", "");
            newsKey = appInfo.metaData.getString("NEWS_API", "");
            imgbbKey = appInfo.metaData.getString("IMGBB_API", "");
            agroKey = appInfo.metaData.getString("AGRO_API", "");
        } catch (Exception e) {
            e.printStackTrace();
        }
        OPEN_WEATHER_API_KEY = weatherKey;
        NEWS_API = newsKey;
        IMGBB_API = imgbbKey;
        AGRO_API = agroKey;
    }
}
