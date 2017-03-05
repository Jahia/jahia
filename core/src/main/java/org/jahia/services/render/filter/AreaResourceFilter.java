/**
 * ==========================================================================================
 * =                   JAHIA'S DUAL LICENSING - IMPORTANT INFORMATION                       =
 * ==========================================================================================
 *
 *                                 http://www.jahia.com
 *
 *     Copyright (C) 2002-2017 Jahia Solutions Group SA. All rights reserved.
 *
 *     THIS FILE IS AVAILABLE UNDER TWO DIFFERENT LICENSES:
 *     1/GPL OR 2/JSEL
 *
 *     1/ GPL
 *     ==================================================================================
 *
 *     IF YOU DECIDE TO CHOOSE THE GPL LICENSE, YOU MUST COMPLY WITH THE FOLLOWING TERMS:
 *
 *     This program is free software: you can redistribute it and/or modify
 *     it under the terms of the GNU General Public License as published by
 *     the Free Software Foundation, either version 3 of the License, or
 *     (at your option) any later version.
 *
 *     This program is distributed in the hope that it will be useful,
 *     but WITHOUT ANY WARRANTY; without even the implied warranty of
 *     MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 *     GNU General Public License for more details.
 *
 *     You should have received a copy of the GNU General Public License
 *     along with this program. If not, see <http://www.gnu.org/licenses/>.
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
