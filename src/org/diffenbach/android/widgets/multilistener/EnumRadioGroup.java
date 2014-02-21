package org.diffenbach.android.widgets.multilistener;

import org.diffenbach.android.widgets.EnumRadioGroupABC;

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

@SuppressWarnings("unused")
public class EnumRadioGroup<T extends Enum<T>> extends EnumRadioGroupABC<T, EnumRadioGroup<T>> {
		
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
	 * Set the (generic parameterized) Change Listener.
	 * Chains to any existing listener
	 * @param listener
	 */
	@Override
	public void setOnCheckedChangeListener(org.diffenbach.android.widgets.OnCheckedChangeListener<T> listener) {
		setOnCheckedChangeListener(false, listener);
		
	}
	
	/**
	 * Sets or adds a new OnCheckedChangeListener.
	 * @param retainExisting 
	 * 		false to replace the old listener, if any, with this one;
	 * 		true to retain existing listeners, if any, and add this one
	 * @param listener the listener to set/add
	 */
	public void setOnCheckedChangeListener(boolean retainExisting, org.diffenbach.android.widgets.OnCheckedChangeListener<T> listener) {
		super.setOnCheckedChangeListener( this.onCheckChangedListener =
				( ! retainExisting || this.onCheckChangedListener == null) 
				? listener : this.onCheckChangedListener.toMulti(listener));
	}
	

	
		
	/** helper classes 
	 * 
	 * @author tpd
	 * 
	 */
	
		
	/**
	 * Predicate for setting the display
	 * @author TP Diffenbach
	 *
	 * @param <T>
	 */
		


	
	
	/**
	 * Function to allow the creation of arrays of predicates.
	 * @param dps
	 * @return
	 */

}
