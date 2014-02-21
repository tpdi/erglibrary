package org.diffenbach.android.widgets;

import java.util.EnumSet;

import org.diffenbach.android.widgets.EnumRadioGroupABC.DisplayPredicate;

import android.widget.RadioButton;

public interface IEnumRadioGroup<T extends Enum<T>, U extends EnumRadioGroup<T>> {

	/**
	 * Return the default enum constant for this Radio Group, as set in the ctor.
	 * @return
	 */
	public abstract T getDefault();

	/**
	 * Determines if the checked button in this group is the default value set in the ctor.
	 * @return true iff the checked button in this group is the default value
	 */
	public abstract boolean isSetToDefault();

	/**
	 * Resets the checked RadioButton to be the default value.
	 */
	public abstract void clearCheck();

	/**
	 * Please prefer using check(T value).
	 * Sets the checked button to be the RadioButton with the  given id, 
	 * IF that button is a member of the group; if not, throws IllegalArgumentException
	 * If the id is -1, checks the RadioButton corresponding to the default passed in the ctor. 
	 * @see android.widget.RadioGroup#check(int)
	 */
	public abstract void check(int id);

	/**
	 * Checks the button corresponding to the enum constant passed.
	 * @param value the enum constant to check
	 */
	public abstract void check(T value);

	/**
	 * Gets the enum value corresponding to the currently checked RadioButton.
	 * @return the enum constant corresponding to the currently checked button.
	 * Note that if you override this to provide non-contiguous ids, 
	 * you'll also need to override isChildRadioButtonIdValid
	 */
	public abstract T getCheckedValue();

	/**
	 * Returns the id of the radioButton in the group corresponding to the enum constant passed.
	 * @param enumConstant
	 * @return id of the radioButton in the group corresponding to the enum constant passed.
	 */
	public abstract int getViewIdForEnum(T enumConstant);

	/**
	 * return the RadioButton  corresponding to the passed-in enum constant.
	 * @param enumConstant
	 * @return
	 */
	// Convenience function
	public abstract RadioButton findViewByEnum(T enumConstant);

	public abstract T[] values();

	/**
	 * Displays only buttons  corresponding to enum constants that pass the filter
	 * @param pred a {@DisplayPredicate} for the Enum<T>s
	 * @return this, for chaining
	 */
	public abstract U filter(DisplayPredicate<T> pred);

	/**
	 * Display only buttons corresponding to enums in the given EnumSet.
	 * @param set am EnumSet<T>
	 * @return this, for chaining
	 */
	public abstract U filter(EnumSet<T> set);

	/**
	 * Display only buttons  corresponding to enums not in the given EnumSet.
	 * @param set am EnumSet<T>
	 * @return this, for chaining
	 */
	public abstract U filterNotIn(EnumSet<T> set);

	/**
	 * Set the (generic parameterized) Change Listener.
	 * Chains to any existing listener
	 * @param listener
	 */
	public abstract void setOnCheckedChangeListener(
			org.diffenbach.android.widgets.OnCheckedChangeListener<T> listener);

}