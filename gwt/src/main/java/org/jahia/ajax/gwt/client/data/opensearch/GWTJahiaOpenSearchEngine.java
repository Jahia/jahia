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
