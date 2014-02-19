package org.diffenbach.android.utils;

/* Based on code Copyright (C) 2006 The Android Open Source Project*/

import java.util.concurrent.atomic.AtomicInteger;


public class AtomicIntViewIdGenerator implements ViewIdGenerator {

	public static final int initialValue = 0x00FFFFFF/4 + (4-0x00FFFFFF%4);
	
	//taken from  View.java
	static protected AtomicInteger sNextGeneratedId = new AtomicInteger(initialValue);
	
	public static AtomicIntViewIdGenerator INSTANCE = new AtomicIntViewIdGenerator();
	 
	// no need to make this private, if anyone makes one, they all use the same AtomicInteger
	
	// We need our ids to be monotonic increasing
	// and there's a very small chance that generateViewId() will rollover.
	// We can't sort them increasing, because that will scramble our
	// ordinal -> id lookup.
	// So we're going to generate them all at once
	// and keep doing it until we are monotonic increasing.
	// No, even better,consecutive!
	// Even better would be starting from zero, but android will recycle views.
	// Make it non-static so it can implement an interface
	// make it an interface so it can be injected or not for testing
    public int generateViewIds(int numberNeeded) {
        for (;;) {
            final int result = sNextGeneratedId.get();
            // aapt-generated IDs have the high byte nonzero; clamp to the range under that.
            int newValue = result + 1;
            if (newValue + numberNeeded - 1 > 0x00FFFFFF) newValue = 1; // Roll over to 1, not 0.
            if (sNextGeneratedId.compareAndSet(result, newValue + numberNeeded - 1)) {
                return result;
            }
        }
    }

}
