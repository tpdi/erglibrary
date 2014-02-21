/**
 * 
 */
package org.diffenbach.android.utils;

import android.view.View;
import android.widget.LinearLayout;
import android.widget.RadioGroup.LayoutParams;

/**
 * @author TP Diffenbach
 *
 * Utilities that mutate views and return the view, for chaining.
 * Best used with an import static org.diffenbach.android.utils.*;
 * Because Android doesn't chain setters.
 * Template methods to return the same derived type as passed.
 * Views are passed as the second argument, to keep the setting close to
 * the method name:
 * 		setLayoutParams(lp, setOrientation(LinearLayout.HORIZONTAL, setId(1, v));
 * not
 * 		setLayoutParams(setOrientation(setId(1, v), LinearLayout.HORIZONTAL), lp);
 */
public class ViewUtils {
	
	public static <L extends LinearLayout> L setOrientation( final int orientation, final L v) {
		v.setOrientation(orientation);
		return v;
	}
	
	public static <V extends View> V setId(final int id, final V v) {
		v.setId(id);
		return v;
	}
	
	public static <V extends View> V setLayoutParams(final LayoutParams lp, final V v) {
		v.setLayoutParams(lp);
		return v;
	}

}
