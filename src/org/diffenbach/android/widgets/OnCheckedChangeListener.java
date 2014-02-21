package org.diffenbach.android.widgets;

import java.util.ArrayList;
import java.util.List;

import org.diffenbach.android.widgets.EnumRadioGroup;

import android.widget.RadioGroup;

	public abstract class OnCheckedChangeListener<T extends Enum<T>> implements RadioGroup.OnCheckedChangeListener {
		
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

