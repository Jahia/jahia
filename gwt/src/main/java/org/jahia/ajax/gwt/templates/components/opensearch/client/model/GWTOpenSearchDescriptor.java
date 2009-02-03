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

package org.jahia.ajax.gwt.templates.components.opensearch.client.model;

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
public class GWTOpenSearchDescriptor implements Serializable {

    private Map<String,Namespace> namespaces;
    private String docPath;

    private String shortName;
    private String description;
    private List<Image> images;
    private List<URL> urls;

    public GWTOpenSearchDescriptor() {
    }

    public GWTOpenSearchDescriptor(Map namespaces, String docPath, String shortName, String description,
                                   List images, List urls) {
        this.namespaces = namespaces;
        this.docPath = docPath;
        this.shortName = shortName;
        this.description = description;
        this.images = images;
        this.urls = urls;
    }

    public Map<String,Namespace> getNamespaces() {
        return namespaces;
    }

    public void setNamespaces(Map<String,Namespace> namespaces) {
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

    public List<Image> getImages() {
        return images;
    }

    public void setImages(List<Image> images) {
        this.images = images;
    }

    public List<URL> getUrls() {
        return urls;
    }

    public void setUrls(List<URL> urls) {
        this.urls = urls;
    }
}
