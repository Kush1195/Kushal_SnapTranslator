package language.translate.snap.translate.activities;

import android.app.Activity;
import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.DisplayMetrics;
import android.view.Display;
import android.view.Gravity;
import android.view.View;
import android.widget.EditText;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.Toast;

import com.ayoubfletcher.consentsdk.ConsentSDK;
import com.google.android.gms.ads.AdSize;
import com.google.android.gms.ads.AdView;

import language.translate.snap.translate.R;
import language.translate.snap.translate.adapter.Language_Adapter;
import language.translate.snap.translate.interfaces.ListClick;
import language.translate.snap.translate.utils.HelperResizer;

import static language.translate.snap.translate.activities.MainActivity.dropdown;
import static language.translate.snap.translate.activities.MainActivity.dropdown1;
import static language.translate.snap.translate.activities.MainActivity.isSource;
import static language.translate.snap.translate.activities.MainActivity.languages;
import static language.translate.snap.translate.activities.MainActivity.sourceBtn;
import static language.translate.snap.translate.activities.MainActivity.targetBtn;
import static language.translate.snap.translate.activities.MainActivity.viv;
import static language.translate.snap.translate.activities.MainActivity.viv2;

public class LanguageSelectionActivity extends Activity implements ListClick {

    Context mContext;
    ListView list_languages;
    EditText search;
    ImageView iv_search, back, search_ic,search_line,ic_close,ic_go;
    LinearLayout lay_search, list_bg;
    Language_Adapter language_adapter;
    private SharedPreferences sharedPreferences;
    private FrameLayout adContainerView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_language_selection);
        mContext = this;
        init();
        resize();

        loadBanner();
    }

    private void resize() {

        int w = getResources().getDisplayMetrics().widthPixels;
        int h = getResources().getDisplayMetrics().heightPixels;

        LinearLayout.LayoutParams search_p = new LinearLayout.LayoutParams((w * 1000) / 1080, (h * 100) / 1920);
        lay_search.setLayoutParams(search_p);

        LinearLayout.LayoutParams back_p = new LinearLayout.LayoutParams((w * 58) / 1080,(h * 50) / 1920);
        back.setLayoutParams(back_p);

        LinearLayout.LayoutParams search_ic_p = new LinearLayout.LayoutParams((w * 50) / 1080,(w * 50) / 1080);
        search_ic.setLayoutParams(search_ic_p);

        LinearLayout.LayoutParams list_bg_p = new LinearLayout.LayoutParams((w * 1000) / 1080, LinearLayout.LayoutParams.MATCH_PARENT);
        list_bg_p.gravity = Gravity.CENTER;
        list_bg_p.topMargin = (h * 40) / 1920;
        list_bg.setLayoutParams(list_bg_p);

        iv_search.setLayoutParams(new LinearLayout.LayoutParams((w * 40) / 1080, (w * 40) / 1080));

        search_line.setLayoutParams(new LinearLayout.LayoutParams((w * 3) / 1080, (h * 50) / 1920));

        ic_go.setLayoutParams(new LinearLayout.LayoutParams((w * 120) / 1080, (h * 70) / 1920));

        ic_close.setLayoutParams(new LinearLayout.LayoutParams((w * 30) / 1080, (w * 30) / 1080));

    }

    private void init() {

        ic_close = findViewById(R.id.ic_close);
        search_line = findViewById(R.id.search_line);
        list_languages = findViewById(R.id.list_languages);
        back = findViewById(R.id.back);
        search_ic = findViewById(R.id.search_ic);
        search = findViewById(R.id.search);
        lay_search = findViewById(R.id.lay_search);
        iv_search = findViewById(R.id.iv_search);
        list_bg = findViewById(R.id.list_bg);
        ic_go = findViewById(R.id.ic_go);

        sharedPreferences = PreferenceManager.getDefaultSharedPreferences(mContext);

        language_adapter = new Language_Adapter(mContext, LanguageSelectionActivity.this);
        list_languages.setAdapter(language_adapter);

        search_ic.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (lay_search.getVisibility() == View.VISIBLE) {
                    lay_search.setVisibility(View.GONE);
                } else {
                    lay_search.setVisibility(View.VISIBLE);
                }
            }
        });

        ic_close.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                search.setText("");
                language_adapter.notifyDataSetChanged();
                ic_go.performClick();
//                lay_search.setVisibility(View.GONE);
            }
        });

        ic_go.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (languages == null || language_adapter == null) {
                    Toast.makeText(mContext, "Empty List", Toast.LENGTH_LONG).show();
                    return;
                }
                try {
                    language_adapter.getFilter().filter(search.getText().toString().toLowerCase());
                } catch (Exception unused) {
                    unused.printStackTrace();
                }
            }
        });

        search.addTextChangedListener(new TextWatcher() {
            public void beforeTextChanged(CharSequence charSequence, int i, int i2, int i3) {
            }

            public void onTextChanged(CharSequence charSequence, int i, int i2, int i3) {
            }

            public void afterTextChanged(Editable editable) {
                if (languages == null || language_adapter == null) {
                    Toast.makeText(mContext, "Empty List", Toast.LENGTH_LONG).show();
                    return;
                }
                try {
                    language_adapter.getFilter().filter(search.getText().toString().toLowerCase());
                } catch (Exception unused) {
                    unused.printStackTrace();
                }
            }
        });

        back.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                onBackPressed();
            }
        });

    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        HelperResizer.checkAd=0;
    }

    @Override
    public void onListClick(int pos, String langname, String lanCode) {

        SharedPreferences.Editor editor = sharedPreferences.edit();

        String sc = sharedPreferences.getString("scode", "en");
        String tc = sharedPreferences.getString("tcode", "hi");

        Language_Adapter.poss = langname;

        if (isSource) {
            if(tc.equals(lanCode)){
                Toast.makeText(mContext, "Cannot Select Same", Toast.LENGTH_SHORT).show();
                return;
            }
            editor.putString("sname", langname);
            editor.putString("scode", lanCode);
            editor.putString("poss",langname);
            editor.apply();

            sourceBtn.setText(langname);
            dropdown.setImageResource(R.drawable.down);
            viv = 1;

        } else {
            if(sc.equals(lanCode)){
                Toast.makeText(mContext, "Cannot Select Same", Toast.LENGTH_SHORT).show();
                return;
            }
            editor.putString("tname", langname);
            editor.putString("tcode", lanCode);
            editor.putString("poss1",langname);
            editor.apply();

            targetBtn.setText(langname);
            dropdown1.setImageResource(R.drawable.down);
            viv2 = 1;
//            TouchTranslatorService.convertedText.clear();
        }
        language_adapter.notifyDataSetChanged();
        editor.apply();
        HelperResizer.checkAd=0;
        finish();
    }

    AdView adView;

    private void loadBanner() {

        adContainerView = findViewById(R.id.ad_view_container);
// Step 1 - Create an AdView and set the ad unit ID on it.
        adView = new AdView(this);
        adView.setAdUnitId(getString(R.string.admob_banner_id));
        adContainerView.addView(adView);

        AdSize adSize = getAdSize();
        adView.setAdSize(adSize);
        adView.loadAd(ConsentSDK.getAdRequest(this));
    }

    private AdSize getAdSize() {
        Display display = getWindowManager().getDefaultDisplay();
        DisplayMetrics outMetrics = new DisplayMetrics();
        display.getMetrics(outMetrics);
        float widthPixels = outMetrics.widthPixels;
        float density = outMetrics.density;
        int adWidth = (int) (widthPixels / density);
        return AdSize.getCurrentOrientationAnchoredAdaptiveBannerAdSize(this, adWidth);
    }
}