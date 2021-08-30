package language.translate.snap.translate.activities;

import androidx.appcompat.app.AppCompatActivity;

import android.accessibilityservice.AccessibilityServiceInfo;
import android.annotation.SuppressLint;
import android.app.AppOpsManager;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Process;
import android.preference.PreferenceManager;
import android.provider.Settings;
import android.util.Log;
import android.view.View;
import android.view.accessibility.AccessibilityManager;
import android.widget.Button;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RatingBar;
import android.widget.TextView;

import com.ayoubfletcher.consentsdk.ConsentSDK;
import com.google.android.gms.ads.AdListener;
import com.google.android.gms.ads.AdLoader;
import com.google.android.gms.ads.InterstitialAd;
import com.google.android.gms.ads.VideoController;
import com.google.android.gms.ads.VideoOptions;
import com.google.android.gms.ads.formats.NativeAdOptions;
import com.google.android.gms.ads.formats.UnifiedNativeAd;
import com.google.android.gms.ads.formats.UnifiedNativeAdView;

import language.translate.snap.translate.R;

import language.translate.snap.translate.model.Language;
import language.translate.snap.translate.receiver.LanguageDetails;
import language.translate.snap.translate.service.TextTranslatorService;
import language.translate.snap.translate.utils.HelperResizer;

import java.util.ArrayList;
import java.util.List;

public class MainActivity extends AppCompatActivity {

    public static UnifiedNativeAd unifiedNativeAd1;
    Context mContext;
    public static int for_intro = 0;
    public static ArrayList<Language> languages = new ArrayList<>();
    public static View view;
    public static ImageView btn_addView, btn_swap, dropdown, dropdown1;
    public static boolean isSource = true;
    LinearLayout ll_addview, source, swap, target, lang_bg;
    public AppOpsManager mAppOpsManager;
    public boolean overlayEnable;
    public SharedPreferences sharedPreferences;
    public static TextView sourceBtn, targetBtn;
    public static int viv = 1, viv2 = 1;
    public static MainActivity mainActivity;
    ImageView btn_help,more;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        mainActivity = this;
        mContext = this;
        loadInterstitial();
        init();
        resize();
        initNativeAdvanceAds();
    }

    @SuppressLint("WrongConstant")
    private void init() {

        boolean for_check = false;

        lang_bg = findViewById(R.id.lang_bg);
        more = findViewById(R.id.more);
        btn_help = findViewById(R.id.btn_help);
        source = findViewById(R.id.source);
        sourceBtn = findViewById(R.id.btn_select_source);
        dropdown = findViewById(R.id.dropdown);
        btn_addView = findViewById(R.id.btn_addView);
        dropdown1 = findViewById(R.id.dropdown1);
        target = findViewById(R.id.target);
        targetBtn = findViewById(R.id.btn_select_target);
        ll_addview = findViewById(R.id.ll_addview);
        swap = findViewById(R.id.swap);
        btn_swap = findViewById(R.id.btn_swap);

        sharedPreferences = PreferenceManager.getDefaultSharedPreferences(mContext);

        if (Build.VERSION.SDK_INT >= 23) {
            mAppOpsManager = (AppOpsManager) getSystemService("appops");
            if (mAppOpsManager.checkOpNoThrow("android:system_alert_window", Process.myUid(), getPackageName()) == 0) {
                for_check = true;
            }
            overlayEnable = for_check;
        } else {
            overlayEnable = true;
        }
        if (!isAccessibilityEnabled(getApplicationContext())) {
            startActivity(new Intent("android.settings.ACCESSIBILITY_SETTINGS"));
        }

        Intent intent = new Intent("android.speech.action.GET_LANGUAGE_DETAILS");
        intent.setPackage("com.google.android.googlequicksearchbox");
        sendOrderedBroadcast(intent, null, new LanguageDetails(), null, -1, null, null);

        btn_swap.setOnClickListener(new View.OnClickListener() {
            public void onClick(View view) {

                String tempName, tempCode;
                tempName = sharedPreferences.getString("sname", "English (India)");
                tempCode = sharedPreferences.getString("scode", "en");

                SharedPreferences.Editor editor = sharedPreferences.edit();
                editor.putString("sname", sharedPreferences.getString("tname", "Hindi (India)"));
                editor.putString("scode", sharedPreferences.getString("tcode", "hi"));
                editor.putString("tname", tempName);
                editor.putString("tcode", tempCode);
                editor.apply();

                sourceBtn.setText(sharedPreferences.getString("sname", "English (India)"));
                targetBtn.setText(sharedPreferences.getString("tname", "Hindi (India)"));

            }
        });

        ll_addview.setOnClickListener(new View.OnClickListener() {
            public void onClick(View view) {
                if (isAccessibilityEnabled(getApplicationContext())) {
                    if (Build.VERSION.SDK_INT >= 23) {
                        if (!Settings.canDrawOverlays(getApplicationContext())) {
                            Intent intent1 = new Intent(Settings.ACTION_MANAGE_OVERLAY_PERMISSION, Uri.parse("package:" + getPackageName()));
                            startActivityForResult(intent1, 0);
                        } else {

                            Intent intent = new Intent(MainActivity.this, TextTranslatorService.class);

                            if (!sharedPreferences.getBoolean("added", false)) {
                                SharedPreferences.Editor editor = sharedPreferences.edit();
                                editor.putBoolean("added", true);
                                editor.apply();
                                startService(intent);

                            } else {
                                SharedPreferences.Editor editor = sharedPreferences.edit();
                                editor.putBoolean("added", false);
                                editor.apply();
                                stopService(intent);
                            }
                        }

                        btn_addView.setImageResource(Integer.parseInt(String.valueOf(sharedPreferences.getBoolean("added", false) ? R.drawable.on : R.drawable.off)));
                    } else {

                        Intent intent = new Intent(MainActivity.this, TextTranslatorService.class);

                        if (!sharedPreferences.getBoolean("added", false)) {
                            SharedPreferences.Editor editor = sharedPreferences.edit();
                            editor.putBoolean("added", true);
                            editor.apply();
                            startService(intent);

                        } else {
                            SharedPreferences.Editor editor = sharedPreferences.edit();
                            editor.putBoolean("added", false);
                            editor.apply();
                            stopService(intent);
                        }

                        btn_addView.setImageResource(Integer.parseInt(String.valueOf(sharedPreferences.getBoolean("added", false) ? R.drawable.on : R.drawable.off)));
                    }
                } else {
                    Intent intent = new Intent(Settings.ACTION_ACCESSIBILITY_SETTINGS);
                    startActivity(intent);
                }

            }
        });

        targetBtn.setText(sharedPreferences.getString("tname", "Hindi (India)"));

        sourceBtn.setText(sharedPreferences.getString("sname", "English (India)"));

        source.setOnClickListener(new View.OnClickListener() {
            public void onClick(View view) {
                isSource = true;
                if (HelperResizer.checkAd==0)
                {
                    if (interstitialAd.isLoaded()) {
                        interstitialAd.setAdListener(new AdListener() {
                            @Override
                            public void onAdClosed() {
                                startActivity(new Intent(mContext, LanguageSelectionActivity.class));
                                loadInterstitial();
                            }
                        });
                        interstitialAd.show();
                    } else {
                        startActivity(new Intent(mContext, LanguageSelectionActivity.class));
                    }
                }
                else{
                    startActivity(new Intent(mContext, LanguageSelectionActivity.class));
                }
                //startActivity(new Intent(mContext, LanguageSelectionActivity.class));

            }
        });

        target.setOnClickListener(new View.OnClickListener() {
            public void onClick(View view) {
                isSource = false;
                if (HelperResizer.checkAd==0) {
                    if (interstitialAd.isLoaded()) {
                        interstitialAd.setAdListener(new AdListener() {
                            @Override
                            public void onAdClosed() {
                                startActivity(new Intent(mContext, LanguageSelectionActivity.class));
                                loadInterstitial();
                            }
                        });
                        interstitialAd.show();
                    } else {
                        startActivity(new Intent(mContext, LanguageSelectionActivity.class));
                    }
                }
                else {
                    startActivity(new Intent(mContext, LanguageSelectionActivity.class));
                }
               // startActivity(new Intent(mContext, LanguageSelectionActivity.class));

            }
        });

        btn_help.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                for_intro = 1;
                if (HelperResizer.checkAd==0) {
                    if (interstitialAd.isLoaded()) {
                        interstitialAd.setAdListener(new AdListener() {
                            @Override
                            public void onAdClosed() {
                                Intent intent = new Intent(MainActivity.this, IntroductionAppActivity.class);
                                intent.putExtra("fromMain", true);
                                startActivity(intent);
                                loadInterstitial();
                            }
                        });
                        interstitialAd.show();
                    } else {
                        Intent intent = new Intent(MainActivity.this, IntroductionAppActivity.class);
                        intent.putExtra("fromMain", true);
                        startActivity(intent);
                    }
                }
                else {
                    Intent intent = new Intent(MainActivity.this, IntroductionAppActivity.class);
                    intent.putExtra("fromMain", true);
                    startActivity(intent);
                }
                /*Intent intent = new Intent(MainActivity.this, IntroductionAppActivity.class);
                intent.putExtra("fromMain", true);
                startActivity(intent);*/
            }
        });

        more.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                if (HelperResizer.checkAd==0) {
                    if (interstitialAd.isLoaded()) {
                        interstitialAd.setAdListener(new AdListener() {
                            @Override
                            public void onAdClosed() {
                                startActivity(new Intent(mainActivity, MoreSettingsActivity.class));
                                loadInterstitial();
                            }
                        });
                        interstitialAd.show();
                    } else {
                        startActivity(new Intent(mainActivity, MoreSettingsActivity.class));
                    }
                }
                else {
                    startActivity(new Intent(mainActivity, MoreSettingsActivity.class));
                }
              //  startActivity(new Intent(mainActivity, MoreSettingsActivity.class));
            }
        });

    }

    public void resize() {
        HelperResizer.getheightandwidth(mContext);
        HelperResizer.setSize(btn_help, 134, 134, true);
        HelperResizer.setSize(more, 134, 134, true);
        HelperResizer.setSize(ll_addview, 1000, 475);
        HelperResizer.setSize(btn_addView, 204, 102);
        HelperResizer.setSize(lang_bg, 1000, 270);
        HelperResizer.setSize(findViewById(R.id.i_one), 40, 40,true);
        HelperResizer.setSize(findViewById(R.id.i_two), 40, 40,true);
        HelperResizer.setSize(source, 414, 134);
        HelperResizer.setSize(target, 414, 134);
        HelperResizer.setSize(btn_swap, 80, 80,true);
        HelperResizer.setSize(dropdown, 40, 40, true);
        HelperResizer.setSize(dropdown1, 40, 40, true);

    }

    @SuppressLint("WrongConstant")
    public static boolean isAccessibilityEnabled(Context context) {
        for (AccessibilityServiceInfo id : ((AccessibilityManager) context.getSystemService("accessibility")).getEnabledAccessibilityServiceList(-1)) {
            if (id.getId().contains(context.getPackageName())) {
                return true;
            }
        }
        return false;
    }

    @Override
    public void onResume() {
        super.onResume();
        btn_addView.setImageResource(sharedPreferences.getBoolean("added", false) ? R.drawable.on : R.drawable.off);
        sourceBtn.setText(sharedPreferences.getString("sname", "English (India)"));
        targetBtn.setText(sharedPreferences.getString("tname", "Hindi (India)"));
    }

    @Override
    public void onBackPressed() {
        if(interstitialAd.isLoaded()){
            interstitialAd.setAdListener(new AdListener() {
                @Override
                public void onAdClosed() {
                    Intent intent = new Intent(MainActivity.this,ExitActivity.class);
                    startActivity(intent);
                    loadInterstitial();
                }
            });
            interstitialAd.show();
        }else {
            Intent intent = new Intent(this,ExitActivity.class);
            startActivity(intent);
        }
    }

    InterstitialAd interstitialAd;
    // Load Interstitial
    private void loadInterstitial() {
        interstitialAd = new InterstitialAd(this);
        interstitialAd.setAdUnitId(getString(R.string.admob_fullscreen_id));
        interstitialAd.loadAd(ConsentSDK.getAdRequest(this));
    }

    private AdLoader adLoader;

    // List of native ads that have been successfully loaded.
    private List<UnifiedNativeAd> mNativeAds = new ArrayList<>();
    public UnifiedNativeAdView nativeAdView;

    private void initNativeAdvanceAds() {
// MobileAds.initialize(this,
// getString(R.string.admob_app_id));

        flNativeAds = findViewById(R.id.flNativeAds);
        flNativeAds.setVisibility(View.GONE);
        nativeAdView = (UnifiedNativeAdView) findViewById(R.id.ad_view);

// The MediaView will display a video asset if one is present in the ad, and the
// first image asset otherwise.
        nativeAdView.setMediaView((com.google.android.gms.ads.formats.MediaView) nativeAdView.findViewById(R.id.ad_media));

// Register the view used for each individual asset.
        nativeAdView.setHeadlineView(nativeAdView.findViewById(R.id.ad_headline));
        nativeAdView.setBodyView(nativeAdView.findViewById(R.id.ad_body));
        nativeAdView.setCallToActionView(nativeAdView.findViewById(R.id.ad_call_to_action));
        nativeAdView.setIconView(nativeAdView.findViewById(R.id.ad_icon));
// nativeAdView.setPriceView(nativeAdView.findViewById(R.id.ad_price));
        nativeAdView.setStarRatingView(nativeAdView.findViewById(R.id.ad_stars));
// nativeAdView.setStoreView(nativeAdView.findViewById(R.id.ad_store));
        nativeAdView.setAdvertiserView(nativeAdView.findViewById(R.id.ad_advertiser));
        loadNativeAds();
    }

    private void populateNativeAdView(UnifiedNativeAd nativeAd, UnifiedNativeAdView adView) {
        VideoController vc = nativeAd.getVideoController();
        vc.setVideoLifecycleCallbacks(new VideoController.VideoLifecycleCallbacks() {
            public void onVideoEnd() {
                super.onVideoEnd();
            }
        });

// Some assets are guaranteed to be in every UnifiedNativeAd.
        ((TextView) adView.getHeadlineView()).setText(nativeAd.getHeadline());
        ((TextView) adView.getBodyView()).setText(nativeAd.getBody());
        ((Button) adView.getCallToActionView()).setText(nativeAd.getCallToAction());

// These assets aren't guaranteed to be in every UnifiedNativeAd, so it's important to
// check before trying to display them.
        com.google.android.gms.ads.formats.NativeAd.Image icon = nativeAd.getIcon();

        if (icon == null) {
            adView.getIconView().setVisibility(View.INVISIBLE);
        } else {
            ((ImageView) adView.getIconView()).setImageDrawable(icon.getDrawable());
            adView.getIconView().setVisibility(View.VISIBLE);
        }

// if (nativeAd.getPrice() == null) {
// adView.getPriceView().setVisibility(View.INVISIBLE);
// } else {
// adView.getPriceView().setVisibility(View.VISIBLE);
// ((TextView) adView.getPriceView()).setText(nativeAd.getPrice());
// }

// if (nativeAd.getStore() == null) {
// adView.getStoreView().setVisibility(View.INVISIBLE);
// } else {
// adView.getStoreView().setVisibility(View.VISIBLE);
// ((TextView) adView.getStoreView()).setText(nativeAd.getStore());
// }

        if (nativeAd.getStarRating() == null) {
            adView.getStarRatingView().setVisibility(View.INVISIBLE);
        } else {
            ((RatingBar) adView.getStarRatingView())
                    .setRating(nativeAd.getStarRating().floatValue());
            adView.getStarRatingView().setVisibility(View.VISIBLE);
        }

        if (nativeAd.getAdvertiser() == null) {
            adView.getAdvertiserView().setVisibility(View.INVISIBLE);
        } else {
            ((TextView) adView.getAdvertiserView()).setText(nativeAd.getAdvertiser());
            adView.getAdvertiserView().setVisibility(View.VISIBLE);
        }

// Assign native ad object to the native view.
        adView.setNativeAd(nativeAd);
    }

    private FrameLayout flNativeAds;

    private void loadNativeAds() {
        VideoOptions videoOptions = new VideoOptions.Builder()
                .setStartMuted(false)
                .build();

        NativeAdOptions adOptions = new NativeAdOptions.Builder()
                .setVideoOptions(videoOptions)
                .build();

        AdLoader.Builder builder = new AdLoader.Builder(this, getString(R.string.admob_native_id));
        adLoader = builder.forUnifiedNativeAd(
                new UnifiedNativeAd.OnUnifiedNativeAdLoadedListener() {
                    @Override
                    public void onUnifiedNativeAdLoaded(UnifiedNativeAd unifiedNativeAd) {
                        // A native ad loaded successfully, check if the ad loader has finished loading
                        // and if so, insert the ads into the list.
//						mNativeAds.add(unifiedNativeAd);
                        if (!adLoader.isLoading()) {
                           // logo.setVisibility(View.INVISIBLE);
                            flNativeAds.setVisibility(View.VISIBLE);
                            unifiedNativeAd1=unifiedNativeAd;
                            populateNativeAdView(unifiedNativeAd, nativeAdView);
                        }
                    }
                }).withAdListener(
                new AdListener() {
                    @Override
                    public void onAdFailedToLoad(int errorCode) {
                        // A native ad failed to load, check if the ad loader has finished loading
                        // and if so, insert the ads into the list.
                        Log.e("MainActivity", "The previous native ad failed to load. Attempting to"
                                + " load another.");
                        if (!adLoader.isLoading()) {
                        }
                    }
                }).withNativeAdOptions(adOptions).build();

        // Load the Native ads.
        adLoader.loadAd(ConsentSDK.getAdRequest(this));
    }
}