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
package org.jahia.services.opensearch;

/**
 * Created by IntelliJ IDEA.
 * User: hollis
 * Date: 10 sept. 2008
 * Time: 17:01:54
 * To change this template use File | Settings | File Templates.
 */
public class SearchEngineBean {

    private String name;
    private String urlType;
    private String descriptorType;
    private String descriptorFile;
    private OpenSearchDescriptor descriptor;

    public SearchEngineBean() {
    }

    public SearchEngineBean(String name, String urlType, String descriptorType, String descriptorFile) {
        this.name = name;
        this.urlType = urlType;
        this.descriptorType = descriptorType;
        this.descriptorFile = descriptorFile;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getUrlType() {
        return urlType;
    }

    public void setUrlType(String urlType) {
        this.urlType = urlType;
    }

    public String getDescriptorType() {
        return descriptorType;
    }

    public void setDescriptorType(String descriptorType) {
        this.descriptorType = descriptorType;
    }

    public String getDescriptorFile() {
        return descriptorFile;
    }

    public void setDescriptorFile(String descriptorFile) {
        this.descriptorFile = descriptorFile;
    }

    public OpenSearchDescriptor getDescriptor() {
        return descriptor;
    }

    public void setDescriptor(OpenSearchDescriptor descriptor) {
        this.descriptor = descriptor;
    }
}
