package org.diffenbach.enumradiogroup;

/* Copyright (C) 2006 The Android Open Source Project*/

import java.util.concurrent.atomic.AtomicInteger;

public class Pre17Support {

	//taken from  View.java
	static public AtomicInteger sNextGeneratedId = new AtomicInteger(0x00FFFFFF/4 + (4-0x00FFFFFF%4));
	
    public static int generateViewId() {
        for (;;) {
            final int result = sNextGeneratedId.get();
            // aapt-generated IDs have the high byte nonzero; clamp to the range under that.
            int newValue = result + 1;
            if (newValue > 0x00FFFFFF) newValue = 1; // Roll over to 1, not 0.
            if (sNextGeneratedId.compareAndSet(result, newValue)) {
                return result;
            }
        }
    }
    
    public static int generateViewIds(int numberNeeded) {
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
