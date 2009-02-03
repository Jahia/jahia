/**
 * 
 * This file is part of Jahia: An integrated WCM, DMS and Portal Solution
 * Copyright (C) 2002-2009 Jahia Limited. All rights reserved.
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
 * in Jahia's FLOSS exception. You should have recieved a copy of the text
 * describing the FLOSS exception, and it is also available here:
 * http://www.jahia.com/license"
 * 
 * Commercial and Supported Versions of the program
 * Alternatively, commercial and supported versions of the program may be used
 * in accordance with the terms contained in a separate written agreement
 * between you and Jahia Limited. If you are unsure which license is appropriate
 * for your use, please contact the sales department at sales@jahia.com.
 */

package org.jahia.analytics.cache;

import java.io.Serializable;
import java.util.Calendar;

import org.jahia.analytics.reports.FilterModes;
import org.jahia.analytics.reports.ReportTypes;
import org.jahia.analytics.util.Utilities;

/**
 * The <code>CachedDocumentId</code> encapsulates all the information required
 * to uniquely identify a cached Google Analytics XML report. Internally this
 * class represents the email and password in a secure-one-way-hashed form.
 * 
 * @author Dan Andrews
 */
public final class CachedDocumentId implements Serializable {

	/** Generated serialVersionUID */
	private static final long serialVersionUID = -7689146258876203442L;

	/** The email hash (internally this is securely hashed and encoded). */
	private String emailHash;

	/** The password hash (internally this is securely hashed and encoded). */
	private String passwordHash;

	/** The report id (allow this field to be optionally null). */
	private String reportId;

	/** The report type. */
	private String reportType;

	/** The start time. */
	private String startTime;

	/** The end time. */
	private String endTime;

	/** The filter. */
	private String filter;

	/** The filter mode. */
	private int filterMode;

	/** The limit. */
	private int limit;

	private int hashCode;

	/**
	 * Creates a <code>CachedDocumentId</code> using the given fields.
	 * 
	 * @param email
	 *            The email (internally this is securely hashed and encoded).
	 * @param password
	 *            The password (internally this is securely hashed and encoded).
	 * @param reportId
	 *            The report id.
	 * @param reportType
	 *            The report type.
	 * @param startTime
	 *            The start time.
	 * @param endTime
	 *            The end time.
	 * @param filter
	 *            The filter.
	 * @param filterMode
	 *            The filter mode.
	 * @param limit
	 *            The limit.
	 */
	private CachedDocumentId(String email, String password, String reportId,
			ReportTypes reportType, Calendar startTime, Calendar endTime,
			String filter, FilterModes filterMode, int limit) {
		this(reportId, reportType, startTime, endTime, filter, filterMode,
				limit, Utilities.stringToHash(email), Utilities
						.stringToHash(password));
	}

	/**
	 * Creates a <code>CachedDocumentId</code> using the given fields where
	 * the email and password are already in hashed form.
	 * 
	 * @param reportId
	 *            The report id.
	 * @param reportType
	 *            The report type.
	 * @param startTime
	 *            The start time.
	 * @param endTime
	 *            The end time.
	 * @param filter
	 *            The filter.
	 * @param filterMode
	 *            The filter mode.
	 * @param limit
	 *            The limit.
	 * @param emailHash
	 *            The email which is already hashed and encoded.
	 * @param passwordHash
	 *            The password which is already hashed and encoded.
	 */
	private CachedDocumentId(String reportId, ReportTypes reportType,
			Calendar startTime, Calendar endTime, String filter,
			FilterModes filterMode, int limit, String emailHash,
			String passwordHash) {
		if (emailHash == null || passwordHash == null || startTime == null
				|| endTime == null || filter == null || filterMode == null
				|| limit <= 0) {
			throw new IllegalArgumentException(
					"Neither email, password, reportType, startTime, "
							+ "endTime, filter, nor filterMode may be null and "
							+ "limit must be greater than zero. The reportId may "
							+ "be nullable.");
		}
		this.emailHash = emailHash;
		this.passwordHash = passwordHash;
		this.reportType = reportType.getValueAsString();
		this.startTime = Utilities.calendarToString(startTime);
		this.endTime = Utilities.calendarToString(endTime);
		this.filter = filter;
		this.filterMode = filterMode.getValue();
		this.limit = limit;
		this.reportId = reportId;
		this.hashCode = (this.emailHash + this.passwordHash + this.reportType
				+ this.startTime + this.endTime + this.filter + this.filterMode + this.limit)
				.hashCode();
	}

	/**
	 * Creates a <code>CachedDocumentId</code> using the given fields.
	 * 
	 * @param email
	 *            The email (internally this is securely hashed and encoded).
	 * @param password
	 *            The password (internally this is securely hashed and encoded).
	 * @param reportId
	 *            The report id.
	 * @param reportType
	 *            The report type.
	 * @param startTime
	 *            The start time.
	 * @param endTime
	 *            The end time.
	 * @param filter
	 *            The filter.
	 * @param filterMode
	 *            The filter mode.
	 * @param limit
	 *            The limit.
	 * @return A new <code>CachedDocumentId</code> object.
	 */
	public static CachedDocumentId createDocumentId(String email,
			String password, String reportId, ReportTypes reportType,
			Calendar startTime, Calendar endTime, String filter,
			FilterModes filterMode, int limit) {
		return new CachedDocumentId(email, password, reportId, reportType,
				startTime, endTime, filter, filterMode, limit);
	}

	/**
	 * Creates a <code>CachedDocumentId</code> using the given fields where
	 * the email and password are already in hashed form.
	 * 
	 * @param reportId
	 *            The report id.
	 * @param reportType
	 *            The report type.
	 * @param startTime
	 *            The start time.
	 * @param endTime
	 *            The end time.
	 * @param filter
	 *            The filter.
	 * @param filterMode
	 *            The filter mode.
	 * @param limit
	 *            The limit.
	 * @param emailHash
	 *            The email which is already hashed and encoded.
	 * @param passwordHash
	 *            The password which is already hashed and encoded.
	 * @return A new <code>CachedDocumentId</code> object.
	 */
	public static CachedDocumentId createDocumentIdHashed(String emailHash,
			String passwordHash, String reportId, ReportTypes reportType,
			Calendar startTime, Calendar endTime, String filter,
			FilterModes filterMode, int limit) {
		return new CachedDocumentId(reportId, reportType, startTime, endTime,
				filter, filterMode, limit, emailHash, passwordHash);
	}

	/**
	 * Returns a reasonably unique hashcode.
	 * 
	 * @return The hashcode value.
	 */
	public int hashCode() {
		return hashCode;
	}

	/**
	 * Determines if this object is equal to the other object.
	 * 
	 * @param obj
	 *            The other <code>CachedDocumentId</code> to compare with.
	 */
	public boolean equals(Object obj) {
		CachedDocumentId other = (CachedDocumentId) obj;

		// Because the user may retrieve an XML report using the default report
		// id, then it will not be known without first doing a login to the
		// Google Analytics service, so the reportId is allowed to be either
		// null or not.
		boolean equalReportIds = (reportId == null && other.reportId == null)
				|| (reportId != null && other.reportId != null);

		boolean equal = (this == other)
				|| (equalReportIds && hashCode == other.hashCode
						&& emailHash.equals(other.emailHash)
						&& passwordHash.equals(other.passwordHash)
						&& reportType.equals(other.reportType)
						&& startTime.equals(other.startTime)
						&& endTime.equals(other.endTime)
						&& filter.equals(other.filter)
						&& filterMode == other.filterMode && limit == other.limit);
		return equal;
	}

	/**
	 * Gets the hashed email.
	 * 
	 * @return The hashed email.
	 */
	public String getEmailHash() {
		return emailHash;
	}

	/**
	 * Gets the end time in Google format.
	 * 
	 * @return The end time in Google format.
	 */
	public String getEndTime() {
		return endTime;
	}

	/**
	 * Gets the filter or an empty string if none.
	 * 
	 * @return The filter or an empty string if none.
	 */
	public String getFilter() {
		return filter;
	}

	/**
	 * Gets the filter mode.
	 * 
	 * @return The filter mode.
	 */
	public int getFilterMode() {
		return filterMode;
	}

	/**
	 * Gets the hash code.
	 * 
	 * @return The hash code.
	 */
	public int getHashCode() {
		return hashCode;
	}

	/**
	 * Gets the hashed password.
	 * 
	 * @return The hashed password.
	 */
	public String getPasswordHash() {
		return passwordHash;
	}

	/**
	 * Gets the report id which is the only optional nullable field.
	 * 
	 * @return The report id.
	 */
	public String getReportId() {
		return reportId;
	}

	/**
	 * Gets the report time.
	 * 
	 * @return The report time.
	 */
	public String getReportType() {
		return reportType;
	}

	/**
	 * Gets the start time in Google format.
	 * 
	 * @return The start time in Google format.
	 */
	public String getStartTime() {
		return startTime;
	}

}
