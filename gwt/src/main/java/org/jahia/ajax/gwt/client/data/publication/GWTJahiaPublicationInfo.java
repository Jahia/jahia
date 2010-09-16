/**
 * This file is part of Jahia: An integrated WCM, DMS and Portal Solution
 * Copyright (C) 2002-2010 Jahia Solutions Group SA. All rights reserved.
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
 * between you and Jahia Solutions Group SA. If you are unsure which license is appropriate
 * for your use, please contact the sales department at sales@jahia.com.
 */

package org.jahia.ajax.gwt.client.data.publication;

import org.jahia.ajax.gwt.client.data.SerializableBaseModel;

import java.util.*;

/**
 * Created by IntelliJ IDEA.
 * User: toto
 * Date: Sep 4, 2009
 * Time: 12:00:28 PM
 * To change this template use File | Settings | File Templates.
 */
public class GWTJahiaPublicationInfo extends SerializableBaseModel {

    public static final int PUBLISHED = 1;
    public static final int LOCKED = 2;
    public static final int MODIFIED = 3;
    public static final int NOT_PUBLISHED = 4;
    public static final int UNPUBLISHED = 5;
    public static final int MANDATORY_LANGUAGE_UNPUBLISHABLE = 6;
    public static final int LIVE_MODIFIED = 7;
    public static final int LIVE_ONLY = 8;
    public static final int CONFLICT = 9;
    public static final int MANDATORY_LANGUAGE_VALID = 10;
    
    private Set<Integer> subnodesStatus = new HashSet<Integer>();
    private Set<Integer> referencesStatus = new HashSet<Integer>();

    private List<GWTJahiaPublicationInfo> subnodes = new ArrayList<GWTJahiaPublicationInfo>();

    public GWTJahiaPublicationInfo() {
    }

    public GWTJahiaPublicationInfo(String path, int status, boolean canPublish) {
        setPath(path);
        setStatus(status);
        setCanPublish(canPublish);
    }

    public String getTitle() {
        return get("title");
    }

    public void setTitle(String path) {
        set("title", path);
    }

    public String getNodetype() {
        return get("nodetype");
    }

    public void setNodetype(String nodetype) {
        set("nodetype", nodetype);
    }

    public String getPath() {
        return get("path");
    }

    public void setPath(String path) {
        set("path", path);
    }

    public Integer getStatus() {
        return get("status");
    }

    public void setStatus(Integer status) {
        set("status", status);
    }

    public Set<Integer> getSubnodesStatus() {
        return subnodesStatus;
    }

    public void setSubnodesStatus(Set<Integer> subnodesStatus) {
        this.subnodesStatus = subnodesStatus;
    }

    public Set<Integer> getReferencesStatus() {
        return referencesStatus;
    }

    public void setReferencesStatus(Set<Integer> referencesStatus) {
        this.referencesStatus = referencesStatus;
    }

    public Boolean isCanPublish() {
        return get("canPublish");
    }

    public void setCanPublish(Boolean canPublish) {
        set("canPublish", canPublish);
    }

    public void addSubnode(GWTJahiaPublicationInfo subnode) {
        subnodes.add(subnode);
    }

    public List<GWTJahiaPublicationInfo> getSubnodes() {
        return subnodes;
    }


}
