package de.bitub.proitbau.common.versioning.util;

import java.io.Serializable;
import java.util.Comparator;

import de.bitub.proitbau.common.versioning.compare_results.FieldResult;

/**
 * 
 * @author T. Reinhardt
 * 
 */
public class OrderIndexComparator implements Comparator<FieldResult>, Serializable {
	
	private static final long serialVersionUID = -437588856495453671L;
	
	@Override
	public int compare(final FieldResult firstFieldResult, final FieldResult secondFieldResult) {
		if (firstFieldResult.getOrderIndex() == secondFieldResult.getOrderIndex()) {
			return 0;
		}
		if (firstFieldResult.getOrderIndex() < secondFieldResult.getOrderIndex()) {
			return -1;
		}
		return 1;
	}
	
}
