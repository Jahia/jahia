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

import org.apache.commons.lang.StringUtils;
import org.jahia.services.render.RenderContext;
import org.jahia.services.render.Resource;

import java.util.*;

/**
 * Default implementation of the module output cache key generator.
 *
 * @author rincevent
 * @author Sergiy Shyrkov
 */
public class DefaultCacheKeyGenerator implements CacheKeyGenerator {

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

    public String generate(Resource resource, RenderContext renderContext) {
        return StringUtils.join(getArguments(resource, renderContext), "@@");
    }

    private Object[] getArguments(Resource resource, RenderContext renderContext) {
        List<String> args = new LinkedList<String>();
        for (CacheKeyPartGenerator generator : partGenerators) {
            args.add(generator.getValue(resource, renderContext));
        }
        return args.toArray(new String[args.size()]);
    }

<<<<<<< .working
    public String getPath(String key) {
        PathCacheKeyPartGenerator pathCacheKeyPartGenerator = (PathCacheKeyPartGenerator) getPartGenerator("path");
        if (pathCacheKeyPartGenerator != null) {
            String[] args = key.split("@@");
            return pathCacheKeyPartGenerator.getPath(args[fields.indexOf("path")]);
=======
    public String appendAcls(final Resource resource, final RenderContext renderContext, boolean appendNodePath) {
        try {
            if (renderContext.getRequest() != null && Boolean.TRUE.equals(renderContext.getRequest().getAttribute("cache.perUser"))) {
                return PER_USER;
            }

            JCRNodeWrapper node = resource.getNode();
            boolean checkRootPath = true;
            Element element = permissionCache.get(node.getPath());
            if(element!=null && Boolean.TRUE==((Boolean)element.getValue())) {
                node = renderContext.getMainResource().getNode();
                checkRootPath = false;
            } else if(element==null) {
                if (node.hasProperty("j:requiredPermissions")) {
                    permissionCache.put(new Element(node.getPath(),Boolean.TRUE));
                    node = renderContext.getMainResource().getNode();
                    checkRootPath = false;
                } else {
                    permissionCache.put(new Element(node.getPath(),Boolean.FALSE));
                }
            }
            String nodePath = node.getPath();
            final Set<String> aclsKeys = new LinkedHashSet<String>();
            aclsKeys.add(getAclsKeyPart(renderContext, checkRootPath, nodePath, appendNodePath, null));
            final Set<String> dependencies = resource.getDependencies();

            if (renderContext.getRequest() != null && Boolean.TRUE.equals(renderContext.getRequest().getAttribute("cache.mainResource"))) {
                aclsKeys.add("mraclmr");
            } else {
                for (final String dependency : dependencies) {
                    if (!dependency.equals(nodePath)) {
                        try {
                            if (!JCRContentUtils.isNotJcrUuid(dependency)) {
                                final boolean finalCheckRootPath = checkRootPath;
                                JCRTemplate.getInstance().doExecuteWithSystemSession(null, Constants.LIVE_WORKSPACE, new JCRCallback<Object>() {
                                    public Object doInJCR(JCRSessionWrapper session) throws RepositoryException {
                                        final JCRNodeWrapper nodeByIdentifier = session.getNodeByIdentifier(dependency);
                                        aclsKeys.add(getAclsKeyPart(renderContext, finalCheckRootPath, nodeByIdentifier.getPath(),
                                            true, null));
                                        return null;
                                    }
                                });
                            } else if (dependency.contains("/")) {
                                aclsKeys.add(getAclsKeyPart(renderContext,
                                        checkRootPath, dependency, true, null));
                            }
                        } catch (ItemNotFoundException ex) {
                            logger.warn("ItemNotFound: "
                                    + dependency
                                    + "  it could be an invalid reference, check jcr integrity");
                        } catch (PathNotFoundException ex) {
                            logger.warn("PathNotFound: "
                                    + dependency
                                    + "  it could be an invalid reference, check jcr integrity");
                        }                            
                    }
                }
            }
            StringBuilder stringBuilder = new StringBuilder();
            for (String aclsKey : aclsKeys) {
                if(stringBuilder.length()>0) {
                    stringBuilder.append("_depacl_");
                }
                stringBuilder.append(aclsKey);
            }
            return stringBuilder.toString();

        } catch (RepositoryException e) {
            logger.error(e.getMessage(), e);
>>>>>>> .merge-right.r44829
        }
        return "";
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
        return StringUtils.join(newArgs,"@@");
    }

}
