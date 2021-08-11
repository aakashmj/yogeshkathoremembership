package com.yogeshkathore.foundation.application;

import android.app.Application;

import androidx.multidex.MultiDex;

import com.google.firebase.FirebaseApp;
import com.google.firebase.database.FirebaseDatabase;


/**
 * application class
 * <p>
 * Created by jangling on 2017/7/20.
 */

public class MyApplication extends Application {
    // File Directory in sd card
    public static final String DIRECTORY_NAME = "YogeshKathoreFoundationMembers";

    @Override
    public void onCreate() {
        super.onCreate();

        FirebaseApp.initializeApp(this);
        FirebaseDatabase.getInstance().setPersistenceEnabled(true);
        MultiDex.install(this);
    }
}


