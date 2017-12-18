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
package org.jahia.utils.i18n;

import java.io.IOException;
import java.io.InputStream;
import java.util.MissingResourceException;
import java.util.PropertyResourceBundle;

/**
 * Slightly modified implementation of the {@link PropertyResourceBundle} that is not throwing {@link MissingResourceException} in case the
 * key is not found.
 * 
 * @author Sergiy Shyrkov
 */
class JahiaPropertyResourceBundle extends PropertyResourceBundle {

    JahiaPropertyResourceBundle(InputStream stream) throws IOException {
        super(stream);
    }

    /**
     * Gets an object for the given key from this resource bundle or one of its parents. This method first tries to obtain the object from
     * this resource bundle using {@link #handleGetObject(java.lang.String) handleGetObject}. If not successful, and the parent resource
     * bundle is not null, it calls the parent's <code>getObject</code> method. If still not successful, it returns <code>null</code>.
     * 
     * @param key
     *            the key for the desired object
     * @exception NullPointerException
     *                if <code>key</code> is <code>null</code>
     * @return the object for the given key or <code>null</code> if it cannot be found in the resource bundle or its parent
     */
    String getStringInternal(String key) {
        Object obj = handleGetObject(key);
        if (obj == null && parent != null) {
            if (parent instanceof JahiaPropertyResourceBundle) {
                obj = ((JahiaPropertyResourceBundle) parent).getStringInternal(key);
            } else {
                try {
                    obj = parent.getObject(key);
                } catch (MissingResourceException e) {
                    // suppress it
                }
            }
        }

        return (String) obj;
    }

}
