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

import org.jahia.registries.ServicesRegistry;
import org.jahia.services.templates.JahiaTemplateManagerService;

/**
 * Helper class for looking up a resource in the template set hierarchy,
 * starting from the current template set root folder, then the parent template
 * set and so on.
 * 
 * <pre>
 * Consider the following example:
 *    
 *    We have:
 *        1) &quot;Basic templates&quot; set with the root folder &quot;basic&quot;
 *        2) &quot;Custom templates v1&quot; set with the root folder &quot;custom_v1&quot;. This template set extends the &quot;Basic templates&quot;
 *        3) Our current virtual site uses the &quot;Custom templates&quot; template set
 *    
 *    In one of our templates we make a call to this tag as follows:
 *        ...
 *            ${jahia.includes.templatePath['common/header.jsp']}
 *        ...
 *    
 *    The result will be as follows, if:
 *        1) the 'header.jsp' is found under custom_v1/common/header.jsp
 * 
 *                /templates/custom_v1/common/header.jsp
 * 
 *        2) the 'header.jsp' is found under basic/common/header.jsp  (in the parent template set)
 * 
 *                /templates/basic/common/header.jsp
 * 
 *        3) the 'header.jsp' is not found --&gt; the path remains unchanged
 * 
 *                common/header.jsp
 * </pre>
 * 
 * @author Sergiy Shyrkov
 * @see JahiaTemplateManagerService
 */
public class TemplatePathResolverBean extends LookupBaseBean<String, String> {

    public static final String REQUEST_ATTRIBUTE_NAME = "templatePathLookup";

    private String templatePackageName;

    /**
     * Initializes an instance of this class.
     * 
     * @param currentTemplatePackageName
     *            the template package name used by the current virtual site.
     */
    public TemplatePathResolverBean(String currentTemplatePackageName) {
        super();
        this.templatePackageName = currentTemplatePackageName;
    }

    /**
     * Resolves the specified path (which is related to the root folder of the
     * template set) into the actual path, considering template set inheritance.
     * 
     * @param key
     *            the resource path to resolve
     * @return the resolved path (context related) to the requested resource or
     *         the unchanged path, if it is not found
     */
    @Override
    public String get(Object key) {
        return lookup((String) key);
    }

    /**
     * Resolves the specified path (which is related to the root folder of the
     * template set) into the actual path, considering template set inheritance.
     * 
     * @param path
     *            the resource path to resolve
     * @return the resolved path (context related) to the requested resource or
     *         <code>null</code>, if the path is not found
     */
    public String lookup(String path) {
        return resolvePath(path);
    }

    /**
     * Resolves the path by looking up the specified path sequence (which is related to the root folder of the
     * template set) until it is found.
     * 
     * @param paths
     *            the resource path sequence to look into
     * @return the resolved path (context related) to the requested resource or
     *         <code>null</code>, if the path is not found
     */
    public String lookup(String... paths) {
        String resolvedPath = null;
        for (String testPath : paths) {
            resolvedPath = resolvePath(testPath);
            if (resolvedPath != null) {
                // we've found the resource
                break;
            }
        }
        return resolvedPath;
    }

    /**
     * Looks up the overridden JSP (from the parent template package) by the
     * specified servlet path name (current JSP path) or <code>null</code> if
     * not found.
     * 
     * @param jspPath
     *            current JSP path
     * @return the overridden JSP (from the parent template package) by the
     *         specified servlet path name (current JSP path) or
     *         <code>null</code> if not found
     */
    public String lookupOverridden(String jspPath) {
        return ServicesRegistry.getInstance().getJahiaTemplateManagerService()
                .resolveOverriddenResourcePathByServletPath(jspPath,
                        templatePackageName);
    }
    
    /**
     * Resolves the specified path (which is related to the root folder of the
     * template set) into the actual path, considering template set inheritance.
     * 
     * @param path
     *            the resource path to resolve
     * @return the resolved path (context related) to the requested resource or
     *         <code>null</code>, if it is not found
     */
    protected String resolvePath(String path) {
        return ServicesRegistry.getInstance().getJahiaTemplateManagerService()
                .resolveResourcePath(path, templatePackageName);
    }
}
