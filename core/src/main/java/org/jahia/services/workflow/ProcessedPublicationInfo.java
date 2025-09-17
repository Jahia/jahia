/*
 * ==========================================================================================
 * =                   JAHIA'S DUAL LICENSING - IMPORTANT INFORMATION                       =
 * ==========================================================================================
 *
 *                                 http://www.jahia.com
 *
 *     Copyright (C) 2002-2025 Jahia Solutions Group SA. All rights reserved.
 *
 *     THIS FILE IS AVAILABLE UNDER TWO DIFFERENT LICENSES:
 *     1/Apache2 OR 2/JSEL
 *
 *     1/ Apache2
 *     ==================================================================================
 *
 *     Copyright (C) 2002-2025 Jahia Solutions Group SA. All rights reserved.
 *
 *     Licensed under the Apache License, Version 2.0 (the "License");
 *     you may not use this file except in compliance with the License.
 *     You may obtain a copy of the License at
 *
 *       http://www.apache.org/licenses/LICENSE-2.0
 *
 *     Unless required by applicable law or agreed to in writing, software
 *     distributed under the License is distributed on an "AS IS" BASIS,
 *     WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *     See the License for the specific language governing permissions and
 *     limitations under the License.
 *
 *
 *     2/ JSEL - Commercial and Supported Versions of the program
 *     ===================================================================================
 *
 *     IF YOU DECIDE TO CHOOSE THE JSEL LICENSE, YOU MUST COMPLY WITH THE FOLLOWING TERMS:
 *
 *     Alternatively, commercial and supported versions of the program - also known as
 *     Enterprise Distributions - must be used in accordance with the terms and conditions
 *     contained in a separate written agreement between you and Jahia Solutions Group SA.
 *
 *     If you are unsure which license is appropriate for your use,
 *     please contact the sales department at sales@jahia.com.
 */
package org.jahia.services.workflow;

import java.io.Serializable;

public class ProcessedPublicationInfo implements Serializable {

    private Integer status;
    private String path;
    private String nodeType;
    private String title;
    private String uuid;
    private String workflowDefinition;
    private String mainPath;
    private String language;
    private Boolean locked;
    private Boolean workInProgress;
    private Boolean isAllowedToPublishWithoutWorkflow;
    private Boolean isNonRootMarkedForDeletion;
    private String i18nUuid;
    private String deletedI18nUuid;
    private Integer mainPathIndex;
    private String workflowGroup;
    private String workflowTitle;
    private String mainUUID;

    public Integer getStatus() {
        return status;
    }

    public void setStatus(Integer status) {
        this.status = status;
    }

    public String getPath() {
        return path;
    }

    public void setPath(String path) {
        this.path = path;
    }

    public String getNodeType() {
        return nodeType;
    }

    public void setNodeType(String nodeType) {
        this.nodeType = nodeType;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getUuid() {
        return uuid;
    }

    public void setUuid(String uuid) {
        this.uuid = uuid;
    }

    public String getWorkflowDefinition() {
        return workflowDefinition;
    }

    public void setWorkflowDefinition(String workflowDefinition) {
        this.workflowDefinition = workflowDefinition;
    }

    public String getMainPath() {
        return mainPath;
    }

    public void setMainPath(String mainPath) {
        this.mainPath = mainPath;
    }

    public String getLanguage() {
        return language;
    }

    public void setLanguage(String language) {
        this.language = language;
    }

    public Boolean getLocked() {
        return locked;
    }

    public void setLocked(Boolean locked) {
        this.locked = locked;
    }

    public Boolean getWorkInProgress() {
        return workInProgress;
    }

    public void setWorkInProgress(Boolean workInProgress) {
        this.workInProgress = workInProgress;
    }

    public Boolean getIsAllowedToPublishWithoutWorkflow() {
        return isAllowedToPublishWithoutWorkflow;
    }

    public void setIsAllowedToPublishWithoutWorkflow(Boolean isAllowedToPublishWithoutWorkflow) {
        this.isAllowedToPublishWithoutWorkflow = isAllowedToPublishWithoutWorkflow;
    }

    public Boolean getIsNonRootMarkedForDeletion() {
        return isNonRootMarkedForDeletion;
    }

    public void setIsNonRootMarkedForDeletion(Boolean isNonRootMarkedForDeletion) {
        this.isNonRootMarkedForDeletion = isNonRootMarkedForDeletion;
    }

    public String getI18nUuid() {
        return i18nUuid;
    }

    public void setI18nUuid(String i18nUuid) {
        this.i18nUuid = i18nUuid;
    }

    public String getDeletedI18nUuid() {
        return deletedI18nUuid;
    }

    public void setDeletedI18nUuid(String deletedI18nUuid) {
        this.deletedI18nUuid = deletedI18nUuid;
    }

    public Integer getMainPathIndex() {
        return mainPathIndex;
    }

    public void setMainPathIndex(Integer mainPathIndex) {
        this.mainPathIndex = mainPathIndex;
    }

    public String getWorkflowGroup() {
        return workflowGroup;
    }

    public void setWorkflowGroup(String workflowGroup) {
        this.workflowGroup = workflowGroup;
    }

    public String getWorkflowTitle() {
        return workflowTitle;
    }

    public void setWorkflowTitle(String workflowTitle) {
        this.workflowTitle = workflowTitle;
    }

    public String getMainUUID() {
        return mainUUID;
    }

    public void setMainUUID(String mainUUID) {
        this.mainUUID = mainUUID;
    }
}
