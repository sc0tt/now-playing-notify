package net.meeplecorp.bingocaller.widgets;

import android.content.Context;
import android.content.res.TypedArray;
import android.support.constraint.ConstraintLayout;
import android.util.AttributeSet;
import android.view.View;
import android.widget.CheckBox;
import android.widget.TextView;

import net.meeplecorp.bingocaller.R;

import butterknife.BindView;
import butterknife.ButterKnife;


public class BooleanPreference extends ConstraintLayout {
    @BindView(R.id.title)
    TextView title;

    @BindView(R.id.summary)
    TextView summary;

    @BindView(R.id.pref_state)
    CheckBox state;

    public BooleanPreference(Context context, AttributeSet attrs) {
        super(context, attrs);
        setTag(false);

        final View v = inflate(context, R.layout.pref_boolean, this);

        ButterKnife.bind(this);
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
