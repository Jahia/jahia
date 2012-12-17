/**
 * This file is part of Jahia, next-generation open source CMS:
 * Jahia's next-generation, open source CMS stems from a widely acknowledged vision
 * of enterprise application convergence - web, search, document, social and portal -
 * unified by the simplicity of web content management.
 *
 * For more information, please visit http://www.jahia.com.
 *
 * Copyright (C) 2002-2012 Jahia Solutions Group SA. All rights reserved.
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
 * Commercial and Supported Versions of the program (dual licensing):
 * alternatively, commercial and supported versions of the program may be used
 * in accordance with the terms and conditions contained in a separate
 * written agreement between you and Jahia Solutions Group SA.
 *
 * If you are unsure which license is appropriate for your use,
 * please contact the sales department at sales@jahia.com.
 */

package org.jahia.services.render.filter.cache;

import org.jahia.services.render.RenderContext;
import org.jahia.services.render.Resource;
import org.jahia.utils.Patterns;

import javax.servlet.http.HttpServletRequest;
import java.util.Arrays;
import java.util.Map;
import java.util.SortedMap;
import java.util.TreeMap;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class QueryStringCacheKeyPartGenerator implements CacheKeyPartGenerator {

    protected static final Pattern QUERYSTRING_REGEXP = Pattern.compile("(.*)(_qs\\[([^\\]]+)\\]_)(.*)");

    @Override
    public String getKey() {
        return "queryString";
    }

    @Override
    public String getValue(Resource resource, RenderContext renderContext) {
        HttpServletRequest request = renderContext.getRequest();
        String[] params = (String[]) request.getAttribute("cache.requestParameters");
        if (params != null && params.length > 0) {
            return "_qs" + Arrays.toString(params) + "_";
        } else {
            return "";
        }
    }

    @Override
    public String replacePlaceholders(RenderContext renderContext, String keyPart) {
        Matcher m = QUERYSTRING_REGEXP.matcher(keyPart);
        if (m.matches()) {
            Map parameterMap = renderContext.getRequest().getParameterMap();
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
}
