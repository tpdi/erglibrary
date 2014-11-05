package org.diffenbach.android.widgets.ui;

import java.util.ArrayList;
import java.util.EnumSet;
import java.util.List;

import org.diffenbach.android.widgets.R;
import org.diffenbach.android.widgets.utils.AtomicIntViewIdGenerator;
import org.diffenbach.android.widgets.utils.ViewIdGenerator;

import android.app.Activity;
import android.content.Context;
import android.content.res.TypedArray;
import android.util.AttributeSet;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.RadioButton;
import android.widget.RadioGroup;


/**
 * A class to show Enum constants as radio buttons.
 * @author TP Diffenbach
 *
 * @param <T> An Enum class (<T extends Enum<T>>)
 */

public class EnumRadioGroup<T extends Enum<T>> extends RadioGroup {
		
	/** 
	 * Wraps findById in a cast
	 * @param a an Activity
	 * @param id an id of an EnumRadioGroup
	 * @return a View that we hope is an EnumRadioGroup, or throw a ClassCastException
	 */
	
	@SuppressWarnings("unchecked")
	public static <E extends Enum<E>> EnumRadioGroup<E> findByIdE( Activity a, int id) {
		return (EnumRadioGroup<E>) a.findViewById(id);
	}
	
	
	@SuppressWarnings("unchecked")
	public static <U extends EnumRadioGroup<?>> U findById( Activity a, int id) {
		return (U) a.findViewById(id);
	}
	
	/** 
	 * Wraps findById in a cast
	 * @param v a View
	 * @param id an id of an EnumRadioGroup
	 * @return a child View that we hope is an EnumRadioGroup, or throw ClassCastException
	 */
	@SuppressWarnings("unchecked")
	public static <E extends Enum<E>> EnumRadioGroup<E> findByIdE( View v, int id) {
		return (EnumRadioGroup<E>) v.findViewById(id);
	}
	
	@SuppressWarnings("unchecked")
	public static <U extends EnumRadioGroup<?>> U findById( View v, int id) {
		return (U) v.findViewById(id);
	}
	
	/**
	 * A test for XML dummies.
	 * @param v a View
	 * @return true if the View is an XML dummy which we will remove.
	 */
	public static boolean isDummy(View v) {
		return v instanceof RadioButton;
	}

	private static final String CLASS_S_NOT_FOUND = "Class \'%s\' not found ";
	private static final String EXC_MSG_UNEQUAL_NAMES = "%d names for %d enum constants; must be equal";
	
	// non-final so we can replace it (via reflection) during testing
	private static ViewIdGenerator viewIdGenerator = AtomicIntViewIdGenerator.INSTANCE;

	protected T defaultValue;
	// While we can get them with defaultValue.getDeclaringClass().getEnumConstants(),
	// it's a bit of work. Let's be timely.
	private T[] enumConstants;
	// the id of the RadioButton with ordinal() == 0
	// all other RadioButton ids are consecutive increasing
	protected int idOffset;  
	// so we can chain listeners, we need to keep a copy of the  listener;
	
	/**
	 * Ctor that takes:
	 * @param context the EnumRadioGroup's context
	 * @param defaultValue the checked value of the  group if no other RadioButton is checked.
	 * @param rbNames an resource id of an array of strings to use as the button's labels 
	 * @param rbLayout the layoout to use for each radio button in the group
	 */
	public EnumRadioGroup(Context context, T defaultValue, int rbNames, int rbLayout) {
		super(context);
		init(context, defaultValue, rbNames, rbLayout);
	}
	
	/**
	 * Ctor that uses a default layout.
	 * @param context
	 * @param defaultValue
	 * @param rbNames
	 */
	public EnumRadioGroup(Context context, T defaultValue, int rbNames) {
		this(context, defaultValue, rbNames, -1);
	}
	
	/**
	 * You really shoulb't use the Enum's nams as display values.
	 * Ctor that uses a default layout and the Enums' toString()s as labels.
	 * @param context
	 * @param defaultValue
	 */
	public EnumRadioGroup(Context context, T defaultValue) {
		this(context, defaultValue, -1, -1);
	}
	
	/** Ctor used to inflate an XML representation of an EnumRadioGroup.
	 * 
	 * @param context
	 * @param attrs
	 */
	public EnumRadioGroup(Context context, AttributeSet attrs) {
		super(context, attrs);
		
		if(isInEditMode()) {
			return; //init(context, (T) Sample.IN, -1, -1);
		} else {
			TypedArray a = context.obtainStyledAttributes(attrs, R.styleable.EnumRadioGroup);
			String enumClassName = a.getString(R.styleable.EnumRadioGroup_enumClassName);
			String enumDefaultValue = a.getString(R.styleable.EnumRadioGroup_enumDefaultValueString);
			int rb_layout = a.getResourceId(R.styleable.EnumRadioGroup_radioButtonLayout, -1);
			int rb_names = a.getResourceId(R.styleable.EnumRadioGroup_radioButtonNames, -1);
			boolean filterFirst = ! a.getBoolean(R.styleable.EnumRadioGroup_enumDefaultIsShown, true);
			a.recycle();
			init(context, enumClassName, enumDefaultValue, rb_names, rb_layout, filterFirst);
		}
	}
	
	/**
	 * Return the default enum constant for this Radio Group, as set in the ctor.
	 * @return
	 */
	public T getDefault() {
		return defaultValue;
	}
	
	/**
	 * Determines if the checked button in this group is the default value set in the ctor.
	 * @return true iff the checked button in this group is the default value
	 */
	public boolean isSetToDefault() {
		return getCheckedValue() == defaultValue;
	}
	
	/**
	 * Resets the checked RadioButton to be the default value.
	 */
	@Override
	public void clearCheck() {
		check(getViewIdForEnum(defaultValue));
	}
	
	/**
	 * Please prefer using check(T value).
	 * Sets the checked button to be the RadioButton with the  given id, 
	 * IF that button is a member of the group; if not, throws IllegalArgumentException
	 * If the id is -1, checks the RadioButton corresponding to the default passed in the ctor. 
	 * @see android.widget.RadioGroup#check(int)
	 */
	@Override
	public void check(int id) {
		if(id == -1) {
			clearCheck();
		} else if(isChildRadioButtonIdValid(id)) {
			super.check(id);
		} else {
			throw new IllegalArgumentException("Argument to \'check\' must be in range -1 to count of enum's constants -1");
		}
	}
	
	/**
	 * Checks the button corresponding to the enum constant passed.
	 * @param value the enum constant to check
	 */
	public void check(T value) {
		check( getViewIdForEnum(value) );
	}
	
	/**
	 * Gets the enum value corresponding to the currently checked RadioButton.
	 * @return the enum constant corresponding to the currently checked button.
	 * Note that if you override this to provide non-contiguous ids, 
	 * you'll also need to override isChildRadioButtonIdValid
	 */
	public T getCheckedValue() {
		return resIdToEnumConstant(getCheckedRadioButtonId());
	}
	
	/**
	 * Returns true if the the currently selected button is visible.
	 * @return true if the the currently selected button is visible.
	 */
	public boolean isCheckedValueVisible() {
		return findCheckedRadioButton().getVisibility() == View.VISIBLE;
	}
	
	/**
	 * Returns the id of the radioButton in the group corresponding to the enum constant passed.
	 * @param enumConstant
	 * @return id of the radioButton in the group corresponding to the enum constant passed.
	 */
	public int getViewIdForEnum(T enumConstant) {
		return enumConstant.ordinal() + idOffset;
	}
	
	/**
	 * return the RadioButton  corresponding to the passed-in enum constant.
	 * @param enumConstant
	 * @return
	 */
	// Convenience function
	public RadioButton findViewByEnum(T enumConstant) {
		return (RadioButton) findViewById(getViewIdForEnum(enumConstant));
	}
	
	/**
	 * Return the checked RadioButton
	 * @return
	 */
	public RadioButton findCheckedRadioButton() {
		return (RadioButton) findViewById(getCheckedRadioButtonId());
	}
	
	public T[] values() {
		return getEnumConstants().clone();
	}

	/**
	 * Displays only buttons  corresponding to enum constants that pass the filter
	 * @param pred a {@DisplayPredicate} for the Enum<T>s
	 * @return this, for chaining
	 * Template method to return derived type if called on derived type
	 */
	@SuppressWarnings("unchecked")
	public <U extends EnumRadioGroup<T>> U filter( DisplayPredicate<T> pred) {
		for( T ec : getEnumConstants()) {
			findViewByEnum(ec).setVisibility(pred.apply(ec) ? View.VISIBLE : View.GONE);
		}
		
		return (U) this;
	}
	
	/**
	 * Display only buttons corresponding to enums in the given EnumSet.
	 * @param set am EnumSet<T>
	 * @return this, for chaining
	 * Template method to return derived type if called on derived type
	 */
	public <U extends EnumRadioGroup<T>> U filter( EnumSet<T> set) {
		return filter(include(set));
	}
	
	/**
	 * Display only buttons  corresponding to enums not in the given EnumSet.
	 * @param set am EnumSet<T>
	 * @return this, for chaining
	 * Template method to return derived type if called on derived type
	 */
	public <U extends EnumRadioGroup<T>> U filterNotIn( EnumSet<T> set) {
		return filter(includeAllBut(set));
	}
	
	/**
	 * Set the (generic parameterized) Change Listener.
	 * Chains to any existing listener
	 * @param listener
	 * @return this, for chaining
	 * Template method to return derived type if called on derived type
	 */
	@SuppressWarnings("unchecked")
	public <U extends EnumRadioGroup<T>> U setOnCheckedChangeListener(OnCheckedChangeListener<T> listener) {
		super.setOnCheckedChangeListener(listener);
		return (U) this;
	}
	
	/**
	 * Protected methods
	 */
	
	/**
	 * Convenience function to produce a Enum's names if the caller doesn't pass us a list of names.
	 * @param enumConstants
	 * @return an array of [toString called on each enum constant]
	 */
	protected String[] getEnumNames(T[] enumConstants) {
		String[] ret = new String[enumConstants.length];
		int offset = 0 ;
		for( T ec : enumConstants) {
			ret[offset] = ec.toString();
			++offset;
		}
		return ret;
	}
	
	/**
	 * The init called form the XML
	 * @param context
	 * @param ecn name of enum class
	 * @param dvn name of default enum value
	 * @param rbNames resource id of human-readable names of enums, or -1
	 * @param rbLayout resource id of layout for radio buttons, or -1
	 * @param filterFirst true if the first enum (ordinal() == 0) should be filtered out
	 */
	@SuppressWarnings("unchecked")
	protected void init(Context context, String ecn, String dvn, int rbNames, int rbLayout,
			boolean filterFirst) {
		try {
			Log.i(getClass().getName(), String.format("Enum Class %s,  Enum Value: %s", ecn, dvn));
			Class<T> ec = (Class<T>) Class.forName(ecn);
			init(context, Enum.valueOf( ec, dvn), rbNames, rbLayout);
			if(filterFirst) {
				filter( includeAllBut(defaultValue));
			}
		} catch (ClassNotFoundException e) {
			// convert to unchecked exception
			throw new IllegalArgumentException(String.format(CLASS_S_NOT_FOUND, ecn), e);
		}
	}
	
	/**
	 * The init called from the all ctors. For each enum conctant, make a RadioButton.
	 * @param context
	 * @param defaultValue the default enum value
	 * @param rbNames resource id of human-readable names of enums, or -1
	 * @param rbLayout resource id of human-readable names of enums, or -1
	 */
	protected void init(Context context, T defaultValue, int rbNames, int rbLayout ) {
		
		this.defaultValue = defaultValue;
		this.enumConstants = defaultValue.getDeclaringClass().getEnumConstants();
		this.idOffset = viewIdGenerator.generateViewIds(enumConstants.length);
		
		if (rbLayout == -1) {
			rbLayout = R.layout.wrapped_radio_button ;
		}
		
		String[] names = rbNames != -1 ? context.getResources().getStringArray(rbNames)
			: getEnumNames(enumConstants);

		
		if(names.length != enumConstants.length) {
			throw new IllegalArgumentException(
					String.format(EXC_MSG_UNEQUAL_NAMES, names.length, enumConstants.length));
		}
		
		LayoutInflater inflater = LayoutInflater.from(context);
		
		int offset = 0;
		for( T ec : enumConstants) {
			// annoyingly, to get layoutparams, we need to inflate this way
			RadioButton rb = (RadioButton) inflater.inflate(rbLayout, this, false);
			
			int id = idOffset + offset;
			rb.setId(id);
			
			String name = names[offset];
			if(name.length() > 0 ) rb.setText(name);
			else rb.setVisibility(View.GONE); //poor XML-man's filter
			
			// bypass RadioGroups's special addView, so we don't have to muck with LayoutParams
	
			addView(rb);
			
			// because we bypassed RadioGroups's special addView, we have to check by hand
			if( ec == defaultValue) {
				//rb.setChecked(true);
				check(id);
			}
			++offset;
		}
	}
	
	/**
	 * we need to fix the position of any XML children
	 * we move any XML children to precede before our buttons
	 * until we see a dummy
	 * and we remove all dummies
	 */
	@Override
	protected void onFinishInflate() {
		super.onFinishInflate();
		
		if( ! isInEditMode()) {
			int childCount = getChildCount();
			boolean foundDummy = false;
			for( int xmlChild = enumConstants.length; xmlChild < childCount; ++xmlChild) {
				View child = getChildAt(xmlChild);
				if(isDummy(child)) {
					removeView(child);
					--xmlChild;
					--childCount;
					foundDummy = true;
				} else if(!foundDummy) {
					removeView(child);
					addView(child, xmlChild - enumConstants.length);
				}
			}
		}
	}
	
	
	/**
	 *  The methods resIdToEnumConstant, isChildRadioButtonIdValid and getViewIdForEnum(enumConstant)
	 *  establish the bi-directional enum constant <-> radiobutton mapping.
	 * They require that radiobutton ids be contiguous.
	 * If you change one, change all.
	 * 
	 * @param resId a child radioButton id
	 * @return the corresponding enum constant
	 */
	protected T resIdToEnumConstant(int resId) {
		// this was a linear search. Ick.
		// then it was a binary search. Fortunately we have monotonic-increasing ids.
		// now it's a subtraction!
		return enumConstants[resId - idOffset];
	}
	
	/**
	 * Check if an id is of a child radiobutton corresponding to an enum constant.
	 * See documentation for resIdToEnumConstant
	 * @param id the id
	 * @return true if it corresponds to a enum constant radiobutton
	 */
	protected boolean isChildRadioButtonIdValid(int id) {
		return id >= idOffset && id < idOffset + getEnumConstants().length;
	}
	
	/**
	 * Return the enum constants that this EnumRadioGroup displays.
	 * @return
	 */
	protected T[] getEnumConstants() {
		return enumConstants;
	}

	
	/** helper classes 
	 * 
	 * @author tpd
	 * 
	 */
	
	public static abstract class OnCheckedChangeListener<T extends Enum<T>> implements RadioGroup.OnCheckedChangeListener {
		
		public abstract void onCheckedChanged(EnumRadioGroup<T> group, T currentValue, int checkedId);
		
		@SuppressWarnings("unchecked")
		@Override
		public void onCheckedChanged(RadioGroup group, int checkedId) {
			EnumRadioGroup<T> erg = (EnumRadioGroup<T>) group;
			onCheckedChanged(erg, erg.getCheckedValue(), checkedId);
		}
		
		public MultiOnCheckedChangeListener<T> toMulti( OnCheckedChangeListener<T> added) {
			return new MultiOnCheckedChangeListener<T>(this, added);
		}
	
		
		private static class MultiOnCheckedChangeListener<T extends Enum<T>> extends OnCheckedChangeListener<T> {
			private final List<OnCheckedChangeListener<T>> listeners;
	
			protected MultiOnCheckedChangeListener( OnCheckedChangeListener<T> l1, OnCheckedChangeListener<T> l2) {
				super();
				this.listeners = new ArrayList<OnCheckedChangeListener<T>>();
				listeners.add(l1);
				listeners.add(l2);
			}
	
			public MultiOnCheckedChangeListener<T> toMulti( OnCheckedChangeListener<T> added) {
				listeners.add(added);
				return this;
			}
	
			@Override
			public void onCheckedChanged(EnumRadioGroup<T> group, T currentValue, int checkedId) {
				for( OnCheckedChangeListener<T> listener : listeners) {
					listener.onCheckedChanged(group, currentValue, checkedId);
				}
				
			}
		}
	}



	
	/**
	 * Predicate for setting the display
	 * @author TP Diffenbach
	 *
	 * @param <T>
	 */
	public interface DisplayPredicate <T extends Enum<T>> {
		boolean apply(T enumConstant);
	}
	
	// Alas, this works, but not with the hack for making arrays...
	// no, this is better, as it's more strongly typed
	@SuppressWarnings("rawtypes")
	public static final DisplayPredicate INCLUDE_ALL = new DisplayPredicate() {

		@Override
		public boolean apply(Enum enumConstant) {
			return true;
		}
		
		@Override
		public String toString() {
			return "Predicate includes all";
		};

	};
	
	/**
	 * Factory functions to create and return filter predicates.
	 * @param notused
	 * @return
	 */
	// That notused class is just for type inference
	@SuppressWarnings("unchecked")
	public static <T extends Enum<T>> DisplayPredicate<T> includeAll(Class<T> notused) {
		return INCLUDE_ALL;
	}
	
	@SuppressWarnings("unchecked")
	public static <T extends Enum<T>> DisplayPredicate<T> includeAllBut(List<T> exclude) {
		return exclude.isEmpty() 
				? INCLUDE_ALL //EnumSet can't cope with empty Collections that are not EnumSets
				: new ExcludeEnumSetPredicate<T>(EnumSet.copyOf(exclude));
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
		
	/*private static class IncludeAllBut<T extends Enum<T>> implements DisplayPredicate<T> {
		private List<T> exclude;
		
		private IncludeAllBut(List<T> exclude) {
			this.exclude = exclude;
		}
		
		@Override
		public boolean apply( T enumConstant) {
			return ! exclude.contains(enumConstant);
		}
		
		@Override
		public String toString() {
			return "Predicate includes all but " + exclude.toString();
		};
	}*/
	
	public static class ExcludeEnumSetPredicate<T extends Enum<T>> implements DisplayPredicate<T> {
		private EnumSet<T> exclude;
		
		public ExcludeEnumSetPredicate(EnumSet<T> eset) {
			this.exclude = eset;
		}

		@Override
		public boolean apply(T enumConstant) {
			return ! exclude.contains(enumConstant);
		}
		
		@Override
		public String toString() {
			return "Predicate includes all but " + exclude.toString();
		};
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
