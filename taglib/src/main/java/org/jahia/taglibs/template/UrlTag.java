/**
 * ==========================================================================================
 * =                   JAHIA'S DUAL LICENSING - IMPORTANT INFORMATION                       =
 * ==========================================================================================
 *
 *     Copyright (C) 2002-2015 Jahia Solutions Group SA. All rights reserved.
 *
 *     THIS FILE IS AVAILABLE UNDER TWO DIFFERENT LICENSES:
 *     1/GPL OR 2/JSEL
 *
 *     1/ GPL
 *     ======================================================================================
 *
 *     IF YOU DECIDE TO CHOSE THE GPL LICENSE, YOU MUST COMPLY WITH THE FOLLOWING TERMS:
 *
 *     "This program is free software; you can redistribute it and/or
 *     modify it under the terms of the GNU General Public License
 *     as published by the Free Software Foundation; either version 2
 *     of the License, or (at your option) any later version.
 *
 *     This program is distributed in the hope that it will be useful,
 *     but WITHOUT ANY WARRANTY; without even the implied warranty of
 *     MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 *     GNU General Public License for more details.
 *
 *     You should have received a copy of the GNU General Public License
 *     along with this program; if not, write to the Free Software
 *     Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston, MA 02110-1301, USA.
 *
 *     As a special exception to the terms and conditions of version 2.0 of
 *     the GPL (or any later version), you may redistribute this Program in connection
 *     with Free/Libre and Open Source Software ("FLOSS") applications as described
 *     in Jahia's FLOSS exception. You should have received a copy of the text
 *     describing the FLOSS exception, also available here:
 *     http://www.jahia.com/license"
 *
 *     2/ JSEL - Commercial and Supported Versions of the program
 *     ======================================================================================
 *
 *     IF YOU DECIDE TO CHOOSE THE JSEL LICENSE, YOU MUST COMPLY WITH THE FOLLOWING TERMS:
 *
 *     Alternatively, commercial and supported versions of the program - also known as
 *     Enterprise Distributions - must be used in accordance with the terms and conditions
 *     contained in a separate written agreement between you and Jahia Solutions Group SA.
 *
 *     If you are unsure which license is appropriate for your use,
 *     please contact the sales department at sales@jahia.com.
 *
 *
 * ==========================================================================================
 * =                                   ABOUT JAHIA                                          =
 * ==========================================================================================
 *
 *     Rooted in Open Source CMS, Jahia’s Digital Industrialization paradigm is about
 *     streamlining Enterprise digital projects across channels to truly control
 *     time-to-market and TCO, project after project.
 *     Putting an end to “the Tunnel effect”, the Jahia Studio enables IT and
 *     marketing teams to collaboratively and iteratively build cutting-edge
 *     online business solutions.
 *     These, in turn, are securely and easily deployed as modules and apps,
 *     reusable across any digital projects, thanks to the Jahia Private App Store Software.
 *     Each solution provided by Jahia stems from this overarching vision:
 *     Digital Factory, Workspace Factory, Portal Factory and eCommerce Factory.
 *     Founded in 2002 and headquartered in Geneva, Switzerland,
 *     Jahia Solutions Group has its North American headquarters in Washington DC,
 *     with offices in Chicago, Toronto and throughout Europe.
 *     Jahia counts hundreds of global brands and governmental organizations
 *     among its loyal customers, in more than 20 countries across the globe.
 *
 *     For more information, please visit http://www.jahia.com
 */
package org.jahia.taglibs.template;

import org.apache.taglibs.standard.tag.common.core.Util;

import java.util.Arrays;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;
import java.util.regex.Pattern;

import javax.servlet.jsp.JspException;

/**
 * JSP tag for creating URLs. Inheriting from the JSTL c:url tag, but adds the possibility
 * to create the query string with the parameters of the current request (with ability to set
 * filters)
 *
 * @author Benjamin Papez
 *
 */
public class UrlTag extends org.apache.taglibs.standard.tag.el.core.UrlTag {
    /** The serialVersionUID. */
    private static final long serialVersionUID = 6171288389547456742L;

    private Set<String> addedParams;
    
    private Set<String> excludeParams;
    
    private Pattern paramIncludePattern;
    
    private boolean useRequestParams = false;

    /**
     * Initializes an instance of this class.
     */
    public UrlTag() {
        super();
        init();
    }

    public void setExcludeParams(String excludeParams) {
        this.excludeParams = new HashSet<String>(Arrays.asList(excludeParams.replace(" ", "").split(",")));
    }

    public void setParamIncludeRegex(String paramIncludeRegex) {
        this.paramIncludePattern = Pattern.compile(paramIncludeRegex);
    }
    
    public void setUseRequestParams(boolean useRequestParams) {
        this.useRequestParams = useRequestParams;
    }

    @Override
    public int doEndTag() throws JspException {
        if (useRequestParams) {
            for (Iterator<Map.Entry<String, String[]>> it = pageContext
                    .getRequest().getParameterMap().entrySet().iterator(); it
                    .hasNext();) {
                Map.Entry<String, String[]> requestParameter = it.next();
                if ((addedParams == null || !addedParams
                        .contains(requestParameter.getKey()))
                        && (excludeParams == null || !excludeParams
                                .contains(requestParameter.getKey()))
                        && (paramIncludePattern == null || paramIncludePattern
                                .matcher(requestParameter.getKey()).matches())) {
                    for (String value : requestParameter.getValue()) {
                        String enc = pageContext.getResponse().getCharacterEncoding();
                        super.addParameter(Util.URLEncode(requestParameter.getKey(), enc), Util.URLEncode(value, enc));
                    }
                }
            }
        }
        
        return super.doEndTag();
    }

    @Override
    public void addParameter(String name, String value) {
        if (addedParams == null) {
            addedParams = new HashSet<String>();
        }
        addedParams.add(name);
        super.addParameter(name, value);
    }

    @Override
    // Releases any resources we may have (or inherit)
    public void release() {
        super.release();
        init();
    }

    // (re)initializes state (during release() or construction)
    private void init() {
        // null implies "no expression"
        addedParams = null;
        excludeParams = null;
        paramIncludePattern = null;
        useRequestParams = false;
    }
    
}
