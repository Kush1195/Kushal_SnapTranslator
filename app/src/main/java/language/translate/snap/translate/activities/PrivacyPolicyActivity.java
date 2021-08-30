package language.translate.snap.translate.activities;

import android.content.Context;
import android.os.Bundle;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.ProgressBar;

import androidx.appcompat.app.AppCompatActivity;

import language.translate.snap.translate.R;

public class PrivacyPolicyActivity extends AppCompatActivity {
    WebView wv;
    Context context;
    ProgressBar pbar;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        requestWindowFeature(Window.FEATURE_NO_TITLE);
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,
                WindowManager.LayoutParams.FLAG_FULLSCREEN);
        setContentView(R.layout.activity_privacy_policy);
        getWindow().setFlags(1024,1024);

        context = this;

        pbar = findViewById(R.id.progress_bar);
        wv = findViewById(R.id.wv_privacy_policy);

        WebSettings settings = wv.getSettings();
        settings.setLoadWithOverviewMode(true);
        settings.setSupportZoom(true);
        settings.setBuiltInZoomControls(false);
        settings.setLayoutAlgorithm(WebSettings.LayoutAlgorithm.SINGLE_COLUMN);
        settings.setCacheMode(WebSettings.LOAD_NO_CACHE);
        settings.setDomStorageEnabled(true);
        wv.setScrollBarStyle(WebView.SCROLLBARS_OUTSIDE_OVERLAY);
        wv.setScrollbarFadingEnabled(true);
        wv.setLayerType(View.LAYER_TYPE_HARDWARE, null);

        init();
    }

    public void init()
    {

        wv.loadUrl("file:///android_asset/privacy_policy.html");
        wv.requestFocus();
        pbar.setVisibility(View.VISIBLE);
        wv.setWebViewClient(new WebViewClient()
        {

            public void onPageFinished(WebView view, String url)
            {
                try
                {
                    pbar.setVisibility(View.GONE);
                }
                catch (Exception e)
                {
                    e.printStackTrace();
                }
            }
        });
    }

    @Override
    public void onBackPressed()
    {
        super.onBackPressed();

        finish();

    }
}
