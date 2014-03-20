/**
 * ==========================================================================================
 * =                        DIGITAL FACTORY v7.0 - Community Distribution                   =
 * ==========================================================================================
 *
 *     Rooted in Open Source CMS, Jahia's Digital Industrialization paradigm is about
 *     streamlining Enterprise digital projects across channels to truly control
 *     time-to-market and TCO, project after project.
 *     Putting an end to "the Tunnel effect", the Jahia Studio enables IT and
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
 *
 * JAHIA'S DUAL LICENSING - IMPORTANT INFORMATION
 * ============================================
 *
 *     Copyright (C) 2002-2014 Jahia Solutions Group SA. All rights reserved.
 *
 *     THIS FILE IS AVAILABLE UNDER TWO DIFFERENT LICENSES:
 *     1/GPL OR 2/JSEL
 *
 *     1/ GPL
 *     ==========================================================
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
 *     describing the FLOSS exception, and it is also available here:
 *     http://www.jahia.com/license"
 *
 *     2/ JSEL - Commercial and Supported Versions of the program
 *     ==========================================================
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
package org.jahia.taglibs.template.pager;

import javax.servlet.jsp.JspException;
import javax.servlet.jsp.tagext.TagSupport;

import java.util.HashMap;
import java.util.Map;

/**
 * User: toto
 * Date: Dec 7, 2009
 * Time: 11:39:29 AM
 * 
 */
public class RemovePagerTag extends TagSupport {
    private static final long serialVersionUID = 1936952653989178313L;

    private String id;

    @Override
    public int doStartTag() throws JspException {
        return SKIP_BODY;
    }

    @Override
    public int doEndTag() throws JspException {
        @SuppressWarnings("unchecked")
        Map<String,Object> moduleMap  = (HashMap<String,Object>)  pageContext.getRequest().getAttribute("moduleMap");
        if (moduleMap == null) {
            moduleMap = new HashMap<String,Object>();
        }
        Object value = moduleMap.get("old_begin");
        if (value != null) {
            moduleMap.put("begin", value);
        }
        value = moduleMap.get("old_end"+id);
        if (value != null) {
            moduleMap.put("end", value);
        }
        value = moduleMap.get("old_pageSize");
        if (value != null) {
            moduleMap.put("pageSize", value);
        }
        value = moduleMap.get("old_nbPages"+id);
        if (value != null) {
            moduleMap.put("nbPages", value);
        }
        value = moduleMap.get("old_currentPage"+id);
        if (value != null) {
            moduleMap.put("currentPage", value);
        }
        value = moduleMap.get("old_paginationActive"+id);
        if (value != null) {
            moduleMap.put("paginationActive", value);
        }
        value = moduleMap.get("old_totalSize"+id);
        if (value != null) {
            moduleMap.put("totalSize", value);
        }
        return super.doEndTag();
    }

    public void setId(String id) {
        this.id = id;
    }
}