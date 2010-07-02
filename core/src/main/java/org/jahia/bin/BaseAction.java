/**
 * This file is part of Jahia: An integrated WCM, DMS and Portal Solution
 * Copyright (C) 2002-2010 Jahia Solutions Group SA. All rights reserved.
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

package org.jahia.bin;

import java.util.List;
import java.util.Map;

import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang.StringUtils;

/**
 * Base implementation of the {@link Action}.
 * 
 * @author Sergiy Shyrkov
 */
public abstract class BaseAction implements Action {

    /**
     * Returns a single value for the specified parameter. If the parameter is
     * not present or its value is empty, returns <code>null</code>.
     * 
     * @param parameters the map of action parameters
     * @param paramName the name of the parameter in question
     * @return a single value for the specified parameter. If the parameter is
     *         not present or its value is empty, returns <code>null</code>
     */
    public static String getParameter(Map<String, List<String>> parameters, String paramName) {
        return getParameter(parameters, paramName, null);
    }

    /**
     * Returns a single value for the specified parameter. If the parameter is
     * not present or its value is empty, returns the provided default value.
     * 
     * @param parameters the map of action parameters
     * @param paramName the name of the parameter in question
     * @param defaultValue the default value to be used if the parameter is not
     *            present or its value is empty
     * @return a single value for the specified parameter. If the parameter is
     *         not present or its value is empty, returns the provided default
     *         value
     */
    public static String getParameter(Map<String, List<String>> parameters, String paramName, String defaultValue) {
        List<String> vals = parameters.get(paramName);
        return CollectionUtils.isNotEmpty(vals) && StringUtils.isNotEmpty(vals.get(0)) ? vals.get(0) : defaultValue;
    }

    private String name;

    /*
     * (non-Javadoc)
     * 
     * @see org.jahia.bin.Action#getName()
     */
    public String getName() {
        return name;
    }

    /**
     * Sets the action name.
     * 
     * @param name the action name
     */
    public void setName(String name) {
        this.name = name;
    }

}
