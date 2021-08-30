package language.translate.snap.translate.activities;

import android.app.Activity;
import android.content.ActivityNotFoundException;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.util.DisplayMetrics;
import android.view.Display;
import android.view.View;
import android.widget.FrameLayout;
import android.widget.ImageView;

import com.ayoubfletcher.consentsdk.ConsentSDK;
import com.google.android.gms.ads.AdSize;
import com.google.android.gms.ads.AdView;

import language.translate.snap.translate.R;
import language.translate.snap.translate.utils.HelperResizer;

public class MoreSettingsActivity extends Activity {

    Context mContext;
    ImageView back, lay_share, lay_rate, lay_pp, lay_copy, ic_on;
    public SharedPreferences mSharedPreferences;
    private FrameLayout adContainerView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_more_settings);
        mContext = this;
        init();
        resize();

        loadBanner();
    }

    private void init() {

        mSharedPreferences = PreferenceManager.getDefaultSharedPreferences(mContext);
        lay_share = findViewById(R.id.lay_share);
        lay_rate = findViewById(R.id.lay_rate);
        lay_pp = findViewById(R.id.lay_pp);
        back = findViewById(R.id.back);
        lay_copy = findViewById(R.id.lay_copy);
        ic_on = findViewById(R.id.ic_on);

        lay_share.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                String shareBody = "https://play.google.com/store/apps/details?id=" + getPackageName();
                Intent sharingIntent = new Intent(Intent.ACTION_SEND);
                sharingIntent.setType("text/plain");
                sharingIntent.putExtra(Intent.EXTRA_TEXT, shareBody);
                startActivity(Intent.createChooser(sharingIntent, "Share via"));

            }
        });

        lay_rate.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                try {
                    startActivity(new Intent("android.intent.action.VIEW", Uri.parse("market://details?id=" + getPackageName())));
                } catch (ActivityNotFoundException e) {
                    startActivity(new Intent("android.intent.action.VIEW", Uri.parse("https://play.google.com/store/apps/details?id=" + getPackageName())));
                }

            }
        });

        lay_pp.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(mContext, PrivacyPolicyActivity.class));
            }
        });

        ic_on.setImageResource(mSharedPreferences.getBoolean("copy", true) ? R.drawable.on_button : R.drawable.off_button);
        ic_on.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                SharedPreferences.Editor editor = mSharedPreferences.edit();
                editor.putBoolean("copy", !mSharedPreferences.getBoolean("copy", true));
                editor.commit();
                ic_on.setImageResource(mSharedPreferences.getBoolean("copy", true) ? R.drawable.on_button : R.drawable.off_button);
            }
        });

        back.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                onBackPressed();
            }
        });
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        HelperResizer.checkAd=0;
    }

    private void resize() {

        HelperResizer.getheightandwidth(mContext);
        HelperResizer.setSize(lay_share,1000,150);
        HelperResizer.setSize(lay_rate,1000,150);
        HelperResizer.setSize(lay_pp,1000,150);
        HelperResizer.setSize(lay_copy,1000,150);
        HelperResizer.setSize(back,58,50);
        HelperResizer.setSize(ic_on,100,56);

        HelperResizer.setMargin(lay_share,0,25,0,0);
        HelperResizer.setMargin(lay_rate,0,25,0,0);
        HelperResizer.setMargin(lay_pp,0,25,0,0);

    }

    AdView adView;

    private void loadBanner() {
        adContainerView = findViewById(R.id.ad_view_container);
// Step 1 - Create an AdView and set the ad unit ID on it.
        adView = new AdView(this);
        adView.setAdUnitId(getString(R.string.admob_banner_id));
        adContainerView.addView(adView);

        AdSize adSize = getAdSize();
        adView.setAdSize(adSize);
        adView.loadAd(ConsentSDK.getAdRequest(this));
    }

    private AdSize getAdSize() {
        Display display = getWindowManager().getDefaultDisplay();
        DisplayMetrics outMetrics = new DisplayMetrics();
        display.getMetrics(outMetrics);
        float widthPixels = outMetrics.widthPixels;
        float density = outMetrics.density;
        int adWidth = (int) (widthPixels / density);
        return AdSize.getCurrentOrientationAnchoredAdaptiveBannerAdSize(this, adWidth);
    }
}