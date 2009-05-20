/**
 * Jahia Enterprise Edition v6
 *
 * Copyright (C) 2002-2009 Jahia Solutions Group. All rights reserved.
 *
 * Jahia delivers the first Open Source Web Content Integration Software by combining Enterprise Web Content Management
 * with Document Management and Portal features.
 *
 * The Jahia Enterprise Edition is delivered ON AN "AS IS" BASIS, WITHOUT WARRANTY OF ANY KIND, EITHER EXPRESSED OR
 * IMPLIED.
 *
 * Jahia Enterprise Edition must be used in accordance with the terms contained in a separate license agreement between
 * you and Jahia (Jahia Sustainable Enterprise License - JSEL).
 *
 * If you are unsure which license is appropriate for your use, please contact the sales department at sales@jahia.com.
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
