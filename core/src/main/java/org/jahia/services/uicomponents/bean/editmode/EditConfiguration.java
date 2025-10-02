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
package org.jahia.services.uicomponents.bean.editmode;

import java.util.Set;

/**
 * Core representation of the edit configuration
 */
public class EditConfiguration {

    String name;
    String defaultUrlMapping;
    String defaultLocation;
    Set<String> skipMainModuleTypesDomParsing;
    Set<String> editableTypes;
    Set<String> nonEditableTypes;
    Set<String> visibleTypes;
    Set<String> nonVisibleTypes;
    Set<String> bypassModeForTypes;
    String requiredPermission;
    String nodeCheckPermission;

    boolean forceHeaders;

    public String getName() {
        return name;
    }

    public Set<String> getBypassModeForTypes() {
        return bypassModeForTypes;
    }

    public String getDefaultUrlMapping() {
        return defaultUrlMapping;
    }

    public String getDefaultLocation() {
        return defaultLocation;
    }

    public Set<String> getSkipMainModuleTypesDomParsing() {
        return skipMainModuleTypesDomParsing;
    }

    public Set<String> getEditableTypes() {
        return editableTypes;
    }

    public Set<String> getNonEditableTypes() {
        return nonEditableTypes;
    }

    public Set<String> getVisibleTypes() {
        return visibleTypes;
    }

    public Set<String> getNonVisibleTypes() {
        return nonVisibleTypes;
    }

    public boolean isForceHeaders() {
        return forceHeaders;
    }

    public String getRequiredPermission() {
        return requiredPermission;
    }

    public String getNodeCheckPermission() {
        return nodeCheckPermission;
    }

    public void setName(String name) {
        this.name = name;
    }

    public void setDefaultUrlMapping(String defaultUrlMapping) {
        this.defaultUrlMapping = defaultUrlMapping;
    }

    public void setDefaultLocation(String defaultLocation) {
        this.defaultLocation = defaultLocation;
    }

    public void setSkipMainModuleTypesDomParsing(Set<String> skipMainModuleTypesDomParsing) {
        this.skipMainModuleTypesDomParsing = skipMainModuleTypesDomParsing;
    }

    public void setEditableTypes(Set<String> editableTypes) {
        this.editableTypes = editableTypes;
    }

    public void setNonEditableTypes(Set<String> nonEditableTypes) {
        this.nonEditableTypes = nonEditableTypes;
    }

    public void setVisibleTypes(Set<String> visibleTypes) {
        this.visibleTypes = visibleTypes;
    }

    public void setNonVisibleTypes(Set<String> nonVisibleTypes) {
        this.nonVisibleTypes = nonVisibleTypes;
    }

    public void setBypassModeForTypes(Set<String> bypassModeForTypes) {
        this.bypassModeForTypes = bypassModeForTypes;
    }

    public void setRequiredPermission(String requiredPermission) {
        this.requiredPermission = requiredPermission;
    }

    public void setNodeCheckPermission(String nodeCheckPermission) {
        this.nodeCheckPermission = nodeCheckPermission;
    }

    public void setForceHeaders(boolean forceHeaders) {
        this.forceHeaders = forceHeaders;
    }
}
