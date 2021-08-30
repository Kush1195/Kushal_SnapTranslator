package language.translate.snap.translate.activities;

import androidx.appcompat.app.AppCompatActivity;

import android.accessibilityservice.AccessibilityServiceInfo;
import android.annotation.SuppressLint;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.media.MediaPlayer;
import android.net.Uri;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.view.View;
import android.view.accessibility.AccessibilityManager;
import android.widget.ImageView;
import android.widget.VideoView;

import language.translate.snap.translate.R;
import language.translate.snap.translate.service.TextTranslatorService;
import language.translate.snap.translate.utils.HelperResizer;

public class IntroductionAppActivity extends AppCompatActivity {

    Context mContext;
    ImageView btn_start,txt_description;
    private VideoView videoView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_introduction);

        mContext = this;
        init();
        resize();
    }

    private void resize() {

        HelperResizer.getheightandwidth(mContext);
        HelperResizer.setSize(txt_description,564,40);
        HelperResizer.setSize(btn_start,563,138);

    }

    private void init() {

        btn_start = findViewById(R.id.btn_start);
        txt_description = findViewById(R.id.txt_description);
        videoView = findViewById(R.id.videoView);

        final SharedPreferences defaultSharedPreferences = PreferenceManager.getDefaultSharedPreferences(mContext);

        videoView.setVideoURI(Uri.parse("android.resource://" + getPackageName() + "/" + R.raw.v3));
        videoView.setOnPreparedListener(new MediaPlayer.OnPreparedListener() {
            @Override
            public void onPrepared(MediaPlayer mp) {
                mp.setVolume(0, 0);
                videoView.start();
            }
        });
        videoView.setOnCompletionListener(new MediaPlayer.OnCompletionListener() {
            @Override
            public void onCompletion(MediaPlayer mp) {
                mp.setVolume(0, 0);
                videoView.setVideoURI(Uri.parse("android.resource://" + getPackageName() + "/" + R.raw.v3));
            }
        });

        btn_start.setOnClickListener(new View.OnClickListener() {
            public void onClick(View view) {

                if (getIntent().getBooleanExtra("fromMain", false)) {
                    onBackPressed();
                    return;
                }

                if (isAccessibilityEnabled(mContext)) {

                    SharedPreferences.Editor edit = defaultSharedPreferences.edit();
                    edit.putBoolean("added", true);
                    edit.apply();

                    startService(new Intent(mContext, TextTranslatorService.class));

                }

                startActivity(new Intent(mContext, MainActivity.class));

            }
        });

        if (getIntent().getBooleanExtra("fromMain", false)) {
            btn_start.setImageResource(R.drawable.start_state_pressed);
        }

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
    public void onBackPressed() {

        HelperResizer.checkAd=0;
        if (MainActivity.for_intro == 1) {
            super.onBackPressed();
        }
        else {
            Intent startMain = new Intent(Intent.ACTION_MAIN);
            startMain.addCategory(Intent.CATEGORY_HOME);
            startMain.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            startActivity(startMain);
        }
    }
}