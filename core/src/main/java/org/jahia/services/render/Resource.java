/**
 * This file is part of Jahia: An integrated WCM, DMS and Portal Solution
 * Copyright (C) 2002-2009 Jahia Solutions Group SA. All rights reserved.
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
package org.jahia.services.render;

import org.jahia.services.content.JCRNodeWrapper;
import org.jahia.services.content.JCRSessionWrapper;
import org.apache.log4j.Logger;

import javax.jcr.RepositoryException;
import java.util.*;

/**
 * A resource is the aggregation of a node and a specific template
 * It's something that can be handled by the render engine to be displayed.
 *
 * @author toto
 */
public class Resource {
    private static Logger logger = Logger.getLogger(Resource.class);
    private JCRNodeWrapper node;
    private String templateType;
    private String template;
    private String forcedTemplate;

    private List<JCRNodeWrapper> dependencies;

    private List<Resource> includedResources;
    private List<String> missingResources;

    /**
     * Creates a resource from the specified parameter
     * @param node The node to display
     * @param templateType template type
     * @param template the template name, null if default
     * @param forcedTemplate the template name, null if default
     */
    public Resource(JCRNodeWrapper node, String templateType, String template, String forcedTemplate) {
        this.node = node;
        this.templateType = templateType;
        this.template = template;
        this.forcedTemplate = forcedTemplate;
        dependencies = new ArrayList<JCRNodeWrapper>();
        dependencies.add(node);

        includedResources = new ArrayList<Resource>();
        missingResources = new ArrayList<String>();
    }

    public JCRNodeWrapper getNode() {
        return node;
    }

    public String getTemplateType() {
        return templateType;
    }

    public String getWorkspace() {
        try {
            return node.getSession().getWorkspace().getName();
        } catch (RepositoryException e) {
            e.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.
        }
        return null;
    }

    public Locale getLocale() {
        try {
            return ((JCRSessionWrapper) node.getSession()).getLocale();
        } catch (RepositoryException e) {
            e.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.
        }
        return null;
    }

    public String getTemplate() {
        return template;
    }

    public String getForcedTemplate() {
        return forcedTemplate;
    }

    public List<String> getTemplates() {
        List<String> l = new ArrayList<String>();

        if (forcedTemplate != null && forcedTemplate.length() > 0) {
            l.add(forcedTemplate);
        }
        try {
            if (node.isNodeType("jmix:renderable") && node.hasProperty("j:defaultTemplate")) {
                l.add( node.getProperty("j:defaultTemplate").getString() );
            }
        } catch (RepositoryException e) {
            logger.error(e.getMessage(), e);
        }
        if (template != null && template.length() > 0) {
            l.add(template);
        }
        l.add("default");
        return l;
    }

    public String getResolvedTemplate() {
        return getTemplates().iterator().next();
    }

    public List<JCRNodeWrapper> getDependencies() {
        return dependencies;
    }

    public List<String> getMissingResources() {
        return missingResources;
    }

    public List<Resource> getIncludedResources() {
        return includedResources;
    }

    @Override
    public String toString() {
        return "Resource{" +
                "node=" + node.getPath() +
                ", templateType='" + templateType + '\'' +
                ", template='" + template + '\'' +
                '}';
    }
}
