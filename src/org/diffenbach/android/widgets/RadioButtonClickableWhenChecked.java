package org.diffenbach.android.widgets;

import android.content.Context;
import android.util.AttributeSet;
import android.util.Log;
import android.widget.RadioButton;

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
    public void toggle() {
        // we override to prevent toggle when the radio is already
        // checked (as opposed to check boxes widgets)
    	Log.i("xxx", "entering toggle, checked is " + isChecked());
        if (!isChecked()) {
        	Log.i("xxx", "not checked, calling super.toggle, checked is " + isChecked());
            super.toggle();
            Log.i("xxx", "not checked, called super.toggle, checked is " + isChecked());
        } else {
        	try {
        		Log.i("xxx", " checked is " + isChecked() + "\nsetting false");
        		setChecked(false);
        		Log.i("xxx", "checked is " + isChecked() + "\nsetting true");
        		setChecked(true);
        		Log.i("xxx", "set true, checked is " + isChecked());
        		int i = 2;
        	} catch(Throwable e) {
        		Log.e("xxx", e.getLocalizedMessage());
        	}
        }
        Log.i("xxx", "exiting toggle, checked is " + isChecked() + "\n");
        int j = 3;
    }
}
