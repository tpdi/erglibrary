package org.diffenbach.enumradiogroup;

import org.diffenbach.enumradiogroup.EnumRadioGroup.DisplayPredicate;
import org.diffenbach.enumradiogroup.EnumRadioGroup.OnCheckChangedListener;

import android.app.Activity;
import android.os.Bundle;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.TextView;

public class MainActivity extends Activity {
	
	enum Agreed {YES, NO, MAYBE}
	enum Pie {APPLE, CHERRY, POTATO}
	enum Sex {REQUIRED_FIELD, FEMALE, MALE}
	
	EnumRadioGroup<Agreed> programmaticAgreeds;
	EnumRadioGroup<Pie> programmaticPies;
	
	DisplayPredicate<Pie>[] pieFilters;
	int pieFilterOffset = 0;
	
	@SuppressWarnings("unchecked")
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);
		
		addViewToWrapper(R.id.p_agreed_wrapper, programmaticAgreeds = 
				// Add a view with a string array that filters what RadioButtons are visible;
				// a layout for the RadioButtons will be selected based on the
				// orientation of the EnumRadioGroup.
				
				// But using the name array to filter is a poor practice
				// only allowed to make it easier for users creating the Group from XML.
				// In programmatic use, just call filter (see below for an example)...
				
				//              Enum type
				new EnumRadioGroup<Agreed>(
						this, 							// context
						Agreed.NO, 						// the default button we clear to
						R.array.agreed_without_no));	// a list of localized names for the buttons
		
		addViewToWrapper(R.id.p_pies_wrapper, programmaticPies =
				// or add a layout id for the RadioButtons
				 new EnumRadioGroup<Pie>(
						 this,							// context
						 Pie.POTATO,                    // the default button we clear to
						 R.array.pie, 					// a list of localized names for the buttons
						 R.layout.horizontal_radio_button_wrapped) // the button layout
						 
						 // Since filter returns this, we can (essentially) filter at creation,
						 // before we assign to a variable.
					.filter(EnumRadioGroup.includeAllBut(Pie.POTATO))	 
				); 
		
		
		// and then set the EnumRadioGroup's orientation match the layout.
		programmaticPies.setOrientation(LinearLayout.HORIZONTAL);
		
		// See the setUpRadioGroupCallback for setting up callbacks.
		// EnumRadioGroup.findById is a somewhat typesafe way to get an EnumRadioGroup.
		setUpRadioGroupCallback(EnumRadioGroup.findById(this, R.id.agreed1), R.id.agreed1_text);
		setUpRadioGroupCallback(EnumRadioGroup.findById(this, R.id.sex), R.id.sex_text);
		setUpRadioGroupCallback(programmaticAgreeds, R.id.p_agreed_text);
		setUpRadioGroupCallback(programmaticPies, R.id.p_pies_text);
		
		
		// we can make, keep, and reuse references to typed filters 
		// that take the Enum class or Enum constants
		pieFilters = EnumRadioGroup.makeDisplayPredicateArray(
				EnumRadioGroup.includeAll(Pie.class),
				EnumRadioGroup.includeAllBut(Pie.APPLE),
				EnumRadioGroup.includeAllBut(Pie.APPLE, Pie.CHERRY)
				); 
		
		// As filers are typed, this won't work:
		// programmaticAgreeds.filter(pieFilters[0]);
		// Neither will this:
		// programmaticAgreeds.filter(EnumRadioGroup.includeAll(Pie.class));
		// Or this:
		// programmaticAgreeds.filter(EnumRadioGroup.includeAllBut(Pie.APPLE));
		
		TextView pieLabel = new TextView(this);
		pieLabel.setText(R.string.pielabel);
		programmaticPies.addView(pieLabel, 0);
		
	}
	
	// This is a generic method, so it properly handles EnumRadioGroups 
	// parameterized on any Enum type.
	// Adding the <T extends Enum<T>> is the price you pay for type safety.
	private <T extends Enum<T>> void setUpRadioGroupCallback( EnumRadioGroup<T> erg, final int textViewid) {
		erg.setOnCheckedChangeListener( new OnCheckChangedListener<T>() {

			@Override
			public void onCheckedChanged(EnumRadioGroup<T> group, T currentValue, int checkedId) {
				
				// we're given the current value, for most things that's all we'll need
				String currentValueName = currentValue.toString() ;
				
				// Getting the (possibly translated) label is about the only reason
				// to ever get the child RadioButtons
				// If you need to do this, findViewByEnum is a (typed) convenience function:
				// But note the RadioButtons themselves are not generic.
				RadioButton currentValueRadioButton = group.findViewByEnum(currentValue);
				
				String currentValueString = currentValueRadioButton.getText().toString();
				
				((TextView) MainActivity.this.findViewById(textViewid))
					.setText(String.format("%s (%s) (%sdefault) id: %d", 
							currentValueName, currentValueString, 
							group.isSetToDefault() ? "" : "not ", checkedId)); 
			}
		});
	}
	
	// Button callback
	public void clear(View v) {
	
		// Clearing an EnumRadioGroup resets it to the default you
		// specified at construction. So the Group always has a value
		// and that value is always strongly typed.
		
		// You can clear it as a RadioGroup, calling the overridden clearCheck:
		((RadioGroup) findViewById(R.id.agreed1)).clearCheck();
		
		// Or you an get it with the type-inferring function, EnumRadioGroup.findById
		// and clear it using the overridden RadioGroup method:
		EnumRadioGroup.findById(this, R.id.sex).check(-1);
		
		//Preferably, you'd use the type inferring function and clearCheck:
		EnumRadioGroup.findById(this, R.id.sex).clearCheck();
		
		// Or you can set it to any enum constant of its enum type;
		// set it with an enum value, and type-safety
		// means you can't set it to the a non-existing value.
		programmaticAgreeds.check(Agreed.NO);
		
		// And you can get it as a value too:
		Pie selected = programmaticPies.getCheckedValue();
		// for efficiency, we should put Pie.values() in a local, but...
		Pie next = Pie.values()[ (selected.ordinal() + 1) % Pie.values().length];
		programmaticPies.check(next);
	}
	
	// Button callback
	public void changeFilter(View v) {
		// we can filter a group after its created
		pieFilterOffset = (pieFilterOffset + 1) % pieFilters.length;
		EnumRadioGroup.DisplayPredicate<Pie> predicate = pieFilters[pieFilterOffset];
		((TextView) findViewById(R.id.p_pies_includes)).setText(predicate.toString());
		programmaticPies.filter(predicate);
	}

	// just a convenience function, to insert a programmatic EnumRadioGroup
	private void addViewToWrapper( int parentId, View child) {
		ViewGroup parent = (ViewGroup) findViewById(parentId);
		parent.addView(child, 0);
	}
	

}
