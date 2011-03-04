/**
 * This file is part of Jahia: An integrated WCM, DMS and Portal Solution
 * Copyright (C) 2002-2011 Jahia Solutions Group SA. All rights reserved.
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

package org.jahia.services.search;

import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.jcr.ItemNotFoundException;
import javax.jcr.PropertyIterator;
import javax.jcr.RepositoryException;

import org.apache.commons.collections.Transformer;
import org.apache.commons.collections.map.LazyMap;
import org.jahia.services.content.JCRContentUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.jahia.api.Constants;
import org.jahia.services.content.JCRNodeWrapper;
import org.jahia.services.content.JCRPropertyWrapper;
import org.jahia.services.render.RenderContext;

/**
 * Search result item, represented by the JCR node. Used as a view object in JSP
 * templates.
 * 
 * @author Sergiy Shyrkov
 */
public class JCRNodeHit extends AbstractHit<JCRNodeWrapper> {

    private static Logger logger = LoggerFactory.getLogger(JCRNodeHit.class);
    
    private String link  = null;

    private Map<String, JCRPropertyWrapper> propertiesFacade;

    /**
     * Initializes an instance of this class.
     * 
     * @param node search result item to be wrapped
     * @param context
     */
    public JCRNodeHit(JCRNodeWrapper node, RenderContext context) {
        super(node, context);
    }

    public String getContentType() {
        return "text/html";
    }

    public Date getCreated() {
        return resource.getCreationDateAsDate();
    }

    public String getCreatedBy() {
        return resource.getCreationUser();
    }

    public Date getLastModified() {
        return resource.getLastModifiedAsDate();
    }

    public String getLastModifiedBy() {
        return resource.getModificationUser();
    }

    public String getLink() {
        if (link == null) {
            link = resolveURL() + getQueryParameter();
        }
        return link;
    }

    public String getPath() {
        return resource.getPath();
    }

    @SuppressWarnings("unchecked")
    public Map<String, JCRPropertyWrapper> getProperties() {

        if (propertiesFacade == null) {
            propertiesFacade = LazyMap.decorate(new HashMap<String, String>(), new Transformer() {

                private Map<Object, JCRPropertyWrapper> accessedProperties = new HashMap<Object, JCRPropertyWrapper>();

                public Object transform(Object input) {
                    JCRPropertyWrapper property = null;
                    if (accessedProperties.containsKey(input)) {
                        property = accessedProperties.get(input);
                    } else {
                        try {
                            property = resource.getProperty(String.valueOf(input));
                        } catch (RepositoryException e) {
                            logger.warn("Error accessing property '" + input + "'.", e);
                        }
                        accessedProperties.put(input, property);
                    }
                    return property;
                }
            });
        }

        return propertiesFacade;
    }

    public String getTitle() {
        return resource.getDisplayableName();
    }

    /**
     * Returns the name of this file or folder.
     * 
     * @return the name of this file or folder
     */
    public String getName() {
        return resource.getName();
    }

    public String getType() {
        String type = null;
        try {
            type = resource.getPrimaryNodeTypeName();
        } catch (RepositoryException e) {
            logger.warn("Unable to retrieve primary node type for the resource " + getPath(), e);
        }

        return type;
    }

    public String getIconType() {
        return "";
    }
    
    public List<AbstractHit<?>> getUsages() {
        List<AbstractHit<?>> usages = new ArrayList<AbstractHit<?>>();
        JCRNodeWrapper pageNode = resource;
        try {
            while (pageNode != null && !pageNode.isNodeType(Constants.JAHIANT_PAGE)) {
                try {
                    pageNode = pageNode.getParent();
                } catch (ItemNotFoundException e) {
                    // we have reached the root node
                    pageNode = null;
                }
            }
            if (pageNode != null && !pageNode.equals(resource)) {
                usages.add(new PageHit(pageNode, context));
            }
        } catch (RepositoryException e) {
        }
        try {        
            for (PropertyIterator it = resource.getReferences(); it.hasNext();) {
                try {
                    JCRNodeWrapper node = (JCRNodeWrapper) it.nextProperty().getParent();
                    AbstractHit<?> hit = node.isNodeType(Constants.JAHIANT_PAGE) ? new PageHit(node,
                            context) : new JCRNodeHit(node, context);
                    if (!usages.contains(hit) && !node.equals(resource)) {
                        usages.add(hit);
                    }
                } catch (Exception e) {
                }
            }
        } catch (RepositoryException e) {
        }
        return usages;
    }

    private String resolveURL() {
        JCRNodeWrapper currentNode = JCRContentUtils.findDisplayableNode(resource, context);
        return context.getURLGenerator().buildURL(currentNode, null, "html");
    }

}
