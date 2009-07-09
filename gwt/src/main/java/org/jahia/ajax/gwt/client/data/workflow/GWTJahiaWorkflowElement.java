/**
 * This file is part of Jahia: An integrated WCM, DMS and Portal Solution
 * Copyright (C) 2002-2009 Jahia Solutions Group SA. All rights reserved.
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
package org.jahia.ajax.gwt.client.data.workflow;

import com.extjs.gxt.ui.client.data.BaseTreeModel;
import com.extjs.gxt.ui.client.data.ModelData;

import java.io.Serializable;
import java.util.Map;
import java.util.Set;

import org.jahia.ajax.gwt.client.data.GWTJahiaNodeOperationResult;

/**
 * Created by IntelliJ IDEA.
 *
 * @author rfelden
 * @version 16 juil. 2008 - 16:10:16
 */
public class GWTJahiaWorkflowElement extends BaseTreeModel implements Serializable, ModelData {

    public final static String PAGE_TYPE = "ContentPage" ;
    public final static String CONTAINER_TYPE = "ContentContainer" ;
    public final static String CONTAINERLIST_TYPE = "ContentContainerList" ;

    static final public int FAILED_OPERATION_STATUS = 0;
    static final public int COMPLETED_OPERATION_STATUS = 1;
    static final public int PARTIAL_OPERATION_STATUS = 2;

    private Map<String, String> workflowStates ;
    private boolean validationBlocker = false ;
    private Map<String, GWTJahiaNodeOperationResult> validation = null ;
    private String stealLock = null ;
    private Map<String, Set<String>> availableAction;

    private boolean canBypassWaiChecks = false ;
    private boolean canBypassUrlChecks = false ;
    private boolean canBypassOtherChecks = false ;

    private boolean accessibleInTable = true ;


    public GWTJahiaWorkflowElement() {
        super() ;
    }

    public GWTJahiaWorkflowElement(String id) {
        super() ;
        setObjectKey(id);
    }

    public GWTJahiaWorkflowElement(int pid, String id, String type, String title, String path, boolean hasChildren, Map<String, String> wfStatus) {
        super() ;
        setPid(Integer.valueOf(pid)) ;
        setObjectKey(id);
        setObjectType(type);
        setTitle(title);
        setPath(path);
        setHasChildren(Boolean.valueOf(hasChildren));
        setWorkflowStates(wfStatus);
        //setCompareLink(compareLink);
    }

    public boolean hasChildren() {
        Boolean hasChildren = hasChildrenObj() ;
        if (hasChildren != null) {
            return hasChildren.booleanValue() ;
        } else {
            return false ;
        }
    }

    private Boolean hasChildrenObj() {
        return get("hasChildren") ;
    }

    public void setHasChildren(boolean hasChildren) {
        set("hasChildren", Boolean.valueOf(hasChildren)) ;
    }

    public String getObjectKey() {
        return get("objectKey") ;
    }

    public void setObjectKey(String objectKey) {
        set("objectKey", objectKey);
    }

    public String getObjectType() {
        return get("objectType") ;
    }

    public void setObjectType(String objectType) {
        set("objectType", objectType) ;
    }

    public String getPath() {
        return get("path");
    }

    public void setPath(String path) {
        set("path", path) ;
    }

    public int getPid() {
        Integer objPid = getObjPid() ;
        if (objPid != null) {
            return objPid.intValue();
        } else {
            return -1 ;
        }
    }

    private Integer getObjPid() {
        return get("pid") ;
    }

    public void setPid(int pid) {
        set("pid", Integer.valueOf(pid)) ;
    }

    public String getTitle() {
        return get("title");
    }

    public void setTitle(String title) {
        set("title", title) ;
    }

    public Map<String, String> getWorkflowStates() {
        return workflowStates;
    }

    public void setWorkflowStates(Map<String, String> workflowStates) {
        this.workflowStates = workflowStates;
    }

    public boolean isValidationBlocker() {
        return validationBlocker;
    }

    public void setValidationBlocker(boolean validationBlocker) {
        this.validationBlocker = validationBlocker;
    }

    public Map<String, GWTJahiaNodeOperationResult> getValidation() {
        return validation;
    }

    public void setValidation(Map<String, GWTJahiaNodeOperationResult> validation) {
        this.validation = validation;
    }

    public boolean canBypassOtherChecks() {
        return canBypassOtherChecks;
    }

    public void setCanBypassOtherChecks(boolean canBypassOtherChecks) {
        this.canBypassOtherChecks = canBypassOtherChecks;
    }

    public boolean canBypassUrlChecks() {
        return canBypassUrlChecks;
    }

    public void setCanBypassUrlChecks(boolean canBypassUrlChecks) {
        this.canBypassUrlChecks = canBypassUrlChecks;
    }

    public boolean canBypassWaiChecks() {
        return canBypassWaiChecks;
    }

    public void setCanBypassWaiChecks(boolean canBypassWaiChecks) {
        this.canBypassWaiChecks = canBypassWaiChecks;
    }

    public String getStealLock() {
        return stealLock;
    }

    public void setStealLock(String stealLock) {
        this.stealLock = stealLock;
    }

    public Map<String, Set<String>> getAvailableAction() {
        return availableAction;
    }

    public void setAvailableAction(Map<String, Set<String>> availableAction) {
        this.availableAction = availableAction;
    }

    public void setCompareLink(String link) {
        set("comparelink", link) ;
    }

    public String getCompareLink() {
        return get("comparelink") ;
    }

    public boolean isAccessibleInTable() {
        return accessibleInTable;
    }

    public void setAccessibleInTable(boolean accessibleInTable) {
        this.accessibleInTable = accessibleInTable;
    }
}