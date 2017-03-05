/**
 * ==========================================================================================
 * =                   JAHIA'S DUAL LICENSING - IMPORTANT INFORMATION                       =
 * ==========================================================================================
 *
 *                                 http://www.jahia.com
 *
 *     Copyright (C) 2002-2017 Jahia Solutions Group SA. All rights reserved.
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
package org.apache.jackrabbit.core.jndi;

import org.apache.commons.collections.map.ReferenceMap;

import javax.jcr.RepositoryException;
import javax.naming.Context;
import javax.naming.Name;
import javax.naming.Reference;
import javax.naming.spi.ObjectFactory;
import java.util.Hashtable;
import java.util.Map;

/**
 * <code>JahiaBindableRepositoryFactory</code> is an object factory that when given
 * a reference for a <code>BindableRepository</code> object, will create an
 * instance of the corresponding  <code>BindableRepository</code>.
 */
public class JahiaBindableRepositoryFactory implements ObjectFactory {

    /**
     * cache using <code>java.naming.Reference</code> objects as keys and
     * storing soft references to <code>BindableRepository</code> instances
     */
    private static final Map cache = new ReferenceMap();

    /**
     * {@inheritDoc}
     */
    public synchronized Object getObjectInstance(
            Object obj, Name name, Context nameCtx, Hashtable environment)
            throws RepositoryException {
        synchronized (cache) {
            Object instance = cache.get(obj);
            if (instance == null && obj instanceof Reference) {
                instance = new JahiaBindableRepository((Reference) obj);
                cache.put(obj, instance);
            }
            return instance;
        }
    }

}
