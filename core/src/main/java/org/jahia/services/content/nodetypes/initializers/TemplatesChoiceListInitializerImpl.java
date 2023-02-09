/*
 * ==========================================================================================
 * =                   JAHIA'S DUAL LICENSING - IMPORTANT INFORMATION                       =
 * ==========================================================================================
 *
 *                                 http://www.jahia.com
 *
 *     Copyright (C) 2002-2023 Jahia Solutions Group SA. All rights reserved.
 *
 *     THIS FILE IS AVAILABLE UNDER TWO DIFFERENT LICENSES:
 *     1/Apache2 OR 2/JSEL
 *
 *     1/ Apache2
 *     ==================================================================================
 *
 *     Copyright (C) 2002-2023 Jahia Solutions Group SA. All rights reserved.
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
package org.jahia.services.content.nodetypes.initializers;

import org.apache.commons.lang.StringUtils;
import org.jahia.api.Constants;
import org.jahia.bin.Jahia;
import org.jahia.data.templates.JahiaTemplatesPackage;
import org.jahia.registries.ServicesRegistry;
import org.jahia.services.content.decorator.JCRSiteNode;
import org.jahia.utils.i18n.Messages;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.jahia.services.content.JCRContentUtils;
import org.jahia.services.content.JCRNodeWrapper;
import org.jahia.services.content.JCRPropertyWrapper;
import org.jahia.services.content.JCRSessionFactory;
import org.jahia.services.content.JCRSessionWrapper;
import org.jahia.services.content.nodetypes.ExtendedNodeType;
import org.jahia.services.content.nodetypes.ExtendedPropertyDefinition;
import org.jahia.services.content.nodetypes.NodeTypeRegistry;
import org.jahia.services.content.nodetypes.ValueImpl;
import org.jahia.services.render.RenderService;
import org.jahia.services.render.View;
import org.springframework.core.io.Resource;

import javax.jcr.*;
import java.io.File;
import java.util.*;

/**
 * Choice list initializer to provide a selection of available templates.
 *
 * @author : rincevent
 * @since JAHIA 6.5
 *        Created : 17 nov. 2009
 */
public class TemplatesChoiceListInitializerImpl implements ChoiceListInitializer {

    /**
     * We use this View wrapper because for filtering common views we just want to compare the keys, not all the attributes like in
     * View.equals()
     *
     * @author guillaume
     */
    public class ViewWrapper implements Comparable<ViewWrapper> {
        private final View view;

        public ViewWrapper(View view) {
            this.view = view;
        }

        public View getView() {
            return view;
        }

        @Override
        public boolean equals(Object obj) {
            return obj != null && this.getClass() == obj.getClass() && view.getKey().equals(((ViewWrapper) obj).view.getKey());
        }

        @Override
        public int hashCode() {
            return view.getKey().hashCode();
        }

        public int compareTo(ViewWrapper o) {
            return view.getKey().compareTo(((ViewWrapper) o).getView().getKey());
        }
    }

    private transient static Logger logger = LoggerFactory.getLogger(TemplatesChoiceListInitializerImpl.class);

    public List<ChoiceListValue> getChoiceListValues(ExtendedPropertyDefinition declaringPropertyDefinition, String param,
                                                     List<ChoiceListValue> values, Locale locale,
                                                     Map<String, Object> context) {
        if (context == null) {
            return new ArrayList<ChoiceListValue>();
        }
        JCRNodeWrapper node = (JCRNodeWrapper) context.get("contextNode");
        JCRNodeWrapper parentNode = (JCRNodeWrapper) context.get("contextParent");
        ExtendedNodeType realNodeType = (ExtendedNodeType) context.get("contextType");
        String propertyName = context.containsKey("dependentProperties") ? ((List<String>)context.get("dependentProperties")).get(0) : null;

        JCRSiteNode site = null;

        SortedSet<View> views = new TreeSet<View>();

        boolean subViews = false;

        try {
            if (node != null) {
                site = node.getResolveSite();
            }
            if (site == null && parentNode != null) {
                site = parentNode.getResolveSite();
            }

            final List<String> nodeTypeList = new ArrayList<String>();
            String nextParam = "";
            if (param.contains(",")) {
                nextParam = StringUtils.substringAfter(param, ",");
                param =  StringUtils.substringBefore(param, ",");
            }
            if ("subnodes".equals(param)) {
                subViews = true;
                if (propertyName == null) {
                    propertyName = "j:allowedTypes";
                }
                if (context.containsKey(propertyName)) {
                    List<String> types = (List<String>)context.get(propertyName);
                    for (String type : types) {
                        nodeTypeList.add(type);
                    }
                } else if (node != null && node.hasProperty(propertyName)) {
                    JCRPropertyWrapper property = node.getProperty(propertyName);
                    if (property.isMultiple()) {
                        Value[] types = property.getValues();
                        for (Value type : types) {
                            nodeTypeList.add(type.getString());
                        }
                    } else {
                        nodeTypeList.add(property.getValue().getString());
                    }
                } else if (node != null && !"j:allowedTypes".equals(propertyName) && node.hasProperty("j:allowedTypes")) {
                    Value[] types = node.getProperty("j:allowedTypes").getValues();
                    for (Value type : types) {
                        nodeTypeList.add(type.getString());
                    }
                } else if (node !=null) {
                    // No restrictions get node type list from already existing nodes
                    NodeIterator nodeIterator = node.getNodes();
                    while (nodeIterator.hasNext()) {
                        Node next = (Node) nodeIterator.next();
                        String name = next.getPrimaryNodeType().getName();
                        if (!nodeTypeList.contains(name) && next.isNodeType("jnt:content")) {
                            nodeTypeList.add(name);
                        }
                    }
                }
                param = nextParam;
            } else if ("reference".equals(param)) {
                if (propertyName == null) {
                    propertyName = Constants.NODE;
                }
                if (context.containsKey(propertyName) && context.get(propertyName) != null) {
                    JCRSessionWrapper session = JCRSessionFactory.getInstance().getCurrentUserSession();
                    List<String> refNodeUuids = (List<String>)context.get(propertyName);
                    for (String refNodeUuid : refNodeUuids) {
                        try {
                            JCRNodeWrapper refNode = session.getNodeByUUID(refNodeUuid);
                            nodeTypeList.addAll(refNode.getNodeTypes());
                        } catch (Exception e) {
                            logger.warn("Referenced node not found to retrieve its nodetype for initializer", e);
                        }
                    }
                } else if (node != null && node.hasProperty(propertyName)) {
                    try {
                        JCRNodeWrapper refNode = (JCRNodeWrapper) node.getProperty(propertyName).getNode();
                        nodeTypeList.addAll(refNode.getNodeTypes());
                    } catch (ItemNotFoundException e) {
                    }
                } else if (node != null && !Constants.NODE.equals(propertyName) && node.hasProperty(Constants.NODE)) {
                    try {
                        JCRNodeWrapper refNode = (JCRNodeWrapper) node.getProperty(Constants.NODE).getNode();
                        nodeTypeList.addAll(refNode.getNodeTypes());
                    } catch (ItemNotFoundException e) {
                    }
                }
                param = nextParam;
            } else if ("mainresource".equals(param)) {
                JCRNodeWrapper matchingParent;
                JCRNodeWrapper parent;
                if (node == null) {
                    parent = (JCRNodeWrapper) context.get("contextParent");
                    site = parent.getResolveSite();
                } else {
                    parent = node.getParent();
                }
                try {
                    while (true) {
                        if (parent.isNodeType("jnt:template")) {
                            matchingParent = parent;
                            break;
                        }
                        parent = parent.getParent();
                    }
                    if (matchingParent.hasProperty("j:applyOn")) {
                        Value[] vs = matchingParent.getProperty("j:applyOn").getValues();
                        for (Value v : vs) {
                            nodeTypeList.add(v.getString());
                        }
                    }
                } catch (ItemNotFoundException e) {
                }
                if (nodeTypeList.isEmpty()) {
                    nodeTypeList.add("jnt:page");
                }
                param = nextParam;
            } else if (param != null && param.indexOf(":") > 0) {
                nodeTypeList.add(param);
                param = nextParam;
            } else {
                if (node != null) {
                    nodeTypeList.addAll(node.getNodeTypes());
                } else if (realNodeType != null) {
                    nodeTypeList.add(realNodeType.getName());
                }
            }

            if (nodeTypeList.isEmpty()) {
                nodeTypeList.add("nt:base");
            }

            SortedSet<ViewWrapper> wrappedViews = new TreeSet<ViewWrapper>();
            Set<ViewWrapper> wrappedViewsSet = new HashSet<ViewWrapper>();
            for (String s : nodeTypeList) {
                SortedSet<View> viewsSet = RenderService.getInstance().getViewsSet(
                        NodeTypeRegistry.getInstance().getNodeType(s), site, "html");

                if (!viewsSet.isEmpty()) {
                    // use of wrapper class to get a simpler equals method, based on the key
                    // to keep only views in common between sub nodes
                    for (Iterator<View> iterator = viewsSet.iterator(); iterator.hasNext();) {
                        wrappedViewsSet.add(new ViewWrapper(iterator.next()));
                    }

                    if (subViews && !wrappedViews.isEmpty() && !NodeTypeRegistry.getInstance().getNodeType(s).getName().equals("jnt:contentReference")) {
                        wrappedViews.retainAll(wrappedViewsSet);
                    } else {
                        wrappedViews.addAll(wrappedViewsSet);
                    }
                }
                wrappedViewsSet.clear();
            }

            for (Iterator<ViewWrapper> iterator = wrappedViews.iterator(); iterator.hasNext();) {
                views.add(iterator.next().getView());
            }

        } catch (RepositoryException e) {
            logger.error(e.getMessage(), e);
        }

        List<ChoiceListValue> vs = new ArrayList<ChoiceListValue>();
        for (View view : views) {
            HashMap<String, Object> map = new HashMap<String, Object>();
            fillProperties(map, view.getDefaultProperties());
            fillProperties(map, view.getProperties());
            boolean isStudio = site != null && site.getPath().startsWith("/modules");
            if (isViewVisible(view.getKey(), param, map, isStudio)) {
                JahiaTemplatesPackage pkg = view.getModule() != null ? view.getModule() : ServicesRegistry.getInstance().getJahiaTemplateManagerService().getTemplatePackageById(JahiaTemplatesPackage.ID_DEFAULT);
                String displayName = Messages.get(pkg, declaringPropertyDefinition.getResourceBundleKey() + "." + JCRContentUtils.replaceColon(view.getKey()),
                        locale, view.getKey());
                ChoiceListValue c =  new ChoiceListValue(displayName, map, new ValueImpl(view.getKey(), PropertyType.STRING, false));
                try {
                    final Resource imagePath = pkg.getResource(File.separator + "img" + File.separator + c.getValue().getString() + ".png");

                    if (imagePath != null && imagePath.exists()) {
                        String s = Jahia.getContextPath();
                        if (s.equals("/")) {
                            s = "";
                        }
                        c.addProperty("image", s + (pkg.getRootFolderPath().startsWith("/")?"":"/")+ pkg.getRootFolderPath() + "/img/" + c.getValue().getString() + ".png");
                    }
                } catch (RepositoryException e) {
                    logger.error(e.getMessage(), e);
                }

                vs.add(c);
            }
        }
        Collections.sort(vs);
        return vs;
    }

    private boolean isViewVisible(String viewKey, String param, HashMap<String, Object> viewProperties, boolean isStudio) {
        final Object visible = viewProperties.get(View.VISIBLE_KEY);
        final Object type = viewProperties.get(View.TYPE_KEY);
        return !View.VISIBLE_FALSE.equals(visible)
                && (!View.VISIBLE_STUDIO_ONLY.equals(visible) || isStudio)
                && ((type == null && StringUtils.isEmpty(param)) || param.equals(type))
                && !viewKey.startsWith("wrapper.")
                && !viewKey.contains("hidden.");
    }

    private void fillProperties(HashMap<String, Object> map, Properties properties) {
        for (Map.Entry<Object, Object> entry : properties.entrySet()) {
            map.put(entry.getKey().toString(), entry.getValue());
        }
    }
}
