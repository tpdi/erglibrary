package org.diffenbach.android.widgets;

import android.content.Context;
import android.util.AttributeSet;
import android.view.ViewParent;
import android.widget.RadioButton;
import android.widget.RadioGroup;

public class RadioButtonClickableWhenChecked extends RadioButton {

	public RadioButtonClickableWhenChecked(Context context) {
		super(context);
		// TODO Auto-generated constructor stub
	}

	public RadioButtonClickableWhenChecked(Context context, AttributeSet attrs) {
		super(context, attrs);
		// TODO Auto-generated constructor stub
	}

	public RadioButtonClickableWhenChecked(Context context, AttributeSet attrs, int defStyle) {
		super(context, attrs, defStyle);
		// TODO Auto-generated constructor stub
	}

	@Override
    public boolean performClick() {

        if(isChecked()) {
            boolean ret = super.performClick();
            ViewParent p = getParent();
            while (p != null && ! (p instanceof RadioGroup)) {
                p = p.getParent();
            }
            if (p != null) {
                RadioGroup erg = (RadioGroup) p;
                erg.check(-1);
                erg.check(this.getId());
            }
            return ret;
        } else {
            return super.performClick();
        }
    }
}
