package language.translate.snap.translate.receiver;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.os.Build;

import org.w3c.dom.Text;

import language.translate.snap.translate.service.TextTranslatorService;

public class BootReceiver extends BroadcastReceiver {

    public void onReceive(Context context, Intent intent) {
        if (Build.VERSION.SDK_INT > 26) {
            context.startForegroundService(new Intent(context, TextTranslatorService.class));
        } else {
            context.startService(new Intent(context, TextTranslatorService.class));
        }
    }
}
