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
 * The <code>Languages</code> enum is used internally by the
 * <code>JAnalytics</code> class. For now <code>JAnalytics</code> only uses
 * the <code>ENGLISH_US</code> enum. Future versions of
 * <code>JAnalytics</code> may allow other languages.
 * 
 * @author Dan Andrews
 */
public final class Languages {

	/**
	 * These enums are defined as the <code>Languages</code> class so we can compile with
	 * JDK 1.4.
	 */
	public static Languages ENGLISH_US = new Languages("en-US");

	public static Languages ENGLISH_GB = new Languages("en-GB");

	public static Languages GERMAN = new Languages("de-DE");

	public static Languages FRENCH = new Languages("fr-FR");

	public static Languages ITALIAN = new Languages("it-IT");

	public static Languages SPANISH = new Languages("es-ES");

	public static Languages DUTCH = new Languages("nl-NL");

	public static Languages JAPANESE = new Languages("ja-JP");

	public static Languages PORTUGUESE_BRAZIL = new Languages("pt-BR");

	public static Languages DANISH = new Languages("da-DK");

	public static Languages FINISH = new Languages("fi-FI");

	public static Languages NORWEGIAN = new Languages("no-NO");

	public static Languages SWEDISH = new Languages("sv-SE");

	public static Languages CHINESE_1 = new Languages("zh-TW");

	public static Languages CHINESE_2 = new Languages("zh-CN");

	public static Languages KOREAN = new Languages("ko-KR");

	public static Languages RUSSIAN = new Languages("ru-RU");

	/** The language value. */
	private String language;

	/**
	 * Constructor.
	 * 
	 * @param language
	 */
	private Languages(String language) {
		this.language = language;
	}

	/**
	 * Gets the language.
	 * 
	 * @return The language.
	 */
	public String getLanguage() {
		return language;
	}

}
