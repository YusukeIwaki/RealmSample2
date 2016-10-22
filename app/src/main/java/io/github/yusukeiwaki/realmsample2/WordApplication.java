package io.github.yusukeiwaki.realmsample2;

import android.app.Application;
import android.util.Log;

import io.realm.Realm;
import io.realm.log.AndroidLogger;
import io.realm.log.RealmLog;

public class WordApplication extends Application {
    @Override
    public void onCreate() {
        super.onCreate();

        Realm.init(this);
        RealmLog.add(new AndroidLogger(Log.VERBOSE));
    }
}
