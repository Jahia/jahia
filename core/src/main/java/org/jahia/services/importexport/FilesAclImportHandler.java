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

package org.jahia.services.importexport;

import org.jahia.api.Constants;
import org.jahia.services.content.JCRObservationManager;
import org.jahia.utils.zip.ZipEntry;
import org.slf4j.Logger;
import org.apache.commons.lang.StringUtils;
import org.jahia.registries.ServicesRegistry;
import org.jahia.services.content.JCRContentUtils;
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

import javax.jcr.*;
import javax.jcr.nodetype.ConstraintViolationException;
import javax.jcr.query.Query;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * 
 * User: toto
 * Date: 6 juil. 2005
 * Time: 17:31:05
 */
public class FilesAclImportHandler extends DefaultHandler {
    private static Logger logger = org.slf4j.LoggerFactory.getLogger(FilesAclImportHandler.class);

    private File archive;
    private NoCloseZipInputStream zis;
    private ZipEntry nextEntry;
    private List<String> fileList = new ArrayList<String>();

    private JahiaSite site;
    private DefinitionsMapping mapping;
    private JCRSessionWrapper session;

    private Map<String, String> davPropertiesMapping = initializeDavPropertiesMapping();

    public static final DateFormat DATE_FORMAT = new SimpleDateFormat(ImportExportService.DATE_FORMAT);

    public FilesAclImportHandler(JahiaSite site, DefinitionsMapping mapping, File archive, List<String> fileList) {
        this.site = site;
        this.mapping = mapping;
        this.archive = archive;
        this.fileList = fileList;
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

            try {
                boolean contentFound = findContent(path);

                if (path.startsWith("/shared")) {
                    path = "/sites/" + site.getSiteKey() + "/files" + path;
                } else if (path.startsWith("/users/")) {
                    Matcher m = Pattern.compile("/users/([^/]+)(/.*)?").matcher(path);
                    if (m.matches()) {
                        path = ServicesRegistry.getInstance().getJahiaUserManagerService().getUserSplittingRule().getPathForUsername(m.group(1));
                        path = path + "/files" + ((m.group(2) != null) ? m.group(2) : "");
                    }
                }

                if (session.itemExists(path)) {
                    return;
                }
                JCRNodeWrapper f;
                path = JCRContentUtils.escapeNodePath(path);
                path = path.replace(":","_");

                String parentPath = StringUtils.substringBeforeLast(path, "/");
                try {
                    f = session.getNode(parentPath);
                } catch (PathNotFoundException e) {
                    f = ImportExportBaseService.getInstance().ensureDir(session, parentPath, site);
                }


                Calendar created = new GregorianCalendar();
                if (attributes.getValue("dav:creationdate") != null) {
                    created.setTime(DATE_FORMAT.parse(attributes.getValue("dav:creationdate")));
                }
                Calendar lastModified = new GregorianCalendar();
                if (attributes.getValue("dav:modificationdate") != null) {
                    lastModified.setTime(DATE_FORMAT.parse(attributes.getValue("dav:modificationdate")));
                }
                String createdBy = attributes.getValue("dav:creationuser");
                String lastModifiedBy = attributes.getValue("dav:modificationuser");

                checkoutNode(f);
                if (!contentFound) {
                    f = f.addNode(StringUtils.substringAfterLast(path, "/"), "jnt:folder", null, created, createdBy, lastModified, lastModifiedBy);
                } else {
                    f = f.addNode(StringUtils.substringAfterLast(path, "/"), "jnt:file", null, created, createdBy, lastModified, lastModifiedBy);
                    f.getFileContent().uploadFile(zis, attributes.getValue("dav:getcontenttype"));
                    zis.close();
                }
                if (acl != null && acl.length() > 0) {
                    StringTokenizer st = new StringTokenizer(acl, "|");
                    while (st.hasMoreElements()) {
                        String s = st.nextToken();
                        int beginIndex = s.lastIndexOf(":");

                        String principal = s.substring(0, beginIndex);

                        Set<String> grantedRoles = new HashSet<String>();
                        Set<String> removedRoles = new HashSet<String>();
                        String perm = s.substring(beginIndex + 1);
                        if (perm.charAt(0) == 'r') {
                            grantedRoles.addAll(LegacyImportHandler.READ_ROLES);
                        } else {
                            removedRoles.addAll(LegacyImportHandler.READ_ROLES);
                        }
                        if (perm.charAt(1) == 'w') {
                            grantedRoles.addAll(LegacyImportHandler.WRITE_ROLES);
                        } else {
                            removedRoles.addAll(LegacyImportHandler.WRITE_ROLES);
                        }

                        if (!grantedRoles.isEmpty()) {
                            f.grantRoles(principal, grantedRoles);
                        }
                        if (!removedRoles.isEmpty()) {
                            f.denyRoles(principal, removedRoles);
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
                session.save(JCRObservationManager.IMPORT);
            } catch (Exception e) {
                logger.error("error", e);
            }
        }
    }

    private String getMappedProperty(ExtendedNodeType baseType, String qName) {
        String mappedProperty = davPropertiesMapping.get(qName);
        if (mappedProperty == null && mapping != null) {
            mappedProperty = mapping.getMappedProperty(baseType, qName);
        }
        if (mappedProperty != null) {
            return mappedProperty;
        }
        return null;
    }

    protected final Map<String, String> initializeDavPropertiesMapping() {
        Map<String, String> davPropertiesMapping = new HashMap<String, String>();
        davPropertiesMapping.put("dav:getcontentlanguage", "mix:language|jcr:content/jcr:language");
        davPropertiesMapping.put("dav:displayname", "mix:title|jcr:title");
        davPropertiesMapping.put("ged:categories", "jmix:categorized|j:defaultCategory");
        return davPropertiesMapping;
    }

    private boolean setPropertyField(ExtendedNodeType baseType, String localName,
                                     JCRNodeWrapper node, String propertyName, String value) throws RepositoryException {
        if (propertyName == null || "#skip".equals(propertyName)) {
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
                        logger.error(e.getMessage(), e);
                    }
                    break;

                default:
                    switch (propertyDefinition.getSelector()) {
                        case SelectorType.CATEGORY: {
                            String[] cats = value.split(",");
                            List<Value> values = new ArrayList<Value>();
                            for (int i = 0; i < cats.length; i++) {
                                String cat = cats[i];
                                Query q = session.getWorkspace().getQueryManager().createQuery("select * from [jnt:category] as cat where NAME(cat) = '"+cat+"'", Query.JCR_SQL2);
                                NodeIterator ni = q.execute().getNodes();
                                if (ni.hasNext()) {
                                    values.add(session.getValueFactory().createValue(ni.nextNode()));
                                }
                            }
                            n.setProperty(propertyName, values.toArray(new Value[values.size()]));
                            break;
                        }
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
                                value = baseType != null && mapping != null ? mapping.getMappedPropertyValue(baseType,
                                        localName, value) : value;
                                if (constraints.isEmpty() || constraints.contains(value)) {
                                    try {
                                        n.setProperty(propertyName, value);
                                    } catch (Exception e) {
                                        logger.error(e.getMessage(), e);
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

    private boolean findContent(String path) throws IOException {
        if (archive == null) {
            return false;
        }
        if (zis == null) {
            zis = new NoCloseZipInputStream(new BufferedInputStream(new FileInputStream(archive)));
            nextEntry = zis.getNextEntry();
        }

        path = JCRContentUtils.replaceColon(path);

        int fileIndex = fileList.indexOf(path);
        if (fileIndex != -1) {
            if (fileList.indexOf("/" + nextEntry.getName().replace('\\', '/')) > fileIndex) {
                zis.reallyClose();
                zis = new NoCloseZipInputStream(new BufferedInputStream(new FileInputStream(archive)));
            }
            do {
                nextEntry = zis.getNextEntry();
            } while (!("/" + nextEntry.getName().replace('\\', '/')).equals(path));

            return true;
        }
        return false;
    }


}
