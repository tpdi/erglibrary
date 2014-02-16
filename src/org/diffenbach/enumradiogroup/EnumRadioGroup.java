package org.diffenbach.enumradiogroup;

import java.util.Arrays;
import java.util.List;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Context;
import android.content.res.TypedArray;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.RadioButton;
import android.widget.RadioGroup;

public class EnumRadioGroup<T extends Enum<T>> extends RadioGroup {
	
	private enum Sample { IN, EDIT, MODE }
	
	public static EnumRadioGroup<?> findById( Activity a, int id) {
		return (EnumRadioGroup<?>) a.findViewById(id);
	}
	
	public static EnumRadioGroup<?> findById( View v, int id) {
		return (EnumRadioGroup<?>) v.findViewById(id);
	}

	private static final String CLASS_S_NOT_FOUND = "Class \'%s\' not found ";
	private static final String EXC_MSG_UNEQUAL_NAMES = "%d names for %d enum constants; must be equal";

	T defaultValue;
	// While we can get them with defaultValue.getDeclaringClass().getEnumConstants(),
	// it's a bit of work. Let's be timely.
	T[] enumConstants;
	int idOffset;
	
	
	public EnumRadioGroup(Context context, T defaultValue, int rbNames, int rbLayout) {
		super(context);
		init(context, defaultValue, rbNames, rbLayout);
	}
	
	public EnumRadioGroup(Context context, T defaultValue, int rbNames) {
		this(context, defaultValue, rbNames, -1);
	}
	
	public EnumRadioGroup(Context context, T defaultValue) {
		this(context, defaultValue, -1, -1);
	}
	
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
	
	public T getDefault() {
		return defaultValue;
	}
	
	public boolean isSetToDefault() {
		return getCheckedValue() == defaultValue;
	}
	
	@Override
	public void clearCheck() {
		check(getViewIdForEnum(defaultValue));
	}
	
	@Override
	public void check(int id) {
		if(id == -1) clearCheck();
		else if(id < idOffset || id >= idOffset + getEnumConstants().length) {
			throw new IllegalArgumentException("Argument to \'check\' must be in range -1 to count of enum's constants -1");
		}
		else super.check(id);
	}
	
	public void check(T value) {
		check( getViewIdForEnum(value) );
	}
	
	public T getCheckedValue() {
		return resIdToEnumConstant(getCheckedRadioButtonId());
	}

	public String[] getEnumNames(T[] enumConstants) {
		String[] ret = new String[enumConstants.length];
		int offset = 0 ;
		for( T ec : enumConstants) {
			ret[offset] = ec.toString();
			++offset;
		}
		return ret;
	}
	
	public int getViewIdForEnum(T enumConstant) {
		return enumConstant.ordinal() + idOffset;
	}
	
	public EnumRadioGroup<T> filter( DisplayPredicate<T> pred) {
		for( T ec : getEnumConstants()) {
			findViewByEnum(ec).setVisibility(pred.display(ec) ? View.VISIBLE : View.GONE);
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
		this.idOffset = Pre17Support.generateViewIds(enumConstants.length);
		
		if (rbLayout == -1) {
			rbLayout = getOrientation() == LinearLayout.VERTICAL 
					? R.layout.vertical_radio_button : R.layout.horizontal_radio_button;
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
		// TODO Auto-generated method stub
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
				} else if( !foundDummy){
					removeView(child);
					addView(child, xmlChild - enumConstants.length);
				}
			}
		}
	}
	
	private boolean isDummy(View v) {
		return v instanceof RadioButton && "DUMMY".equals(v.getTag());
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

	
	
	public static abstract class OnCheckChangedListener<T extends Enum<T>> implements RadioGroup.OnCheckedChangeListener {

		@SuppressWarnings("unchecked")
		@Override
		public void onCheckedChanged(RadioGroup group, int checkedId) {
			EnumRadioGroup<T> erg = (EnumRadioGroup<T>) group;
			onCheckedChanged(erg, erg.getCheckedValue(), checkedId);
			
		}
		
		public abstract void onCheckedChanged(EnumRadioGroup<T> group, T currentValue, int checkedId);
		
	}
	
	
	public interface DisplayPredicate <T extends Enum<T>> {
		boolean display(T enumConstant);
	}
	
	//Alas, this works, but not with the hack for making arrays...
	@SuppressWarnings("rawtypes")
	private static final DisplayPredicate INCLUDE_ALL = new DisplayPredicate() {

		@Override
		public boolean display(Enum enumConstant) {
			return true;
		}
		
		@Override
		public String toString() {
			return "Predicate includes all";
		};

	};
	
	
	@SuppressWarnings("unchecked")
	public static <T extends Enum<T>> DisplayPredicate<T> includeAll(Class<T> notused) {
		return INCLUDE_ALL;
	}
	
	public static <T extends Enum<T>> DisplayPredicate<T> includeAllBut(List<T> exclude) {
		return new IncludeAllBut<T>(exclude);
	}
	
	public static <T extends Enum<T>> DisplayPredicate<T> includeAllBut(T... exclude) {
		return new IncludeAllBut<T>(Arrays.asList(exclude));
	}
	
	/*
	private static class IncludeAll<T extends Enum<T>> implements DisplayPredicate<T> {
		
		@Override
		public boolean display( T enumConstant) {
			return true;
		}
	}*/
	
	private static class IncludeAllBut<T extends Enum<T>> implements DisplayPredicate<T> {
		private List<T> exclude;
		
		private IncludeAllBut(List<T> exclude) {
			this.exclude = exclude;
		}
		
		@Override
		public boolean display( T enumConstant) {
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
