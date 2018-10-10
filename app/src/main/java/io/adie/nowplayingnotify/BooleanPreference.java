package io.adie.nowplayingnotify;

import android.content.Context;
import android.content.res.TypedArray;
import androidx.constraintlayout.widget.ConstraintLayout;
import android.util.AttributeSet;
import android.view.View;
import android.widget.CheckBox;
import android.widget.TextView;


public class BooleanPreference extends ConstraintLayout {
    TextView title;
    TextView summary;
    CheckBox state;

    public BooleanPreference(Context context, AttributeSet attrs) {
        super(context, attrs);
        setTag(false);

        final View v = inflate(context, R.layout.pref_boolean, this);
        title = v.findViewById(R.id.title);
        summary = v.findViewById(R.id.summary);
        state = v.findViewById(R.id.pref_state);

        TypedArray a = context.getTheme().obtainStyledAttributes(
                attrs,
                R.styleable.PreferenceFields,
                0, 0);

        try {
            final boolean showBottomBorder = a.getBoolean(R.styleable.PreferenceFields_pref_bottom_border, false);
            v.findViewById(R.id.bottom_border).setVisibility(showBottomBorder ? View.VISIBLE : View.INVISIBLE);

            final boolean checked = a.getBoolean(R.styleable.PreferenceFields_pref_checked, false);
            state.setChecked(checked);

            final String title = a.getString(R.styleable.PreferenceFields_pref_title);
            this.title.setText(title);

            final String summary = a.getString(R.styleable.PreferenceFields_pref_summary);
            if (summary == null) {
                this.summary.setVisibility(View.GONE);
            } else {
                this.summary.setText(summary);
            }
        } finally {
            a.recycle();
        }
    }

    public void setChecked(final boolean checked) {
        setTag(checked);
        this.state.setOnCheckedChangeListener(null);
        this.state.setChecked(checked);
    }

    public void setSummary(final String summary) {
        this.summary.setText(summary);
    }
}
