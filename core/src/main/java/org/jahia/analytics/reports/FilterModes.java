/**
 * Jahia Enterprise Edition v6
 *
 * Copyright (C) 2002-2009 Jahia Solutions Group. All rights reserved.
 *
 * Jahia delivers the first Open Source Web Content Integration Software by combining Enterprise Web Content Management
 * with Document Management and Portal features.
 *
 * The Jahia Enterprise Edition is delivered ON AN "AS IS" BASIS, WITHOUT WARRANTY OF ANY KIND, EITHER EXPRESSED OR
 * IMPLIED.
 *
 * Jahia Enterprise Edition must be used in accordance with the terms contained in a separate license agreement between
 * you and Jahia (Jahia Sustainable Enterprise License - JSEL).
 *
 * If you are unsure which license is appropriate for your use, please contact the sales department at sales@jahia.com.
 */
package org.jahia.analytics.reports;

/**
 * Filter modes for Google Analytics xml reports.
 * 
 * @author Dan Andrews
 */
public final class FilterModes {

	/**
	 * Don't filter at all enum value.
	 */
	public static final int _NONE = -1;

	/** Match filters enum value. */
	public static final int _MATCH = 0;

	/**
	 * Don't match filters enum value.
	 */
	public static final int _DONT_MATCH = 1;

	/*
	 * These enums are defined as the <code>ReportTypes</code> class so we can
	 * compile with JDK 1.4.
	 */

	/** Don't filter enum. */
	public static final FilterModes NONE = new FilterModes(_NONE);

	/** Match filters enum. */
	public static final FilterModes MATCH = new FilterModes(_MATCH);

	/** Don't match filters enum. */
	public static final FilterModes DONT_MATCH = new FilterModes(_DONT_MATCH);

	/** The value of the enum. */
	private int value;

	/**
	 * Constructor.
	 * 
	 * @param value
	 *            The value of the enum.
	 */
	FilterModes(int value) {
		this.value = value;
	}

	/**
	 * Gets the enum value.
	 * 
	 * @return The enum value.
	 */
	public int getValue() {
		return value;
	}

	/**
	 * Gets the enum value as a string, for example the Executive Overview will
	 * return a value of either "1" for DONT_MATCH or "2" for MATCH.
	 * 
	 * @return The value of the enum as a string.
	 */
	public String getValueAsString() {
		return Integer.toString(value);
	}

	/**
	 * Gets the filter mode from one of the filter mode values.
	 * 
	 * @param value
	 *            The value of the filter mode.
	 * @return The corresponding <code>FilterModes</code> object.
	 */
	public static FilterModes fromInt(int value) {
		FilterModes rv = null;
		switch (value) {
		case _NONE:
			rv = NONE;
			break;
		case _MATCH:
			rv = MATCH;
			break;
		case _DONT_MATCH:
			rv = DONT_MATCH;
			break;
		default:
			throw new IllegalArgumentException(
					"Invalid filter mode enum value value: " + value);
		}
		return rv;
	}

	/**
	 * Gets the string representation of the value.
	 * 
	 * @return The value of the enum as a string.
	 */
	public String toString() {
		return Integer.toString(value);
	}

}
