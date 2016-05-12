/**
 * ==========================================================================================
 * =                   JAHIA'S DUAL LICENSING - IMPORTANT INFORMATION                       =
 * ==========================================================================================
 *
 *                                 http://www.jahia.com
 *
 *     Copyright (C) 2002-2016 Jahia Solutions Group SA. All rights reserved.
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
package org.jahia.services.render.filter.cache;

import org.apache.commons.lang.StringUtils;
import org.jahia.services.render.RenderContext;
import org.jahia.services.render.Resource;

import java.util.Properties;

import javax.servlet.http.HttpServletRequest;

/**
 * Key part generator to store node type restrictions in cache key to be able to re-add them in the request attributes.
 *
 * These restrictions are directly handled in the ModuleTag; we need to restore them to be able to re-calculate nodes that should'nt be displayed
 * by the ModuleTag.
 *
 * Created by jkevan on 22/04/2016.
 */
public class NodeTypesRestrictionKeyPartGenerator implements CacheKeyPartGenerator, RenderContextTuner {

    @Override
    public String getKey() {
        return "nodeTypesRestriction";
    }

    @Override
    public String getValue(Resource resource, RenderContext renderContext, Properties properties) {
        HttpServletRequest request = renderContext.getRequest();
        Integer level = (Integer) request.getAttribute("org.jahia.modules.level");
        if (level != null) {
            String restrictions = (String) request.getAttribute("areaNodeTypesRestriction" + level);
            if (StringUtils.isNotEmpty(restrictions)) {
                return restrictions + "*" + level;
            }
        }
        return "";
    }

    @Override
    public String replacePlaceholders(RenderContext renderContext, String keyPart) {
        return keyPart;
    }

    @Override
    public Object prepareContextForContentGeneration(String value, Resource resource, RenderContext renderContext) {
        HttpServletRequest request = renderContext.getRequest();
        request.removeAttribute("areaNodeTypesRestriction" + request.getAttribute("org.jahia.modules.level"));
        if (StringUtils.isNotEmpty(value)) {
            String[] values = value.split("\\*");
            Integer level = Integer.parseInt(values[1]);
            String restrictions = values[0];
            request.setAttribute("org.jahia.modules.level", level);
            request.setAttribute("areaNodeTypesRestriction" + level, restrictions);
        }
        return null;
    }

    @Override
    public void restoreContextAfterContentGeneration(String value, Resource resource, RenderContext renderContext, Object original) {
    }
}
