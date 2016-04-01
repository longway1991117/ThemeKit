package com.longway.theme;

import android.util.Log;

/**
 * Created by longway on 16/3/30.
 */
public class Time {
    private static final String TAG = Time.class.getSimpleName();
    private long mPerformStartTime;

    public void setPerformStartTime(long performStartTime) {
        this.mPerformStartTime = performStartTime;
    }

    public void print() {
        Log.d(TAG, "consume time:" + (System.currentTimeMillis() - mPerformStartTime));
    }

}
