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
package org.jahia.services.uicomponents.bean.contentmanager;

import java.util.Locale;

import javax.servlet.http.HttpServletRequest;

import org.jahia.registries.ServicesRegistry;
import org.jahia.services.content.JCRNodeWrapper;
import org.jahia.services.uicomponents.bean.Visibility;
import org.jahia.services.usermanager.JahiaUser;

/**
 * Checks for the availability of the specified module (using bundle symbolic name).
 *
 * @author Sergiy Shyrkov
 * @deprecated
 */
public class ModuleAvailableVisibility extends Visibility {

    private String moduleId;

    @Override
    public boolean getRealValue(JCRNodeWrapper contextNode, JahiaUser jahiaUser, Locale locale,
            HttpServletRequest request) {
        return isModuleAvailable() && super.getRealValue(contextNode, jahiaUser, locale, request);
    }

    private boolean isModuleAvailable() {
        return ServicesRegistry.getInstance().getJahiaTemplateManagerService().getTemplatePackageById(moduleId) != null;
    }

    public void setModuleId(String webDavBundleId) {
        this.moduleId = webDavBundleId;
    }

}
