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
import java.util.HashMap;

/**
 * Created by IntelliJ IDEA.
 * User: hollis
 * Date: 18 oct. 2007
 * Time: 17:18:14
 * To change this template use File | Settings | File Templates.
 */
public class GWTJahiaOpenSearchEngine implements Serializable {

    private String name;
    private String urlType;
    private String descriptorType;
    /* key is group name */
    private HashMap<String,Boolean> enabledStateByGroup;

    private GWTJahiaOpenSearchDescriptor descriptor;

    public GWTJahiaOpenSearchEngine() {
        enabledStateByGroup = new HashMap<String,Boolean>();
    }

    /**
     *
     * @param name
     * @param urlType
     * @param descriptorType
     * @param descriptor
     * @throws Exception
     */
    public GWTJahiaOpenSearchEngine(String name, String urlType,
                        String descriptorType, GWTJahiaOpenSearchDescriptor descriptor) {
        this.name = name;
        this.urlType = urlType;
        this.descriptorType = descriptorType;
        this.descriptor = descriptor;
        this.enabledStateByGroup = new HashMap<String,Boolean>();
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

    public GWTJahiaOpenSearchDescriptor getDescriptor() {
        return descriptor;
    }

    public String getDescriptorType() {
        return descriptorType;
    }

    public void setDescriptorType(String descriptorType) {
        this.descriptorType = descriptorType;
    }

    public void setDescriptor(GWTJahiaOpenSearchDescriptor descriptor) {
        this.descriptor = descriptor;
    }

    public boolean isEnabled(String groupName) {
        Boolean result = this.enabledStateByGroup.get(groupName);
        return result != null && result;
    }

    public void setEnabled(String groupName,boolean enabled) {
        if (groupName == null){
            return;
        }
        this.enabledStateByGroup.put(groupName,enabled);
    }

    public boolean supportRssSearchMode() {
        return ((urlType.indexOf("rss") != -1) || hasRssSearchURL());
    }

    private boolean hasRssSearchURL(){
        if (this.descriptor != null){
            for (GWTJahiaURL url : this.descriptor.getUrls()){
                if (url.getType().indexOf("rss") != -1){
                    return true;
                }
            }
        }
        return false;
    }
}
