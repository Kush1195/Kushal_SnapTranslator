package language.translate.snap.translate.service;

import android.annotation.SuppressLint;
import android.app.Service;
import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.Intent;
import android.graphics.PixelFormat;
import android.os.Build;
import android.os.IBinder;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.WindowManager;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import language.translate.snap.translate.R;

public class MoreTextFloatingService extends Service {

    private WindowManager mWindowManager;
    private View mFloatingWidget;
    String ext_text;

    public MoreTextFloatingService() {
    }

    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        ext_text = intent.getStringExtra("text");

        mFloatingWidget = LayoutInflater.from(this).inflate(R.layout.custom_dialog_layout, null);

        int flag;
        if (Build.VERSION.SDK_INT >= 26) {
            flag = WindowManager.LayoutParams.TYPE_APPLICATION_OVERLAY;
        } else {
            flag = WindowManager.LayoutParams.TYPE_SYSTEM_ALERT;
        }

        final WindowManager.LayoutParams params = new WindowManager.LayoutParams(
                flag,
                WindowManager.LayoutParams.FLAG_NOT_TOUCH_MODAL |
                        WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE |
                        WindowManager.LayoutParams.FLAG_LAYOUT_IN_SCREEN, PixelFormat.TRANSLUCENT);

        params.width = WindowManager.LayoutParams.MATCH_PARENT;
        params.height = WindowManager.LayoutParams.WRAP_CONTENT;

        params.gravity = Gravity.BOTTOM | Gravity.CENTER;
        params.x = 0;
        params.y = 0;

        mWindowManager = (WindowManager) getSystemService(WINDOW_SERVICE);
        mWindowManager.addView(mFloatingWidget, params);

        int w = getResources().getDisplayMetrics().widthPixels;
        int h = getResources().getDisplayMetrics().heightPixels;

        TextView text = mFloatingWidget.findViewById(R.id.text);
        ImageView close = mFloatingWidget.findViewById(R.id.close);
        ImageView copy = mFloatingWidget.findViewById(R.id.copy);
        ImageView whts = mFloatingWidget.findViewById(R.id.whts);
        ImageView share = mFloatingWidget.findViewById(R.id.share);
        LinearLayout result_bg = mFloatingWidget.findViewById(R.id.result_bg);
        LinearLayout bottom_bg = mFloatingWidget.findViewById(R.id.bottom_bg);
        LinearLayout header_trans = mFloatingWidget.findViewById(R.id.header_trans);

        LinearLayout.LayoutParams result_bg_p = new LinearLayout.LayoutParams((w * 1004) / 1080,(h * 487) / 1920);
        result_bg.setLayoutParams(result_bg_p);

        LinearLayout.LayoutParams header_trans_p = new LinearLayout.LayoutParams((w * 1000) / 1080,(h * 92) / 1920);
        header_trans.setLayoutParams(header_trans_p);

        LinearLayout.LayoutParams bottom_bg_p = new LinearLayout.LayoutParams((w * 1004) / 1080,(h * 107) / 1920);
        bottom_bg_p.gravity = Gravity.BOTTOM;
        bottom_bg.setLayoutParams(bottom_bg_p);

        LinearLayout.LayoutParams close_p = new LinearLayout.LayoutParams((w * 92) / 1080,(w * 92) / 1080);
        close.setLayoutParams(close_p);

        LinearLayout.LayoutParams btn_p = new LinearLayout.LayoutParams((w * 50) / 1080,(w * 50) / 1080);
        copy.setLayoutParams(btn_p);
        whts.setLayoutParams(btn_p);
        share.setLayoutParams(btn_p);

        text.setText(ext_text);

        copy.setOnClickListener(new View.OnClickListener() {
            @SuppressLint("WrongConstant")
            @Override
            public void onClick(View view) {

                ((ClipboardManager) getSystemService("clipboard")).setPrimaryClip(ClipData.newPlainText("label", ext_text));
                Toast.makeText(getApplicationContext(), "Copied to Clipboard", Toast.LENGTH_LONG).show();

            }
        });

        whts.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                try {
                    Intent intent = new Intent(Intent.ACTION_SEND);
                    intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                    intent.setType("text/*");
                    intent.setPackage("com.whatsapp");
                    intent.putExtra(Intent.EXTRA_TEXT, ext_text);
                    startActivity(intent);
                } catch (Exception e) {
                    Toast.makeText(MoreTextFloatingService.this, "WhatsApp Not Found", Toast.LENGTH_SHORT).show();
                }
            }
        });

        share.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                Intent sendIntent = new Intent();
                sendIntent.setAction(Intent.ACTION_SEND);
                sendIntent.putExtra(Intent.EXTRA_TEXT, ext_text);
                sendIntent.setType("text/plain");

                Intent shareIntent = Intent.createChooser(sendIntent, null);
                shareIntent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                startActivity(shareIntent);
            }
        });

        close.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                stopSelf();
            }
        });

        return super.onStartCommand(intent, flags, startId);
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        if (mFloatingWidget != null) mWindowManager.removeView(mFloatingWidget);
    }
}