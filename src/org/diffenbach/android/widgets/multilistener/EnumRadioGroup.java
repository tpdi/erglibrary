package org.diffenbach.android.widgets.multilistener;

import java.util.EnumSet;
import java.util.List;

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
	
	@Override
	public EnumRadioGroup<T> filter(org.diffenbach.android.widgets.EnumRadioGroup.DisplayPredicate<T> pred) {
		return (EnumRadioGroup<T>) super.filter(pred);
	};
	
	@Override
	public EnumRadioGroup<T> filter(java.util.EnumSet<T> set) {
		return (EnumRadioGroup<T>) super.filter(set);
	}
	
	@Override
	public EnumRadioGroup<T> filterNotIn(java.util.EnumSet<T> set) {
		return (EnumRadioGroup<T>) super.filterNotIn(set);
	};
	
		
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
	 * Factory functions to create and return filter predicates.
	 * @param notused
	 * @return
	 */
	// That notused class is just for type inference
	@SuppressWarnings("unchecked")
	public static <T extends Enum<T>> DisplayPredicate<T> includeAll(Class<T> notused) {
		return org.diffenbach.android.widgets.EnumRadioGroup.INCLUDE_ALL;
	}
	
	public static <T extends Enum<T>> DisplayPredicate<T> includeAllBut(List<T> exclude) {
		return new ExcludeEnumSetPredicate<T>(EnumSet.copyOf(exclude));
	}
	
	public static <T extends Enum<T>> DisplayPredicate<T> includeAllBut(T first, T... exclude) {
		return new ExcludeEnumSetPredicate<T>(EnumSet.of(first, exclude));
	}
	
	public static <T extends Enum<T>> DisplayPredicate<T> include(EnumSet<T> eset) {
		return new ExcludeEnumSetPredicate<T>(EnumSet.complementOf(eset));
	}
	
	public static <T extends Enum<T>> DisplayPredicate<T> include(T first, T... rest) {
		return new ExcludeEnumSetPredicate<T>(EnumSet.complementOf(EnumSet.of(first, rest)));
	}
	
	public static <T extends Enum<T>> DisplayPredicate<T> includeAllBut(EnumSet<T> eset) {
		return new ExcludeEnumSetPredicate<T>(eset);
	}
		

	
	
	/**
	 * Function to allow the creation of arrays of predicates.
	 * @param dps
	 * @return
	 */
	// Hello! I am a hack to allow the creation of generic array which are otherwise disallowed.
	public static <T extends Enum<T>> DisplayPredicate<T>[] makeDisplayPredicateArray( DisplayPredicate<T>... dps) {
		return dps;
	}
	
}
