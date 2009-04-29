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

    private static final long serialVersionUID = -490258542682246910L;
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

    public GWTJahiaURL(GWTJahiaURLTemplate template, String type, int indexOffset, int pageOffset, List<GWTJahiaParameter> parameters) {
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

    public List<GWTJahiaParameter> getParameters() {
        return parameters;
    }

    public void setParameters(List<GWTJahiaParameter> parameters) {
        this.parameters = parameters;
    }
}
