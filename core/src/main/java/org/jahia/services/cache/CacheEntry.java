/**
 * ==========================================================================================
 * =                   JAHIA'S DUAL LICENSING - IMPORTANT INFORMATION                       =
 * ==========================================================================================
 *
 *                                 http://www.jahia.com
 *
 *     Copyright (C) 2002-2018 Jahia Solutions Group SA. All rights reserved.
 *
 *     THIS FILE IS AVAILABLE UNDER TWO DIFFERENT LICENSES:
 *     1/GPL OR 2/JSEL
 *
 *     1/ GPL
 *     ==================================================================================
 *
 *     IF YOU DECIDE TO CHOOSE THE GPL LICENSE, YOU MUST COMPLY WITH THE FOLLOWING TERMS:
 *
 *     This program is free software: you can redistribute it and/or modify
 *     it under the terms of the GNU General Public License as published by
 *     the Free Software Foundation, either version 3 of the License, or
 *     (at your option) any later version.
 *
 *     This program is distributed in the hope that it will be useful,
 *     but WITHOUT ANY WARRANTY; without even the implied warranty of
 *     MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 *     GNU General Public License for more details.
 *
 *     You should have received a copy of the GNU General Public License
 *     along with this program. If not, see <http://www.gnu.org/licenses/>.
 *
 *
 *     2/ JSEL - Commercial and Supported Versions of the program
 *     ===================================================================================
 *
 *     IF YOU DECIDE TO CHOOSE THE JSEL LICENSE, YOU MUST COMPLY WITH THE FOLLOWING TERMS:
 *
 *     Alternatively, commercial and supported versions of the program - also known as
 *     Enterprise Distributions - must be used in accordance with the terms and conditions
 *     contained in a separate written agreement between you and Jahia Solutions Group SA.
 *
 *     If you are unsure which license is appropriate for your use,
 *     please contact the sales department at sales@jahia.com.
 */
package org.jahia.services.cache;

import java.util.Collections;
import java.util.Map;
import java.util.HashMap;
import java.util.Date;
import java.io.Serializable;


/** <p>This class represents an entry in a class.</p>
 * <p>Each entry holds an object and some statistics on that object,
 * like its expiration date, last accessed date and the amount of times it has
 * been request from the cache.</p>
 *
 * @author  Fulco Houkes, Copyright (c) 2003 by Jahia Ltd.
 * @version 1.0
 * @since   Jahia 4.0
 * @see     org.jahia.services.cache.Cache Cache
 */
public class CacheEntry<V> implements Serializable {

    private static final long serialVersionUID = -319255477144589731L;

    /** the entry object. */
    private V object;

    /** the properties table. */
    private Map<String, Serializable> properties;


    /** <p>Default constructor, creates a new <code>CacheEntry</code> instance.</p>
     */
    public CacheEntry () {
        super();
    }


    /** <p>Creates a new <code>CacheEntry</code> object instance.</p>
     *
     * @param entryObj      the object instance to associate with the cache entry
     */
    public CacheEntry (final V entryObj) {
        this();
        object = entryObj;
    }


    /** <p>Retrieves the object associated with this cache entry.</p>
     *
     * @return  the stored object
     */
    public V getObject () {
        return object;
    }


    /** <p>Sets/Updates the object associated to this cache entry.</p>
     *
     * @param object    the new object to store in this cache entry.
     */
    public void setObject (final V object) {
        this.object = object;
    }


    /** <p>Set the property of name <code>key</code> with the specified value
     * <code>value</code>.</p>
     *
     * <p>Properties can have a <code>null</code> value, but key names do not. When a property
     * already exists in the cache entry, the property's value is updated. When the
     * <code>name</code> is </code>null</code>, the set operation is canceled.</p>
     *
     * @param key  the property's key
     * @param value the property's value
     */
    public void setProperty (final String key, final Serializable value) {
        if (key == null) {
            return;
        }
        if (properties == null) {
            properties = new HashMap<String, Serializable>(1);
        }
        properties.put (key, value);
    }


    /** <p>Retrieve the property value associated with the key <code>key</code>.</p>
     *
     * <p>Properties can have a <code>null</code> value, but key names must be not
     * <code>null</code>. When value of <code>null</code> is returned, it does mean
     * the key is not found in the entry or the associated property has a
     * <code>null</code> value. The two cases can be distinguished with the
     * <code>containsKey()</code> method.</p>
     *
     * @param key  the property key
     *
     * @return  return the property value.
     */
    public Object getProperty (final String key) {
        return properties != null ? properties.get (key) : null;
    }
    
    /** <p>Returns <code>true</code> if this entry contains the mapping for the specified
     * property name <code>key</code>.</p>
     *
     * @param key   the property name
     *
     * @return <code>true</code> if this entry contains the mapping for the specified
     *          property name <code>key</code>.
     */
    public boolean containsKey (final String key) {
        if (key == null || properties == null) {
            return false;
        }
        return properties.containsKey (key);
    }


    /**
     * <p>Retrieves the properties <code>Map</code> instance.</p>
     *
     * @return  the properties <code>Map</code> instance
     */
    public Map<String, Serializable> getExtendedProperties() {
        return properties != null ? properties : Collections.<String, Serializable>emptyMap();
    }


    /** <p>Sets the new properties <code>Map</code> instance. The operation is
     * ignored if the <code>newProperties</code> is <code>null</code>.</p>
     *
     * @param newProperties the new properties
     */
    public void setExtendedProperties(final Map<String, Serializable> newProperties) {
        if (newProperties == null) {
            return;
        }
        properties = newProperties;
    }


    /** <p>Retrieves the number of hits of the entry (the number of times the
     * entry was requested).</p>
     *
     * @return  the entry's hits
     */
    @Deprecated
    final public int getHits () {
        return 0;
    }


    /** <p>Resets the number of times the entry was requested.</p>
     */
    @Deprecated
    final public void resetHits () {
    }


    /** <p>Increments the number of hits by one.</p>
     *
     */
    @Deprecated
    final public void incrementHits () {
        // do nothing
    }


    /** <p>Retrieves the entry's expiration date.</p>
     *
     * @return  the expiration date
     */
    @Deprecated
    final public Date getExpirationDate () {
        return null;
    }


    /** <p>Sets the entry's expiration date.</p>
     *
     * @param expirationDate    the expiration date
     */
    @Deprecated
    final public void setExpirationDate (Date expirationDate) {
        // do nothing
    }


    /** <p>Set the last accessed date to the current time.</p>
     */
    @Deprecated
    final public void setLastAccessedTimeNow () {
    }
    
    @Deprecated
    public long getLastAccessedTimeMillis() {
        return 0;
    }

}
