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
package org.jahia.services.render.filter;

import org.jahia.api.Constants;
import org.jahia.services.content.JCRNodeWrapper;
import org.jahia.services.render.RenderContext;
import org.jahia.services.render.Resource;

import javax.servlet.http.HttpServletRequest;

/**
 * This render filter adds the area resource in the request params
 * to be able to calculate contribute buttons in contribute mode.
 *
 * This filter is only used in contribute mode.
 *
 * areaResource and areaListResource parameters are only read by ModuleTag in contribute mode
 *
 * User: david
 * Date: 1/17/11
 * Time: 4:47 PM
 */
public class AreaResourceFilter extends AbstractFilter {

    @Override
    public String prepare(RenderContext renderContext, Resource resource, RenderChain chain) throws Exception {
        JCRNodeWrapper node = resource.getNode();
        final HttpServletRequest request = renderContext.getRequest();
        if (node.isNodeType("jnt:area") || node.isNodeType("jnt:mainResourceDisplay")) {
            chain.pushAttribute(request, "areaListResource", resource.getNode());
        } else if (node.isNodeType(Constants.JAHIAMIX_LIST)) {
            if (request.getAttribute("areaListResource") == null) {
                chain.pushAttribute(request, TemplateAttributesFilter.AREA_RESOURCE, resource.getNode());
            } else {
                chain.pushAttribute(request, TemplateAttributesFilter.AREA_RESOURCE, request.getAttribute("areaListResource"));
            }
            chain.pushAttribute(request, "areaListResource", null);
        }
        return null;
    }
}
