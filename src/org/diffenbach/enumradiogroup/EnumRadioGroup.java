package org.diffenbach.enumradiogroup;

import java.util.Arrays;
import java.util.EnumSet;
import java.util.List;

import android.app.Activity;
import android.content.Context;
import android.content.res.TypedArray;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.RadioButton;
import android.widget.RadioGroup;

public class EnumRadioGroup<T extends Enum<T>> extends RadioGroup {
		
	/** 
	 * Wraps findById in a cast
	 * @param a an Activity
	 * @param id an id of an EnumRadioGroup
	 * @return a View that we hope is an EnumRadioGroup, or ClasscastException
	 */
	public static EnumRadioGroup<?> findById( Activity a, int id) {
		return (EnumRadioGroup<?>) a.findViewById(id);
	}
	
	/** 
	 * Wraps findById in a cast
	 * @param v a View
	 * @param id an id of an EnumRadioGroup
	 * @return a child View that we hope is an EnumRadioGroup, or ClasscastException
	 */
	public static EnumRadioGroup<?> findById( View v, int id) {
		return (EnumRadioGroup<?>) v.findViewById(id);
	}
	
	/**
	 * A test for XML dummies.
	 * @param v a View
	 * @return true if the View is an XML dummy which we will remove.
	 */
	public static boolean isDummy(View v) {
		return v instanceof RadioButton && "DUMMY".equals(v.getTag());
	}

	private static final String CLASS_S_NOT_FOUND = "Class \'%s\' not found ";
	private static final String EXC_MSG_UNEQUAL_NAMES = "%d names for %d enum constants; must be equal";
	
	// non-final so we can replace it (via reflection) during testing
	private static ViewIdGenerator viewIdGenerator = AtomicIntViewIdGenerator.INSTANCE;

	protected T defaultValue;
	// While we can get them with defaultValue.getDeclaringClass().getEnumConstants(),
	// it's a bit of work. Let's be timely.
	private T[] enumConstants;
	protected int idOffset;
	
	
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
	 * Return the default value set in the ctor.
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
	 * Sets the check to the default value.
	 */
	@Override
	public void clearCheck() {
		check(getViewIdForEnum(defaultValue));
	}
	
	/**
	 * Sets the checked button to be the RadioButton with the  given id, 
	 * IF that button is a member of the group; if not, throws IllegalArgumentException
	 * If the id is -1, checks the RadioButton corresponding to the default passed in the ctor. 
	 * @see android.widget.RadioGroup#check(int)
	 */
	@Override
	public void check(int id) {
		if(id == -1) clearCheck();
		else if(id < idOffset || id >= idOffset + getEnumConstants().length) {
			throw new IllegalArgumentException("Argument to \'check\' must be in range -1 to count of enum's constants -1");
		}
		else super.check(id);
	}
	
	/**
	 * Checks the button corresponding to the enum constant passed.
	 * @param value the enum constant to check
	 */
	public void check(T value) {
		check( getViewIdForEnum(value) );
	}
	
	/**
	 * Gets the enum value corresponding to the currently check RadioButton.
	 * @return the enum value of the currently checked button
	 */
	public T getCheckedValue() {
		return resIdToEnumConstant(getCheckedRadioButtonId());
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
	 * Displays only buttons that pass the filter
	 * @param pred a {@DisplayPredicate} for the Enum<T>s
	 * @return this, for chaining
	 */
	public EnumRadioGroup<T> filter( DisplayPredicate<T> pred) {
		for( T ec : getEnumConstants()) {
			findViewByEnum(ec).setVisibility(pred.apply(ec) ? View.VISIBLE : View.GONE);
		}
		
		return this;
	}
	
	public EnumRadioGroup<T> filter( EnumSet<T> set) {
		for( T ec : getEnumConstants()) {
			findViewByEnum(ec).setVisibility(set.contains(ec)? View.VISIBLE : View.GONE);
		}
		
		return this;
	}
	
	
	// Convenience function
	public RadioButton findViewByEnum(T enumConstant) {
		return (RadioButton) findViewById(getViewIdForEnum(enumConstant));
	}

	public void setOnCheckedChangeListener(OnCheckChangedListener<T> listener) {
		super.setOnCheckedChangeListener(listener);
	}
	
	protected String[] getEnumNames(T[] enumConstants) {
		String[] ret = new String[enumConstants.length];
		int offset = 0 ;
		for( T ec : enumConstants) {
			ret[offset] = ec.toString();
			++offset;
		}
		return ret;
	}
	
	@SuppressWarnings("unchecked")
	protected void init(Context context, String ecn, String dvn, int rbNames, int rbLayout,
			boolean filterFirst) {
		try {
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
	
	protected void init(Context context, T defaultValue, int rbNames, int rbLayout ) {
		
		this.defaultValue = defaultValue;
		this.enumConstants = defaultValue.getDeclaringClass().getEnumConstants();
		this.idOffset = viewIdGenerator.generateViewIds(enumConstants.length);
		
		if (rbLayout == -1) {
			rbLayout = getOrientation() == LinearLayout.VERTICAL 
					? R.layout.horizontal_wrapped_radio_button : R.layout.horizontal_radio_button;
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
	

	// we 'll need to fix the position of any XML children
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
	
	protected T resIdToEnumConstant(int resId) {
		// this was a linear search. Ick.
		// then it was a binary search. Fortunately we have monotonic-increasing ids.
		// now it's a subtraction!
		return enumConstants[resId - idOffset];
	}
	
	private T[] getEnumConstants() {
		return enumConstants;
	}

	
	/** helper classes 
	 * 
	 * @author tpd
	 * 
	 */
	
	// We give this the "EGR" prefixed name so anyone using it won't
	// have to disambiguate from  RadioGroup.OnCheckedChangeListener...
	public interface ERGOnCheckChangedListener<T extends Enum<T>> extends RadioGroup.OnCheckedChangeListener {
		 void onCheckedChanged(EnumRadioGroup<T> group, T currentValue, int checkedId);
	}
	
	
	// ...but mostly to save the un-prefixed name because most users will want to use this one.
	public static abstract class OnCheckChangedListener<T extends Enum<T>> 
		implements ERGOnCheckChangedListener<T> {

		@SuppressWarnings("unchecked")
		@Override
		public void onCheckedChanged(RadioGroup group, int checkedId) {
			EnumRadioGroup<T> erg = (EnumRadioGroup<T>) group;
			onCheckedChanged(erg, erg.getCheckedValue(), checkedId);
			
		}
		
		public abstract void onCheckedChanged(EnumRadioGroup<T> group, T currentValue, int checkedId);
		
	}
	
	
	public interface DisplayPredicate <T extends Enum<T>> {
		boolean apply(T enumConstant);
	}
	
	// Alas, this works, but not with the hack for making arrays...
	// no, this is better, as it's more strongly typed
	@SuppressWarnings("rawtypes")
	private static final DisplayPredicate INCLUDE_ALL = new DisplayPredicate() {

		@Override
		public boolean apply(Enum enumConstant) {
			return true;
		}
		
		@Override
		public String toString() {
			return "Predicate includes all";
		};

	};
	
	
	// That notused class is just for type inference
	@SuppressWarnings("unchecked")
	public static <T extends Enum<T>> DisplayPredicate<T> includeAll(Class<T> notused) {
		return INCLUDE_ALL;
	}
	
	public static <T extends Enum<T>> DisplayPredicate<T> includeAllBut(List<T> exclude) {
		return new ExcludeEnumSetPredicate<T>(EnumSet.copyOf(exclude));
	}
	
	public static <T extends Enum<T>> DisplayPredicate<T> includeAllBut(T first, T... exclude) {
		return new ExcludeEnumSetPredicate<T>(EnumSet.of(first, exclude));
	}
	
	public static <T extends Enum<T>> DisplayPredicate<T> includeAll(EnumSet<T> eset) {
		return new ExcludeEnumSetPredicate<T>(EnumSet.complementOf(eset));
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
	
	private static class ExcludeEnumSetPredicate<T extends Enum<T>> implements DisplayPredicate<T> {
		private EnumSet<T> exclude;
		
		private ExcludeEnumSetPredicate(EnumSet<T> eset) {
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
	
	
	// Hello! I am a hack to allow the creation of generic array which are otherwise disallowed.
	public static <T extends Enum<T>> DisplayPredicate<T>[] makeDisplayPredicateArray( DisplayPredicate<T>... dps) {
		return dps;
	}
	
}
