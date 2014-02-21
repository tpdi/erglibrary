/**
 * 
 */
package org.diffenbach.android.utils;

import android.view.View;
import android.widget.LinearLayout;

/**
 * @author TP Diffenbach
 *
 * Utilities that mutate views and return the view, for chaining.
 * Because Android doesn't chain setters.
 */
public class ViewUtils {
	
	public static <L extends LinearLayout> L setOrientation( int orientation, L v) {
		v.setOrientation(orientation);
		return v;
	}
	
	public static <V extends View> V setId(int id, V v) {
		v.setId(id);
		return v;
	}

}
