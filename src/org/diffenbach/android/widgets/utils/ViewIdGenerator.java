/**
 * 
 */
package org.diffenbach.android.widgets.utils;

/**
 * @author tpd
 *
 */
public interface ViewIdGenerator {

	/**
	 * Returns the initial id of numberNeeded consecutive increasing Ids.
	 * The next request will return at least (returned + numberNeeded).
	 * In particular, consecutive Ids will not span a counter rollover.
	 * 
	 * @param numberNeeded the number of Ids to reserve.
	 * @return the first Id of numberNeeded reserved Ids
	 */
	int generateViewIds(int numberNeeded);

}

