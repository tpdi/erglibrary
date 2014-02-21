package org.diffenbach.android.widgets.multilistener;

import android.app.Activity;
import android.content.Context;
import android.util.AttributeSet;
import android.view.View;


/**
 * A class to show Enum constants as radio buttons.
 * @author TP Diffenbach
 *
 * @param <T> An Enum class (<T extends Enum<T>>)
 */

public class EnumRadioGroup<T extends Enum<T>> extends org.diffenbach.android.widgets.EnumRadioGroup<T> {
		
	/** 
	 * Wraps findById in a cast
	 * @param a an Activity
	 * @param id an id of an EnumRadioGroup
	 * @return a View that we hope is an EnumRadioGroup, or throw a ClassCastException
	 */
	
	@SuppressWarnings("unchecked")
	public static <E extends Enum<E>> EnumRadioGroup<E> findById( Activity a, int id) {
		return (EnumRadioGroup<E>) a.findViewById(id);
	}
	
	/** 
	 * Wraps findById in a cast
	 * @param v a View
	 * @param id an id of an EnumRadioGroup
	 * @return a child View that we hope is an EnumRadioGroup, or throw ClassCastException
	 */
	@SuppressWarnings("unchecked")
	public static <E extends Enum<E>> EnumRadioGroup<E> findById( View v, int id) {
		return (EnumRadioGroup<E>) v.findViewById(id);
	}
	
	public EnumRadioGroup(Context context, AttributeSet attrs) {
		super(context, attrs);
	}

	public EnumRadioGroup(Context context, T defaultValue, int rbNames,
			int rbLayout) {
		super(context, defaultValue, rbNames, rbLayout);
	}

	public EnumRadioGroup(Context context, T defaultValue, int rbNames) {
		super(context, defaultValue, rbNames);
	}

	public EnumRadioGroup(Context context, T defaultValue) {
		super(context, defaultValue);
	}

	protected org.diffenbach.android.widgets.OnCheckedChangeListener<T> onCheckChangedListener;
	
	/**
	 * Sets or adds a new OnCheckedChangeListener.
	 * @param retainExisting 
	 * 		false to replace the old listener, if any, with this one;
	 * 		true to retain existing listeners, if any, and add this one
	 * @param listener the listener to set/add
	 */
	public <U extends EnumRadioGroup<T>> U setOnCheckedChangeListener(boolean retainExisting, org.diffenbach.android.widgets.OnCheckedChangeListener<T> listener) {
		return super.setOnCheckedChangeListener( this.onCheckChangedListener =
				( ! retainExisting || this.onCheckChangedListener == null) 
				? listener : this.onCheckChangedListener.toMulti(listener));
	}
	
	public <U extends EnumRadioGroup<T>> U addOnCheckedChangeListener(boolean retainExisting, org.diffenbach.android.widgets.OnCheckedChangeListener<T> listener) {
		return setOnCheckedChangeListener(true, listener);
	}

}
