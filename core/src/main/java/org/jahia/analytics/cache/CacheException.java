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
package org.jahia.analytics.cache;

import org.jahia.analytics.reports.JAnalyticsException;

/**
 * The <code>CacheException</code> is used to throw exceptions from
 * implementation of the <code>DocumentCache</code> interface.
 * 
 * @author Dan Andrews
 */
public class CacheException extends JAnalyticsException {

	/** Generated serialVersionUID. */
	private static final long serialVersionUID = -4613059740316917950L;

	/**
	 * Constructs a <code>CacheException</code> with the given message.
	 * 
	 * @param message
	 *            The exception message.
	 */
	public CacheException(String message) {
		this(message, null);
	}

	/**
	 * Constructs a <code>CacheException</code> with the given message.
	 * 
	 * @param message
	 *            The exception message.
	 * @param cause
	 *            The <code>Throwable</code> that caused this exception or
	 *            null if generated solely from within the
	 *            <code>JAnalytics</code> class.
	 */
	public CacheException(String message, Throwable cause) {
		super(message, cause);
	}

}
