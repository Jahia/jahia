/**
 * This file is part of Jahia: An integrated WCM, DMS and Portal Solution
 * Copyright (C) 2002-2010 Jahia Solutions Group SA. All rights reserved.
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

package org.jahia.services.render.filter.cache;

import java.text.FieldPosition;
import java.text.MessageFormat;
import java.text.ParseException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.LinkedHashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;

import org.apache.commons.collections.Predicate;
import org.apache.commons.collections.SetUtils;
import org.jahia.services.render.RenderContext;
import org.jahia.services.render.Resource;

/**
 * Default implementation of the module output cache key generator.
 * 
 * @author Sergiy Shyrkov
 */
public class DefaultCacheKeyGenerator implements CacheKeyGenerator {

    private static final Set<String> KNOWN_FIELDS = new LinkedHashSet<String>(Arrays.asList(new String[] { "workspace",
            "language", "path", "template", "templateType", "queryString" }));

    private List<String> fieldList = new ArrayList<String>(KNOWN_FIELDS);
    private Set<String> fields = new LinkedHashSet<String>(KNOWN_FIELDS);

    private MessageFormat format = new MessageFormat("#{0}#{1}#{2}#{3}#{4}#{5}");

    public String generate(Resource resource, RenderContext renderContext) {
        return format.format(getArguments(resource, renderContext), new StringBuffer(32), new FieldPosition(0))
                .toString();
    }

    private Object[] getArguments(Resource resource, RenderContext renderContext) {
        List<String> args = new LinkedList<String>();
        for (String field : fields) {
            if ("workspace".equals(field)) {
                args.add(resource.getWorkspace());
            } else if ("language".equals(field)) {
                args.add(resource.getLocale().toString());
            } else if ("path".equals(field)) {
                args.add(resource.getNode().getPath());
            } else if ("template".equals(field)) {
                args.add(resource.getResolvedTemplate());
            } else if ("templateType".equals(field)) {
                args.add(resource.getTemplateType());
            } else if ("queryString".equals(field)) {
                args.add(renderContext.getRequest().getQueryString());
            }
        }
        return args.toArray(new String[] {});
    }

    public String getPath(String key) throws ParseException {
        return parse(key)[fieldList.indexOf("path")];
    }

    public String[] parse(String key) throws ParseException {
        Object[] values = format.parse(key);
        String[] result = new String[values.length];
        System.arraycopy(values, 0, result, 0, values.length);
        return result;
    }

    public String replaceField(String key, String fieldName, String newValue) throws ParseException {
        String[] args = parse(key);
        args[fieldList.indexOf(fieldName)] = newValue;
        return format.format(args, new StringBuffer(32), new FieldPosition(0)).toString();
    }

    @SuppressWarnings("unchecked")
    public void setFields(Set<String> fields) {
        this.fields = SetUtils.predicatedSet(fields, new Predicate() {
            public boolean evaluate(Object object) {
                return (object instanceof String) && KNOWN_FIELDS.contains(object);
            }
        });
        fieldList = new ArrayList<String>(this.fields);
    }

    public void setFormat(String format) {
        this.format = new MessageFormat(format);
    }

}
