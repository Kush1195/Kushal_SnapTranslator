package language.translate.snap.translate.activities;

import androidx.appcompat.app.AppCompatActivity;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.Display;

import com.ayoubfletcher.consentsdk.ConsentSDK;
import com.google.android.gms.ads.AdListener;
import com.google.android.gms.ads.InterstitialAd;
import com.google.android.gms.ads.LoadAdError;

import language.translate.snap.translate.R;
import language.translate.snap.translate.utils.HelperResizer;

import org.json.JSONArray;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.lang.reflect.Method;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLEncoder;
import java.text.SimpleDateFormat;
import java.util.Date;

import static language.translate.snap.translate.utils.Utils.status_bar_height;

public class SplashActivity extends AppCompatActivity {

    Context mContext;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_splash);
        mContext = this;
        initConsentSDK(this);
        if (!isConsentDone() && isNetworkAvailable() && ConsentSDK.isUserLocationWithinEea(this)) {
//        if(!isConsentDone()&& isNetworkAvailable()){
            consentSDK.checkConsent(new ConsentSDK.ConsentCallback() {

                @Override
                public void onResult(boolean isRequestLocationInEeaOrUnknown) {
                    setPref();
                    ConsentSDK.Builder.dialog.dismiss();
                    init();
                }
            });


        } else {
            init();
        }

        resize();
        HelperResizer.checkAd=1;
    }

    private void resize() {

        HelperResizer.getheightandwidth(mContext);
        HelperResizer.setSize(findViewById(R.id.ic_logo),200,200,true);

    }

    @SuppressLint("WrongConstant")
    private void init() {

        status_bar_height = getStatusBarHeight();
        Log.e("XXX", "status bar height: "+ status_bar_height);

        if (isNetworkAvailable()) {
            new CheckApis().execute();
        }else {

        new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {
                startActivity(new Intent(mContext, FirstActivity.class));
            }
        },3000);
        }

    }

    public int getStatusBarHeight() {
        int statusBarHeight = 0;

        if (!hasOnScreenSystemBar()) {
            int resourceId = getResources().getIdentifier("status_bar_height", "dimen", "android");
            if (resourceId > 0) {
                statusBarHeight = getResources().getDimensionPixelSize(resourceId);
            }
        }

        return statusBarHeight;
    }

    private boolean hasOnScreenSystemBar() {
        Display display = getWindowManager().getDefaultDisplay();
        int rawDisplayHeight = 0;
        try {
            Method getRawHeight = Display.class.getMethod("getRawHeight");
            rawDisplayHeight = (Integer) getRawHeight.invoke(display);
        } catch (Exception ex) {
        }

        int UIRequestedHeight = display.getHeight();

        return rawDisplayHeight - UIRequestedHeight > 0;
    }

    private boolean isNetworkAvailable() {
        ConnectivityManager connectivityManager
                = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo activeNetworkInfo = connectivityManager.getActiveNetworkInfo();
        return activeNetworkInfo != null && activeNetworkInfo.isConnected();
    }

    public class CheckApis extends AsyncTask {

        private String recognizedText, text, source, target;

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            Log.e("CCC", "Start Translate");
        }

        @Override
        protected void onProgressUpdate(Object[] values) {
            super.onProgressUpdate(values);
        }

        @Override
        protected Object doInBackground(Object[] objects) {

            text = "hi";
            target = "hi";
            source = "en";
            try {

                // change here for Google API error
                StringBuilder sb2 = new StringBuilder();
                sb2.append("https://translate.googleapis.com/translate_a/single?client=gtx&sl=");
                sb2.append(source);
                sb2.append("&tl=");
                sb2.append(target);
                sb2.append("&dt=t&ie=UTF-8&ae=UTF-8&q=");
                sb2.append(URLEncoder.encode(text, "UTF-8"));
                String sb3 = sb2.toString();

                URL url = new URL(sb3);
                StringBuilder response = new StringBuilder();
                HttpURLConnection con = (HttpURLConnection) url.openConnection();
                String COOKIE_BASE =
                        "_ga=GA1.3.233903037.1531810972; "
                                + "NID=138=H3VSTKzsssKDJ8sheq_6-mNhWtYNksI2A3aMckdiKxJktvsviyNOrvxseIjK0eJfdQ9HL68evaquXDEsIfVG7LTs77-MslbKk2DzGrm3UKDBQNrgYHjwuw-5p5G3da98; "
                                + "1P_JAR=";
                SimpleDateFormat SIMPLE_DATE_FORMAT = new SimpleDateFormat("yyyy-MM-dd");
                String cookie = COOKIE_BASE + SIMPLE_DATE_FORMAT.format(new Date()) + "-" + 1;
                con.setRequestProperty("cookie", cookie);
//                con.setRequestProperty("User-Agent", "Mozilla/5.0");
                con.setRequestProperty("User-Agent", "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/68.0.3440.106 Safari/537.36");

                BufferedReader in = new BufferedReader(new InputStreamReader(con.getInputStream()));
                String inputLine;
                while ((inputLine = in.readLine()) != null) {
                    response.append(inputLine);
                }
                in.close();
                recognizedText = response.toString();

                String final_string = "";

                try{
                    JSONArray jsonArray = new JSONArray(recognizedText);

                    for (int i = 0; i < jsonArray.length(); i++) {

                        JSONArray jsonArray1 = (JSONArray) jsonArray.getJSONArray(0).get(i);

                        String s = jsonArray1.get(0).toString();
                        Log.e("ccc", "onPostExecute: "+ s);

                        final_string = final_string + " " + s;

                    }

                } catch (Exception e) {
                    e.printStackTrace();
                }

                recognizedText = final_string;

                if (recognizedText.startsWith("<!DOCTYPE")) {
                    recognizedText = "Error Translate";
                    return "Fail";
                }
                return "Success";
            } catch (Exception e) {
                return "Fail";
            }

        }

        @Override
        protected void onPostExecute(Object o) {
            super.onPostExecute(o);
            Log.e("AAA", "Response :" + recognizedText);
            if (o.equals("Success")) {
                Log.e("AAA", "Google API To Use");
                SharedPreferences.Editor editor = getSharedPreferences("apicall", MODE_PRIVATE).edit();
                editor.putBoolean("isGoogleApi", true);
                editor.apply();
            } else if (o.equals("Fail")) {
                Log.e("AAA", "NLP API To Use");
                SharedPreferences.Editor editor = getSharedPreferences("apicall", MODE_PRIVATE).edit();
                editor.putBoolean("isGoogleApi", false);
                editor.apply();
            }

            loadInterstitial();
        }

    }
    InterstitialAd interstitialAd;

    // Load Interstitial
    private void loadInterstitial() {
        interstitialAd = new InterstitialAd(this);
        interstitialAd.setAdUnitId(getString(R.string.admob_fullscreen_id));
        // You have to pass the AdRequest from ConsentSDK.getAdRequest(this) because it handle the right way to load the ad
        interstitialAd.loadAd(ConsentSDK.getAdRequest(this));
        interstitialAd.setAdListener(new AdListener() {

            @Override
            public void onAdClosed() {
                Intent i = new Intent(SplashActivity.this, FirstActivity.class);
                startActivity(i);
                finish();
            }

            @Override
            public void onAdFailedToLoad(LoadAdError loadAdError) {
                Intent i = new Intent(SplashActivity.this, FirstActivity.class);
                startActivity(i);
                finish();
            }

            @Override
            public void onAdLoaded() {
                interstitialAd.show();
            }
        });
        interstitialAd.loadAd(ConsentSDK.getAdRequest(this));
    }
    private ConsentSDK consentSDK;

    private void initConsentSDK(Context context) {
        // Initialize ConsentSDK
        consentSDK = new ConsentSDK.Builder(this)
//                .addTestDeviceId("77259D4779E9E87A669924752B4E3B2B")
                .addCustomLogTag("CUSTOM_TAG") // Add custom tag default: ID_LOG
                .addPrivacyPolicy(getString(R.string.privacy_link)) // Add your privacy policy url
                .addPublisherId(getString(R.string.admob_publisher_id)) // Add your admob publisher id
                .build();
    }



    void setPref() {
        SharedPreferences.Editor editor = getSharedPreferences("consentpreff", MODE_PRIVATE).edit();
        editor.putBoolean("isDone", true);
        editor.apply();
    }

    boolean isConsentDone() {
        SharedPreferences prefs = getSharedPreferences("consentpreff", MODE_PRIVATE);
        return prefs.getBoolean("isDone", false);
    }



}