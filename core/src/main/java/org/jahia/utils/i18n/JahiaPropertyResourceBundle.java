/*
 * ==========================================================================================
 * =                   JAHIA'S DUAL LICENSING - IMPORTANT INFORMATION                       =
 * ==========================================================================================
 *
 *                                 http://www.jahia.com
 *
 *     Copyright (C) 2002-2023 Jahia Solutions Group SA. All rights reserved.
 *
 *     THIS FILE IS AVAILABLE UNDER TWO DIFFERENT LICENSES:
 *     1/Apache2 OR 2/JSEL
 *
 *     1/ Apache2
 *     ==================================================================================
 *
 *     Copyright (C) 2002-2023 Jahia Solutions Group SA. All rights reserved.
 *
 *     Licensed under the Apache License, Version 2.0 (the "License");
 *     you may not use this file except in compliance with the License.
 *     You may obtain a copy of the License at
 *
 *       http://www.apache.org/licenses/LICENSE-2.0
 *
 *     Unless required by applicable law or agreed to in writing, software
 *     distributed under the License is distributed on an "AS IS" BASIS,
 *     WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *     See the License for the specific language governing permissions and
 *     limitations under the License.
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
