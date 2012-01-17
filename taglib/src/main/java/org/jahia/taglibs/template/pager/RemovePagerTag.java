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

package org.jahia.taglibs.template.pager;

import javax.servlet.jsp.JspException;
import javax.servlet.jsp.PageContext;
import javax.servlet.jsp.tagext.TagSupport;
import java.util.HashMap;
import java.util.Map;

/**
 * 
 * User: toto
 * Date: Dec 7, 2009
 * Time: 11:39:29 AM
 * 
 */
public class RemovePagerTag extends TagSupport {
    private String prefix;

    private int pageSize;
    private long totalSize;

    private String id;

    public void setPageSize(int pageSize) {
        this.pageSize = pageSize;
    }

    public void setTotalSize(int totalSize) {
        this.totalSize = totalSize;
    }

    @Override
    public int doStartTag() throws JspException {
        return SKIP_BODY;
    }

    @Override
    public int doEndTag() throws JspException {
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