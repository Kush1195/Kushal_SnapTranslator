package language.translate.snap.translate;


import android.app.Application;
import android.content.Context;

import com.google.android.gms.ads.MobileAds;
import com.google.android.gms.ads.initialization.InitializationStatus;
import com.google.android.gms.ads.initialization.OnInitializationCompleteListener;

public class MyApp extends Application {

    private static MyApp app;

    Context mContext;
    private static AppOpenManager appOpenManager;

    @Override
    public void onCreate() {
        super.onCreate();
        app = this;

        MobileAds.initialize(
                this,
                new OnInitializationCompleteListener() {
                    @Override
                    public void onInitializationComplete(InitializationStatus initializationStatus) {
                    }
                });

        appOpenManager = new AppOpenManager(this);

        mContext = getApplicationContext();

    }

    public static MyApp getInstance() {
        return app;
    }

}