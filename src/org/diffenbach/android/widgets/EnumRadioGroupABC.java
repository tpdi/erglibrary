package org.diffenbach.android.widgets;

import java.util.EnumSet;
import java.util.List;

import org.diffenbach.android.utils.AtomicIntViewIdGenerator;
import org.diffenbach.android.utils.ViewIdGenerator;

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

/**
 * EnumRadioGroupABC is the abstract base class for all EnumRadioGroup specializations.
 *  
 *  EnumRadioGroupABC takes two generic parameters:
 *  	an enum, which happens to be a Curiously Recurring Template (Coplien, 1995)
 *  	and one of its own sub classes, because it is also a Curiously Recurring Template
 *  EnumRadioGroupABC is Curiously Recurring so that chaining methods (which return this)
 *  are type to return the subclass type. Otherwise, chaining will return the base class
 *  type, removing the static type of the derived class.
 *  
 *  A problem exists: we need the most-derived subclass to communicate its type to the base.
 *  This means that derived but not most-derived classes must also take the type parameter, 
 *  if only to propagate it. But that means it's uglier for a client programmer to instantiate the  
 *  "middle" class, as the client programmer now has to supply the type, even though it's the type
 *  being instantiated.  
 *  
 *  We can solve this by getting rid of any instantiable "middle" classes. This is actually good 
 *  practice: concrete classes should usually be leaf classes. But then we lose the ability to 
 *  upcast from EnumRadioGroup (multi) to EnumradioGroup (single), as they are now sibling, not 
 *  child and parent. multi is-a single, in teh sense that it can act as a single. On the othre hand, 
 *  it's not is-a, in that once act to act as multi, it behavior is different than single's.
 *  It "does all that AND MORE."
 *  
 *  So at this point, we have:
 *  	chaining, without needing overrides that only cast
 *  	an intuitive way to refer to EnumRadioGroup *and* its static method and member classes
 *  We don't have:
 *  	 the ability to upcast a multilistener.EnumRadioGroup to an EnumRadioGroup
 *  	
 */

public class EnumRadioGroupABC<T extends Enum<T>, U extends EnumRadioGroupABC<T, U>> 
	extends RadioGroup  {
		

	
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
	public EnumRadioGroupABC(Context context, T defaultValue, int rbNames, int rbLayout) {
		super(context);
		init(context, defaultValue, rbNames, rbLayout);
	}
	
	/**
	 * Ctor that uses a default layout.
	 * @param context
	 * @param defaultValue
	 * @param rbNames
	 */
	public EnumRadioGroupABC(Context context, T defaultValue, int rbNames) {
		this(context, defaultValue, rbNames, -1);
	}
	
	/**
	 * You really shoulb't use the Enum's nams as display values.
	 * Ctor that uses a default layout and the Enums' toString()s as labels.
	 * @param context
	 * @param defaultValue
	 */
	public EnumRadioGroupABC(Context context, T defaultValue) {
		this(context, defaultValue, -1, -1);
	}
	
	/** Ctor used to inflate an XML representation of an EnumRadioGroup.
	 * 
	 * @param context
	 * @param attrs
	 */
	public EnumRadioGroupABC(Context context, AttributeSet attrs) {
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
	
	/* (non-Javadoc)
	 * @see org.diffenbach.android.widgets.IEnumRadioGroup#getDefault()
	 */
	public T getDefault() {
		return defaultValue;
	}
	
	/* (non-Javadoc)
	 * @see org.diffenbach.android.widgets.IEnumRadioGroup#isSetToDefault()
	 */
	public boolean isSetToDefault() {
		return getCheckedValue() == defaultValue;
	}
	
	/* (non-Javadoc)
	 * @see org.diffenbach.android.widgets.IEnumRadioGroup#clearCheck()
	 */
	@Override
	public void clearCheck() {
		check(getViewIdForEnum(defaultValue));
	}
	
	/* (non-Javadoc)
	 * @see org.diffenbach.android.widgets.IEnumRadioGroup#check(int)
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
	
	/* (non-Javadoc)
	 * @see org.diffenbach.android.widgets.IEnumRadioGroup#check(T)
	 */
	public void check(T value) {
		check( getViewIdForEnum(value) );
	}
	
	/* (non-Javadoc)
	 * @see org.diffenbach.android.widgets.IEnumRadioGroup#getCheckedValue()
	 */
	public T getCheckedValue() {
		return resIdToEnumConstant(getCheckedRadioButtonId());
	}
	
	/* (non-Javadoc)
	 * @see org.diffenbach.android.widgets.IEnumRadioGroup#getViewIdForEnum(T)
	 */
	public int getViewIdForEnum(T enumConstant) {
		return enumConstant.ordinal() + idOffset;
	}
	
	/* (non-Javadoc)
	 * @see org.diffenbach.android.widgets.IEnumRadioGroup#findViewByEnum(T)
	 */
	// Convenience function
	public RadioButton findViewByEnum(T enumConstant) {
		return (RadioButton) findViewById(getViewIdForEnum(enumConstant));
	}
	
	/* (non-Javadoc)
	 * @see org.diffenbach.android.widgets.IEnumRadioGroup#values()
	 */
	public T[] values() {
		return getEnumConstants().clone();
	}

	/* (non-Javadoc)
	 * @see org.diffenbach.android.widgets.IEnumRadioGroup#filter(org.diffenbach.android.widgets.EnumRadioGroupABC.DisplayPredicate)
	 */
	@SuppressWarnings("unchecked")
	public U filter( DisplayPredicate<T> pred) {
		for( T ec : getEnumConstants()) {
			findViewByEnum(ec).setVisibility(pred.apply(ec) ? View.VISIBLE : View.GONE);
		}
		
		return (U) this;
	}
	
	/* (non-Javadoc)
	 * @see org.diffenbach.android.widgets.IEnumRadioGroup#filter(java.util.EnumSet)
	 */
	public U filter( EnumSet<T> set) {
		return filter(include(set));
	}
	
	/* (non-Javadoc)
	 * @see org.diffenbach.android.widgets.IEnumRadioGroup#filterNotIn(java.util.EnumSet)
	 */
	public U filterNotIn( EnumSet<T> set) {
		return filter(includeAllBut(set));
	}
	
	/* (non-Javadoc)
	 * @see org.diffenbach.android.widgets.IEnumRadioGroup#setOnCheckedChangeListener(org.diffenbach.android.widgets.OnCheckedChangeListener)
	 */
	public void setOnCheckedChangeListener(org.diffenbach.android.widgets.OnCheckedChangeListener<T> listener) {
		super.setOnCheckedChangeListener(listener);
	}
	
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
	

	/* OnCheckedChangeListener<T extends Enum<T>> is in its own file. 
	 * 
	 */

	
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
