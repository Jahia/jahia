/**
 * Jahia Enterprise Edition v6
 *
 * Copyright (C) 2002-2009 Jahia Solutions Group. All rights reserved.
 *
 * Jahia delivers the first Open Source Web Content Integration Software by combining Enterprise Web Content Management
 * with Document Management and Portal features.
 *
 * The Jahia Enterprise Edition is delivered ON AN "AS IS" BASIS, WITHOUT WARRANTY OF ANY KIND, EITHER EXPRESSED OR
 * IMPLIED.
 *
 * Jahia Enterprise Edition must be used in accordance with the terms contained in a separate license agreement between
 * you and Jahia (Jahia Sustainable Enterprise License - JSEL).
 *
 * If you are unsure which license is appropriate for your use, please contact the sales department at sales@jahia.com.
 */
package org.jahia.services.templates;

import static org.jahia.services.templates.JahiaTemplatesPackageHandler.NS_URI_DEF;
import static org.jahia.services.templates.JahiaTemplatesPackageHandler.NS_URI_JAHIA;
import static org.jahia.services.templates.JahiaTemplatesPackageHandler.NS_URI_XSI;
import static org.jahia.services.templates.JahiaTemplatesPackageHandler.SCHEMA_LOCATION;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.Writer;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import org.apache.commons.digester.Digester;
import org.apache.log4j.Logger;
import org.jahia.data.templates.JahiaTemplateDef;
import org.jahia.data.templates.JahiaTemplatesPackage;
import org.jahia.exceptions.JahiaException;
import org.jahia.exceptions.JahiaTemplateServiceException;
import org.jahia.services.importexport.DataWriter;
import org.jahia.utils.xml.XMLParser;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.xml.sax.ErrorHandler;
import org.xml.sax.SAXException;
import org.xml.sax.SAXParseException;
import org.xml.sax.helpers.AttributesImpl;

/**
 * Helper class for parsing and serializing the templates deployment descriptor
 * file.
 * 
 * @author Sergiy Shyrkov
 */
final class TemplateDeploymentDescriptorHelper {

    private static Logger logger = Logger
            .getLogger(TemplateDeploymentDescriptorHelper.class);

    public static final String TEMPLATES_DEPLOYMENT_DESCRIPTOR_NAME = "templates.xml";

    public static JahiaTemplatesPackage parse(String docPath, ClassLoader cl)
            throws JahiaException {
        Digester digester = new Digester();
        digester.setValidating(true);
        try {
            digester.setFeature("http://xml.org/sax/features/validation", true);
            digester.setFeature(
                    "http://apache.org/xml/features/validation/schema", true);
            digester.setFeature("http://xml.org/sax/features/namespaces", true);
        } catch (Exception e) {
            logger
                    .error(
                            "Unable to enable XML schema validation feature for parsing template deployment descriptors",
                            e);
        }
        digester.setEntityResolver(org.jahia.settings.SettingsBean
                .getInstance().getDtdEntityResolver());
        digester.setErrorHandler(new ErrorHandler() {
            public void error(SAXParseException exception) throws SAXException {
                throw exception;
            }

            public void fatalError(SAXParseException exception)
                    throws SAXException {
                throw exception;
            }

            public void warning(SAXParseException exception)
                    throws SAXException {
                logger.warn(exception);
            }
        });

        digester.setClassLoader(cl);

        digester.addObjectCreate("template-set", JahiaTemplatesPackage.class);
        digester.addBeanPropertySetter("template-set/package-name", "name");
        digester.addBeanPropertySetter("template-set/extends", "extends");
        digester
                .addBeanPropertySetter("template-set/root-folder", "rootFolder");
//        digester.addBeanPropertySetter("template-set/classes-file",
//                "classesFile");
//        digester.addBeanPropertySetter("template-set/classes-root",
//                "classesRoot");
        digester.addBeanPropertySetter("template-set/initial-import",
                "initialImport");

        digester.addBeanPropertySetter("template-set/provider", "provider");
        digester.addBeanPropertySetter("template-set/thumbnail", "thumbnail");
        digester.addBeanPropertySetter("template-set/description",
                "description");
        digester.addBeanPropertySetter("template-set/resource-bundle",
                "resourceBundleName");
        digester.addBeanPropertySetter("template-set/definitions-file",
                "definitionsFile");
        digester.addBeanPropertySetter("template-set/rules-file",
                "rulesFile");

        digester.addBeanPropertySetter("template-set/common-pages/my-settings",
                "mySettingsPageName");
        digester.addBeanPropertySetter(
                "template-set/common-pages/my-settings-success",
                "mySettingsSuccessPageName");
        digester.addBeanPropertySetter(
                "template-set/common-pages/search-results",
                "searchResultsPageName");

        digester.addCallMethod("template-set/properties/property",
                "addProperty", 2);
        digester.addCallParam("template-set/properties/property", 0, "key");
        digester.addCallParam("template-set/properties/property", 1, "value");

        digester.addSetProperties("template-set/templates", "homepage",
                "homePageName");
        digester.addSetProperties("template-set/templates", "default",
                "defaultPageName");

        digester.addObjectCreate("template-set/templates/template",
                JahiaTemplateDef.class);
        digester.addSetProperties("template-set/templates/template", "name",
                "name");
        digester.addSetProperties("template-set/templates/template",
                "display-name", "displayName");
        digester.addSetProperties("template-set/templates/template",
                "filename", "fileName");
        digester.addSetProperties("template-set/templates/template",
                "page-type", "pageType");
        digester.addSetProperties("template-set/templates/template",
                "description", "description");        
        digester.addSetProperties("template-set/templates/template", "visible",
                "visible");

        digester
                .addSetNext("template-set/templates/template", "addTemplateDef");

        JahiaTemplatesPackage templatesPackage = null;
        try {
            templatesPackage = (JahiaTemplatesPackage) digester.parse("file:"
                    + docPath);
        } catch (Exception e) {
            throw new JahiaTemplateServiceException(
                    "Error parsing template deployment descriptor rom file: "
                            + docPath, e);
        }
        return templatesPackage;
    }

    public static JahiaTemplatesPackage parseLegacyFormat(Document m_XMLDocument)
            throws JahiaException {
        Element docElNode = (Element) m_XMLDocument.getDocumentElement();
        JahiaTemplatesPackage templatesPackage = new JahiaTemplatesPackage();

        if (!docElNode.getNodeName().equalsIgnoreCase("tpml")) {

            throw new JahiaException("Invalid XML format",
                    "tpml tag is not present as starting tag in file",
                    JahiaException.ERROR_SEVERITY, JahiaException.SERVICE_ERROR);
        }

        // get the package name
        templatesPackage.setName(XMLParser.getParameterValue(docElNode,
                "package-name"));

        // get the root folder
        templatesPackage.setRootFolder(XMLParser.getParameterValue(docElNode,
                "root-folder"));

        // get the class file entry
        templatesPackage.setClassesFile(XMLParser.getParameterValue(docElNode,
                "classes-file"));

        // get the class file entry
        templatesPackage.setClassesRoot(XMLParser.getParameterValue(docElNode,
                "classes-root"));

        // get the class file entry
        templatesPackage.setInitialImport(XMLParser.getParameterValue(
                docElNode, "initial-import"));

        // get the provider info
        templatesPackage.setProvider(XMLParser.getParameterValue(docElNode,
                "provider"));

        // get the definitions files
        templatesPackage.setDefinitionsFile(XMLParser.getParameterValue(
                docElNode, "definitions-file"));

        // get the rules files
        templatesPackage.setRulesFile(XMLParser.getParameterValue(
                docElNode, "rules-file"));

        // get the thumbnail image file name
        templatesPackage.setThumbnail(XMLParser.getParameterValue(docElNode,
                "thumbnail"));

        // build the templates list
        List nodesList = XMLParser.getChildNodes(docElNode, "template");

        int size = nodesList.size();
        if (size > 0) {

            Node nodeItem = null;
            String templateName = "";
            String templateFile = "";
            String templateDisplayName = "";
            String pageType = "";
            for (int i = 0; i < size; i++) {
                nodeItem = (Node) nodesList.get(i);

                templateName = XMLParser.getParameterValue(nodeItem, "name");

                templateFile = XMLParser
                        .getParameterValue(nodeItem, "filename");

                pageType = XMLParser.getParameterValue(nodeItem, "page-type");

                templateDisplayName = XMLParser.getParameterValue(nodeItem,
                        "display-name");
                if (templateDisplayName.length() <= 0) {
                    templateDisplayName = templateName;
                }
                
                String description = XMLParser.getParameterValue(nodeItem,
                        "description");

                boolean visible = true;

                String val = XMLParser.getAttributeValue(nodeItem, "visible");
                if (val != null) {
                    visible = (Integer.parseInt(val) == 1);
                }

                boolean isHomePage = false;
                val = XMLParser.getAttributeValue(nodeItem, "homepage");
                if (val != null) {
                    isHomePage = (Integer.parseInt(val) == 1);
                }

                boolean isDefault = false;
                val = XMLParser.getAttributeValue(nodeItem, "default");
                if (val != null) {
                    isDefault = (Integer.parseInt(val) == 1);
                }

                if ((templateName != null) && (templateName.length() > 0)
                        && (templateFile != null)
                        && (templateFile.length() > 0)
                        && (templateDisplayName != null)
                        && (templateDisplayName.length() > 0)) {

                    templatesPackage.addTemplateDef(new JahiaTemplateDef(
                            templateName, templateFile, null,
                            templateDisplayName, pageType, description, visible, isHomePage,
                            isDefault));

                    if (isHomePage) {
                        templatesPackage.setHomePageName(templateName);
                    }
                    if (isDefault) {
                        templatesPackage.setDefaultPageName(templateName);
                    }
                }
            }
            if (templatesPackage.getHomePageName() == null) {
                templatesPackage
                        .setHomePageName(((JahiaTemplateDef) templatesPackage
                                .getTemplates().get(0)).getName());
            }
            if (templatesPackage.getDefaultPageName() == null) {
                templatesPackage
                        .setDefaultPageName(((JahiaTemplateDef) templatesPackage
                                .getTemplates().get(0)).getName());
            }
        }

        return templatesPackage;
    }

    public static void serialize(JahiaTemplatesPackage pkg,
            File templateSetFolder) throws JahiaTemplateServiceException {

        try {
            serialize(pkg, new FileWriter(new File(templateSetFolder,
                    TEMPLATES_DEPLOYMENT_DESCRIPTOR_NAME)));
        } catch (IOException e) {
            logger.error(
                    "Unable to persist template deployment desriptor for package '"
                            + pkg.getName(), e);
            throw new JahiaTemplateServiceException(
                    "Unable to persist template deployment desriptor for package '"
                            + pkg.getName(), e);
        }
    }

    public static void serialize(JahiaTemplatesPackage pkg, Writer writer)
            throws JahiaTemplateServiceException {
        try {
            DataWriter ch = new DataWriter(writer);
            ch.startDocument();

            AttributesImpl emptyAttr = new AttributesImpl();
            AttributesImpl attr = new AttributesImpl();
            attr.addAttribute(NS_URI_DEF, "xsi",
                    "xmlns:xsi", "CDATA",
                    NS_URI_XSI);
            attr.addAttribute("", "xmlns", "xmlns", "CDATA",
                    NS_URI_JAHIA);
            attr.addAttribute(NS_URI_XSI,
                    "schemaLocation", "xsi:schemaLocation", "CDATA",
                    SCHEMA_LOCATION);

            ch.startElement(NS_URI_JAHIA,
                    "template-set", "template-set", attr);

            ch.dataElement("package-name", pkg.getName());

            if (pkg.getExtends() != null) {
                ch.dataElement("extends", pkg.getExtends());
            }

            ch.dataElement("root-folder", pkg.getRootFolder());

            if (pkg.getClassesFile() != null) {
                ch.dataElement("classes-file", pkg.getClassesFile());
            }

            if (pkg.getClassesRoot() != null) {
                ch.dataElement("classes-root", pkg.getClassesRoot());
            }

            if (pkg.getInitialImport() != null) {
                ch.dataElement("initial-import", pkg.getInitialImport());
            }

            if (pkg.getProvider() != null) {
                ch.dataElement("provider", pkg.getProvider());
            }

            if (pkg.getThumbnail() != null) {
                ch.dataElement("thumbnail", pkg.getThumbnail());
            }

            if (pkg.getDescription() != null) {
                ch.dataElement("description", pkg.getDescription());
            }

            if (pkg.getResourceBundleName() != null) {
                ch.dataElement("resource-bundle", pkg.getResourceBundleName());
            }

            if (!pkg.getDefinitionsFiles().isEmpty()) {
                for (String name : pkg.getDefinitionsFiles()) {
                    ch.dataElement("definitions-file", name);
                }
            }

            if (!pkg.getRulesFiles().isEmpty()) {
                for (String name : pkg.getRulesFiles()) {
                    ch.dataElement("rules-file", name);
                }
            }

            // common pages
            if (pkg.getMySettingsPageName() != null
                    || pkg.getSearchResultsPageName() != null) {
                ch.startElement(NS_URI_JAHIA,
                        "common-pages", "common-pages", emptyAttr);
                if (pkg.getMySettingsPageName() != null) {
                    ch.dataElement("my-settings", pkg.getMySettingsPageName());
                }
                if (pkg.getMySettingsSuccessPageName() != null) {
                    ch.dataElement("my-settings-success", pkg
                            .getMySettingsSuccessPageName());
                }
                if (pkg.getSearchResultsPageName() != null) {
                    ch.dataElement("search-results", pkg
                            .getSearchResultsPageName());
                }
                ch.endElement(NS_URI_JAHIA,
                        "common-pages", "common-pages");
            }

            // properties
            if (!pkg.getProperties().isEmpty()) {
                ch.startElement(NS_URI_JAHIA,
                        "properties", "properties", emptyAttr);
                for (Map.Entry<String, String> property : pkg.getProperties()
                        .entrySet()) {
                    attr = new AttributesImpl();
                    attr.addAttribute("", "key", "key", "CDATA", property
                            .getKey());
                    attr.addAttribute("", "value", "value", "CDATA", property
                            .getValue());
                    ch.emptyElement(NS_URI_JAHIA,
                            "property", "property", attr);
                }
                ch.endElement(NS_URI_JAHIA,
                        "properties", "properties");
            }

            // templates
            attr = new AttributesImpl();
            if (pkg.getDefaultPageName() != null) {
                attr.addAttribute("", "default", "default", "CDATA", pkg
                        .getDefaultPageName());
            }
            if (pkg.getHomePageName() != null) {
                attr.addAttribute("", "homepage", "homepage", "CDATA", pkg
                        .getHomePageName());
            }
            ch.startElement(NS_URI_JAHIA,
                    "templates", "templates", attr);
            for (Iterator iterator = pkg.getTemplates().iterator(); iterator
                    .hasNext();) {
                JahiaTemplateDef template = (JahiaTemplateDef) iterator.next();
                attr = new AttributesImpl();
                attr.addAttribute("", "name", "name", "CDATA", template
                        .getName());
                if (template.getDisplayName() != null) {
                    attr.addAttribute("", "display-name", "display-name",
                            "CDATA", template.getDisplayName());
                }
                attr.addAttribute("", "filename", "filename", "CDATA", template
                        .getFileName());
                if (!template.isVisible()) {
                    attr.addAttribute("", "visible", "visible", "CDATA",
                            "false");
                }
                if (template.getPageType() != null) {
                    attr.addAttribute("", "page-type", "page-type", "CDATA",
                            template.getPageType());
                }
                if (template.getDescription() != null) {
                    attr.addAttribute("", "description", "description", "CDATA",
                            template.getDescription());
                }
                ch.emptyElement(NS_URI_JAHIA,
                        "template", "template", attr);
            }
            ch.endElement(NS_URI_JAHIA,
                    "templates", "templates");

            ch.endElement(NS_URI_JAHIA,
                    "template-set", "template-set");
            ch.endDocument();
            ch.flush();
            writer.flush();
        } catch (Exception e) {
            logger.error(
                    "Unable to persist template deployment desriptor for package '"
                            + pkg.getName(), e);
            throw new JahiaTemplateServiceException(
                    "Unable to persist template deployment desriptor for package '"
                            + pkg.getName(), e);
        }
    }

}