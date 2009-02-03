/**
 * 
 * This file is part of Jahia: An integrated WCM, DMS and Portal Solution
 * Copyright (C) 2002-2009 Jahia Limited. All rights reserved.
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
 * in Jahia's FLOSS exception. You should have recieved a copy of the text
 * describing the FLOSS exception, and it is also available here:
 * http://www.jahia.com/license"
 * 
 * Commercial and Supported Versions of the program
 * Alternatively, commercial and supported versions of the program may be used
 * in accordance with the terms contained in a separate written agreement
 * between you and Jahia Limited. If you are unsure which license is appropriate
 * for your use, please contact the sales department at sales@jahia.com.
 */

package org.jahia.data.beans;

import org.jahia.services.templates.JahiaTemplateManagerService;

/**
 * Helper class for looking up a Web resource (CSS or JavaScript file, image
 * etc.) in the template set hierarchy, starting from the current template set
 * root folder, then the parent template set and so on.
 * 
 * <pre>
 * Consider the following example:
 *    
 *    We have:
 *        1) &quot;Basic templates&quot; set with the root folder &quot;basic&quot;
 *        2) &quot;Custom templates v1&quot; set with the root folder &quot;custom_v1&quot;. This template set extends the &quot;Basic templates&quot;
 *        3) Our current virtual site uses the &quot;Custom templates&quot; template set
 *        4) Web application context is &quot;/jahia&quot;
 *    
 *    In one of our templates we make a call to this tag as follows:
 *        ...
 *            &lt;img src=&quot;${jahia.includes.webPath['common/logo.gif']}&quot; alt=&quot;logo&quot;/&gt;
 *        ...
 *    
 *    The result will be as follows, if:
 *        1) the 'logo.gif' is found under custom_v1/common/logo.gif 
 * 
 *                &lt;img src=&quot;/jahia/jsp/jahia/templates/custom_v1/common/logo.gif&quot; alt=&quot;logo&quot;/&gt;
 * 
 *        2) the 'logo.gif' is found under basic/common/logo.gif  (in the parent template set)
 * 
 *                &lt;img src=&quot;/jahia/jsp/jahia/templates/basic/common/logo.gif&quot; alt=&quot;logo&quot;/&gt;
 * 
 *        3) the 'logo.gif' is not found --&gt; the path remains unchanged
 * 
 *                &lt;img src=&quot;common/logo.gif&quot; alt=&quot;logo&quot;/&gt;
 * </pre>
 * 
 * @author Sergiy Shyrkov
 * @see JahiaTemplateManagerService
 */
public class WebPathResolverBean extends TemplatePathResolverBean {

    public static final String REQUEST_ATTRIBUTE_NAME = "webPathLookup";

    private String contextPath;

    /**
     * Initializes an instance of this class.
     * 
     * @param currentTemplatePackageName
     *            the template package name used by the current virtual site
     * @param contextPath
     *            current Web context path
     */
    public WebPathResolverBean(String currentTemplatePackageName,
            String contextPath) {
        super(currentTemplatePackageName);
        this.contextPath = contextPath;
    }

    /**
     * Resolves the specified path (which is related to the root folder of the
     * template set) into the actual Web path, considering template set
     * inheritance.
     * 
     * @param key
     *            the resource path to resolve
     * @return the resolved path (with Web context included) to the requested
     *         resource or the unchanged path, if it is not found
     */
    @Override
    public String get(Object key) {
        String path = (String) key;
        String resolvedPath = resolvePath(path);

        return resolvedPath != null ? contextPath + resolvedPath : path;
    }
}
