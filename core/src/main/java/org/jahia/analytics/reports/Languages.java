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
