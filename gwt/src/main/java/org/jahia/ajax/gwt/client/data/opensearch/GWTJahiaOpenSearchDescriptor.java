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

import java.io.Serializable;
import java.util.List;
import java.util.Map;

/**
 * Created by IntelliJ IDEA.
 * User: hollis
 * Date: 19 oct. 2007
 * Time: 09:35:53
 * To change this template use File | Settings | File Templates.
 */
public class GWTJahiaOpenSearchDescriptor implements Serializable {

    private Map<String, GWTJahiaNamespace> namespaces;
    private String docPath;

    private String shortName;
    private String description;
    private List<GWTJahiaImage> images;
    private List<GWTJahiaURL> urls;

    public GWTJahiaOpenSearchDescriptor() {
    }

    public GWTJahiaOpenSearchDescriptor(Map namespaces, String docPath, String shortName, String description,
                                   List images, List urls) {
        this.namespaces = namespaces;
        this.docPath = docPath;
        this.shortName = shortName;
        this.description = description;
        this.images = images;
        this.urls = urls;
    }

    public Map<String, GWTJahiaNamespace> getNamespaces() {
        return namespaces;
    }

    public void setNamespaces(Map<String, GWTJahiaNamespace> namespaces) {
        this.namespaces = namespaces;
    }

    public String getDocPath() {
        return docPath;
    }

    public void setDocPath(String docPath) {
        this.docPath = docPath;
    }

    public String getShortName() {
        return shortName;
    }

    public void setShortName(String shortName) {
        this.shortName = shortName;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public List<GWTJahiaImage> getImages() {
        return images;
    }

    public void setImages(List<GWTJahiaImage> images) {
        this.images = images;
    }

    public List<GWTJahiaURL> getUrls() {
        return urls;
    }

    public void setUrls(List<GWTJahiaURL> urls) {
        this.urls = urls;
    }
}
