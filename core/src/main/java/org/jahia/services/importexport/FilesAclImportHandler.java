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

package org.jahia.services.importexport;

import org.slf4j.Logger;
import org.apache.commons.lang.StringUtils;
import org.jahia.registries.ServicesRegistry;
import org.jahia.services.content.JCRNodeWrapper;
import org.jahia.services.content.JCRSessionWrapper;
import org.jahia.services.content.nodetypes.ExtendedNodeType;
import org.jahia.services.content.nodetypes.ExtendedPropertyDefinition;
import org.jahia.services.content.nodetypes.SelectorType;
import org.jahia.services.content.nodetypes.ValueImpl;
import org.jahia.services.sites.JahiaSite;
import org.jahia.services.usermanager.JahiaGroup;
import org.jahia.services.usermanager.JahiaUser;
import org.jahia.utils.i18n.ResourceBundleMarker;
import org.xml.sax.Attributes;
import org.xml.sax.SAXException;
import org.xml.sax.helpers.DefaultHandler;

import javax.jcr.Node;
import javax.jcr.PropertyType;
import javax.jcr.RepositoryException;
import javax.jcr.Value;
import javax.jcr.nodetype.ConstraintViolationException;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.StringTokenizer;

/**
 * Created by IntelliJ IDEA.
 * User: toto
 * Date: 6 juil. 2005
 * Time: 17:31:05
 * 
 */
public class FilesAclImportHandler extends DefaultHandler {
    private static Logger logger = org.slf4j.LoggerFactory.getLogger(FilesAclImportHandler.class);
    
    private JahiaSite site;
    private DefinitionsMapping mapping;
    private JCRSessionWrapper session;
    
    private Map<String, String> davPropertiesMapping = initializeDavPropertiesMapping();

    public FilesAclImportHandler(JahiaSite site, DefinitionsMapping mapping) {
        this.site = site;
        this.mapping = mapping;
        try {
            this.session = ServicesRegistry.getInstance().getJCRStoreService().getSessionFactory().getCurrentUserSession();
        } catch (RepositoryException e) {
            logger.error(e.getMessage(), e);
        }
    }

    public void startElement(String uri, String localName, String qName, Attributes attributes) throws SAXException {
        if ("file".equals(localName) && ImportExportBaseService.JAHIA_URI.equals(uri)) {
            String path = attributes.getValue(ImportExportBaseService.JAHIA_URI, "path");
            String acl = attributes.getValue(ImportExportBaseService.JAHIA_URI, "fileacl");
            if (path.startsWith("/shared")) {
                path = "/sites/" + site.getSiteKey() + "/files" + path;
            } else if (path.startsWith("/users")) {
                path = path.replaceFirst("/users/([^/]+)/", "/users/$1/files/");
            }
            try {
                JCRSessionWrapper session = ServicesRegistry.getInstance().getJCRStoreService().getSessionFactory().getCurrentUserSession();
                JCRNodeWrapper f = session.getNode(path);
                if (acl != null && acl.length() > 0) {
                    StringTokenizer st = new StringTokenizer(acl, "|");
                    while (st.hasMoreElements()) {
                        String s = st.nextToken();
                        int beginIndex = s.lastIndexOf(":");

                        String principal = s.substring(0, beginIndex);
                        String userName = principal.substring(2);
                        String value = null;

                        if (principal.charAt(0) == 'u') {
                            JahiaUser user = ServicesRegistry
                                    .getInstance()
                                    .getJahiaSiteUserManagerService()
                                    .getMember(site.getID(),
                                            userName);
                            if (user != null) {
                                value = "/users/" + user.getUsername();
                            }
                        } else {
                            JahiaGroup group = ServicesRegistry
                                    .getInstance()
                                    .getJahiaGroupManagerService()
                                    .lookupGroup(site.getID(),
                                            userName);
                            if (group != null) {
                                value = "+/groups/"
                                        + group.getGroupname()
                                        + "/members";
                            }
                        }

                        if (value != null) {
//                            f.changeRoles(value, s
//                                    .substring(beginIndex + 1));
                        }
                    }
                }
                for (int i = 0; i < attributes.getLength(); i++) {
                    String attUri = attributes.getURI(i);
                    String attName = attributes.getLocalName(i);
                    if (!ImportExportBaseService.JAHIA_URI.equals(attUri)
                            || (!"path".equals(attName) && !"fileacl".equals(attName) && !"lastModification".equals(attName))) {
                        try {
                            setPropertyField(f.getPrimaryNodeType(), localName, f, getMappedProperty(f.getPrimaryNodeType(), attributes.getQName(i)), attributes.getValue(i));
                        } catch (RepositoryException e) {
                            logger.warn("Error importing " + localName + " " + path, e);
                        }
                    }
                }
                session.save();
            } catch (RepositoryException e) {
                logger.error("error", e);
            }
        }
    }
    
    private String getMappedProperty(ExtendedNodeType baseType, String qName) {
        String property = qName;
        String mappedProperty = davPropertiesMapping.get(qName);
        if (mappedProperty == null) {
            mappedProperty = mapping.getMappedProperty(baseType, qName);
        }
        if (mappedProperty != null) {
            property = mappedProperty;
        }
        return property;
    }
    
    protected final Map<String, String> initializeDavPropertiesMapping() {
        Map<String, String> davPropertiesMapping = new HashMap<String, String>(); 
        davPropertiesMapping.put("dav:creationdate", "jcr:created");
        davPropertiesMapping.put("dav:creationuser", "jcr:createdBy");
        davPropertiesMapping.put("dav:getlastmodified", "jcr:lastModified");
        davPropertiesMapping.put("dav:modificationdate", "jcr:lastModified");        
        davPropertiesMapping.put("dav:modificationuser", "jcr:lastModifiedBy");
        davPropertiesMapping.put("dav:getcontenttype", "jcr:content/jcr:mimeType");
        davPropertiesMapping.put("dav:getcontentlanguage", "mix:language|jcr:content/jcr:language");
        davPropertiesMapping.put("dav:owner", "#skip");
        davPropertiesMapping.put("dav:displayname", "mix:title|jcr:title");
        return davPropertiesMapping;
    }
    
    private boolean setPropertyField(ExtendedNodeType baseType, String localName,
            JCRNodeWrapper node, String propertyName, String value) throws RepositoryException {
        if ("#skip".equals(propertyName)) {
            return false;
        }
        JCRNodeWrapper parent = node;
        String mixinType = null;
        if (propertyName.contains("|")) {
            mixinType = StringUtils.substringBefore(propertyName, "|");
            propertyName = StringUtils.substringAfter(propertyName, "|");
        }
        if (StringUtils.contains(propertyName, "/")) {
            String parentPath = StringUtils.substringBeforeLast(propertyName, "/");
            if (parent.hasNode(parentPath)) {
                parent = parent.getNode(parentPath);
            }
            propertyName = StringUtils.substringAfterLast(propertyName, "/");
        }
        parent = checkoutNode(parent);
        if (!StringUtils.isEmpty(mixinType) && !parent.isNodeType(mixinType)) {
            parent.addMixin(mixinType);
        }

        ExtendedPropertyDefinition propertyDefinition = null;
        propertyDefinition = parent.getApplicablePropertyDefinition(propertyName);
        if (propertyDefinition == null) {
            return false;

        }
        if (propertyDefinition.isProtected()) {
            // System.out.println("protected : " + propertyName);
            return false;
        }
        Node n = parent;

        // System.out.println("setting " + propertyName);

        if (value != null && value.length() != 0 && !value.equals("<empty>")) {
            switch (propertyDefinition.getRequiredType()) {
                case PropertyType.DATE:
                    GregorianCalendar cal = new GregorianCalendar();
                    try {
                        DateFormat df = new SimpleDateFormat(ImportExportService.DATE_FORMAT);
                        Date d = df.parse(value);
                        cal.setTime(d);
                        n.setProperty(propertyName, cal);
                    } catch (java.text.ParseException e) {
                        e.printStackTrace();
                    }
                    break;

                default:
                    switch (propertyDefinition.getSelector()) {
                        case SelectorType.RICHTEXT: {
                            n.setProperty(propertyName, value);
                            break;
                        }
                        default: {
                            String[] vcs = propertyDefinition.getValueConstraints();
                            List<String> constraints = Arrays.asList(vcs);
                            if (!propertyDefinition.isMultiple()) {
                                if (value.startsWith("<jahia-resource")) {
                                    value = ResourceBundleMarker.parseMarkerValue(value)
                                            .getResourceKey();
                                    if (value.startsWith(propertyDefinition.getResourceBundleKey())) {
                                        value = value.substring(propertyDefinition
                                                .getResourceBundleKey().length() + 1);
                                    }
                                }
                                value = baseType != null ? mapping.getMappedPropertyValue(baseType,
                                        localName, value) : value;
                                if (constraints.isEmpty() || constraints.contains(value)) {
                                    try {
                                        n.setProperty(propertyName, value);
                                    } catch (Exception e) {
                                        e.printStackTrace();
                                    }
                                }
                            } else {
                                String[] strings = value.split("\\$\\$\\$");
                                List<Value> values = new ArrayList<Value>();
                                for (int i = 0; i < strings.length; i++) {
                                    String string = strings[i];

                                    if (string.startsWith("<jahia-resource")) {
                                        string = ResourceBundleMarker.parseMarkerValue(string)
                                                .getResourceKey();
                                        if (string.startsWith(propertyDefinition
                                                .getResourceBundleKey())) {
                                            string = string.substring(propertyDefinition
                                                    .getResourceBundleKey().length() + 1);
                                        }
                                    }
                                    value = baseType != null ? mapping.getMappedPropertyValue(
                                            baseType, localName, value) : value;
                                    if (constraints.isEmpty() || constraints.contains(value)) {
                                        values.add(new ValueImpl(string, propertyDefinition
                                                .getRequiredType()));
                                    }
                                }
                                ;
                                n.setProperty(propertyName,
                                        values.toArray(new Value[values.size()]));
                            }
                            break;
                        }
                    }
            }
        } else {
            return false;
        }

        return true;
    }
    
    private JCRNodeWrapper checkoutNode(JCRNodeWrapper node) throws RepositoryException {
        if (!node.isCheckedOut()) {
            session.checkout(node);
        }
        return node;
    }
}
