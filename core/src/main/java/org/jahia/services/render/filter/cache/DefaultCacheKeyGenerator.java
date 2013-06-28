/**
 * This file is part of Jahia, next-generation open source CMS:
 * Jahia's next-generation, open source CMS stems from a widely acknowledged vision
 * of enterprise application convergence - web, search, document, social and portal -
 * unified by the simplicity of web content management.
 *
 * For more information, please visit http://www.jahia.com.
 *
 * Copyright (C) 2002-2013 Jahia Solutions Group SA. All rights reserved.
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

import org.apache.commons.codec.digest.DigestUtils;
import org.apache.commons.lang.StringUtils;
import org.apache.jackrabbit.util.Base64;
import org.jahia.services.render.RenderContext;
import org.jahia.services.render.Resource;
import org.jahia.settings.SettingsBean;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.StringWriter;
import java.io.UnsupportedEncodingException;
import java.util.*;

/**
 * Default implementation of the module output cache key generator.
 *
 * @author rincevent
 * @author Sergiy Shyrkov
 */
public class DefaultCacheKeyGenerator implements CacheKeyGenerator {
    private static Logger logger = LoggerFactory.getLogger(DefaultCacheKeyGenerator.class);
    private List<CacheKeyPartGenerator> partGenerators;
    private List<String> fields;

    public List<CacheKeyPartGenerator> getPartGenerators() {
        return partGenerators;
    }

    public void setPartGenerators(List<CacheKeyPartGenerator> partGenerators) {
        this.partGenerators = partGenerators;
        this.fields = new ArrayList<String>();
        for (CacheKeyPartGenerator generator : partGenerators) {
            fields.add(generator.getKey());
        }
    }

    public String generate(Resource resource, RenderContext renderContext, Properties properties) {
        return StringUtils.join(getArguments(resource, renderContext, properties), "@@");
    }

    private Object[] getArguments(Resource resource, RenderContext renderContext, Properties properties) {
        List<String> args = new LinkedList<String>();
<<<<<<< .working
        for (CacheKeyPartGenerator generator : partGenerators) {
            args.add(generator.getValue(resource, renderContext, properties));
=======
        for (String field : fields) {
            if ("workspace".equals(field)) {
                args.add(encodeString(resource.getWorkspace()));
            } else if ("language".equals(field)) {
                args.add(encodeString(resource.getLocale().toString()));
            } else {
                if ("path".equals(field)) {
                    StringBuilder s = new StringBuilder(resource.getNode().getPath());
                    if (Boolean.TRUE.equals(request.getAttribute("cache.mainResource"))) {
                        s.append(MAIN_RESOURCE_KEY);
                    }
                    args.add(encodeString(s.toString()));
                } else if ("template".equals(field)) {
                    if (resource.getContextConfiguration().equals("page") && resource.getNode().getPath().equals(
                            renderContext.getMainResource().getNode().getPath())) {
                        args.add(encodeString(renderContext.getMainResource().getResolvedTemplate()));
                    } else {
                        args.add(encodeString(resource.getResolvedTemplate()));
                    }
                } else if ("templateType".equals(field)) {
                    String templateType = resource.getTemplateType();
                    if (renderContext.isAjaxRequest()) {
                        templateType += ".ajax";
                    }
                    args.add(encodeString(templateType));
                } else if ("queryString".equals(field)) {
                    String[] params = (String[]) request.getAttribute("cache.requestParameters");
                    if (params != null && params.length > 0) {
                        args.add(encodeString("_qs" + Arrays.toString(params) + "_"));
                    } else {
                        args.add("");
                    }
                } else if ("acls".equals(field)) {
                    args.add(encodeString(appendAcls(resource, renderContext, true)));
                } else if ("wrapped".equals(field)) {
                    args.add(encodeString(String.valueOf(resource.hasWrapper())));
                } else if ("context".equals(field)) {
                    args.add(encodeString(String.valueOf(resource.getContextConfiguration())));
                } else if ("custom".equals(field)) {
                    args.add((resource.getModuleParams().get("module.cache.additional.key") != null ? encodeString(resource.getModuleParams().get("module.cache.additional.key").toString()) : "") +
                            (request.getAttribute("module.cache.additional.key") != null ? encodeString(request.getAttribute("module.cache.additional.key").toString()) : ""));
                } else if ("templateNodes".equals(field)) {
                    final Template t = (Template) request.getAttribute("previousTemplate");
                    args.add(encodeString(t != null ? t.serialize() : ""));
                } else if ("resourceID".equals(field)) {
                    try {
                        args.add(encodeString(resource.getNode().getIdentifier()));
                    } catch (RepositoryException e) {
                        logger.error(e.getMessage(), e);
                    }
                } else if ("inArea".equals(field)) {
                    Object inArea = request.getAttribute("inArea");
                    args.add(encodeString(inArea != null ? inArea.toString() : ""));
                } else if ("site".equals(field)) {
                    // Todo : Do we need to find another way of getting the urlresolver ?
                    URLResolver urlResolver = (URLResolver) renderContext.getRequest().getAttribute("urlResolver");
                    args.add(encodeString(urlResolver == null ||
                            urlResolver.getSiteKeyByServerName() == null ? new StringBuilder().append(
                            renderContext.getSite().getSiteKey()).append(":").append("virtualhost").append(":").append(
                            request.getParameter("jsite")).toString() : new StringBuilder().append(
                            renderContext.getSite().getSiteKey()).append(":").append(request.getParameter(
                            "jsite")).toString()));
                }
            }
>>>>>>> .merge-right.r46549
        }
        return args.toArray(new String[args.size()]);
    }

    public Map<String, String> parse(String key) {
        String[] values = key.split("@@");
        Map<String, String> result = new LinkedHashMap<String, String>(fields.size());
        for (int i = 0; i < values.length; i++) {
            String value = values[i];
            result.put(fields.get(i), value == null || value.equals("null") ? null : value);
        }
        return result;
    }

    public String replaceField(String key, String fieldName, String newValue) {
        String[] args = key.split("@@");
        args[fields.indexOf(fieldName)] = newValue;
        return StringUtils.join(args, "@@");
    }

    public CacheKeyPartGenerator getPartGenerator(String field) {
        return partGenerators.get(fields.indexOf(field));
    }

    public String replacePlaceholdersInCacheKey(RenderContext renderContext, String key) {
        String[] args = key.split("@@");
        String[] newArgs = new String[args.length];
        for (int i = 0; i < args.length; i++) {
            String value = args[i];
            newArgs[i] = partGenerators.get(i).replacePlaceholders(renderContext,value);
        }
        String s = StringUtils.join(newArgs,"@@");
//        if (SettingsBean.getInstance().isProductionMode()) {
//            try {
//                byte[] b = DigestUtils.getSha512Digest().digest(s.getBytes("UTF-8"));
//                StringWriter sw = new StringWriter();
//                Base64.encode(b, 0, b.length, sw);
//                return sw.toString();
//            } catch (Exception e) {
//                logger.warn("Issue while digesting key",e);
//            }
//        }
        return s;
    }

}
