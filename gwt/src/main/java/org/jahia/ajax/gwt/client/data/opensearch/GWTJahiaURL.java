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
 * in Jahia's FLOSS exception. You should have received a copy of the text
 * describing the FLOSS exception, and it is also available here:
 * http://www.jahia.com/license
 * 
 * Commercial and Supported Versions of the program
 * Alternatively, commercial and supported versions of the program may be used
 * in accordance with the terms contained in a separate written agreement
 * between you and Jahia Limited. If you are unsure which license is appropriate
 * for your use, please contact the sales department at sales@jahia.com.
 */

package org.jahia.ajax.gwt.client.data.opensearch;

import java.util.List;
import java.io.Serializable;

/**
 * Created by IntelliJ IDEA.
 * User: hollis
 * Date: 19 oct. 2007
 * Time: 10:52:07
 * To change this template use File | Settings | File Templates.
 */
public class GWTJahiaURL implements Serializable {

    public static final String TEMPLATE_ATTRIBUTE = "template";
    public static final String TYPE_ATTRIBUE = "type";
    public static final String INDEX_OFFSET_ATTRIBUTE = "indexOffset";
    public static final String PAGE_OFFSET_ATTRIBUTE = "pageOffset";

    private GWTJahiaURLTemplate template;
    private String type;
    private int indexOffset;
    private int pageOffset;
    private List<GWTJahiaParameter> parameters;

    public GWTJahiaURL() {
    }

    public GWTJahiaURL(GWTJahiaURLTemplate template, String type, int indexOffset, int pageOffset, List parameters) {
        this.template = template;
        this.type = type;
        this.indexOffset = indexOffset;
        this.pageOffset = pageOffset;
        this.parameters = parameters;
    }

    public GWTJahiaURLTemplate getTemplate() {
        return template;
    }

    public void setTemplate(GWTJahiaURLTemplate template) {
        this.template = template;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public int getIndexOffset() {
        return indexOffset;
    }

    public void setIndexOffset(int indexOffset) {
        this.indexOffset = indexOffset;
    }

    public int getPageOffset() {
        return pageOffset;
    }

    public void setPageOffset(int pageOffset) {
        this.pageOffset = pageOffset;
    }

    public List getParameters() {
        return parameters;
    }

    public void setParameters(List parameters) {
        this.parameters = parameters;
    }
}
