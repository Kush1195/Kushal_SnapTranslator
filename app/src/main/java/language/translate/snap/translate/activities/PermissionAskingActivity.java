package language.translate.snap.translate.activities;

import androidx.appcompat.app.AppCompatActivity;

import android.accessibilityservice.AccessibilityServiceInfo;
import android.annotation.SuppressLint;
import android.content.Context;
import android.content.Intent;
import android.media.MediaPlayer;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.provider.Settings;
import android.view.View;
import android.view.WindowManager;
import android.view.accessibility.AccessibilityManager;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.VideoView;

import language.translate.snap.translate.R;
import language.translate.snap.translate.utils.HelperResizer;

public class PermissionAskingActivity extends AppCompatActivity {

    Context mContext;
    public TextView btn_next_acc, btn_next_draw;
    public boolean checkAccessibility;
    int videoId;
    public VideoView videoView;
    public ImageView view1, view2, view3, btn_accessibility, txt_permission, btn_drawOverApp;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_second);

        mContext = this;
        init();
        resize();
    }

    @SuppressLint("WrongConstant")
    private void init() {

        videoView = findViewById(R.id.videoView);
        view1 = findViewById(R.id.view1);
        view2 = findViewById(R.id.view2);
        view3 = findViewById(R.id.view3);
        txt_permission = findViewById(R.id.txt_permission);
        btn_accessibility = findViewById(R.id.btn_accessbility);
        btn_next_acc = findViewById(R.id.btn_next_acc);
        btn_drawOverApp = findViewById(R.id.btn_drawOverApp);
        btn_next_draw = findViewById(R.id.btn_next_draw);

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {

            if (isAccessibilityEnabled(mContext)) {

                if (Settings.canDrawOverlays(getApplicationContext())) {

                    btn_next_acc.setVisibility(View.GONE);
                    btn_drawOverApp.setVisibility(View.VISIBLE);
                    startActivity(new Intent(mContext, IntroductionAppActivity.class));

                    finish();
//                    txt_permission.setText("Touch Over Permission Enabled");
                    view1.setImageResource(R.drawable.next_previous_dot);
                    view2.setImageResource(R.drawable.next_previous_dot);
                    view3.setImageResource(R.drawable.current_dot);

                } else {

                    btn_next_acc.setVisibility(View.GONE);
                    btn_drawOverApp.setVisibility(View.VISIBLE);
                    btn_next_draw.setVisibility(View.GONE);
                    txt_permission.setImageResource(R.drawable.floattext);
                    view1.setImageResource(R.drawable.next_previous_dot);
                    view2.setImageResource(R.drawable.next_previous_dot);
                    view3.setImageResource(R.drawable.current_dot);

                }
            }

        }

//        videoId = R.raw.v1;

        btn_accessibility.setOnClickListener(new View.OnClickListener() {
            public void onClick(View view) {

                if (!isAccessibilityEnabled(mContext)) {
                    startActivity(new Intent("android.settings.ACCESSIBILITY_SETTINGS"));
                }
            }
        });

        btn_next_acc.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (Build.VERSION.SDK_INT >= 23) {
                    if (!Settings.canDrawOverlays(getApplicationContext())) {
                        btn_accessibility.setVisibility(View.GONE);
                        btn_next_acc.setVisibility(View.GONE);
                        btn_drawOverApp.setVisibility(View.VISIBLE);
                        txt_permission.setImageResource(R.drawable.floattext);
                    } else {
                        startActivity(new Intent(mContext, IntroductionAppActivity.class));
                        finish();
                    }
                } else {
                    startActivity(new Intent(mContext, IntroductionAppActivity.class));
                    finish();
                }
            }
        });

        btn_drawOverApp.setOnClickListener(new View.OnClickListener() {
            public void onClick(View view) {
                Intent intent1 = new Intent(Settings.ACTION_MANAGE_OVERLAY_PERMISSION, Uri.parse("package:" + getPackageName()));
                startActivityForResult(intent1, 101);
            }
        });

        btn_next_draw.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                btn_accessibility.setVisibility(View.GONE);
                btn_next_acc.setVisibility(View.GONE);
                btn_drawOverApp.setVisibility(View.GONE);
                startActivity(new Intent(mContext, IntroductionAppActivity.class));
                finish();
            }
        });

        prepareVideo();

        if (Build.VERSION.SDK_INT == 26) {
            try {
                if (Settings.canDrawOverlays(mContext)) {
                    View view = new View(getApplicationContext());
                    WindowManager windowManager = (WindowManager) getSystemService(WINDOW_SERVICE);
                    windowManager.addView(view, new WindowManager.LayoutParams());
                    windowManager.removeView(view);
                    btn_next_acc.setVisibility(View.GONE);
                    btn_drawOverApp.setVisibility(View.GONE);
                    btn_next_draw.setVisibility(View.VISIBLE);
//                    txt_permission.setText(R.string.access_enable);
                    view1.setImageResource(R.drawable.next_previous_dot);
                    view2.setImageResource(R.drawable.next_previous_dot);
                    view3.setImageResource(R.drawable.current_dot);
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }

    }

    private void resize() {

        HelperResizer.getheightandwidth(mContext);

        HelperResizer.setSize(view1, 30, 30, true);
        HelperResizer.setSize(view2, 30, 30, true);
        HelperResizer.setSize(view3, 30, 30, true);
        HelperResizer.setSize(btn_accessibility, 563, 138);
        HelperResizer.setSize(btn_next_draw, 900, 150);
        HelperResizer.setSize(btn_drawOverApp, 679, 138);
        HelperResizer.setSize(btn_next_acc, 900, 150);

    }

    private void prepareVideo() {

        if (!isAccessibilityEnabled(getApplicationContext())) {
            videoId = R.raw.v1;
        } else {
            videoId = R.raw.v2;
        }

        StringBuilder sb = new StringBuilder();
        sb.append("android.resource://");
        sb.append(getPackageName());
        sb.append("/");
        sb.append(videoId);
        videoView.setVideoURI(Uri.parse(sb.toString()));

        videoView.setOnPreparedListener(new MediaPlayer.OnPreparedListener() {
            public void onPrepared(MediaPlayer mediaPlayer) {

                mediaPlayer.setVolume(0.0f, 0.0f);
                videoView.start();

            }
        });

        videoView.setOnCompletionListener(new MediaPlayer.OnCompletionListener() {
            public void onCompletion(MediaPlayer mediaPlayer) {

                mediaPlayer.setVolume(0.0f, 0.0f);

                StringBuilder sb = new StringBuilder();
                sb.append("android.resource://");
                sb.append(getPackageName());
                sb.append("/");
                sb.append(videoId);

                videoView.setVideoURI(Uri.parse(sb.toString()));

            }
        });
    }

    @Override
    protected void onResume() {

        super.onResume();
        HelperResizer.getheightandwidth(mContext);

        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.M) {
            view3.setVisibility(View.GONE);
        }

        if (!isAccessibilityEnabled(getApplicationContext())) {
            videoId = R.raw.v1;

            view1.setImageResource(R.drawable.current_dot);
            view2.setImageResource(R.drawable.next_previous_dot);
            view3.setImageResource(R.drawable.next_previous_dot);
            HelperResizer.setSize(txt_permission,909,109);
            txt_permission.setImageResource(R.drawable.accstext);
        }

        if (isAccessibilityEnabled(getApplicationContext())) {

            videoId = R.raw.v2;
            prepareVideo();
            checkAccessibility = true;
            btn_accessibility.setVisibility(View.GONE);

            try {

                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {

                    if (!Settings.canDrawOverlays(getApplicationContext())) {
                        btn_drawOverApp.setVisibility(View.VISIBLE);
                        HelperResizer.setSize(txt_permission,740,108);
                        txt_permission.setImageResource(R.drawable.floattext);
                    } else {
                        startActivity(new Intent(mContext, IntroductionAppActivity.class));
                        finish();
                    }
                } else {
                    startActivity(new Intent(mContext, IntroductionAppActivity.class));
                    finish();
                }
            } catch (Exception e) {
                e.printStackTrace();
            }

            view1.setImageResource(R.drawable.next_previous_dot);
            view2.setImageResource(R.drawable.current_dot);
            view3.setImageResource(R.drawable.next_previous_dot);

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
    public void onActivityResult(int requestCode, int resultCode, Intent intent) {
        super.onActivityResult(requestCode, resultCode, intent);
        if (requestCode == 101 && Build.VERSION.SDK_INT == 26 && !Settings.canDrawOverlays(mContext)) {
            finish();
            startActivity(new Intent(mContext, PermissionAskingActivity.class));
        }
    }

    @Override
    public void onBackPressed() {
        finishAffinity();
    }

}