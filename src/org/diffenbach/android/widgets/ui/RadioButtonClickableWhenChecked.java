package org.diffenbach.android.widgets.ui;

import android.content.Context;
import android.util.AttributeSet;
import android.util.Log;
import android.view.ViewParent;
import android.widget.RadioButton;
import android.widget.RadioGroup;

public class RadioButtonClickableWhenChecked extends RadioButton {

	public RadioButtonClickableWhenChecked(Context context) {
		super(context);
	}

	public RadioButtonClickableWhenChecked(Context context, AttributeSet attrs) {
		super(context, attrs);
	}

	public RadioButtonClickableWhenChecked(Context context, AttributeSet attrs, int defStyle) {
		super(context, attrs, defStyle);
	}

	@Override
    public boolean performClick() {

        if(isChecked()) {
        	Log.i("RadioButtonClickableWhenChecked", "about to call super.performClick()");
            boolean ret = super.performClick();
            ViewParent p = getParent();
            while (p != null && ! (p instanceof EnumRadioGroup<?>)) {
                p = p.getParent();
            }
            if (p != null) {
            	@SuppressWarnings("rawtypes")
				EnumRadioGroup erg = (EnumRadioGroup<?>) p;
                Log.i("RadioButtonClickableWhenChecked", "about to call callOnChecked");
                erg.callOnChecked();
                Log.i("RadioButtonClickableWhenChecked", "called callOnChecked");
                //erg.check(this.getId());
            }
            return ret;
        } else {
            return super.performClick();
        }
    }
}
