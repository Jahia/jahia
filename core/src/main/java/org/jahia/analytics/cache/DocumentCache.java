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
package org.jahia.analytics.cache;

import java.io.InputStream;
import java.util.Properties;

import org.w3c.dom.Document;

/**
 * You are free to not use a <code>DocumentCache</code>, to use the
 * <code>FileDocumentCacheOld</code>, or implement your own version of the this
 * interface. The methods <code>getDocument</code> and
 * <code>cacheDocument</code> should be synchronized.
 * 
 * @author Dan Andrews
 * 
 */
public interface DocumentCache {

	/**
	 * Typically a general implementation will require further configuration and
	 * may be configured with this method. Instead of invoking this method
	 * directly you should let the <code>CacheContextListener</code> invoke
	 * this via the <code>DocumentCacheFactory</code>. Alternately you may
	 * invoke on of the <code>loadConfig</code> methods of the
	 * <code>DocumentCacheFactory</code> object. If your implementation does
	 * not require further configuration you may implement this with a do
	 * nothing method.
	 * 
	 * @param config
	 *            The configuration properties.
	 * @throws CacheException
	 */
	public void configure(Properties config) throws CacheException;

	/**
	 * Gets the cached document or returns null if there is not a cached
	 * document that matched the given id. Most implementations will wish to
	 * track a last modified value and discard the cached <code>Document</code>
	 * if older that a given age.
	 * 
	 * @param id
	 *            The id of the <code>Document</code> to get.
	 * @return The cached <code>Document</code> if available and recent.
	 * @throws CacheException
	 */
	public Document getDocument(CachedDocumentId id) throws CacheException;

	/**
	 * There are times where it is more efficient to retreive an
	 * <code>InputStream</code> and not <code>Document</code>, so this
	 * method the cached document as an <code>InputStream</code> or returns
	 * null if there is not a cached document that matched the given id. Most
	 * implementations will wish to track a last modified value and discard the
	 * cached <code>Document</code> if older that a given age.
	 * 
	 * @param id
	 *            The id of the <code>Document</code> to get.
	 * @return The cached <code>InputStream</code> if available and recent.
	 * @throws CacheException
	 */
	public InputStream getDocumentAsInputStream(CachedDocumentId id)
			throws CacheException;

	/**
	 * Cache a document with the given id. Most implementations will wish to
	 * maintain a last modified value when the document is cached. Some
	 * implementations will also wish to purge old entries when new documents
	 * are added to the cache.
	 * 
	 * @param document
	 *            The <code>Document</code> to cache.
	 * @param id
	 *            The id of the <code>Document</code> to get. *
	 * @throws CacheException
	 */
	public void cacheDocument(Document document, CachedDocumentId id)
			throws CacheException;

}
