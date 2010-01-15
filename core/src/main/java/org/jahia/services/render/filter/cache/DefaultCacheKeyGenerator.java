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
import org.apache.log4j.Logger;
import org.jahia.services.cache.ehcache.EhCacheProvider;
import org.jahia.services.render.RenderContext;
import org.jahia.services.render.Resource;
import org.jahia.services.usermanager.JahiaGroup;
import org.jahia.services.usermanager.JahiaGroupManagerService;
import org.jahia.services.usermanager.JahiaUser;
import org.jahia.services.usermanager.JahiaUserManagerService;
import org.springframework.beans.factory.InitializingBean;

import javax.jcr.Node;
import javax.jcr.NodeIterator;
import javax.jcr.RepositoryException;
import javax.jcr.Value;
import javax.jcr.query.*;
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

    private static transient Logger logger = Logger.getLogger(DefaultCacheKeyGenerator.class);

    private static final Set<String> KNOWN_FIELDS = new LinkedHashSet<String>(Arrays.asList("workspace", "language",
                                                                                            "path", "template",
                                                                                            "templateType", "acls",
                                                                                            "queryString"));
    private static final String CACHE_NAME = "nodeusersacls";
    private List<String> fields = new LinkedList<String>(KNOWN_FIELDS);

    private MessageFormat format = new MessageFormat("#{0}#{1}#{2}#{3}#{4}#{5}#{6}");

    private JahiaGroupManagerService groupManagerService;
    private Set<JahiaGroup> aclGroups = null;
    private EhCacheProvider cacheProvider;
    private Cache cache;

    public void setGroupManagerService(JahiaGroupManagerService groupManagerService) {
        this.groupManagerService = groupManagerService;
    }

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
                args.add(resource.getNode().getPath());
            } else if ("template".equals(field)) {
                args.add(resource.getResolvedTemplate());
            } else if ("templateType".equals(field)) {
                args.add(resource.getTemplateType());
            } else if ("queryString".equals(field)) {
                final String queryString = renderContext.getRequest().getQueryString();
                args.add(queryString != null ? queryString : "");
            } else if ("acls".equals(field)) {
                try {
                    // Search for user specific acl
                    final QueryManager queryManager = resource.getNode().getSession().getWorkspace().getQueryManager();
                    JahiaUser principal = renderContext.getUser();
                    final String userName = principal.getUsername();
                    if (hasUserAcl(userName, queryManager)) {
                        args.add((String) cache.get(userName).getValue());
                    }
                    // else use user groupmembership
                    else {
                        Set<JahiaGroup> aclGroups = getAllAclsGroups(queryManager);
                        StringBuilder b = new StringBuilder();
                        for (JahiaGroup g : aclGroups) {
                            if (g != null && g.isMember(principal)) {
                                if (b.length() > 0) {
                                    b.append("|");
                                }
                                b.append(g.getGroupname());
                            }
                        }
                        if (b.toString().equals(
                                JahiaGroupManagerService.GUEST_GROUPNAME) && !principal.getUsername().equals(
                                JahiaUserManagerService.GUEST_USERNAME)) {
                            b.append("|" + JahiaGroupManagerService.USERS_GROUPNAME);
                        }
                        String userKey = b.toString();
                        final Element element = new Element(userName, userKey);
                        element.setEternal(true);
                        cache.put(element);
                        args.add(userKey);
                    }
                } catch (RepositoryException e) {
                    logger.error(e.getMessage(), e);
                }
            }
        }
        return args.toArray(new String[KNOWN_FIELDS.size()]);
    }

    private boolean hasUserAcl(String userName, QueryManager queryManager) throws RepositoryException {
        if(cache.getSize()==0) {
            Query query = queryManager.createQuery(
                "select * from [jnt:ace] as ace where ace.[j:principal] like 'u%" + userName + "'",
                Query.JCR_SQL2);
            QueryResult queryResult = query.execute();
            NodeIterator rowIterator = queryResult.getNodes();
            while (rowIterator.hasNext()) {
                Node node = (Node) rowIterator.next();
                String s = StringUtils.substringAfter(node.getProperty("j:principal").getString(),":");
                if(!cache.isKeyInCache(s) && !node.getPath().startsWith("/users/"+s+"/j:acl")) {
                    final Element element = new Element(s, s);
                    element.setEternal(true);
                    cache.put(element);
                }
            }
        }
        return cache.isKeyInCache(userName);
    }

    private Set<JahiaGroup> getAllAclsGroups(QueryManager queryManager) throws RepositoryException {
        if (aclGroups == null) {
            aclGroups = new LinkedHashSet<JahiaGroup>();
            Query groupQuery = queryManager.createQuery(
                    "select u.[j:principal] as name from [jnt:ace] as u where u.[j:principal] like 'g%'",
                    Query.JCR_SQL2);
            QueryResult groupQueryResult = groupQuery.execute();
            final RowIterator nodeIterator = groupQueryResult.getRows();
            while (nodeIterator.hasNext()) {
                Row row = (Row) nodeIterator.next();
                final Value value = row.getValues()[0];
                final String groupName = StringUtils.substringAfter(value.getString(), ":");
                final JahiaGroup group = groupManagerService.lookupGroup(groupName);
                if (!aclGroups.contains(group)) {
                    aclGroups.add(group);
                }
            }
        }
        return aclGroups;
    }

    public String getPath(String key) throws ParseException {
        return parse(key).get("path");
    }

    public Map<String, String> parse(String key) throws ParseException {
        Object[] values = format.parse(key);
        Map<String, String> result = new LinkedHashMap<String, String>(fields.size());
        for (int i = 0; i < values.length; i++) {
            Object object = values[i];
            result.put(fields.get(i), (String) object);
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
        this.aclGroups = null;
        cache.flush();
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
        CacheManager cacheManager = cacheProvider.getCacheManager();
        if (!cacheManager.cacheExists(CACHE_NAME)) {
            cacheManager.addCache(CACHE_NAME);
        }
        cache = cacheManager.getCache(CACHE_NAME);
    }
}
