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

import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.util.Properties;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import org.jahia.analytics.reports.JAnalyticsException;
import org.jahia.analytics.util.Utilities;

/**
 * The <code>DocumentCacheFactory</code> provides the <code>JAnalytics</code>
 * class with access to a <code>DocumentCache</code> implementation.
 * 
 * @author Dan Andrews
 */
public class DocumentCacheFactory {

	/** The name of the document cache element. */
	private static final String DOCUMENT_CACHE_ELEMENT_NAME = "documentcache";

	/** The name of the class name attribute. */
	private static final String CLASSNAME_ATTRIBUTE_NAME = "classname";

	/** The name of the param element. */
	private static final String PARAM_ELEMENT_NAME = "param";

	/** The name of the value attribute. */
	private static final String VALUE_ATTRIBUTE_NAME = "value";

	/** The name of the name attribute. */
	private static final String NAME_ATTRIBUTE_NAME = "name";

	/** The <code>DocumentCache</code> implementation. */
	private static DocumentCache documentCache;

	/**
	 * Initializes the <code>DocumentCacheFactory</code> by reading the
	 * analytics.xml configuration file.
	 * 
	 * <pre>
	 * &lt;?xml version=&quot;1.0&quot; encoding=&quot;UTF-8&quot;?&gt; 
	 * &lt;analytics&gt; 
	 * &lt;documentcache
	 * classname=&quot;analytics.cache.impl.FileDocumentCacheOld&quot;&gt;
	 * &lt;param name=&quot;basedirectory&quot; value=&quot;C:\temp&quot;/&gt; 
	 * &lt;param name=&quot;maxageminutes&quot; value=&quot;60&quot;/&gt;
	 * &lt;param name=&quot;cachesize&quot; value=&quot;5&quot;/&gt; 
	 * &lt;/documentcache&gt; 
	 * &lt;/analytics&gt;
	 * </pre>
	 * 
	 * <p>
	 * You are free to not use a <code>DocumentCache</code>, to use the
	 * <code>FileDocumentCacheOld</code>, or implement your own version of the
	 * <code>DocumentCache</code> interface. If you choose to use a
	 * <code>DocumentCache</code> then you must invoke the
	 * <code>loadConfig</code> method early in your applications startup.
	 * </p>
	 * <p>
	 * If you are using a this in a web application which uses a servlet 2.3 or
	 * higher API, then you can automatically invoke this method using the
	 * <code>CacheContextListener</code>.
	 * </p>
	 * 
	 * @param urlFile
	 *            The <code>URL</code> to the analytics.xml configuration
	 *            file.
	 * @throws CacheException
	 *             if there was a problem configuring the
	 *             <code>DocumentCacheFactory</code>.
	 */
	public static void loadConfig(URL urlFile) throws CacheException {
		Document document;
		try {
			document = Utilities.getDocument(urlFile.openStream());

		} catch (IOException e) {
			throw new CacheException("Could not open '" + urlFile.getFile()
					+ "'", e);
		} catch (JAnalyticsException e) {
			throw new CacheException("Could not open '" + urlFile.getFile()
					+ "'", e);
		}
		Element element = (Element) document.getElementsByTagName(
				DOCUMENT_CACHE_ELEMENT_NAME).item(0);
		if (element != null) {
			String className = element.getAttribute(CLASSNAME_ATTRIBUTE_NAME);
			if (className != null) {
				Properties config = new Properties();
				NodeList nodes = element
						.getElementsByTagName(PARAM_ELEMENT_NAME);
				if (nodes != null) {
					for (int i = 0, count = nodes.getLength(); i < count; i++) {
						Node node = nodes.item(i);
						if (node instanceof Element) {
							Element n = (Element) node;
							String name = n.getAttribute(NAME_ATTRIBUTE_NAME);
							String value = n.getAttribute(VALUE_ATTRIBUTE_NAME);
							config.put(name, value);
						}
					}
				}
				loadConfig(className, config);
			}
		}
	}

	/**
	 * Initializes the <code>DocumentCacheFactory</code> with an alternate
	 * mechanism to the analytics.xml configuration file. Instead of using the
	 * other <code>loadConfig</code> method you may initialize the
	 * <code>DocumentCacheFactory</code> directly with the class name of the
	 * <code>DocumentCache</code> implementation you wish to use and it's
	 * corresponding configuration properties.
	 * <p>
	 * You are free to not use a <code>DocumentCache</code>, to use the
	 * <code>FileDocumentCacheOld</code>, or implement your own version of the
	 * <code>DocumentCache</code> interface. If you choose to use a
	 * <code>DocumentCache</code> then you must invoke the
	 * <code>loadConfig</code> method early in your applications startup.
	 * </p>
	 * <p>
	 * If you are using a this in a web application which uses a servlet 2.3 or
	 * higher API, then you can automatically invoke this method using the
	 * <code>CacheContextListener</code>.
	 * </p>
	 * 
	 * @param className
	 *            The class name of the <code>DocumentCache</code>
	 *            implementation.
	 * @param config
	 *            The configuration properties.
	 * @throws CacheException
	 *             if there was a problem configuring the
	 *             <code>DocumentCacheFactory</code>.
	 */
	public static void loadConfig(String className, Properties config)
			throws CacheException {
		try {
			initializeDocumentCacheFactory(className, config);
		} catch (ClassNotFoundException e) {
			throw new CacheException("Could not find '" + className + "'", e);
		} catch (InstantiationException e) {
			throw new CacheException("Could not instantiate '" + className
					+ "'", e);
		} catch (IllegalAccessException e) {
			throw new CacheException("Could not access '" + className + "'", e);
		}
	}

	/**
	 * Initialize the <code>DocumentCache</code> implementation.
	 * 
	 * @param className
	 *            The class name of the <code>DocumentCache</code>
	 *            implementation.
	 * @param config
	 *            The configuration properties for the
	 *            <code>DocumentCache</code> implementation.
	 * @throws ClassNotFoundException
	 * @throws InstantiationException
	 * @throws IllegalAccessException
	 * @throws CacheException
	 */
	private static void initializeDocumentCacheFactory(String className,
			Properties config) throws ClassNotFoundException,
			InstantiationException, IllegalAccessException, CacheException {
		Class c = Class.forName(className);
		documentCache = (DocumentCache) c.newInstance();
		documentCache.configure(config);
	}

	/**
	 * Creates or reuses the <code>DocumentCache</code> implementation. If the
	 * <code>DocumentCacheFactory</code> was not initialize through either of
	 * the <code>loadConfig</code> methods, then this method will return an do
	 * no caching <code>DocumentCache</code> implementation.
	 * 
	 * @return The <code>DocumentCache</code> implementation.
	 */
	public static DocumentCache createDocumentCache() {
		if (documentCache == null) {
			documentCache = new DocumentCacheAdapter();
		}
		return documentCache;
	}

	/**
	 * Inner class no caching <code>DocumentCache</code> implementation.
	 */
	public static class DocumentCacheAdapter implements DocumentCache {

		public void configure(Properties config) throws CacheException {
			// noop
		}

		public Document getDocument(CachedDocumentId id) throws CacheException {
			return null;
		}

		public void cacheDocument(Document document, CachedDocumentId id)
				throws CacheException {
			// noop
		}

		public InputStream getDocumentAsInputStream(CachedDocumentId id)
				throws CacheException {
			return null;
		}

	}

}
