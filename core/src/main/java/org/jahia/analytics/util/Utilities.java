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
package org.jahia.analytics.util;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.StringWriter;
import java.io.UnsupportedEncodingException;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;

import javax.servlet.ServletException;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.OutputKeys;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerConfigurationException;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.TransformerFactoryConfigurationError;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;

import org.apache.commons.codec.binary.Base64;
import org.w3c.dom.Document;
import org.xml.sax.EntityResolver;
import org.xml.sax.SAXException;

import org.jahia.analytics.cache.CachedDocumentId;
import org.jahia.analytics.cache.DocumentCacheFactory;
import org.jahia.analytics.reports.FilterModes;
import org.jahia.analytics.reports.JAnalytics;
import org.jahia.analytics.reports.JAnalyticsException;
import org.jahia.analytics.reports.ReportTypes;

/**
 * The <code>Utilities</code> provides static utility methods which can be
 * used across several analytics packages.
 * 
 * @author Dan Andrews
 */
public final class Utilities {

	/**
	 * Constructor - no need to instantiate this class.
	 */
	private Utilities() {
	}

	/**
	 * Static utility method to roll the calendar to given amount in days.
	 * 
	 * @param cal
	 *            The <code>Calendar</code> to roll.
	 * @param amount
	 *            The amount in days to roll.
	 */
	public static void rollCalendarDay(Calendar cal, int amount) {
		long time = amount * 24 * 60 * 60 * 1000L + cal.getTime().getTime();
		cal.setTime(new Date(time));
	}

	/**
	 * Converts the calendar to a Google get/post formatted date (i.e yyyymmdd).
	 * 
	 * @param calendar
	 *            The calendar from which to format a date for the get request.
	 * @return The date suitable for a get/post request to Google Analytics.
	 */
	public static String calendarToString(Calendar calendar) {
		return JAnalytics.GOOGLE_DATE_FORMAT.format(trimCalendar(calendar)
				.getTime());
	}

	/**
	 * Converts the date string from a a Google get/post formatted date to a
	 * <code>Calendar</code> object.
	 * 
	 * @param dateString
	 *            The date suitable for a get request.
	 * 
	 * @return The calendar from which to format a date for the get request.
	 */
	public static Calendar stringToCalendar(String dateString) {
		Calendar calendar = Calendar.getInstance();

		try {
			calendar.setTime(new SimpleDateFormat(JAnalytics.GOOGLE_DATE_PATTERN).parse(dateString));
			calendar = trimCalendar(calendar);
		} catch (ParseException e) {
			throw new IllegalArgumentException("The date '" + dateString
					+ "' cannot be parsed using the format '"
					+ JAnalytics.GOOGLE_DATE_PATTERN + "'");
		}
		return calendar;
	}

	/**
	 * Trims the given calendar by setting milliseconds, seconds, minutes, and
	 * hours to zero.
	 * 
	 * @param calendar
	 *            The calendar to trim.
	 * @return A defensive copy of the calendar.
	 */
	public static Calendar trimCalendar(Calendar calendar) {
		Calendar clone = (Calendar) calendar.clone();
		clone.set(Calendar.MILLISECOND, 0);
		clone.set(Calendar.SECOND, 0);
		clone.set(Calendar.MINUTE, 0);
		clone.set(Calendar.HOUR, 0);
		return clone;
	}

	/**
	 * Creates a one way hash of the given string for security purposes.
	 * 
	 * @param text
	 *            The text from which to create a one way hash.
	 * @return The hashed text.
	 */
	public static String stringToHash(String text) {
		MessageDigest md = null;
		try {
			md = MessageDigest.getInstance("SHA-1");
			md.update(text.getBytes("iso-8859-1"), 0, text.length());
		} catch (NoSuchAlgorithmException e) {
			throw new IllegalStateException(
					"Should not happened: SHA-1 algorithm is missing.");
		} catch (UnsupportedEncodingException e) {
			throw new IllegalStateException(
					"Should not happened: Could not encode text bytes '" + text
							+ "' to iso-8859-1.");
		}
		return new String(Base64.encodeBase64(md.digest()));
	}

	/**
	 * Gets the XML <code>InputStream</code> as a <code>Document</code>.
	 * 
	 * @param inputStream
	 *            An XML <code>InputStream</code>.
	 * @return The <code>Document</code>.
	 * @throws JAnalyticsException
	 */
	public static Document getDocument(InputStream inputStream)
			throws JAnalyticsException {
		return getDocument(inputStream, null);
	}

	/**
	 * Gets the XML <code>InputStream</code> as a <code>Document</code>.
	 * 
	 * @param inputStream
	 *            An XML <code>InputStream</code>.
	 * @param resolver
	 *            An <code>EntityResolver</code> for if and when Google
	 *            Analytics uses a dtd, xsd,....
	 * @return The <code>Document</code>.
	 * @throws JAnalyticsException
	 */
	public static Document getDocument(InputStream inputStream,
			EntityResolver resolver) throws JAnalyticsException {
		Document document = null;
		try {
			DocumentBuilder builder = DocumentBuilderFactory.newInstance()
					.newDocumentBuilder();
			if (resolver != null) {
				builder.setEntityResolver(resolver);
			}
			document = builder.parse(inputStream);

		} catch (IOException e) {
			throw new JAnalyticsException("Exception while getting report: "
					+ e.getMessage(), e);
		} catch (ParserConfigurationException e) {
			throw new JAnalyticsException("Exception while getting report: "
					+ e.getMessage(), e);
		} catch (SAXException e) {
			throw new JAnalyticsException("Exception while getting report: "
					+ e.getMessage(), e);
		} finally {
			try {
				inputStream.close();
			} catch (IOException e) {
				throw new JAnalyticsException("Could not close IOException."
						+ e.getMessage(), e);
			}
		}
		return document;
	}

	/**
	 * Gets the given <code>Document</code> as a string.
	 * 
	 * @param document
	 *            The <code>Document</code>.
	 * @param prettyPrint
	 *            If true then indent the string.
	 * @return The <code>Document</code> as a string.
	 * @throws JAnalyticsException
	 */
	public static String getDocumentAsString(Document document,
			boolean prettyPrint) throws JAnalyticsException {
		String prettyString = null;
		try {
			TransformerFactory tf = TransformerFactory.newInstance();
			Transformer tfm = tf.newTransformer();

			if (prettyPrint) {
				Utilities.configureTransformerIndent(tf, tfm);
			}
			tfm.setOutputProperty(OutputKeys.ENCODING, "UTF-8");
			DOMSource source = new DOMSource(document);
			StringWriter writer = new StringWriter();
			StreamResult result = new StreamResult(writer);
			tfm.transform(source, result);
			prettyString = writer.toString();
		} catch (TransformerConfigurationException e) {
			throw new JAnalyticsException("Exception while getting report: "
					+ e.getMessage(), e);
		} catch (TransformerFactoryConfigurationError e) {
			throw new JAnalyticsException("Exception while getting report: "
					+ e.getMessage(), e);
		} catch (TransformerException e) {
			throw new JAnalyticsException("Exception while getting report: "
					+ e.getMessage(), e);
		}
		prettyString = prettyString.replaceAll("\uFFFD", "?");
		prettyString = prettyString.replaceAll("\ufffd", "?");
		prettyString = prettyString.replaceAll("&#65533;", "?");
		return prettyString;
	}

	/**
	 * Configure the transform for pretty print of XML documents.
	 * 
	 * @param tf
	 *            The <code>TransformerFactory</code> object.
	 * @param tfm
	 *            The <code>Transformer</code> object.
	 */
	public static void configureTransformerIndent(TransformerFactory tf,
			Transformer tfm) {
		boolean isJDK14 = System.getProperty("java.version").startsWith("1.4");
		boolean isJDK15 = System.getProperty("java.version").startsWith("1.5");
		boolean isJDK16 = System.getProperty("java.version").startsWith("1.6");
		try {
			if (isJDK15 || isJDK16) {
				// Work around http://bugs.sun.com/
				// bugdatabase/view_bug.do?bug_id=6296446
				// tf.setAttribute("indent-number", new Integer(2));
			}
			tfm.setOutputProperty(OutputKeys.INDENT, "yes");
			tfm.setOutputProperty(OutputKeys.METHOD, "xml");

			if (isJDK14 || isJDK16) {
				tfm.setOutputProperty(
						"{http://xml.apache.org/xslt}indent-amount", "2");
			}
		} catch (Exception e) {
			// noop: catch unsupported errors from various jdks
		}
	}

	/**
	 * Gets the cache document if available and otherwise used the Google
	 * Analytics service. If the Google Analytics service is required then the
	 * document will be cached.
	 * 
	 * @param email
	 *            The a valid Google Analytics email account.
	 * @param password
	 *            The corresponding Google Analytics email password.
	 * @param reportId
	 *            The report id that must be one of the values returned by the
	 *            <code>getReportIds</code> method or the first value is used
	 *            by default.
	 * @param reportType
	 *            The report type which cannot be null.
	 * @param startTime
	 *            The start time which cannot be null.
	 * @param endTime
	 *            The end time which cannot be null and must be equal to or
	 *            later than the start time.
	 * @param filter
	 *            A named filter or null if none.
	 * @param filterMode
	 *            The filter mode.
	 * @param limit
	 *            A limit which must be greater than zero and can not exceed the
	 *            maxLimit.
	 * @return The document.
	 * @throws JAnalyticsException
	 */
	private static Document getDocument(String email, String password,
			String reportId, ReportTypes reportType, Calendar startTime,
			Calendar endTime, String filter, FilterModes filterMode, int limit)
			throws JAnalyticsException {

		// Report id is allowed to be null at this point.
		CachedDocumentId id = CachedDocumentId.createDocumentId(email,
				password, reportId, reportType, startTime, endTime, filter,
				filterMode, limit);

		Document document = getCachedDocument(id);
		if (document == null) {
			JAnalytics analytics = new JAnalytics();
			analytics.login(email, password);
			if (reportId == null) {
				// Many users will just use this default rid.
				reportId = analytics.getReportIds()[0];
			}

			// TODO: Andrew Kerr indicated that limit is not being used any
			// more, so this might be broken.
			document = analytics.getReportDocument(reportId, reportType,
					startTime, endTime, filter, FilterModes.DONT_MATCH, 0,
					limit);
			cacheDocument(document, id);
		}
		return document;
	}

	/**
	 * Gets the document as an <code>InputStream</code> by using the cache
	 * document if available and otherwise uses the Google Analytics service. If
	 * the Google Analytics service is required then the document will be
	 * cached.
	 * 
	 * @param email
	 *            The a valid Google Analytics email account.
	 * @param password
	 *            The corresponding Google Analytics email password.
	 * @param reportId
	 *            The report id that must be one of the values returned by the
	 *            <code>getReportIds</code> method or the first value is used
	 *            by default.
	 * @param reportType
	 *            The report type which cannot be null.
	 * @param startTime
	 *            The start time which cannot be null.
	 * @param endTime
	 *            The end time which cannot be null and must be equal to or
	 *            later than the start time.
	 * @param filter
	 *            A named filter or null if none.
	 * @param filterMode
	 *            The filter mode.
	 * @param limit
	 *            A limit which must be greater than zero and can not exceed the
	 *            maxLimit.
	 * @return The <code>InputStream</code>.
	 * @throws JAnalyticsException
	 */
	public static InputStream getDocumentAsInputStream(String email,
			String password, String reportId, ReportTypes reportType,
			Calendar startTime, Calendar endTime, String filter,
			FilterModes filterMode, int limit) throws JAnalyticsException {

		// This is a round about way of doing getting the stream. The problem is
		// that if we get it directly as a stream then we might have to cache it
		// and also get a new stream anyhow. As long as the documents stay small
		// then this shouldn't be too inefficient.
		Document document = getDocument(email, password, reportId, reportType,
				startTime, endTime, filter, filterMode, limit);
		ByteArrayInputStream inputStream;
		try {
			inputStream = new ByteArrayInputStream(getDocumentAsString(
					document, false).getBytes("UTF-8"));
		} catch (UnsupportedEncodingException e) {
			throw new JAnalyticsException(e.getMessage(), e);
		}
		return inputStream;
	}

	/**
	 * Gets the document as a <code>String</code> by using the cache document
	 * if available and otherwise uses the Google Analytics service. If the
	 * Google Analytics service is required then the document will be cached.
	 * 
	 * @param email
	 *            The a valid Google Analytics email account.
	 * @param password
	 *            The corresponding Google Analytics email password.
	 * @param reportId
	 *            The report id that must be one of the values returned by the
	 *            <code>getReportIds</code> method or the first value is used
	 *            by default.
	 * @param reportType
	 *            The report type which cannot be null.
	 * @param startTime
	 *            The start time which cannot be null.
	 * @param endTime
	 *            The end time which cannot be null and must be equal to or
	 *            later than the start time.
	 * @param filter
	 *            A named filter or null if none.
	 * @param filterMode
	 *            The filter mode.
	 * @param limit
	 *            A limit which must be greater than zero and can not exceed the
	 *            maxLimit.
	 * @return The <code>Document</code> as a string.
	 * @throws JAnalyticsException
	 */
	public static String getDocumentAsString(String email, String password,
			String reportId, ReportTypes reportType, Calendar startTime,
			Calendar endTime, String filter, FilterModes filterMode, int limit)
			throws JAnalyticsException {
		// TODO: Andrew Kerr indicated that limit is not being used any
		// more, so this might be broken.

		Document document = getDocument(email, password, reportId, reportType,
				startTime, endTime, filter, filterMode, limit);
		return getDocumentAsString(document, true);
	}

	/**
	 * Gets the cache document if available.
	 * 
	 * @param id
	 *            The id of the document
	 * @return The cached document or null if not available.
	 * @throws JAnalyticsException
	 */
	public static Document getCachedDocument(CachedDocumentId id)
			throws JAnalyticsException {
		Document document = null;
		document = DocumentCacheFactory.createDocumentCache().getDocument(id);
		return document;
	}

	/**
	 * Gets the cache document if available.
	 * 
	 * @param id
	 *            The id of the document
	 * @return The cached document or null if not available.
	 * @throws JAnalyticsException
	 * @throws ServletException
	 */
	private static void cacheDocument(Document document, CachedDocumentId id)
			throws JAnalyticsException {
		DocumentCacheFactory.createDocumentCache().cacheDocument(document, id);
	}

}
