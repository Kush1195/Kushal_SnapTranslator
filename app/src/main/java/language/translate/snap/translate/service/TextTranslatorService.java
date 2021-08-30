package language.translate.snap.translate.service;

import android.accessibilityservice.AccessibilityServiceInfo;
import android.animation.ValueAnimator;
import android.annotation.SuppressLint;
import android.app.AlarmManager;
import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.BroadcastReceiver;
import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.graphics.PixelFormat;
import android.graphics.PorterDuff;
import android.graphics.Rect;
import android.net.ConnectivityManager;
import android.net.Network;
import android.net.NetworkCapabilities;
import android.net.NetworkRequest;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Handler;
import android.os.HandlerThread;
import android.os.IBinder;
import android.os.Looper;
import android.os.Message;
import android.os.Process;
import android.os.SystemClock;
import android.preference.PreferenceManager;
import android.provider.Settings;
import android.util.DisplayMetrics;
import android.util.Log;
import android.util.TypedValue;
import android.view.GestureDetector;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.WindowManager;
import android.view.accessibility.AccessibilityEvent;
import android.view.accessibility.AccessibilityManager;
import android.view.accessibility.AccessibilityNodeInfo;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.evgenii.jsevaluator.JsEvaluator;
import language.translate.snap.translate.R;
import language.translate.snap.translate.activities.MainActivity;
import language.translate.snap.translate.utils.AutoFitEditText;
import language.translate.snap.translate.utils.Help;
import language.translate.snap.translate.utils.Utils;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLEncoder;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;

import androidx.annotation.RequiresApi;
import androidx.core.app.NotificationCompat;
import androidx.core.app.TaskStackBuilder;

import org.json.JSONArray;
import org.json.JSONObject;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;

import static language.translate.snap.translate.service.DetectTextService.mNodeInfo;
import static language.translate.snap.translate.utils.Help.gone;
import static language.translate.snap.translate.utils.Utils.status_bar_height;

public class TextTranslatorService extends Service {

    public static View main_view, Setting_View, main_text_View;
    public static ProgressBar loading_bar;
    public static WindowManager.LayoutParams main_view_params, main_text_params, setting_view_params;
    public int pos_xDelta, pos_yDelta;
    public static WindowManager mWindowManager;
    public boolean DisplayToast = false;
    public Rect oldRect;
    public static TextView main_tv;
    public static ImageView lay_to_expand;
    public SharedPreferences mSharedPreferences;
    GestureDetector mGestureDetector;
    int clik_counting = 0;
    boolean event_occupied;
    long startTime;
    static final int MAX_DURATION = 200;
    public int lastAction;
    JsEvaluator jsEvaluator = new JsEvaluator(this);
    ArrayList<Http> httpArrayList = new ArrayList<>();
    //    public static HashMap<String, String> convertedText = new HashMap<>();
    public InternetConnectionReceiver chek_net_receiver;
    ConnectivityManager connectionManager;
    ConnectionStateMonitor connectionStateMonitor;
    public static String translated_text;
    public static boolean stay_on = false;
    public static String IdentifiedText = "";
    Handler mClickHandler = new Handler();
    Handler mTouchHandler = new Handler();
    boolean singleClick;
    boolean touchIsOn;

    InnerRecevier classReceiver;
    public static View myView;
    RelativeLayout addLay;
    public static boolean DisplayAll;
    int w;
    LinearLayout mainLay;
    ImageView myglobalayout, setting, close_pop, off, check_clipboard;
    Help.AppPreferences preferences;

    Notification.Builder mBuilder;
    int NOTIF_ID = 1234;

    public TextTranslatorService() {}

    @SuppressLint("WrongConstant")
    private void startServiceOreoCondition() {

        NotificationManager mNotificationManager = (NotificationManager) getSystemService("notification");
        if (Build.VERSION.SDK_INT >= 26) {
            NotificationChannel notificationChannel = new NotificationChannel("channelid", "channelname", 0);
            notificationChannel.setLightColor(-16776961);
            mNotificationManager.createNotificationChannel(notificationChannel);
            mBuilder = new Notification.Builder(this, "channelid");
        } else {
            mBuilder = new Notification.Builder(this);
        }
        Intent intent5 = new Intent(this, MainActivity.class);
        TaskStackBuilder create = TaskStackBuilder.create(this);
        create.addNextIntentWithParentStack(intent5);
        PendingIntent pendingIntent = create.getPendingIntent(0, 134217728);
        mBuilder.setSmallIcon(R.mipmap.ic_launcher).setAutoCancel(false).setOngoing(true)
                .setContentIntent(pendingIntent).setTicker(getResources().getString(R.string.app_name));
        startForeground(NOTIF_ID, mBuilder.build());

    }

    @Override
    public void onCreate() {
        super.onCreate();

        Help.width = getResources().getDisplayMetrics().widthPixels;
        Help.height = getResources().getDisplayMetrics().heightPixels;
        preferences = new Help.AppPreferences(this);

        w = getResources().getDisplayMetrics().widthPixels;
        mGestureDetector = new GestureDetector(getApplicationContext(), new GestureListener());
        connectionManager = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
        if (Build.VERSION.SDK_INT >= 28) {
            connectionStateMonitor = new ConnectionStateMonitor();
            final NetworkRequest networkRequest;
            networkRequest = new NetworkRequest.Builder().addTransportType(NetworkCapabilities.TRANSPORT_CELLULAR).addTransportType(NetworkCapabilities.TRANSPORT_WIFI).build();
            connectionManager.registerNetworkCallback(networkRequest, connectionStateMonitor);
        } else {
            IntentFilter intentFilter = new IntentFilter();
            intentFilter.addAction(ConnectivityManager.CONNECTIVITY_ACTION);
            registerReceiver(chek_net_receiver, new IntentFilter());
        }

        classReceiver = new InnerRecevier();
        IntentFilter intentFilter = new IntentFilter(Intent.ACTION_CLOSE_SYSTEM_DIALOGS);
        registerReceiver(classReceiver, intentFilter);
    }

    @Override
    public int onStartCommand(final Intent intent, int flags, int startId) {

        startServiceOreoCondition();

        try {
            mSharedPreferences = PreferenceManager.getDefaultSharedPreferences(getApplicationContext());
            main_text_View = LayoutInflater.from(getApplicationContext()).inflate(R.layout.translate_view, null);
            main_tv = main_text_View.findViewById(R.id.textView);
            loading_bar = main_text_View.findViewById(R.id.progress);
            lay_to_expand = main_text_View.findViewById(R.id.expand_lay);

            int color = getResources().getColor(R.color.thm);
            loading_bar.getIndeterminateDrawable().setColorFilter(color, PorterDuff.Mode.SRC_IN);

            mWindowManager = (WindowManager) getSystemService(WINDOW_SERVICE);

            myView = LayoutInflater.from(getApplicationContext()).inflate(R.layout.myview, null);
            int LAYOUT_FLAG;
            if (Build.VERSION.SDK_INT >= 26) {
                LAYOUT_FLAG = 2038;
            } else {
                LAYOUT_FLAG = 2002;
            }

            WindowManager.LayoutParams localLayoutParams = new WindowManager.LayoutParams();
            localLayoutParams.type = LAYOUT_FLAG;
            localLayoutParams.gravity = Gravity.START | Gravity.TOP;
            localLayoutParams.flags = WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE |
                    WindowManager.LayoutParams.FLAG_NOT_TOUCH_MODAL |
                    WindowManager.LayoutParams.FLAG_LAYOUT_IN_SCREEN;
            localLayoutParams.width = WindowManager.LayoutParams.MATCH_PARENT;
            localLayoutParams.height = WindowManager.LayoutParams.MATCH_PARENT;
            localLayoutParams.format = PixelFormat.TRANSPARENT;

//            WindowManager.LayoutParams localLayoutParams = new WindowManager.LayoutParams(-1, -1, LAYOUT_FLAG, 262664, -3);
//            localLayoutParams.gravity = Gravity.START | Gravity.TOP;
            mWindowManager.addView(myView, localLayoutParams);
            myView.setVisibility(View.GONE);
            DisplayAll = false;

            addLay = myView.findViewById(R.id.addLay);

            myView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    DisplayAll = false;
                    myView.setVisibility(View.GONE);
                }
            });

            lay_to_expand.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    try {

                        if (main_text_View.getVisibility() == View.VISIBLE) {
                            main_text_View.setVisibility(View.GONE);
                        }

                        Intent serviceIntent = new Intent(getApplicationContext(), MoreTextFloatingService.class);
                        serviceIntent.putExtra("text", main_tv.getText().toString());
                        startService(serviceIntent);
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }
            });
        } catch (Exception e) {
            e.printStackTrace();
        }

        main_view = LayoutInflater.from(this).inflate(R.layout.view_search, null);
        main_view.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (Setting_View.getWindowToken() == null) {
                    ((ImageView) main_view.findViewById(R.id.img_search)).setImageResource(R.drawable.scan_icon);
                    mWindowManager.addView(Setting_View, setting_view_params);
                }
            }
        });
        Setting_View = LayoutInflater.from(this).inflate(R.layout.view_setting, null);

        mainLay = Setting_View.findViewById(R.id.mainLay);
        myglobalayout = Setting_View.findViewById(R.id.global);
        setting = Setting_View.findViewById(R.id.setting);
        close_pop = Setting_View.findViewById(R.id.close_pop);
        off = Setting_View.findViewById(R.id.off);
        check_clipboard = Setting_View.findViewById(R.id.check_clipboard);

        setting.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent1 = new Intent(getApplicationContext(), MainActivity.class);
                intent1.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                startActivity(intent1);
                mWindowManager.removeView(Setting_View);
            }
        });

        off.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mWindowManager.removeView(Setting_View);
                mWindowManager.removeView(main_view);
                SharedPreferences.Editor editor = mSharedPreferences.edit();
                editor.putBoolean("added", false);
                editor.apply();
                try {
                    MainActivity.btn_addView.setImageResource(R.drawable.off);
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        });

        lay_to_expand.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                if (main_text_View.getVisibility() == View.VISIBLE) {
                    main_text_View.setVisibility(View.GONE);
                }

                Intent serviceIntent = new Intent(getApplicationContext(), MoreTextFloatingService.class);
                serviceIntent.putExtra("text", main_tv.getText().toString());
                startService(serviceIntent);

            }
        });

        check_clipboard.setImageResource(mSharedPreferences.getBoolean("copy", true) ? R.drawable.clipboard_h : R.drawable.clipboard);

        check_clipboard.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //Copy Click
                SharedPreferences.Editor editor = mSharedPreferences.edit();
                editor.putBoolean("copy", !mSharedPreferences.getBoolean("copy", true));
                editor.apply();
                check_clipboard.setImageResource(mSharedPreferences.getBoolean("copy", true) ? R.drawable.clipboard_h : R.drawable.clipboard);
                mWindowManager.removeView(Setting_View);
            }
        });

        myglobalayout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                counter = 0;
                DisplayAll = true;
                if (main_text_View != null) {
                    if (main_text_View.getVisibility() == View.VISIBLE) {
                        main_text_View.setVisibility(View.GONE);
                    }
                }
                myView.setVisibility(View.VISIBLE);
                addLay.removeAllViews();
                printAllViews(mNodeInfo);
                mWindowManager.removeView(Setting_View);
                ((ImageView) main_view.findViewById(R.id.img_search)).setImageResource(R.drawable.stable_state_pressed);
            }
        });

        close_pop.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                mWindowManager.removeView(Setting_View);
            }
        });

        Help.setSize(mainLay, 270, 440, false);
//        Help.setPadding(mainLay, 40, 60, 30, 60, false);
        Help.setSize(myglobalayout, 112, 112, true);
        Help.setSize(setting, 112, 112, true);
        Help.setSize(off, 112, 112, true);

        Help.setSize(check_clipboard, 160, 116, false);
        Help.setSize(close_pop, 160, 160, false);

        main_view.setLayerType(View.LAYER_TYPE_SOFTWARE, null);

        int flag;
        if (Build.VERSION.SDK_INT >= 26) {
            flag = WindowManager.LayoutParams.TYPE_APPLICATION_OVERLAY;
        } else {
            flag = WindowManager.LayoutParams.TYPE_SYSTEM_ALERT;
        }

        main_view_params = new WindowManager.LayoutParams(
                flag,
                WindowManager.LayoutParams.FLAG_NOT_TOUCH_MODAL |
                        WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE |
                        WindowManager.LayoutParams.FLAG_LAYOUT_IN_SCREEN, PixelFormat.TRANSLUCENT);

        setting_view_params = new WindowManager.LayoutParams(
                flag,
                WindowManager.LayoutParams.FLAG_NOT_TOUCH_MODAL |
                        WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE |
                        WindowManager.LayoutParams.FLAG_LAYOUT_IN_SCREEN |
                        WindowManager.LayoutParams.FLAG_WATCH_OUTSIDE_TOUCH, PixelFormat.TRANSLUCENT);
        main_text_params = new WindowManager.LayoutParams(
                flag,
                WindowManager.LayoutParams.FLAG_NOT_TOUCH_MODAL |
                        WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE |
                        WindowManager.LayoutParams.FLAG_LAYOUT_IN_SCREEN, PixelFormat.TRANSLUCENT);

        main_view_params.width = (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 45, getResources().getDisplayMetrics());
        main_view_params.height = (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 45, getResources().getDisplayMetrics());
        main_view_params.y = getResources().getDisplayMetrics().heightPixels / 2;
        main_view_params.gravity = Gravity.LEFT | Gravity.TOP;

        main_text_params.gravity = Gravity.LEFT | Gravity.TOP;
        main_text_params.x = 0;
        main_text_params.y = 0;

        setting_view_params.width = WindowManager.LayoutParams.WRAP_CONTENT;
        setting_view_params.height = WindowManager.LayoutParams.WRAP_CONTENT;
        setting_view_params.gravity = Gravity.LEFT | Gravity.CENTER_VERTICAL;
        if (Build.VERSION.SDK_INT > 28) {
            int yy = getResources().getDisplayMetrics().heightPixels - status_bar_height;
            setting_view_params.y = (yy * 25) / 1920;
        } else {
            setting_view_params.y = (getResources().getDisplayMetrics().heightPixels * 55) / 1920;
        }

        Setting_View.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, final MotionEvent event) {

                main_view.setOnClickListener(null);

                ((ImageView) main_view.findViewById(R.id.img_search)).setImageResource(R.drawable.stable_state_pressed);

                int[] location = new int[2];
                main_view.getLocationOnScreen(location);
                int x;
                int y;
                event_occupied = true;

                Setting_View.getLocationOnScreen(location);
                x = location[0];
                y = location[1];
                if (event.getRawX() < x || event.getRawY() < y
                        || event.getRawX() > (x + Setting_View.getLayoutParams().width)
                        || event.getRawY() > (y + Setting_View.getLayoutParams().height)) {
                    if (Setting_View.getWindowToken() != null) {
                        mWindowManager.removeView(Setting_View);
                    }
                }

                main_view.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {

                        if (Setting_View.getWindowToken() == null) {
                            ((ImageView) main_view.findViewById(R.id.img_search)).setImageResource(R.drawable.scan_icon);
                            mWindowManager.addView(Setting_View, setting_view_params);
                        }

                    }
                });

                new Handler().postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        event_occupied = false;
                    }
                }, 100);

                return true;

            }
        });

//        convertedText = new HashMap<>();
//        convertedText.clear();

        main_view.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(final View v, MotionEvent event) {

                if (mGestureDetector.onTouchEvent(event) || singleClick) {
                    singleClick = true;
                    DisplayAll = false;
                    myView.setVisibility(View.GONE);
                    mClickHandler.postDelayed(new Runnable() {
                        @Override
                        public void run() {
                            mClickHandler.removeCallbacksAndMessages(null);
                            singleClick = false;
                        }
                    }, 100);
                    return true;
                } else {
                    ((ImageView) main_view.findViewById(R.id.img_search)).setImageResource(R.drawable.scan_icon);
                }

                final int X = (int) event.getRawX();
                final int Y = (int) event.getRawY();
                final int W = getResources().getDisplayMetrics().widthPixels;

                switch (event.getAction() & MotionEvent.ACTION_MASK) {

                    case MotionEvent.ACTION_DOWN:
                        DisplayAll = false;
                        myView.setVisibility(View.GONE);
                        touchIsOn = true;
                        main_tv.setText("");
                        if (main_text_View.getVisibility() == View.VISIBLE) {
                            main_text_View.setVisibility(View.GONE);
                        }

                        WindowManager.LayoutParams lParams = (WindowManager.LayoutParams) main_view.getLayoutParams();
                        pos_xDelta = X - lParams.x;
                        pos_yDelta = Y - lParams.y;
                        DisplayToast = false;
                        main_view.findViewById(R.id.img_search).setVisibility(View.VISIBLE);
                        lastAction = MotionEvent.ACTION_DOWN;
                        startTime = System.currentTimeMillis();
                        clik_counting++;
                        ((ImageView) main_view.findViewById(R.id.img_search)).setImageResource(R.drawable.scan_icon);

                        Log.e("VVV", "onTouch: Down");

                        break;

                    case MotionEvent.ACTION_UP:
                        touchIsOn = false;

                        oldRect = null;

                        if (lastAction == MotionEvent.ACTION_DOWN) {

                        } else {

                            IdentifiedText = "";

                            ValueAnimator translateLeft = ValueAnimator.ofInt(main_view_params.x, 0);
                            ValueAnimator translateTop = ValueAnimator.ofInt(main_view_params.y, getResources().getDisplayMetrics().heightPixels / 2);

                            translateTop.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
                                @Override
                                public void onAnimationUpdate(ValueAnimator animation) {
                                    int val = (Integer) animation.getAnimatedValue();
                                    main_view_params.y = val;
                                    mWindowManager.updateViewLayout(main_view, main_view_params);
                                }
                            });

                            translateLeft.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
                                @Override
                                public void onAnimationUpdate(ValueAnimator valueAnimator) {
                                    int val = (Integer) valueAnimator.getAnimatedValue();
                                    main_view_params.x = val;
                                    mWindowManager.updateViewLayout(main_view, main_view_params);
                                }
                            });

                            translateLeft.setDuration(200);
                            translateTop.setDuration(200);

                            translateLeft.start();
                            translateTop.start();

                            final Handler handler = new Handler();

                            handler.removeMessages(0);

                            handler.postDelayed(new Runnable() {
                                @Override
                                public void run() {

                                    new Handler(Looper.getMainLooper()).postDelayed(new Runnable() {
                                        @Override
                                        public void run() {

                                            try {

                                                stay_on = false;
                                                ((ImageView) main_view.findViewById(R.id.img_search)).setImageResource(R.drawable.stable_state_pressed);
                                                mWindowManager.updateViewLayout(main_view, main_view_params);

                                            } catch (Exception e) {
                                            }
                                        }
                                    }, 500);
                                }
                            }, 0);
                        }

                        Log.e("VVV", "onTouch: Up");

                        break;

                    case MotionEvent.ACTION_MOVE:

                        if (Setting_View.getWindowToken() != null) {
                            mWindowManager.removeView(Setting_View);
                        }

                        touchIsOn = true;

                        lastAction = MotionEvent.ACTION_MOVE;
                        WindowManager.LayoutParams layoutParams = (WindowManager.LayoutParams) main_view.getLayoutParams();
                        layoutParams.x = X - pos_xDelta - (W * 50 / 1080);
                        layoutParams.y = Y - pos_yDelta - (W * 50 / 1080);
                        mWindowManager.updateViewLayout(main_view, layoutParams);

                        mTouchHandler.postDelayed(new Runnable() {
                            @Override
                            public void run() {
                                mTouchHandler.removeCallbacksAndMessages(null);
                                if (touchIsOn) {
                                    if (mNodeInfo != null) {
                                        printAllViews(mNodeInfo);
                                    }
                                }
                            }
                        }, 100);

                        Log.e("VVV", "onTouch: Move");

                        break;
                }

                v.invalidate();
                return true;
            }
        });

        if (Build.VERSION.SDK_INT >= 23) {
            if (Settings.canDrawOverlays(getApplicationContext())) {
                if (mSharedPreferences.getBoolean("added", false) && isAccessibilityEnabled(getApplicationContext())) {
                    if (main_view.getWindowToken() == null) {
                        mWindowManager.addView(main_view, main_view_params);
                    }
                } else {
                    try {
                        mWindowManager.removeView(main_view);
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }
            }
        } else {
            if (mSharedPreferences.getBoolean("added", false) && isAccessibilityEnabled(getApplicationContext())) {
                if (main_view.getWindowToken() == null) {
                    mWindowManager.addView(main_view, main_view_params);
                }
            } else {
                try {
                    mWindowManager.removeView(main_view);
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }

        return START_STICKY;
    }

    @Override
    public IBinder onBind(Intent intent) {
        throw new UnsupportedOperationException("Not yet implemented");
    }

    int counter;

    private void printAllViews(final AccessibilityNodeInfo mNodeInfo) {
        try {
            if (mNodeInfo == null) {
                return;
            }

            if (mNodeInfo.getChildCount() < 1) {
                return;
            }
            for (int i = 0; i < mNodeInfo.getChildCount(); i++) {
                if (mNodeInfo.getChild(i) != null) {
                    if (mNodeInfo.getChild(i).getClassName().toString().endsWith("TextView") ||
                            mNodeInfo.getChild(i).getClassName().toString().endsWith("Button") ||
                            mNodeInfo.getChild(i).getClassName().toString().endsWith("EditText")) {

                        Rect rect = new Rect();
                        mNodeInfo.getChild(i).getBoundsInScreen(rect);

                        if (!DisplayAll) {
                            try {
                                DisplayMetrics dm = new DisplayMetrics();
                                int[] l = new int[2];
                                main_view.getLocationOnScreen(l);
                                int x = l[0];
                                int y = l[1];
//                                showLog("x:-" + x + ",y:-" + y);
//                                showLog("pos:-" + i + ",left:-" + rect.left + ",right:-" + rect.right + ",top:-" + rect.top + ",bottom:-" + rect.bottom);
//                                showLog("width:-" + rect.width() + ",height:-" + rect.height());
//                                if (rect.contains(x + (layoutParams.width / 2), y + (layoutParams.height / 2))) {
                                if ((x >= rect.left && x <= rect.right) && (y >= rect.top && y <= rect.bottom)) {

                                    Log.d("log", mNodeInfo.getChild(i).getText() + " <-- " +
                                            mNodeInfo.getChild(i).getClassName());
                                    if (oldRect == null) {
                                        oldRect = new Rect();
                                    }
                                    oldRect.set(rect);

                                    main_text_params.x = rect.left;
                                    main_text_params.y = rect.top;

                                    Log.e("CCC",
                                            "Rect Values : Left :" + rect.left + " Right : " + rect.right
                                                    + " Screen Width : " + getResources().getDisplayMetrics().widthPixels
                                                    + " View Height : " + rect.height()
                                                    + " View Width : " + rect.width());

                                    main_text_params.gravity = Gravity.START | Gravity.TOP;
                                    int width = rect.width();
                                    int height = rect.height();
                                    if (width < (w * 300 / 1080)) {
                                        main_text_params.width = w * 300 / 1080;
                                    } else if (width >= w) {
                                        main_text_params.width = width - (w * 50 / 1080);
                                        main_text_params.gravity = Gravity.CENTER | Gravity.TOP;
                                    } else {
                                        main_text_params.width = width;
                                    }

                                    if (height < (w * 100 / 1080)) {
                                        main_text_params.height = (w * 200 / 1080);
                                    } else {
                                        main_text_params.height = height + (w * 100 / 1080);
                                    }

                                    if (main_text_View.getWindowToken() == null) {
                                        mWindowManager.addView(main_text_View, main_text_params);
                                    } else {
                                        mWindowManager.updateViewLayout(main_text_View, main_text_params);
                                    }

                                    main_text_View.setVisibility(View.VISIBLE);

                                    for (int j = 0; j < httpArrayList.size(); j++) {
                                        httpArrayList.get(j).cancel(true);
                                        httpArrayList.remove(j);
                                    }
                                    main_tv.setText("");

                                    if (!Utils.hasConnection(getApplicationContext())) {
                                        Toast.makeText(getApplicationContext(), "No Internet Connection", Toast.LENGTH_LONG).show();
                                        return;
                                    }

                                    String sc = mSharedPreferences.getString("scode", "en");
                                    sc = getMyLanguage(sc);
                                    String tc = mSharedPreferences.getString("tcode", "hi");
                                    tc = getMyLanguage(tc);
                                    String boxText = mNodeInfo.getChild(i).getText().toString();
                                    String translate_text = preferences.getTranslate(tc + "---" + boxText);
                                    if (translate_text.equals("")) {
                                        loading_bar.setVisibility(View.VISIBLE);
                                        Http http = new Http();
                                        httpArrayList.add(http);
                                        http.source = sc;
                                        http.target = tc;
                                        http.text = boxText;
                                        http.execute();
                                    } else {
                                        try {
                                            loading_bar.setVisibility(View.GONE);
                                            main_tv.setText(translate_text);
                                            if (mSharedPreferences.getBoolean("copy", true)) {
                                                ClipboardManager clipboard = (ClipboardManager) getSystemService(CLIPBOARD_SERVICE);
                                                ClipData clip = ClipData.newPlainText("label", translated_text);
                                                clipboard.setPrimaryClip(clip);
                                            }
                                        } catch (Exception e) {

                                        }
                                    }
                                    break;
                                }
                            } catch (Exception e) {
                                Log.e("CCC", "Error : " + e.toString());
                            }
                        } else {

                            try {
                                String boxText = mNodeInfo.getChild(i).getText().toString();
                                int color = getResources().getColor(R.color.blck);

                                final View myview = LayoutInflater.from(getApplicationContext()).inflate(R.layout.my_in_view, null, false);

                                ProgressBar pd = myview.findViewById(R.id.pd);
                                AutoFitEditText text = myview.findViewById(R.id.text);

                                text.setTextColor(color);
                                text.setTextSize(50f);
                                text.setMinTextSize(10f);
                                text.setBackgroundResource(R.drawable.my_border_theme);

                                RelativeLayout.LayoutParams params = new RelativeLayout.LayoutParams(rect.width() - 1, rect.height() - 1);
                                myview.setLayoutParams(params);
                                myview.setX(rect.left);
                                if (Build.VERSION.SDK_INT > 28) {
                                    myview.setY(rect.top - status_bar_height);
                                } else {
                                    myview.setY(rect.top);
                                }
                                text.adjustTextSize();
                                int p = w * 5 / 1080;
                                text.setPadding(p, p, p, p);
                                addLay.addView(myview);
                                counter++;
                                String sc = mSharedPreferences.getString("scode", "en");
                                sc = getMyLanguage(sc);
                                String tc = mSharedPreferences.getString("tcode", "hi");
                                tc = getMyLanguage(tc);
                                text.setFocusable(false);
                                text.setOnClickListener(new View.OnClickListener() {
                                    @Override
                                    public void onClick(View view) {
                                        DisplayAll = false;
                                        gone(myView);
                                    }
                                });
                                pd.getIndeterminateDrawable().setColorFilter(color, PorterDuff.Mode.SRC_IN);
                                String translate_text = preferences.getTranslate(tc + "---" + boxText);
                                if (!translate_text.equals("")) {
                                    text.setText(translate_text);
                                    gone(pd);
                                } else {

                                    AllData1 http = new AllData1(boxText, sc, tc, counter, text, pd);
                                    SharedPreferences pref = getSharedPreferences("apicall", MODE_PRIVATE);
//                                    if (pref.getBoolean("isGoogleApi", true)) {
//                                        http.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);
//                                    } else {
//                                        http.execute();
//                                    }
                                    http.execute();
                                }
                            } catch (Exception e) {
                                Log.e("CCC", e.toString());
                            }
                        }
                    }
                    printAllViews(mNodeInfo.getChild(i));
                }
            }

        } catch (Exception e) {
            Log.e("CCC", e.toString());
        }

        mWindowManager.updateViewLayout(main_view, main_view_params);
    }

    public class Http extends AsyncTask {

        private String text, source, target;


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

            try {

                SharedPreferences pref = getSharedPreferences("apicall", MODE_PRIVATE);
                if (pref.getBoolean("isGoogleApi", true)) {

                    String yourURL = "https://script.google.com/macros/s/AKfycbw_vOQSOIEZiW3crJCY4eqrROoj8-PqDofSCcEq7c4hGEwcIDU/exec";
                    String urlStr = yourURL + "?q=" + URLEncoder.encode(text, "UTF-8") + "&target=" + target + "&source="
                            + source;

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
                    IdentifiedText = response.toString();

                    String final_string = "";

                    try {
                        JSONArray jsonArray = new JSONArray(IdentifiedText);

                        for (int i = 0; i < jsonArray.length(); i++) {

                            JSONArray jsonArray1 = (JSONArray) jsonArray.getJSONArray(0).get(i);

                            String s = jsonArray1.get(0).toString();
                            Log.e("ccc", "onPostExecute: " + s);

                            final_string = final_string + " " + s;

                        }


                    } catch (Exception e) {
                        e.printStackTrace();
                    }

                    IdentifiedText = final_string;
                    if (IdentifiedText.startsWith("<!DOCTYPE")) {
                        IdentifiedText = "Error Translate";
                    }
                    return "Success";
                } else {

                    String url = "https://nlp-translation.p.rapidapi.com/v1/translate?text=" + text + "&to=" + target + "&from=" + source;

                    OkHttpClient client = new OkHttpClient();
                    Request request = new Request.Builder()
                            .url(url)
                            .get()
                            .addHeader("x-rapidapi-key", "586e8758a1msh519d6d0be59d0c7p193836jsn732dfa587edb")
                            .addHeader("x-rapidapi-host", "nlp-translation.p.rapidapi.com")
                            .build();

                    Response response = client.newCall(request).execute();
                    String forres = response.body().string();
                    JSONObject Jobject = new JSONObject(forres);
                    IdentifiedText = Jobject.getJSONObject("translated_text").getString(target);

                    Log.e("DDD", "doInBackground: " + IdentifiedText);

                    return "Success";
                }

            } catch (Exception e) {
                stay_on = false;
                return "Fail";
            }

        }

        @Override
        protected void onPostExecute(Object o) {
            super.onPostExecute(o);
            Log.e("CCC", "Start End");
            if (o.equals("Success")) {
                stay_on = true;
                translated_text = IdentifiedText;
                loading_bar.setVisibility(View.GONE);
                String s = "\\\\n";
                translated_text = IdentifiedText.replaceAll(s, "\n");
                translated_text = translated_text.replace("\"", "");
                IdentifiedText = translated_text;
                main_tv.setText(translated_text);

//                convertedText.put(text, recognizedText);
                preferences.setTranslate(target + "---" + text, IdentifiedText);
                if (mSharedPreferences.getBoolean("copy", true)) {
                    ClipboardManager clipboard = (ClipboardManager) getSystemService(CLIPBOARD_SERVICE);
                    ClipData clip = ClipData.newPlainText("label", IdentifiedText);
                    clipboard.setPrimaryClip(clip);
                    Toast.makeText(getApplicationContext(), "Copied to Clipboard", Toast.LENGTH_SHORT).show();
                }
            }
//            if (recognizedText != null && recognizedText.trim().length() > 0) {
//                Toast.makeText(getApplicationContext(), recognizedText, Toast.LENGTH_LONG).show();
//                textView.setText(recognizedText);
//            }
        }

    }

    public class AllData1 extends AsyncTask {

        private String text, source, target, recognizedText;
        int i;
        AutoFitEditText tv;
        ProgressBar pb;

        public AllData1(String text, String source, String target, int i, AutoFitEditText tv, ProgressBar pb) {
            this.text = text;
            this.source = source;
            this.target = target;
            this.i = i;
            this.tv = tv;
            this.pb = pb;
        }

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            Log.e("CCC", text);
        }

        @Override
        protected void onProgressUpdate(Object[] values) {
            super.onProgressUpdate(values);
        }

        @Override
        protected Object doInBackground(Object[] objects) {

            try {

                SharedPreferences pref = getSharedPreferences("apicall", MODE_PRIVATE);
                if (pref.getBoolean("isGoogleApi", true)) {

                    String yourURL = "https://script.google.com/macros/s/AKfycbw_vOQSOIEZiW3crJCY4eqrROoj8-PqDofSCcEq7c4hGEwcIDU/exec";
                    String urlStr = yourURL + "?q=" + URLEncoder.encode(text, "UTF-8") + "&target="
                            + target + "&source=" + source;

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

                    try {
                        JSONArray jsonArray = new JSONArray(recognizedText);

                        for (int i = 0; i < jsonArray.length(); i++) {

                            JSONArray jsonArray1 = (JSONArray) jsonArray.getJSONArray(0).get(i);

                            String s = jsonArray1.get(0).toString();
                            Log.e("ccc", "onPostExecute: " + s);

                            final_string = final_string + " " + s;

                        }


                    } catch (Exception e) {
                        e.printStackTrace();
                    }

                    recognizedText = final_string;

                    if (recognizedText.startsWith("<!DOCTYPE")) {
                        recognizedText = "Error Translate";
                        return "fail";
                    }
                    return "Success";
                } else {

                    String url = "https://nlp-translation.p.rapidapi.com/v1/translate?text=" + text + "&to=" + target + "&from=" + source;

                    OkHttpClient client = new OkHttpClient();
                    Request request = new Request.Builder()
                            .url(url)
                            .get()
                            .addHeader("x-rapidapi-key", "586e8758a1msh519d6d0be59d0c7p193836jsn732dfa587edb")
                            .addHeader("x-rapidapi-host", "nlp-translation.p.rapidapi.com")
                            .build();

                    Response response = client.newCall(request).execute();
                    String forres = response.body().string();
                    JSONObject Jobject = new JSONObject(forres);
                    recognizedText = Jobject.getJSONObject("translated_text").getString(target);

                    Log.e("DDD", "doInBackground: " + recognizedText);

                    return "Success";
                }

            } catch (Exception e) {
                stay_on = false;
                return "Fail";
            }

//            try {
//                String yourURL = "https://script.google.com/macros/s/AKfycbw_vOQSOIEZiW3crJCY4eqrROoj8-PqDofSCcEq7c4hGEwcIDU/exec";
//                String urlStr = yourURL + "?q=" + URLEncoder.encode(text, "UTF-8") + "&target=" + target + "&source="
//                        + source;
//                URL url = new URL(urlStr);
//                StringBuilder response = new StringBuilder();
//                HttpURLConnection con = (HttpURLConnection) url.openConnection();
//                con.setRequestProperty("User-Agent", "Mozilla/5.0");
//                BufferedReader in = new BufferedReader(new InputStreamReader(con.getInputStream()));
//                String inputLine;
//                while ((inputLine = in.readLine()) != null) {
//                    response.append(inputLine);
//                }
//                in.close();
//                recognizedText = response.toString();
//                if (recognizedText.startsWith("<!DOCTYPE")) {
//                    recognizedText = "Error Translate";
//                }
//                return "Success";
//            } catch (Exception e) {
//                hold_on = false;
//                return "Fail";
//            }
        }

        @Override
        protected void onPostExecute(Object o) {
            super.onPostExecute(o);
            Log.e("CCC", "" + i);
            if (o.equals("Success")) {
                stay_on = true;
                tv.setText(recognizedText);
                preferences.setTranslate(target + "---" + text, recognizedText);
            } else {

                cancel(true);
                SharedPreferences.Editor editor = getSharedPreferences("apicall", MODE_PRIVATE).edit();
                editor.putBoolean("isGoogleApi", false);
                editor.apply();

                AllData1 http = new AllData1(text, source, target, i, tv, pb);
                http.execute();
            }
            gone(pb);
        }
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        Log.e("AAA", "onDestroy: ");
        try {
            if (Build.VERSION.SDK_INT >= 28) {
                connectionManager.unregisterNetworkCallback(connectionStateMonitor);
            } else {
                unregisterReceiver(chek_net_receiver);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        try {
            mWindowManager.removeView(main_view);
        } catch (Exception e) {
            e.printStackTrace();
        }
        unregisterReceiver(classReceiver);
        stopSelf();

    }

    public static boolean isAccessibilityEnabled(Context context) {

        AccessibilityManager am = (AccessibilityManager) context
                .getSystemService(Context.ACCESSIBILITY_SERVICE);

        List<AccessibilityServiceInfo> runningServices = am
                .getEnabledAccessibilityServiceList(AccessibilityEvent.TYPES_ALL_MASK);
        for (AccessibilityServiceInfo service : runningServices) {
//            Log.d("serviceId", service.getId());
//            Log.d("servicePackages", StringUtils.join(",", service.packageNames));
            if (service.getId().contains(context.getPackageName())) {
                return true;
            }
        }

        return false;
    }

    static class InternetConnectionReceiver extends BroadcastReceiver {

        @Override
        public void onReceive(Context context, Intent intent) {
            if (!Utils.hasConnection(context)) {
                Toast.makeText(context, "Internet Disabled", Toast.LENGTH_LONG).show();
            }
        }
    }

    @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
    public class ConnectionStateMonitor extends ConnectivityManager.NetworkCallback {

        final NetworkRequest networkRequest;

        public ConnectionStateMonitor() {
            networkRequest = new NetworkRequest.Builder().addTransportType(NetworkCapabilities.TRANSPORT_CELLULAR).addTransportType(NetworkCapabilities.TRANSPORT_WIFI).build();
        }

        public void enable(Context context) {
            ConnectivityManager connectivityManager = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
            connectivityManager.registerNetworkCallback(networkRequest, this);


        }

        // Likewise, you can have a disable method that simply calls ConnectivityManager.unregisterNetworkCallback(NetworkCallback) too.

        @Override
        public void onAvailable(Network network) {
            // Do what you need to do here

        }

        @Override
        public void onLost(Network network) {
            super.onLost(network);
            if (!Utils.hasConnection(getApplicationContext())) {
                Toast.makeText(getApplicationContext(), "Internet Required", Toast.LENGTH_SHORT).show();
            }
        }
    }

    private class GestureListener extends GestureDetector.SimpleOnGestureListener {

        @Override
        public boolean onDown(MotionEvent e) {

            return true;
        }

        // event when double tap occurs
        @Override
        public boolean onDoubleTap(MotionEvent e) {
            return true;
        }

        @Override
        public boolean onSingleTapUp(MotionEvent e) {
            if (!event_occupied) {
                main_view.performClick();
            }
            return super.onSingleTapUp(e);
        }

        @Override
        public boolean onSingleTapConfirmed(MotionEvent e) {

            return super.onSingleTapConfirmed(e);
        }

        @Override
        public void onLongPress(MotionEvent e) {
            super.onLongPress(e);
        }
    }

    String getMyLanguage(String str) {
        if (str.contains("-")) {
            String[] sp = str.split("-");
            str = sp[0];
        }
        if (str.equals("yue")) {
            str = "cmn";
        }
        return str;
    }

    public static class InnerRecevier extends BroadcastReceiver {

        @Override
        public void onReceive(Context context, Intent intent) {
            String action = intent.getAction();
            if (action != null) {
                Log.e("9999", action);
//                DisplayAll = false;
//                myView.setVisibility(View.GONE);
                if (main_text_View != null) {
                    if (main_text_View.getWindowToken() != null) {
                        main_text_View.setVisibility(View.GONE);
                    }
                }
            }
        }
    }
}