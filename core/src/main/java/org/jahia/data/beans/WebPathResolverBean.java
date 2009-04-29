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
 *                &lt;img src=&quot;/jahia/templates/custom_v1/common/logo.gif&quot; alt=&quot;logo&quot;/&gt;
 * 
 *        2) the 'logo.gif' is found under basic/common/logo.gif  (in the parent template set)
 * 
 *                &lt;img src=&quot;/jahia/templates/basic/common/logo.gif&quot; alt=&quot;logo&quot;/&gt;
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
