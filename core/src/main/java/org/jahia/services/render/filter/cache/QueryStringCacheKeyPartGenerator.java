/**
 * ==========================================================================================
 * =                   JAHIA'S DUAL LICENSING - IMPORTANT INFORMATION                       =
 * ==========================================================================================
 *
 *                                 http://www.jahia.com
 *
 *     Copyright (C) 2002-2018 Jahia Solutions Group SA. All rights reserved.
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
import org.jahia.utils.Patterns;

import javax.jcr.RepositoryException;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class QueryStringCacheKeyPartGenerator implements CacheKeyPartGenerator {

    protected static final Pattern QUERYSTRING_REGEXP = Pattern.compile("(.*)(_qs\\[([^\\]]+)\\]_)(.*)");

    @Override
    public String getKey() {
        return "queryString";
    }

    @Override
    public String getValue(Resource resource, RenderContext renderContext, Properties properties) {
        String property = properties.getProperty("cache.requestParameters");
        if (!StringUtils.isEmpty(property)) {
            try {
                property = property.replace("${currentNode.identifier}",resource.getNode().getIdentifier());
            } catch (RepositoryException e) {
                property = property.replace("${currentNode.identifier}","*");
            }
            String[] params = Patterns.COMMA.split(property);
            return "_qs" + Arrays.toString(params) + "_";
        } else {
            return "";
        }
    }

    @Override
    public String replacePlaceholders(RenderContext renderContext, String keyPart) {
        Map parameterMap = renderContext.getRequest().getParameterMap();
        if (!parameterMap.isEmpty()) {
            Matcher m = QUERYSTRING_REGEXP.matcher(keyPart);
            if (m.matches()) {
                String qsString = m.group(2);
                String[] params = Patterns.COMMA.split(m.group(3));

                SortedMap<String, String> qs = new TreeMap<String, String>();
                for (String param : params) {
                    param = param.trim();
                    if (param.endsWith("*")) {
                        param = param.substring(0, param.length() - 1);
                        for (Map.Entry o : (Iterable<? extends Map.Entry>) parameterMap.entrySet()) {
                            String k = (String) o.getKey();
                            if (k.startsWith(param)) {
                                qs.put(k, Arrays.toString((String[]) o.getValue()));
                            }
                        }
                    } else if (parameterMap.containsKey(param)) {
                        qs.put(param, Arrays.toString((String[]) parameterMap.get(param)));
                    }
                }
                keyPart = keyPart.replace(qsString, qs.toString());
            }
            return keyPart;
        }
        return "{}";
    }
}
