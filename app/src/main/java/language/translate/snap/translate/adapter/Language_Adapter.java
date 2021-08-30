package language.translate.snap.translate.adapter;

import android.content.Context;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.Filter;
import android.widget.ImageView;
import android.widget.LinearLayout;

import language.translate.snap.translate.R;
import language.translate.snap.translate.activities.MainActivity;
import language.translate.snap.translate.interfaces.ListClick;
import language.translate.snap.translate.model.Language;
import language.translate.snap.translate.utils.AutoResizeTextView;
import language.translate.snap.translate.utils.HelperResizer;

import java.util.ArrayList;
import java.util.Collection;

import static language.translate.snap.translate.activities.MainActivity.isSource;

public class Language_Adapter extends BaseAdapter {

    Context mContext;
    private Filter filter;
    LayoutInflater inflater = null;
    ArrayList<Language> languages;
    ListClick click;
    ImageView ic_select;
    public static String poss = "-1";
    public static String poss1 = "-1";
    SharedPreferences sharedPreferences;

    // constructor
    public Language_Adapter(Context context, ListClick click) {
        mContext = context;
        this.click = click;
        inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        languages = new ArrayList<>();
        languages.addAll(MainActivity.languages);

        sharedPreferences = PreferenceManager.getDefaultSharedPreferences(mContext);
        poss = sharedPreferences.getString("poss", "English (India)");
        poss1 = sharedPreferences.getString("poss1", "Hindi (India)");
    }

    public int getCount() {
        if (languages != null)
            return languages.size();
        else
            return 0;
    }

    public Object getItem(int position) {
        return position;
    }

    public long getItemId(int position) {
        return position;
    }

    // returns the individual images to the widget as it requires them
    public View getView(final int position, View convertView, ViewGroup parent) {
        View vi = convertView;

        if (convertView == null)
            vi = inflater.inflate(R.layout.list_language, null);

        AutoResizeTextView txt_name;
        ImageView line;
        LinearLayout list_lay;

        txt_name = vi.findViewById(R.id.txt_name);
        line = vi.findViewById(R.id.line);
        list_lay = vi.findViewById(R.id.list_lay);
        ic_select = vi.findViewById(R.id.ic_select);

        HelperResizer.getheightandwidth(mContext);
        HelperResizer.setSize(line,940,2,true);
        HelperResizer.setSize(ic_select,40,40,true);
        HelperResizer.setHeightByWidth(mContext, list_lay,120);
        HelperResizer.setHeightByWidth(mContext, txt_name,107);

        txt_name.setMinTextSize(30);
        txt_name.setMaxLines(2);
        if (languages != null) {
            txt_name.setText(languages.get(position).getDisplayName());
        }
        notifyDataSetChanged();

        if (isSource) {
            if (poss.equalsIgnoreCase(languages.get(position).getDisplayName())) {
                ic_select.setImageResource(R.drawable.select);
            } else {
                ic_select.setImageResource(R.drawable.deselect);
            }
        } else {
            if (poss1.equalsIgnoreCase(languages.get(position).getDisplayName())) {
                ic_select.setImageResource(R.drawable.select);
            } else {
                ic_select.setImageResource(R.drawable.deselect);
            }
        }

        vi.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (click != null) {
                    click.onListClick(position, languages.get(position).getDisplayName(), languages.get(position).getCode());
                }
            }
        });

        return vi;
    }

    private class RecordFilter1 extends Filter {
        private RecordFilter1() {
        }

        public FilterResults performFiltering(CharSequence charSequence) {

            FilterResults filterResults = new FilterResults();
            ArrayList<Language> arrayList = new ArrayList<>();

            if (MainActivity.languages != null) {

                if (charSequence == null || TextUtils.isEmpty(charSequence)) {

                    filterResults.count = MainActivity.languages.size();
                    filterResults.values = MainActivity.languages;
                } else {

                    if (MainActivity.languages != null && MainActivity.languages.size() > 0) {

                        for (int i = 0; i < MainActivity.languages.size(); i++) {

                            if (MainActivity.languages.get(i).getDisplayName().toLowerCase().startsWith(charSequence.toString().toLowerCase())) {

                                arrayList.add(MainActivity.languages.get(i));

                            }

                        }

                    }

                    filterResults.values = arrayList;

                }
            }
            return filterResults;
        }

        public void publishResults(CharSequence charSequence, FilterResults filterResults) {
            languages.clear();
            languages.addAll((Collection<? extends Language>) filterResults.values);
            notifyDataSetChanged();
        }
    }

    public Filter getFilter() {
        if (filter == null) {
            filter = new RecordFilter1();
        }
        return filter;
    }

}
