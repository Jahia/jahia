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
