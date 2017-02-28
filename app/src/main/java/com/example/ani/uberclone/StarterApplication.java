package com.example.ani.uberclone;

import android.app.Application;

import com.parse.Parse;
import com.parse.ParseACL;

public class StarterApplication extends Application {
    @Override
    public void onCreate() {
        super.onCreate();

        Parse.enableLocalDatastore(this);

        Parse.initialize(new Parse.Configuration.Builder(getApplicationContext())
                .applicationId("bcaf45625b511f13e12cdb2e5dd1064c8490c604")
                .clientKey("8c8dd806c9fe52e6c574ca75874ae8b673d45edd")
                .server("http://ec2-54-202-196-45.us-west-2.compute.amazonaws.com:80/parse/")
                .build()
        );

        ParseACL defaultACL = new ParseACL();
        defaultACL.setPublicReadAccess(true);
        defaultACL.setPublicWriteAccess(true);
        ParseACL.setDefaultACL(defaultACL, true);

    }
}
