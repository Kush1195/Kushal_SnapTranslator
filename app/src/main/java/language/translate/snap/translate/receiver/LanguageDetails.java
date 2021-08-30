package language.translate.snap.translate.receiver;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;

import language.translate.snap.translate.activities.MainActivity;
import language.translate.snap.translate.model.GetLanguage;
import language.translate.snap.translate.model.Language;

import java.util.List;

public class LanguageDetails extends BroadcastReceiver {

    private String languagePreference;
    private List<String> supportedLanguages;

    public void onReceive(Context context, Intent intent) {

        Bundle resultExtras = getResultExtras(true);
        String str = "android.speech.extra.LANGUAGE_PREFERENCE";

        if (resultExtras.containsKey(str)) {
            languagePreference = resultExtras.getString(str);
        }

        String str2 = "android.speech.extra.SUPPORTED_LANGUAGES";
        if (resultExtras.containsKey(str2)) {
            supportedLanguages = resultExtras.getStringArrayList(str2);
        }

        MainActivity.languages.clear();

        for (int i = 0; i < 119; i++) {
            MainActivity.languages.add(new Language(GetLanguage.getLanguageName(i), GetLanguage.getLanguageCode(i)));
        }
    }
}
