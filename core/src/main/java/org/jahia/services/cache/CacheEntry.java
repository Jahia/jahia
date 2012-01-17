/**
 * This file is part of Jahia, next-generation open source CMS:
 * Jahia's next-generation, open source CMS stems from a widely acknowledged vision
 * of enterprise application convergence - web, search, document, social and portal -
 * unified by the simplicity of web content management.
 *
 * For more information, please visit http://www.jahia.com.
 *
 * Copyright (C) 2002-2012 Jahia Solutions Group SA. All rights reserved.
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
 * Commercial and Supported Versions of the program (dual licensing):
 * alternatively, commercial and supported versions of the program may be used
 * in accordance with the terms and conditions contained in a separate
 * written agreement between you and Jahia Solutions Group SA.
 *
 * If you are unsure which license is appropriate for your use,
 * please contact the sales department at sales@jahia.com.
 */

package org.jahia.services.cache;

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

    /** the normal operation mode constant. */
    public static final String MODE_NORMAL = "normal";

    /** the debugging operation mode constant. */
    public static final String MODE_DEBUG = "debug";

    /** the edition operation mode constant. */
    public static final String MODE_EDIT = "edit";


    /** the entry object. */
    private V object;


    /** number of time the entry was requested. */
    protected int hits = 0;

    /** the operation mode. */
    protected String operationMode = ""; // this can take the values
    // "normal", "edit" or "debug" (see ProcessingContext defined modes)

    /** the properties table. */
    protected Map<String, Serializable> properties = new HashMap<String, Serializable>();

    /** the entry's expiration date. */
    protected Date expirationDate;

    /** last accessed date. */
    protected long lastAccessedTimeMillis;


    /** <p>Default constructor, creates a new <code>CacheEntry</code> instance.</p>
     */
    public CacheEntry () {
    }


    /** <p>Creates a new <code>CacheEntry</code> object instance.</p>
     *
     * @param entryObj      the object instance to associate with the cache entry
     */
    public CacheEntry (final V entryObj) {
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
        if (key == null)
            return;
        properties.put (key, value);
    }


    /** <p>Retrieve the property value associated with the key <code>key</code>.</p>
     *
     * <p>Propertiescan have a <code>null</code> value, but key names must be not
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
        return properties.get (key);
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
        if (key == null)
            return false;
        return properties.containsKey (key);
    }


    /** <p>Retrieves the properties <code>Map</code> instance.</p>
     *
     * @return  the properties <code>Map</code> instance
     */
    public Map<String, Serializable> getExtendedProperties () {
        return properties;
    }


    /** <p>Sets the new properties <code>Map</code> instance. The operation is
     * ignored if the <code>newProperties</code> is <code>null</code>.</p>
     *
     * @param newProperties the new properties
     */
    public void setExtendedProperties (final Map<String, Serializable> newProperties) {
        if (newProperties == null)
            return;
        properties = newProperties;
    }


    /** <p>Retrieves the number of hits of the entry (the number of times the
     * entry was requested).</p>
     *
     * @return  the entry's hits
     */
    final public int getHits () {
        return hits;
    }


    /** <p>Resets the number of times the entry was requested.</p>
     */
    final public void resetHits () {
        hits = 0;
    }


    /** <p>Increments the number of hits by one.</p>
     * */
    final public void incrementHits () {
        hits++;
    }


    /** <p>Retrieve the operation mode.</p>
     *
     * <p>Returned values are <code>MODE_NORMAL</code>, <code>MODE_EDIT</code>
     * or <code>MODE_DEBUG</code>, corresponding respectively to the normal
     * operation mode, the edition mode and debugginf mode.</p>
     *
     * @return  the operation mode.
     */
    final public String getOperationMode () {
        return this.operationMode;
    }


    /** <p>Sets the operation mode.</p>
     *
     * <p>Accepted values are <code>MODE_NORMAL</code>, <code>MODE_EDIT</code>
     * or <code>MODE_DEBUG</code>, corresponding respectively to the normal
     * operation mode, the edition mode and debugginf mode.</p>
     *
     * @param   opMode  the operation mode
     */
    final public void setOperationMode (final String opMode) {
        this.operationMode = opMode;
    }


    /** <p>Retrieves the entry's expiration date.</p>
     *
     * @return  the expiration date
     */
    final public Date getExpirationDate () {
        return expirationDate;
    }


    /** <p>Sets the entry's expiration date.</p>
     *
     * @param expirationDate    the expiration date
     */
    final public void setExpirationDate (Date expirationDate) {
        this.expirationDate = expirationDate;
    }


    /** <p>Set the last accessed date to the current time.</p>
     */
    final public void setLastAccessedTimeNow () {
        lastAccessedTimeMillis = System.currentTimeMillis();
        // lastAccessedDate = new Date ();
    }
    public long getLastAccessedTimeMillis() {
        return lastAccessedTimeMillis;
    }

}
