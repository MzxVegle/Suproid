package com.vegle.supriod;

import android.app.Application;

/**
 * Created by Vegle on 2017/10/28.
 */

public class MyApp extends Application {
    public static MyApp instance = null;
    public static MyApp getInstance(){
        return instance;
    }

    @Override
    public void onCreate() {
        super.onCreate();
        instance = this;
    }
}
