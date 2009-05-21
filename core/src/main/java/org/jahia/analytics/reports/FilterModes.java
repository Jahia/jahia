/**
 * This file is part of Jahia: An integrated WCM, DMS and Portal Solution
 * Copyright (C) 2002-2009 Jahia Solutions Group SA. All rights reserved.
 *
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the GNU General Public License
 * as published by the Free Software Foundation; either version 2
 * of the License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program; if not, write to the Free Software
 * Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston, MA 02110-1301, USA.
 *
 * As a special exception to the terms and conditions of version 2.0 of
 * the GPL (or any later version), you may redistribute this Program in connection
 * with Free/Libre and Open Source Software ("FLOSS") applications as described
 * in Jahia's FLOSS exception. You should have received a copy of the text
 * describing the FLOSS exception, and it is also available here:
 * http://www.jahia.com/license
 *
 * Commercial and Supported Versions of the program
 * Alternatively, commercial and supported versions of the program may be used
 * in accordance with the terms contained in a separate written agreement
 * between you and Jahia Solutions Group SA. If you are unsure which license is appropriate
 * for your use, please contact the sales department at sales@jahia.com.
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
