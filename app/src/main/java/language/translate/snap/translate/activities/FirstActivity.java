package language.translate.snap.translate.activities;

import androidx.appcompat.app.AppCompatActivity;

import android.accessibilityservice.AccessibilityServiceInfo;
import android.annotation.SuppressLint;
import android.app.AppOpsManager;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Build;
import android.os.Bundle;
import android.os.Process;
import android.preference.PreferenceManager;
import android.view.View;
import android.view.accessibility.AccessibilityManager;
import android.widget.ImageView;

import language.translate.snap.translate.R;
import language.translate.snap.translate.utils.HelperResizer;

public class FirstActivity extends AppCompatActivity {

    Context mContext;
    public boolean overlayEnable = false;
    public AppOpsManager mAppOpsManager;
    public SharedPreferences sharedPreferences;
    ImageView ic_icon, ic_text, ic_start, ic_btm_txt, btn_start, btn_policy;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_first);
        mContext = this;
        init();
        resize();
    }

    private void resize() {

        HelperResizer.getheightandwidth(mContext);
        HelperResizer.setSize(ic_icon,186,186,true);
        HelperResizer.setSize(ic_text,731,174);
        HelperResizer.setSize(ic_start,908,580);
        HelperResizer.setSize(ic_btm_txt,702,99);
        HelperResizer.setSize(btn_start,563,138);
        HelperResizer.setSize(btn_policy,370,73);

    }

    @SuppressLint("WrongConstant")
    private void init() {

        btn_start = findViewById(R.id.btn_start);
        ic_icon = findViewById(R.id.ic_icon);
        ic_text = findViewById(R.id.ic_text);
        ic_start = findViewById(R.id.ic_start);
        ic_btm_txt = findViewById(R.id.ic_btm_txt);
        btn_policy = findViewById(R.id.btn_policy);

        sharedPreferences = PreferenceManager.getDefaultSharedPreferences(mContext);

        boolean to_check = true;
        if (Build.VERSION.SDK_INT >= 23) {
            mAppOpsManager = (AppOpsManager) getSystemService("appops");
            if (mAppOpsManager.checkOpNoThrow("android:system_alert_window", Process.myUid(), getPackageName()) != 0) {
                to_check = false;
            }
            overlayEnable = to_check;
        } else {
            overlayEnable = true;
        }

        if (isAccessibilityEnabled(mContext)) {
            if (Build.VERSION.SDK_INT < 23) {
                startActivity(new Intent(mContext, MainActivity.class));
            } else if (overlayEnable) {
                startActivity(new Intent(mContext, MainActivity.class));
            }
        }

        btn_start.setOnClickListener(new View.OnClickListener() {
            public void onClick(View view) {

                if (!isAccessibilityEnabled(mContext)) {

                    if (Build.VERSION.SDK_INT < 23) {
                        Intent intent = new Intent(mContext, PermissionAskingActivity.class);
                        startActivity(intent);
                        finish();

                    } else if (!overlayEnable) {

                        Intent intent2 = new Intent(mContext, PermissionAskingActivity.class);
                        startActivity(intent2);
                        finish();

                    } else {

                        Intent intent3 = new Intent(mContext, PermissionAskingActivity.class);
                        intent3.setFlags(67108864);
                        startActivity(intent3);
                        finish();

                    }

                } else if (Build.VERSION.SDK_INT < 23) {

                    startActivity(new Intent(mContext, MainActivity.class));

                } else if (!overlayEnable) {

                    Intent intent4 = new Intent(mContext, PermissionAskingActivity.class);
                    startActivity(intent4);
                    finish();

                } else {

                    startActivity(new Intent(mContext, MainActivity.class));

                }
            }
        });

        btn_policy.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(mContext, PrivacyPolicyActivity.class));
            }
        });

    }

     @SuppressLint("WrongConstant")
    @Override
    public void onResume() {
        super.onResume();
        boolean z = true;
        if (Build.VERSION.SDK_INT >= 23) {
            mAppOpsManager = (AppOpsManager) getSystemService("appops");
            if (mAppOpsManager.checkOpNoThrow("android:system_alert_window", Process.myUid(), getPackageName()) != 0) {
                z = false;
            }
            overlayEnable = z;
            return;
        }
        overlayEnable = true;
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
        finishAffinity();
    }
}