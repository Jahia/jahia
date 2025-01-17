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
package org.jahia.bundles.securityfilter.views;

import org.jahia.bin.Jahia;
import org.jahia.services.render.filter.RenderFilter;
import org.jahia.services.securityfilter.PermissionService;
import org.jahia.services.render.RenderContext;
import org.jahia.services.render.Resource;
import org.jahia.services.render.filter.RenderChain;
import org.osgi.framework.Constants;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Reference;
import org.osgi.service.component.annotations.ReferenceCardinality;

/**
 * Filter that checks permission configuration before rendering a view.
 */
@Component(
        service = RenderFilter.class,
        immediate = true,
        property = {
                Constants.SERVICE_DESCRIPTION + "=Security filter: Render filter that checks permission configuration before rendering a view",
                Constants.SERVICE_VENDOR + "=" + Jahia.VENDOR_NAME
        }
)
public class PermissionFilter extends BasePermissionFilter {

    public PermissionFilter() {
        super();
        setDescription("Filter that checks permission configuration before rendering a view");
        setApplyOnTemplateTypes("json,html");
    }

    protected PermissionService permissionService;

    @Reference(cardinality = ReferenceCardinality.MANDATORY)
    public void setPermissionService(PermissionService permissionService) {
        this.permissionService = permissionService;
    }

    @Override
    public String prepare(RenderContext renderContext, Resource resource, RenderChain chain) throws Exception {
        // Bypass the check if a specific permission has been defined on the view through the requirePermissions property
        if (hasViewRequirePermissions(renderContext)) {
            return null;
        }

        // Otherwise, check the API permissions rules
        String api = VIEW + "." + resource.getTemplateType() + "." + resource.getResolvedTemplate();
        if (!permissionService.hasPermission(api, resource.getNode())) {
            throw new PermissionSecurityAccessDeniedException(api, resource.getPath());
        }
        return null;
    }
}
