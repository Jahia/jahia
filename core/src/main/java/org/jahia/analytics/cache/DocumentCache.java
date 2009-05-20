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
