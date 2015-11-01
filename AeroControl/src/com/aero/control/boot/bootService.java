package com.aero.control.boot;


import android.app.Service;
import android.content.Intent;
import android.os.IBinder;

import com.aero.control.helpers.settingsHelper;

public class bootService extends Service {

    private static final settingsHelper settings = new settingsHelper();

    @Override
    public IBinder onBind(Intent intent)
    {
        return null;
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        // service started

        settings.setSettings(getBaseContext(), null, true);

        return START_NOT_STICKY;
    }
}