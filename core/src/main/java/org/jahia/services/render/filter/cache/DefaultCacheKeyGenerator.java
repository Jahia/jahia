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

import net.sf.ehcache.Cache;
import net.sf.ehcache.CacheManager;
import net.sf.ehcache.Element;
import org.apache.commons.collections.ListUtils;
import org.apache.commons.collections.Predicate;
import org.apache.commons.lang.StringUtils;
import org.apache.jackrabbit.core.security.JahiaAccessManager;
import org.jahia.services.cache.ehcache.EhCacheProvider;
import org.jahia.services.content.JCRCallback;
import org.jahia.services.content.JCRNodeWrapper;
import org.jahia.services.content.JCRSessionWrapper;
import org.jahia.services.content.JCRTemplate;
import org.jahia.services.rbac.Role;
import org.jahia.services.rbac.RoleIdentity;
import org.jahia.services.render.RenderContext;
import org.jahia.services.render.Resource;
import org.jahia.services.render.filter.Template;
import org.jahia.services.usermanager.JahiaGroup;
import org.jahia.services.usermanager.JahiaGroupManagerService;
import org.jahia.services.usermanager.JahiaUser;
import org.jahia.services.usermanager.JahiaUserManagerService;
import org.slf4j.Logger;
import org.springframework.beans.factory.InitializingBean;

import javax.jcr.Node;
import javax.jcr.NodeIterator;
import javax.jcr.RepositoryException;
import javax.jcr.Value;
import javax.jcr.query.Query;
import javax.jcr.query.QueryResult;
import javax.jcr.query.Row;
import javax.jcr.query.RowIterator;
import java.text.FieldPosition;
import java.text.MessageFormat;
import java.text.ParseException;
import java.util.*;

/**
 * Default implementation of the module output cache key generator.
 *
 * @author Sergiy Shyrkov
 */
public class DefaultCacheKeyGenerator implements CacheKeyGenerator, InitializingBean {

    private static transient Logger logger = org.slf4j.LoggerFactory.getLogger(DefaultCacheKeyGenerator.class);

    private static final Set<String> KNOWN_FIELDS = new LinkedHashSet<String>(Arrays.asList("workspace", "language",
            "path", "template", "templateType", "acls", "context", "wrapped", "custom", "queryString",
            "templateNodes"));
    private static final String CACHE_NAME = "nodeusersacls";
    private List<String> fields = new LinkedList<String>(KNOWN_FIELDS);

    private MessageFormat format = new MessageFormat("#{0}#{1}#{2}#{3}#{4}#{5}#{6}#{7}#{8}#{9}#{10}");

    private EhCacheProvider cacheProvider;
    private Cache cache;

    public String generate(Resource resource, RenderContext renderContext) {
        return format.format(getArguments(resource, renderContext), new StringBuffer(32), new FieldPosition(
                0)).toString();
    }

    private Object[] getArguments(Resource resource, RenderContext renderContext) {
        List<String> args = new LinkedList<String>();
        for (String field : fields) {
            if ("workspace".equals(field)) {
                args.add(resource.getWorkspace());
            } else if ("language".equals(field)) {
                args.add(resource.getLocale().toString());
            } else if ("path".equals(field)) {
                StringBuilder s = new StringBuilder(resource.getNode().getPath());
                if (Boolean.TRUE.equals(renderContext.getRequest().getAttribute("cache.mainResource"))) {
                    s.append("_mr_");
                }
                args.add(s.toString());
            } else if ("template".equals(field)) {
                if (resource.getContextConfiguration().equals("page") && resource.getNode().getPath().equals(
                        renderContext.getMainResource().getNode().getPath())) {
                    args.add(renderContext.getMainResource().getResolvedTemplate());
                } else {
                    args.add(resource.getResolvedTemplate());
                }
            } else if ("templateType".equals(field)) {
                args.add(resource.getTemplateType());
            } else if ("queryString".equals(field)) {
                final String queryString = renderContext.getRequest().getQueryString();
                args.add(queryString != null ? queryString : "");
            } else if ("acls".equals(field)) {
                args.add(appendAcls(resource, renderContext));
            } else if ("wrapped".equals(field)) {
                args.add(String.valueOf(resource.hasWrapper()));
            } else if ("context".equals(field)) {
                args.add(String.valueOf(resource.getContextConfiguration()));
            } else if ("custom".equals(field)) {
                args.add((String) resource.getModuleParams().get("module.cache.additional.key"));
            } else if ("templateNodes".equals(field)) {
                final Template t = (Template) renderContext.getRequest().getAttribute("previousTemplate");
                args.add(t != null ? t.serialize() : "");
            }
        }
        return args.toArray(new String[KNOWN_FIELDS.size()]);
    }

    public String appendAcls(Resource resource, RenderContext renderContext) {
        try {
            if (Boolean.TRUE.equals(renderContext.getRequest().getAttribute("cache.perUser"))) {
                return "_perUser_";
            }

            JCRNodeWrapper node = resource.getNode();
            if(node.hasProperty("j:requiredPermissions")){
                node = renderContext.getMainResource().getNode();
            }

            // Search for user specific acl
            JahiaUser principal = renderContext.getUser();
            final String userName = principal.getUsername();
            Set<String> roles = ((JahiaAccessManager) node.getAccessControlManager()).getRoles(node.getPath());
            StringBuilder b = new StringBuilder();
            for (String g : roles) {
                if (b.length() > 0) {
                    b.append("|");
                }
                b.append(g);
            }
            if (b.toString().equals(JahiaGroupManagerService.GUEST_GROUPNAME) && !userName.equals(
                    JahiaUserManagerService.GUEST_USERNAME)) {
                b.append("|" + JahiaGroupManagerService.USERS_GROUPNAME);
            }
            String userKey = b.toString();
            if ("".equals(userKey.trim()) && userName.equals(JahiaUserManagerService.GUEST_USERNAME)) {
                userKey = userName;
            }
            return userKey;
        } catch (RepositoryException e) {
            logger.error(e.getMessage(), e);
        }
        return "";
    }

    public String getPath(String key) throws ParseException {
        return parse(key).get("path").replaceAll("_mr_", "");
    }

    public Map<String, String> parse(String key) throws ParseException {
        Object[] values = format.parse(key);
        Map<String, String> result = new LinkedHashMap<String, String>(fields.size());
        for (int i = 0; i < values.length; i++) {
            String value = (String) values[i];
            result.put(fields.get(i), value == null || value.equals("null") ? null : value.replaceAll("_mr_", ""));
        }
        return result;
    }

    public String replaceField(String key, String fieldName, String newValue) throws ParseException {
        Map<String, String> args = parse(key);
        args.put(fieldName, newValue);
        return format.format(args.values().toArray(new String[KNOWN_FIELDS.size()]), new StringBuffer(32),
                new FieldPosition(0)).toString();
    }

    @SuppressWarnings("unchecked")
    public void setFields(List<String> fields) {
        this.fields = ListUtils.predicatedList(fields, new Predicate() {
            public boolean evaluate(Object object) {
                return (object instanceof String) && KNOWN_FIELDS.contains(object);
            }
        });
    }

    public void setFormat(String format) {
        this.format = new MessageFormat(format);
    }

    public void flushUsersGroupsKey() {
        if (cache != null) {
            cache.removeAll();
            cache.flush();
        }
    }

    public void setCacheProvider(EhCacheProvider cacheProvider) {
        this.cacheProvider = cacheProvider;
    }

    /**
     * Invoked by a BeanFactory after it has set all bean properties supplied
     * (and satisfied BeanFactoryAware and ApplicationContextAware).
     * <p>This method allows the bean instance to perform initialization only
     * possible when all bean properties have been set and to throw an
     * exception in the event of misconfiguration.
     *
     * @throws Exception in the event of misconfiguration (such
     *                   as failure to set an essential property) or if initialization fails.
     */
    public void afterPropertiesSet() throws Exception {
        /*CacheManager cacheManager = cacheProvider.getCacheManager();
        if (!cacheManager.cacheExists(CACHE_NAME)) {
            cacheManager.addCache(CACHE_NAME);
        }
        cache = cacheManager.getCache(CACHE_NAME);*/
    }
}
