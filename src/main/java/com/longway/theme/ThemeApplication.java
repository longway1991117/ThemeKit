package com.longway.theme;

import android.app.Application;
import android.content.Context;

/**
 * Created by longway on 16/3/30.
 */
public class ThemeApplication extends Application {

    private static ThemeApplication sApp;

    @Override
    public void onCreate() {
        super.onCreate();
        sApp = this;
        ThemeManager.getInstance().init(this);
    }

    public static ThemeApplication getInstance() {
        return sApp;
    }

    @Override
    protected void attachBaseContext(Context base) {
        super.attachBaseContext(base);
    }

    @Override
    public void onLowMemory() {
        super.onLowMemory();
        sApp = this;
    }
}
