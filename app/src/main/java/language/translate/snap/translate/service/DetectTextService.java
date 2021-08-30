package language.translate.snap.translate.service;

import android.accessibilityservice.AccessibilityService;
import android.app.ActivityManager;
import android.content.Context;
import android.content.Intent;
import android.util.Log;
import android.view.View;
import android.view.accessibility.AccessibilityEvent;
import android.view.accessibility.AccessibilityNodeInfo;

import static language.translate.snap.translate.service.TextTranslatorService.myView;
import static language.translate.snap.translate.service.TextTranslatorService.DisplayAll;
import static language.translate.snap.translate.service.TextTranslatorService.main_text_View;

public class DetectTextService extends AccessibilityService {

    public static AccessibilityNodeInfo mNodeInfo;
    int eventType = AccessibilityEvent.TYPE_VIEW_CLICKED;

    public void onInterrupt() {}

    public void onAccessibilityEvent(AccessibilityEvent event) {

        try {

            if (event == null || event.getPackageName() == null) {
                return;
            }

            if (eventType == event.getEventType()) {
                Log.e("LLL", "Back Pressed");
                if (myView != null) {
                    if (myView.getWindowToken() != null) {
                        if (myView.getVisibility() == View.VISIBLE) {
                            myView.setVisibility(View.GONE);
                        }
                    }
                }

                if (main_text_View != null) {
                    if (main_text_View.getWindowToken() != null) {
                        if (main_text_View.getVisibility() == View.VISIBLE) {
                            main_text_View.setVisibility(View.GONE);
                        }
                    }
                }

            }

            if (event.getPackageName().equals("com.android.systemui")) {
                return;
            }

            if (!event.getPackageName().equals(getPackageName()) && event.getSource() != null && event.getSource().getChildCount() >= 1) {

                DisplayAll = false;
//                myView.setVisibility(View.GONE);

                if (main_text_View != null) {
                    if (main_text_View.getWindowToken() != null) {
                        main_text_View.setVisibility(View.GONE);
                    }
                }

                ActivityManager manager = (ActivityManager) getSystemService(Context.ACTIVITY_SERVICE);
                for (ActivityManager.RunningServiceInfo service : manager.getRunningServices(Integer.MAX_VALUE)) {
                    if (MoreTextFloatingService.class.getName().equals(service.service.getClassName())) {
                        Intent intent = new Intent(getApplicationContext(), MoreTextFloatingService.class);
                        stopService(intent);
                    }
                }

                Log.d("detectedPackage", "" + event.getPackageName());
                Log.d("detectedPackage", "" + event.getClassName());

                if (mNodeInfo != event.getSource()) {
                    mNodeInfo = event.getSource();
                    mNodeInfo.refresh();
                }

            }

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

}
