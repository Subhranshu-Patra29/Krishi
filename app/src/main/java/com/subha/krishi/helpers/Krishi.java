package com.subha.krishi.helpers;

import android.app.Application;

public class Krishi extends Application {
    private static Krishi instance;

    public static Krishi getInstance() {
        return instance;
    }

    @Override
    public void onCreate() {
        super.onCreate();
        instance = this;
    }
}
