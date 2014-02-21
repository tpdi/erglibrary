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
 * 
 * Extends org.diffenbach.android.widgets.EnumRadioGroup to add the ability
 * to add multiple OnCheckChanged listeners. Note that all listeners can be
 * replaced, but cannot be individually removed.
 * 
 * org.diffenbach.android.widgets.multilistener.EnumRadioGroup uses
 * the same OnCheckChangedListeners as the single--listener 
 * org.diffenbach.android.widgets.EnumRadioGroup, and will itself handle 
 * changing them to Multi listeners as required.
 * 
 * Please note that even for multi-listener, the type of the EnumRadioGroup
 * passed back to the listener is always the  base single listener type
 * (org.diffenbach.android.widgets.EnumRadioGroup).
 * 
 * This allows the same listener to be freely used with both single and multi types,
 * at the cost of removing (safe) access to the add/set multi listener methods.
 * It seems reasonable that you're not likely going to want to add a listener
 * in a listener callback (though you infrequently  might want to replace it).
 */

public class EnumRadioGroup<T extends Enum<T>> extends org.diffenbach.android.widgets.EnumRadioGroup<T> {
	
	/** Unfortunately, we need to redefine these statics, so that clients can
	 * explicitly specify them without have to specify the EnumRadioGroup,
	 * only specifying the enum type.
	 */
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
	
	/**
	 * Ctor for inflation from XML.
	 * @param context
	 * @param attrs
	 */
	public EnumRadioGroup(Context context, AttributeSet attrs) {
		super(context, attrs);
	}

	public EnumRadioGroup(Context context, T defaultValue, int rbNames, int rbLayout) {
		super(context, defaultValue, rbNames, rbLayout);
	}

	public EnumRadioGroup(Context context, T defaultValue, int rbNames) {
		super(context, defaultValue, rbNames);
	}

	public EnumRadioGroup(Context context, T defaultValue) {
		super(context, defaultValue);
	}

	protected OnCheckedChangeListener<T> onCheckChangedListener;
	
	/**
	 * Sets or adds a new OnCheckedChangeListener.
	 * @param retainExisting 
	 * 		false to replace the old listener, if any, with this one;
	 * 		true to retain existing listeners, if any, and add this one
	 * @param listener the listener to set/add
	 */
	public <U extends EnumRadioGroup<T>> U setOnCheckedChangeListener(boolean retainExisting, OnCheckedChangeListener<T> listener) {
		return super.setOnCheckedChangeListener( this.onCheckChangedListener =
				( ! retainExisting || this.onCheckChangedListener == null) 
				? listener : this.onCheckChangedListener.toMulti(listener));
	}
	
	public <U extends EnumRadioGroup<T>> U addOnCheckedChangeListener(boolean retainExisting, OnCheckedChangeListener<T> listener) {
		return setOnCheckedChangeListener(true, listener);
	}

}
