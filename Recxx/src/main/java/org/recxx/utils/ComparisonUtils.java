package org.recxx.utils;

import java.math.BigDecimal;
import java.math.RoundingMode;

import org.apache.commons.beanutils.ConvertUtils;
import org.recxx.domain.ComparisonResult;

public class ComparisonUtils {
	
	public static final BigDecimal DEFAULT_TOLERANCE_PERCENTAGE = BigDecimal.ZERO;
	public static final BigDecimal DEFAULT_SMALLEST_ABSOLUTE_VALUE = BigDecimal.valueOf(0.00001);

	public static BigDecimal absoluteDifference(BigDecimal o1, BigDecimal o2) {
		return o1.subtract(o2).abs();
	}

	public static BigDecimal percentageDifference(BigDecimal o1, BigDecimal o2) {
		BigDecimal original = o1.compareTo( BigDecimal.ZERO ) == 0  ? o2 : o1;
		return (o2.subtract(o1)).divide(original, 6, RoundingMode.HALF_UP);
	}

	public static BigDecimal percentageMatch(Number o1, Number o2) {
		return percentageMatch((BigDecimal)ConvertUtils.convert(o1, BigDecimal.class), 
								(BigDecimal)ConvertUtils.convert(o2, BigDecimal.class));
	}
	
	public static BigDecimal percentageMatch(BigDecimal o1, BigDecimal o2) {
		if (o1.compareTo(o2) == 0) return BigDecimal.valueOf(1);
		if ((o1.compareTo(BigDecimal.ZERO) == 0 && o2.compareTo(BigDecimal.ZERO) != 0) ||
				(o1.compareTo(BigDecimal.ZERO) != 0 && o2.compareTo(BigDecimal.ZERO) == 0)) return BigDecimal.ZERO;
		return (o1).divide(o2, 6, RoundingMode.HALF_UP);
	}
	
	public static ComparisonResult compare(Object o1, Object o2) {
		return compare(o1, o2, DEFAULT_SMALLEST_ABSOLUTE_VALUE, DEFAULT_TOLERANCE_PERCENTAGE);
	}
	
	public static ComparisonResult compare(Object o1, Object o2, boolean equalsIgnoreCase) {
		return compare(o1, o2, DEFAULT_SMALLEST_ABSOLUTE_VALUE, DEFAULT_TOLERANCE_PERCENTAGE, equalsIgnoreCase);
	}
	
	public static ComparisonResult compare(Object o1, Object o2, BigDecimal smallestAbsoluteValue, BigDecimal tolerancePercentage) {
		return compare(o1, o2, smallestAbsoluteValue, tolerancePercentage, false);
	}	
		
	public static ComparisonResult compare(Object o1, Object o2, BigDecimal smallestAbsoluteValue, BigDecimal tolerancePercentage, boolean equalsIgnoreCase) {
		ComparisonResult result = null;		
		if (o1 instanceof Number && o2 instanceof Number) {
			;
			result = compareNumeric((BigDecimal)ConvertUtils.convert(o1, BigDecimal.class), 
									(BigDecimal)ConvertUtils.convert(o2, BigDecimal.class), 
									smallestAbsoluteValue, tolerancePercentage);
		}
		else {
			result = compareNonNumeric(o1, o2, equalsIgnoreCase);
		}
		return result;
	}
		
	private static ComparisonResult compareNonNumeric(Object o1, Object o2, boolean equalsIgnoreCase) {
		if (o1 == null && o2 == null) {
			return ComparisonResult.valueOf(false);	
		} 
		else if (o1 == null || o2 == null) {
			return ComparisonResult.valueOf(true);	
		}
		else {
			if (o1 instanceof String && o2 instanceof String && equalsIgnoreCase) {
				return ComparisonResult.valueOf(!((String)o1).equalsIgnoreCase((String)o2));
			}
			return ComparisonResult.valueOf(!o1.equals(o2));
		}	
	}
	
	private static ComparisonResult compareNumeric(BigDecimal o1, BigDecimal o2, BigDecimal smallestAbsoluteValue, BigDecimal tolerancePercentage) {
		boolean difference = false;
		if ((o1 == null || o2 == null)) {
			if (!(o1 == null && o2 == null)) { 
				difference = true;	
			}
		}
		else if ((o1.abs().compareTo(smallestAbsoluteValue)) == 1 || (o2.abs().compareTo(smallestAbsoluteValue) == 1)) {
			BigDecimal percentageDifference = percentageDifference(o1, o2);
			BigDecimal absoluteDifference = absoluteDifference(o1, o2);
			if (percentageDifference.abs().compareTo(tolerancePercentage) == 1) {
				difference = true;
			}
			return ComparisonResult.valueOf(difference, absoluteDifference, percentageDifference);
		}
		return ComparisonResult.valueOf(difference, null, null);
	}

	
}
