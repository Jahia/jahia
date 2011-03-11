/**
 * This file is part of Jahia: An integrated WCM, DMS and Portal Solution
 * Copyright (C) 2002-2011 Jahia Solutions Group SA. All rights reserved.
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

package org.jahia.utils.i18n;

import org.slf4j.Logger;
import org.jahia.data.templates.JahiaTemplatesPackage;
import org.jahia.registries.ServicesRegistry;
import org.jahia.settings.SettingsBean;

import java.lang.ref.ReferenceQueue;
import java.lang.ref.SoftReference;
import java.lang.ref.WeakReference;
import java.text.MessageFormat;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

/**
 * Jahia implementation of the resource bundle, which considers template set inheritance.
 * User: rincevent
 * Date: 26 févr. 2009
 * Time: 17:45:02
 */
public class JahiaResourceBundle extends ResourceBundle {
    private transient static Logger logger = org.slf4j.LoggerFactory.getLogger(JahiaResourceBundle.class);
    private static final Locale EMPTY_LOCALE = new Locale("", "");
    private final String basename;
    private final Locale locale;
    private final JahiaTemplatesPackage templatesPackage;
    public static final String JAHIA_INTERNAL_RESOURCES = "JahiaInternalResources";
    private static final String MISSING_RESOURCE = "???";


    /**
     * The cache is a map from cache keys (with bundle base name, locale, and
     * class loader) to either a resource bundle or NONEXISTENT_BUNDLE wrapped by a
     * JahiaBundleReference.
     * <p/>
     * The cache is a ConcurrentMap, allowing the cache to be searched
     * concurrently by multiple threads.  This will also allow the cache keys
     * to be reclaimed along with the ClassLoaders they reference.
     * <p/>
     * This variable would be better named "cache", but we keep the old
     * name for compatibility with some workarounds for bug 4212439.
     */
    private static final ConcurrentMap<JahiaCacheKey, JahiaBundleReference> jahiaCacheList = new ConcurrentHashMap<JahiaCacheKey, JahiaBundleReference>(
            64);

    /**
     * Queue for reference objects referring to class loaders or bundles.
     */
    private static final ReferenceQueue jahiaReferenceQueue = new ReferenceQueue();


    public static void clearCache() {
        jahiaCacheList.clear();
    }
    
    public JahiaResourceBundle(Locale locale, String templatesPackageName) {
        this(null, locale, templatesPackageName, null);
    }

    public JahiaResourceBundle(String basename, Locale locale) {
        this(basename, locale, null, null);
    }

    public JahiaResourceBundle(Locale locale, String templatesPackageName, ClassLoader classLoader) {
        this(null, locale, templatesPackageName, classLoader);
    }

    public JahiaResourceBundle(String basename, Locale locale, String templatesPackageName) {
        this(basename, locale, templatesPackageName, null);
    }

    public JahiaResourceBundle(String basename, Locale locale, String templatesPackageName, ClassLoader classLoader) {
        this.basename = basename;
        this.locale = locale;
        this.templatesPackage = templatesPackageName != null ? ServicesRegistry.getInstance().getJahiaTemplateManagerService().getTemplatePackage(
                templatesPackageName) : null;
    }

    @Override
    public Object handleGetObject(String s) {
        final JahiaTemplatesRBLoader templatesRBLoader = getClassLoader();
        Object o = null;
        if (basename != null) {
            try {
                o = lookupBundle(basename, locale, templatesRBLoader).getString(s);
            } catch (MissingResourceException e) {
                logger.debug("Not found '" + s + "' in the base resource bundle '" + basename + "'");
            }
        }
        if (o == null && templatesPackage != null) {
            final List<String> stringList = templatesPackage.getResourceBundleHierarchy();
            for (String bundleToLookup : stringList) {
                if (basename != null && basename.equals(bundleToLookup)) {
                    // we did the lookup in this bundle already
                    continue;
                }
                try {
                    o = lookupBundle(bundleToLookup, locale, templatesRBLoader).getString(s);
                    if (o != null) {
                        break;
                    }
                } catch (MissingResourceException e1) {
                    logger.debug("Try to find '" + s + "' in resource bundle '" + bundleToLookup + "'");
                }
            }
        }
        if (o == null) {
            throw new MissingResourceException("Cannot find resource " + s, basename, s);
        }


        return o;
    }

    /**
     * Returns an enumeration of the keys.
     *
     * @return an <code>Enumeration</code> of the keys contained in
     *         this <code>ResourceBundle</code> and its parent bundles.
     */
    public Enumeration<String> getKeys() {
        return lookupBundle(basename, locale, getClassLoader()).getKeys();
    }

    /**
     * @return
     */
    private JahiaTemplatesRBLoader getClassLoader() {
        String templatesPackageName = templatesPackage != null ? templatesPackage.getName() : null;
        return templatesPackageName != null ? JahiaTemplatesRBLoader.getInstance(
                Thread.currentThread().getContextClassLoader(), templatesPackageName) : null;
    }

    /**
     * Shortcut methods to call a resource key from the engine resource bundle
     *
     * @param key,          the key to search inside the JahiaInternalResources bundle
     * @param locale,       the locale in which we want to find the key
     * @param defaultValue, the defaultValue (surrounded by ???) if not found
     * @return the resource in locale language or defaultValue surrounded by (???)
     */
    public static String getJahiaInternalResource(String key, Locale locale, String defaultValue) {
        final ResourceBundle resourceBundle = lookupBundle(JAHIA_INTERNAL_RESOURCES, locale);
        try {
            String value = resourceBundle.getString(key);
            return value;
        } catch (MissingResourceException e) {
            return defaultValue != null ? defaultValue : (MISSING_RESOURCE + key + MISSING_RESOURCE);
        }
    }

    /**
     * Get message by key and local
     *
     * @param key
     * @param locale
     * @return
     */
    public static String getJahiaInternalResource(String key, Locale locale) {
        return getJahiaInternalResource(key, locale, null);
    }

    /**
     * Get message depending on the key
     *
     * @param bundle
     * @param key
     * @param locale
     * @param templatePackageName
     * @return
     */
    public static String getString(String bundle, String key, Locale locale, String templatePackageName) {
        return new JahiaResourceBundle(bundle, locale, templatePackageName,
                                       Thread.currentThread().getContextClassLoader()).getString(key);
    }

    /**
     * Get message depending on the key
     *
     * @param bundle
     * @param key
     * @param locale
     * @param templatePackageName
     * @param loader
     * @return
     */
    public static String getString(String bundle, String key, Locale locale, String templatePackageName,
                                   ClassLoader loader) {
        return new JahiaResourceBundle(bundle, locale, templatePackageName, loader).getString(key);
    }

    /**
     * find  ResourceBundle dependinng on a baseName
     *
     * @param baseName
     * @param locale
     * @return
     */
    private static ResourceBundle lookupBundle(String baseName, Locale locale) {
        return lookupBundle(baseName, locale, null);
    }

    /**
     * find  ResourceBundle dependinng on a baseName
     *
     * @param baseName
     * @param preferredLocale
     * @param loader
     * @return
     */
    private static ResourceBundle lookupBundle(String baseName, Locale preferredLocale, ClassLoader loader) {
        JahiaCacheKey cacheKey = new JahiaCacheKey(baseName, preferredLocale, loader);
        ResourceBundle bundle = null;

        // Quick lookup of the cache.
        JahiaBundleReference bundleRef = jahiaCacheList.get(cacheKey);
        if (bundleRef != null) {
            bundle = bundleRef.get();
            bundleRef = null;
            if (bundle != null) return bundle;
        }
        bundle = loader != null ? ResourceBundle.getBundle(baseName, preferredLocale,
                                                           loader) : ResourceBundle.getBundle(baseName,
                                                                                              preferredLocale);
        jahiaCacheList.put(cacheKey, new JahiaBundleReference(bundle, jahiaReferenceQueue, cacheKey));


        ResourceBundle match = null;

        if (!SettingsBean.getInstance().isConsiderDefaultJVMLocale()) {
            Locale availableLocale = bundle.getLocale();
            if (availableLocale.equals(preferredLocale)) {
                match = bundle;
            } else if (preferredLocale.getLanguage().equals(
                    availableLocale.getLanguage()) && (availableLocale.getCountry().length() == 0 || preferredLocale.getCountry().equals(
                    availableLocale.getCountry()))) {
                match = bundle;
            }
            if (match == null && !EMPTY_LOCALE.equals(preferredLocale)) {
                match = lookupBundle(baseName, EMPTY_LOCALE, loader);
            }
        } else {
            match = bundle;
        }

        return match;
    }

    /**
     * Get message depending on a key. If not found, return the default value
     *
     * @param key
     * @param defaultValue
     * @return
     */
    public String get(String key, String defaultValue) {
        return getString(key, defaultValue);
    }

    /**
     * Get formateed message
     *
     * @param key
     * @param defaultValue
     * @param arguments
     * @return
     */
    public String getFormatted(String key, String defaultValue, Object... arguments) {
    	String text = get(key, defaultValue);
    	if (text != null) {
    		text = text.replace("'", "''");
    	}
        String value = MessageFormat.format(text, arguments);


        return value;
    }

    /**
     * Get message depending on a key. If not found, return the default value
     *
     * @param key
     * @param defaultValue
     * @return
     */
    public String getString(String key, String defaultValue) {
        String message;
        try {
            message = getString(key);

        } catch (MissingResourceException e) {
            message = defaultValue;
        }
        return message;
    }

    /**
     * Returns names of bundles where the key is looked up.
     *
     * @return list of bundles where the key is looked up
     */
    public List<String> getLookupBundles() {
        List<String> bundles = new LinkedList<String>();
        if (basename != null) {
            bundles.add(basename);
        }
        if (templatesPackage != null) {
            List<String> bundleHierarchy = templatesPackage.getResourceBundleHierarchy();
            for (String name : bundleHierarchy) {
                if (basename == null || !basename.equals(name)) {
                    bundles.add(name);
                }
            }
        }
        return bundles;
    }

    /**
     * Key used for cached resource bundles.  The key checks the base
     * name, the locale, and the class loader to determine if the
     * resource is a match to the requested one. The loader may be
     * null, but the base name and the locale must have a non-null
     * value.
     */
    private static final class JahiaCacheKey implements Cloneable {
        // These three are the actual keys for lookup in Map.
        private String name;
        private Locale locale;
        private JahiaLoaderReference loaderRef;

        // bundle format which is necessary for calling
        // Control.needsReload().
        private String format;

        // These time values are in JahiaCacheKey so that NONEXISTENT_BUNDLE
        // doesn't need to be cloned for caching.

        // The time when the bundle has been loaded
        private volatile long loadTime;

        // The time when the bundle expires in the cache, or either
        // Control.TTL_DONT_CACHE or Control.TTL_NO_EXPIRATION_CONTROL.
        private volatile long expirationTime;

        // Placeholder for an error report by a Throwable
        private Throwable cause;

        // Hash code value cache to avoid recalculating the hash code
        // of this instance.
        private int hashCodeCache;

        JahiaCacheKey(String baseName, Locale locale, ClassLoader loader) {
            this.name = baseName;
            this.locale = locale;
            if (loader == null) {
                this.loaderRef = null;
            } else {
                loaderRef = new JahiaLoaderReference(loader, jahiaReferenceQueue, this);
            }
            calculateHashCode();
        }

        String getName() {
            return name;
        }

        JahiaCacheKey setName(String baseName) {
            if (!this.name.equals(baseName)) {
                this.name = baseName;
                calculateHashCode();
            }
            return this;
        }

        Locale getLocale() {
            return locale;
        }

        JahiaCacheKey setLocale(Locale locale) {
            if (!this.locale.equals(locale)) {
                this.locale = locale;
                calculateHashCode();
            }
            return this;
        }

        ClassLoader getLoader() {
            return (loaderRef != null) ? loaderRef.get() : null;
        }

        public boolean equals(Object other) {
            if (this == other) {
                return true;
            }
            try {
                final JahiaCacheKey otherEntry = (JahiaCacheKey) other;
                //quick check to see if they are not equal
                if (hashCodeCache != otherEntry.hashCodeCache) {
                    return false;
                }
                //are the names the same?
                if (!name.equals(otherEntry.name)) {
                    return false;
                }
                // are the locales the same?
                if (!locale.equals(otherEntry.locale)) {
                    return false;
                }
                //are refs (both non-null) or (both null)?
                if (loaderRef == null) {
                    return otherEntry.loaderRef == null;
                }
                ClassLoader loader = loaderRef.get();
                return (otherEntry.loaderRef != null)
                       // with a null reference we can no longer find
                       // out which class loader was referenced; so
                       // treat it as unequal
                       && (loader != null) && (loader == otherEntry.loaderRef.get());
            } catch (NullPointerException e) {
            } catch (ClassCastException e) {
            }
            return false;
        }

        public int hashCode() {
            return hashCodeCache;
        }

        private void calculateHashCode() {
            hashCodeCache = name.hashCode() << 3;
            hashCodeCache ^= locale.hashCode();
            ClassLoader loader = getLoader();
            if (loader != null) {
                hashCodeCache ^= loader.hashCode();
            }
        }

        public Object clone() {
            try {
                JahiaCacheKey clone = (JahiaCacheKey) super.clone();
                if (loaderRef != null) {
                    clone.loaderRef = new JahiaLoaderReference(loaderRef.get(), jahiaReferenceQueue, clone);
                }
                // Clear the reference to a Throwable
                clone.cause = null;
                return clone;
            } catch (CloneNotSupportedException e) {
                //this should never happen
                throw new InternalError();
            }
        }

        String getFormat() {
            return format;
        }

        void setFormat(String format) {
            this.format = format;
        }

        private void setCause(Throwable cause) {
            if (this.cause == null) {
                this.cause = cause;
            } else {
                // Override the cause if the previous one is
                // ClassNotFoundException.
                if (this.cause instanceof ClassNotFoundException) {
                    this.cause = cause;
                }
            }
        }

        private Throwable getCause() {
            return cause;
        }

        public String toString() {
            String l = locale.toString();
            if (l.length() == 0) {
                if (locale.getVariant().length() != 0) {
                    l = "__" + locale.getVariant();
                } else {
                    l = "\"\"";
                }
            }
            return "JahiaCacheKey[" + name + ", lc=" + l + ", ldr=" + getLoader() + "(format=" + format + ")]";
        }
    }

    /**
     * The common interface to get a JahiaCacheKey in JahiaLoaderReference and
     * JahiaBundleReference.
     */
    private static interface JahiaCacheKeyReference {
        public JahiaCacheKey getCacheKey();
    }

    /**
     * References to class loaders are weak references, so that they can be
     * garbage collected when nobody else is using them. The ResourceBundle
     * class has no reason to keep class loaders alive.
     */
    private static final class JahiaLoaderReference extends WeakReference<ClassLoader>
            implements JahiaCacheKeyReference {
        private JahiaCacheKey cacheKey;

        JahiaLoaderReference(ClassLoader referent, ReferenceQueue q, JahiaCacheKey key) {
            super(referent, q);
            cacheKey = key;
        }

        public JahiaCacheKey getCacheKey() {
            return cacheKey;
        }
    }

    /**
     * References to bundles are soft references so that they can be garbage
     * collected when they have no hard references.
     */
    private static final class JahiaBundleReference extends SoftReference<ResourceBundle>
            implements JahiaCacheKeyReference {
        private JahiaCacheKey cacheKey;

        JahiaBundleReference(ResourceBundle referent, ReferenceQueue q, JahiaCacheKey key) {
            super(referent, q);
            cacheKey = key;
        }

        public JahiaCacheKey getCacheKey() {
            return cacheKey;
        }
    }
}