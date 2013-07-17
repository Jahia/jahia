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

package org.jahia.services.templates;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.UnsupportedEncodingException;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

import javax.jcr.NodeIterator;
import javax.jcr.PathNotFoundException;
import javax.jcr.Property;
import javax.jcr.PropertyIterator;
import javax.jcr.PropertyType;
import javax.jcr.RepositoryException;
import javax.jcr.Value;
import javax.jcr.nodetype.NodeType;
import javax.jcr.nodetype.NodeTypeIterator;
import javax.jcr.query.Query;
import javax.jcr.query.QueryManager;
import javax.xml.transform.TransformerException;

import org.apache.commons.io.Charsets;
import org.apache.commons.io.FileUtils;
import org.apache.commons.io.IOUtils;
import org.apache.commons.lang.StringUtils;
import org.dom4j.Document;
import org.dom4j.Element;
import org.dom4j.io.OutputFormat;
import org.dom4j.io.SAXReader;
import org.dom4j.io.XMLWriter;
import org.jahia.api.Constants;
import org.jahia.data.templates.JahiaTemplatesPackage;
import org.jahia.services.content.JCRCallback;
import org.jahia.services.content.JCRNodeWrapper;
import org.jahia.services.content.JCRPropertyWrapper;
import org.jahia.services.content.JCRSessionWrapper;
import org.jahia.services.content.JCRTemplate;
import org.jahia.services.content.decorator.JCRSiteNode;
import org.jahia.services.content.nodetypes.ExtendedNodeType;
import org.jahia.services.content.nodetypes.NodeTypeRegistry;
import org.jahia.services.importexport.ImportExportBaseService;
import org.jahia.services.importexport.ImportExportService;
import org.jahia.services.importexport.ReferencesHelper;
import org.jahia.services.sites.JahiaSitesService;
import org.jahia.settings.SettingsBean;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.context.ApplicationEventPublisherAware;
import org.xml.sax.SAXException;

import difflib.DiffUtils;
import difflib.Patch;
import difflib.PatchFailedException;
import difflib.myers.Equalizer;
import difflib.myers.MyersDiff;

/**
 * Module installation helper.
 * 
 * @author Sergiy Shyrkov
 */
public class ModuleInstallationHelper implements ApplicationEventPublisherAware {

    private static Logger logger = LoggerFactory.getLogger(ModuleInstallationHelper.class);

    private static final MyersDiff MYERS_DIFF = new MyersDiff(new Equalizer() {
        public boolean equals(Object o, Object o1) {
            String s1 = (String) o;
            String s2 = (String) o1;
            return s1.trim().equals(s2.trim());
        }
    });

    private static Pattern UNICODE_PATTERN = Pattern.compile("\\\\u([0-9A-Fa-f]{2})([0-9A-Fa-f]{2})");

    private ApplicationEventPublisher applicationEventPublisher;

    private OutputFormat prettyPrint = OutputFormat.createPrettyPrint();

    private JahiaSitesService siteService;

    private SourceControlFactory sourceControlFactory;

    private TemplatePackageRegistry templatePackageRegistry;

    private boolean addDependencyValue(JCRNodeWrapper originalNode, JCRNodeWrapper destinationNode, String propertyName)
            throws RepositoryException {
        // Version v = templatePackageRegistry.lookupByFileName(originalNode.getName()).getLastVersion();
        String newStringValue = originalNode.getName();
        if (destinationNode.hasProperty(propertyName)) {
            JCRPropertyWrapper installedModules = destinationNode.getProperty(propertyName);
            Value[] values = installedModules.getValues();
            for (Value value : values) {
                if (value.getString().equals(originalNode.getName())) {
                    return true;
                }
            }

            destinationNode.getSession().checkout(destinationNode);
            installedModules.addValue(originalNode.getName());
        } else {
            destinationNode.setProperty(propertyName, new String[] { newStringValue });
        }
        return false;
    }

    public void autoInstallModulesToSites(JahiaTemplatesPackage module, JCRSessionWrapper session)
            throws RepositoryException {
        Set<String> autoInstalled = new HashSet<String>();
        if (StringUtils.isNotBlank(module.getAutoDeployOnSite())) {
            if ("system".equals(module.getAutoDeployOnSite())) {
                if (session.nodeExists("/sites/systemsite")) {
                    installModule(module, "/sites/systemsite", session);
                    autoInstalled.add("systemsite");
                }
            } else if ("all".equals(module.getAutoDeployOnSite())) {
                if (session.nodeExists("/sites/systemsite")) {
                    installModuleOnAllSites(module, session, null);
                    return;
                }
            }
        }

        List<JCRNodeWrapper> sites = new ArrayList<JCRNodeWrapper>();
        NodeIterator ni = session.getNode("/sites").getNodes();
        while (ni.hasNext()) {
            JCRNodeWrapper next = (JCRNodeWrapper) ni.next();
            if (autoInstalled.contains(next.getName())) {
                continue;
            }
            if (next.hasProperty("j:installedModules")) {
                Value[] v = next.getProperty("j:installedModules").getValues();
                for (Value value : v) {
                    if (value.getString().equals(module.getRootFolder())) {
                        sites.add(next);
                    }
                }
            }
        }
        if (!sites.isEmpty()) {
            installModuleOnAllSites(module, session, sites);
        }
    }

    private List<String> convertToNativeEncoding(List<String> sourceContent, Charset charset)
            throws UnsupportedEncodingException {
        List<String> targetContent = new ArrayList<String>();
        for (String s : sourceContent) {
            Matcher m;
            int start = 0;
            while ((m = UNICODE_PATTERN.matcher(s)).find(start)) {
                String replacement = new String(new byte[] { (byte) Integer.parseInt(m.group(1), 16),
                        (byte) Integer.parseInt(m.group(2), 16) }, "UTF-16");
                if (charset.decode(charset.encode(replacement)).toString().equals(replacement)) {
                    s = m.replaceFirst(replacement);
                }
                start = m.start() + 1;
            }
            targetContent.add(s);
        }
        return targetContent;
    }

    public SourceControlFactory getSourceControlFactory() {
        return sourceControlFactory;
    }

    public void installModule(final JahiaTemplatesPackage module, final String sitePath, final JCRSessionWrapper session)
            throws RepositoryException {
        installModules(Arrays.asList(module), sitePath, session);
    }

    public void installModule(final String module, final String sitePath, String username) throws RepositoryException {
        JCRTemplate.getInstance().doExecuteWithSystemSession(username, new JCRCallback<Object>() {
            public Object doInJCR(JCRSessionWrapper session) throws RepositoryException {
                installModules(Arrays.asList(templatePackageRegistry.lookupByFileName(module)), sitePath, session);
                session.save();
                return null;
            }
        });
    }

    public void installModuleOnAllSites(JahiaTemplatesPackage module, JCRSessionWrapper sessionWrapper,
            List<JCRNodeWrapper> sites) throws RepositoryException {
        if (sites == null) {
            sites = new ArrayList<JCRNodeWrapper>();
            NodeIterator ni = sessionWrapper.getNode("/sites").getNodes();
            while (ni.hasNext()) {
                JCRNodeWrapper next = (JCRNodeWrapper) ni.next();
                sites.add(next);
            }
        }

        JCRNodeWrapper tpl = sessionWrapper.getNode("/modules/" + module.getRootFolderWithVersion());
        for (JCRNodeWrapper site : sites) {
            if (tpl.hasProperty("j:moduleType")
                    && JahiaTemplateManagerService.MODULE_TYPE_TEMPLATES_SET.equals(tpl.getProperty("j:moduleType")
                            .getString())) {
                if (tpl.getName().equals(site.getResolveSite().getTemplateFolder())) {
                    installModule(module, site.getPath(), sessionWrapper);
                }
            } else {
                installModule(module, site.getPath(), sessionWrapper);
            }
        }
    }

    public void installModules(final List<JahiaTemplatesPackage> modules, final String sitePath,
            final JCRSessionWrapper session) throws RepositoryException {
        if (!sitePath.startsWith("/sites/")) {
            return;
        }
        final JCRSiteNode siteNode = (JCRSiteNode) session.getNode(sitePath);

        HashMap<String, List<String>> references = new HashMap<String, List<String>>();
        for (JahiaTemplatesPackage module : modules) {
            logger.info("Installing " + module.getName() + " on " + sitePath);
            JCRNodeWrapper moduleNode = null;
            try {
                moduleNode = session.getNode("/modules/" + module.getRootFolder());

                String moduleName = moduleNode.getName();

                if (moduleNode.isNodeType("jnt:module")) {
                    moduleNode = moduleNode.getNode(module.getVersion().toString());
                }
                synchro(moduleNode, siteNode, session, moduleName, references);

                ReferencesHelper.resolveCrossReferences(session, references);

                addDependencyValue(moduleNode.getParent(), siteNode, "j:installedModules");
                logger.info("Done installing " + module.getName() + " on " + sitePath);
            } catch (PathNotFoundException e) {
                logger.warn("Cannot find module for path {}. Skipping deployment to site {}.", module, sitePath);
                return;
            }

        }

        applicationEventPublisher.publishEvent(new JahiaTemplateManagerService.ModuleDeployedOnSiteEvent(sitePath,
                ModuleInstallationHelper.class.getName()));
    }

    private boolean isBinary(List<String> text) {
        for (String s : text) {
            if (s.contains("\u0000")) {
                return true;
            }
        }
        return false;
    }

    private void keepReference(JCRNodeWrapper destinationNode, Map<String, List<String>> references, Property property,
            String value) throws RepositoryException {
        if (!references.containsKey(value)) {
            references.put(value, new ArrayList<String>());
        }
        references.get(value).add(destinationNode.getIdentifier() + "/" + property.getName());
    }

    public void purgeModuleContent(final List<JahiaTemplatesPackage> modules, final String sitePath,
            final JCRSessionWrapper session) throws RepositoryException {
        QueryManager manager = session.getWorkspace().getQueryManager();
        for (JahiaTemplatesPackage module : modules) {
            NodeTypeIterator nti = NodeTypeRegistry.getInstance().getNodeTypes(module.getRootFolder());
            while (nti.hasNext()) {
                ExtendedNodeType next = (ExtendedNodeType) nti.next();
                Query q = manager.createQuery("select * from ['" + next.getName()
                        + "'] as c where isdescendantnode(c,'" + sitePath + "')", Query.JCR_SQL2);
                try {
                    NodeIterator ni = q.execute().getNodes();
                    while (ni.hasNext()) {
                        JCRNodeWrapper nodeWrapper = (JCRNodeWrapper) ni.nextNode();
                        nodeWrapper.remove();
                    }
                } catch (RepositoryException e) {
                    logger.error("Cannot remove node", e);
                }
            }
        }
    }

    public List<File> regenerateImportFile(String moduleName, File sources, JCRSessionWrapper session)
            throws RepositoryException {
        List<File> modifiedFiles = new ArrayList<File>();

        SourceControlManagement scm = null;
        try {
            scm = sourceControlFactory.getSourceControlManagement(sources);
        } catch (Exception e) {
            logger.error("Cannot get SCM", e);
        }

        // Handle import
        File sourcesImportFolder = new File(sources, "src/main/import");

        JahiaTemplatesPackage aPackage = templatePackageRegistry.lookupByFileName(moduleName);

        try {
            File f = File.createTempFile("import", null);

            if (session.getLocale() != null) {
                throw new RepositoryException("Cannot generated export with i18n session");
            }

            Map<String, Object> params = new HashMap<String, Object>();
            params.put(ImportExportService.XSL_PATH, SettingsBean.getInstance().getJahiaEtcDiskPath()
                    + "/repository/export/templatesCleanup.xsl");
            FileOutputStream out = new FileOutputStream(f);
            ImportExportBaseService.getInstance().exportZip(
                    session.getNode("/modules/" + aPackage.getRootFolderWithVersion()), session.getRootNode(), out,
                    params);
            IOUtils.closeQuietly(out);
            ZipInputStream zis = null;
            try {
                zis = new ZipInputStream(new FileInputStream(f));
                ZipEntry zipentry;
                while ((zipentry = zis.getNextEntry()) != null) {
                    if (!zipentry.isDirectory()) {
                        try {
                            String name = zipentry.getName();
                            name = name.replace(aPackage.getRootFolderWithVersion(), aPackage.getRootFolder());
                            File sourceFile = new File(sourcesImportFolder, name);
                            if (saveFile(zis, sourceFile)) {
                                modifiedFiles.add(sourceFile);
                            }
                        } catch (IOException e) {
                            logger.error(e.getMessage(), e);
                        }
                    }
                }
            } catch (Exception e) {
                logger.error("Cannot patch import file", e);
            } finally {
                if (zis != null) {
                    IOUtils.closeQuietly(zis);
                }
            }

            if (scm != null) {
                try {
                    scm.setModifiedFile(modifiedFiles);
                } catch (Exception e) {
                    logger.error(e.getMessage(), e);
                }
            }
        } catch (RepositoryException e1) {
            logger.error(e1.getMessage(), e1);
        } catch (SAXException e11) {
            logger.error(e11.getMessage(), e11);
        } catch (IOException e12) {
            logger.error(e12.getMessage(), e12);
        } catch (TransformerException e13) {
            logger.error(e13.getMessage(), e13);
        }

        return modifiedFiles;
    }

    @SuppressWarnings("unchecked")
    private boolean saveFile(InputStream source, File target) throws IOException, PatchFailedException {
        Charset transCodeTarget = null;
        if (target.getParentFile().getName().equals("resources") && target.getName().endsWith(".properties")) {
            transCodeTarget = Charsets.ISO_8859_1;
        }

        if (!target.exists()) {
            target.getParentFile().mkdirs();
            if (transCodeTarget != null) {
                FileUtils.writeLines(target, transCodeTarget.name(),
                        convertToNativeEncoding(IOUtils.readLines(source, Charsets.UTF_8), transCodeTarget), "\n");
            } else {
                FileOutputStream output = FileUtils.openOutputStream(target);
                try {
                    IOUtils.copy(source, output);
                    output.close();
                } finally {
                    IOUtils.closeQuietly(output);
                }
            }
            return true;
        } else {
            List<String> targetContent = FileUtils.readLines(target, transCodeTarget != null ? transCodeTarget
                    : Charsets.UTF_8);
            if (!isBinary(targetContent)) {
                List<String> sourceContent = IOUtils.readLines(source, Charsets.UTF_8);
                if (transCodeTarget != null) {
                    sourceContent = convertToNativeEncoding(sourceContent, transCodeTarget);
                }
                Patch patch = DiffUtils.diff(targetContent, sourceContent, MYERS_DIFF);
                if (!patch.getDeltas().isEmpty()) {
                    targetContent = (List<String>) patch.applyTo(targetContent);
                    FileUtils.writeLines(target, transCodeTarget != null ? transCodeTarget.name() : "UTF-8",
                            targetContent, "\n");
                    return true;
                }
            } else {
                byte[] sourceArray = IOUtils.toByteArray(source);
                FileInputStream input = new FileInputStream(target);
                FileOutputStream output = null;
                try {
                    byte[] targetArray = IOUtils.toByteArray(input);
                    if (!Arrays.equals(sourceArray, targetArray)) {
                        output = new FileOutputStream(target);
                        IOUtils.write(sourceArray, output);
                        return true;
                    }
                } finally {
                    IOUtils.closeQuietly(input);
                    IOUtils.closeQuietly(output);
                }
            }
        }
        return false;
    }

    public void setApplicationEventPublisher(ApplicationEventPublisher applicationEventPublisher) {
        this.applicationEventPublisher = applicationEventPublisher;
    }

    private void setDependenciesInPom(File sources, List<String> dependencies) {
        try {
            SAXReader reader = new SAXReader();
            File pom = new File(sources, "pom.xml");
            Document document = reader.read(pom);
            Element e = document.getRootElement();
            List elements = e.elements("build");
            if (elements.isEmpty()) {
                e = e.addElement("build");
            } else {
                e = (Element) elements.get(0);
            }
            elements = e.elements("plugins");
            if (elements.isEmpty()) {
                e = e.addElement("plugins");
            } else {
                e = (Element) elements.get(0);
            }
            Element pluginArtifactId = (Element) e
                    .selectSingleNode("*[name()='plugin']/*[name()='artifactId' and text()='maven-bundle-plugin']");
            if (pluginArtifactId == null) {
                pluginArtifactId = (Element) e
                        .selectSingleNode("*[name()='plugin']/*[name()='artifactId' and text()='maven-war-plugin']");
                if (pluginArtifactId == null) {
                    e = e.addElement("plugin");
                    e.addElement("groupId").setText("org.apache.felix");
                    e.addElement("artifactId").setText("maven-bundle-plugin");
                    e.addElement("extensions").setText("true");
                    e = e.addElement("configuration");
                    e = e.addElement("instructions");
                    e = e.addElement("Jahia-Depends");
                } else {
                    e = pluginArtifactId.getParent();
                    e = (Element) e.elements("configuration").get(0);
                    e = (Element) e.elements("archive").get(0);
                    e = (Element) e.elements("manifestEntries").get(0);
                    e = (Element) e.elements("depends").get(0);
                }
            } else {
                e = pluginArtifactId.getParent();
                elements = e.elements("configuration");
                if (elements.isEmpty()) {
                    e = e.addElement("configuration");
                } else {
                    e = (Element) elements.get(0);
                }
                elements = e.elements("instructions");
                if (elements.isEmpty()) {
                    e = e.addElement("instructions");
                } else {
                    e = (Element) elements.get(0);
                }
                elements = e.elements("Jahia-Depends");
                if (elements.isEmpty()) {
                    e = e.addElement("Jahia-Depends");
                } else {
                    e = (Element) elements.get(0);
                }
            }
            e.setText(StringUtils.join(dependencies, ","));
            File modifiedPom = new File(sources, "pom-modified.xml");
            XMLWriter writer = new XMLWriter(new FileWriter(modifiedPom), prettyPrint);
            try {
                writer.write(document);
            } finally {
                writer.close();
            }
            FileInputStream source = new FileInputStream(modifiedPom);
            try {
                saveFile(source, pom);
            } finally {
                IOUtils.closeQuietly(source);
                modifiedPom.delete();
            }
        } catch (Exception e) {
            logger.error(e.getMessage(), e);
        }
    }

    public void setSiteService(JahiaSitesService siteService) {
        this.siteService = siteService;
    }

    public void setSourceControlFactory(SourceControlFactory sourceControlFactory) {
        this.sourceControlFactory = sourceControlFactory;
    }

    public void setTemplatePackageRegistry(TemplatePackageRegistry registry) {
        templatePackageRegistry = registry;
    }

    public void setXmlIndentation(int i) {
        prettyPrint.setIndentSize(i);
    }

    public void synchro(JCRNodeWrapper source, JCRNodeWrapper destinationNode, JCRSessionWrapper session,
            String moduleName, Map<String, List<String>> references) throws RepositoryException {
        if (source.isNodeType("jnt:moduleVersion")) {
            session.getUuidMapping().put(source.getIdentifier(), destinationNode.getIdentifier());
            NodeIterator ni = source.getNodes();
            while (ni.hasNext()) {
                JCRNodeWrapper child = (JCRNodeWrapper) ni.next();
                if (child.isNodeType("jnt:versionInfo") || child.isNodeType("jnt:moduleVersionFolder")) {
                    continue;
                }
                JCRNodeWrapper node;
                boolean newNode = false;
                String childName = child.getName();
                if (destinationNode.hasNode(childName)) {
                    node = destinationNode.getNode(childName);
                } else {
                    session.checkout(destinationNode);
                    String primaryNodeTypeName = child.getPrimaryNodeTypeName();
                    node = destinationNode.addNode(childName, primaryNodeTypeName);
                    newNode = true;
                }

                if (!child.isNodeType("jnt:templatesFolder") && !child.isNodeType("jnt:componentFolder")) {
                    templatesSynchro(child, node, session, references, newNode, true);
                }
            }
        }
    }

    public void templatesSynchro(final JCRNodeWrapper source, final JCRNodeWrapper destinationNode,
            JCRSessionWrapper session, Map<String, List<String>> references, boolean doUpdate, boolean doChildren)
            throws RepositoryException {
        if ("j:acl".equals(destinationNode.getName())) {
            return;
        }

        if (logger.isDebugEnabled()) {
            logger.debug("Synchronizing node : " + destinationNode.getPath() + ", update=" + doUpdate + "/children="
                    + doChildren);
        }

        // Set for jnt:template nodes : declares if the template was originally created with that module, false otherwise
        // boolean isCurrentModule = (!destinationNode.hasProperty("j:moduleTemplate") && moduleName == null) ||
        // (destinationNode.hasProperty("j:moduleTemplate") &&
        // destinationNode.getProperty("j:moduleTemplate").getString().equals(moduleName));

        session.checkout(destinationNode);

        final Map<String, String> uuidMapping = session.getUuidMapping();

        ExtendedNodeType[] mixin = source.getMixinNodeTypes();
        List<ExtendedNodeType> destMixin = Arrays.asList(destinationNode.getMixinNodeTypes());
        for (ExtendedNodeType aMixin : mixin) {
            if (!destMixin.contains(aMixin)) {
                destinationNode.addMixin(aMixin.getName());
            }
        }

        uuidMapping.put(source.getIdentifier(), destinationNode.getIdentifier());

        List<String> names = new ArrayList<String>();

        if (doUpdate) {
            if (source.hasProperty(Constants.JCR_LANGUAGE)
                    && (!destinationNode.hasProperty(Constants.JCR_LANGUAGE) || (!destinationNode
                            .getProperty(Constants.JCR_LANGUAGE).getString()
                            .equals(source.getProperty(Constants.JCR_LANGUAGE).getString())))) {
                destinationNode.setProperty(Constants.JCR_LANGUAGE, source.getProperty(Constants.JCR_LANGUAGE)
                        .getString());
            }

            PropertyIterator props = source.getProperties();

            while (props.hasNext()) {
                Property property = props.nextProperty();
                names.add(property.getName());
                try {
                    if (!property.getDefinition().isProtected()
                            && !Constants.forbiddenPropertiesToCopy.contains(property.getName())) {
                        if (property.getType() == PropertyType.REFERENCE
                                || property.getType() == PropertyType.WEAKREFERENCE) {
                            if (property.getDefinition().isMultiple() && (property.isMultiple())) {
                                if (!destinationNode.hasProperty(property.getName())
                                        || !Arrays.equals(destinationNode.getProperty(property.getName()).getValues(),
                                                property.getValues())) {
                                    destinationNode.setProperty(property.getName(), new Value[0]);
                                    Value[] values = property.getValues();
                                    for (Value value : values) {
                                        keepReference(destinationNode, references, property, value.getString());
                                    }
                                }
                            } else {
                                if (!destinationNode.hasProperty(property.getName())
                                        || !destinationNode.getProperty(property.getName()).getValue()
                                                .equals(property.getValue())) {
                                    keepReference(destinationNode, references, property, property.getValue()
                                            .getString());
                                }
                            }
                        } else if (property.getDefinition().isMultiple() && (property.isMultiple())) {
                            if (!destinationNode.hasProperty(property.getName())
                                    || !Arrays.equals(destinationNode.getProperty(property.getName()).getValues(),
                                            property.getValues())) {
                                destinationNode.setProperty(property.getName(), property.getValues());
                            }
                        } else if (!destinationNode.hasProperty(property.getName())
                                || !destinationNode.getProperty(property.getName()).getValue()
                                        .equals(property.getValue())) {
                            destinationNode.setProperty(property.getName(), property.getValue());
                        }
                    }
                } catch (Exception e) {
                    logger.warn("Unable to copy property '" + property.getName() + "'. Skipping.", e);
                }
            }

            PropertyIterator pi = destinationNode.getProperties();
            while (pi.hasNext()) {
                JCRPropertyWrapper oldChild = (JCRPropertyWrapper) pi.next();
                if (!oldChild.getDefinition().isProtected()) {
                    if (!names.contains(oldChild.getName()) && !oldChild.getName().equals("j:published")
                            && !oldChild.getName().equals(Constants.JAHIA_MODULE_TEMPLATE)
                            && !oldChild.getName().equals("j:sourceTemplate")) {
                        oldChild.remove();
                    }
                }
            }

            mixin = destinationNode.getMixinNodeTypes();
            for (NodeType aMixin : mixin) {
                if (!source.isNodeType(aMixin.getName())) {
                    destinationNode.removeMixin(aMixin.getName());
                }
            }
        }

        NodeIterator ni = source.getNodes();

        names.clear();

        while (ni.hasNext()) {
            JCRNodeWrapper child = (JCRNodeWrapper) ni.next();
            boolean isPageNode = child.isNodeType("jnt:page");

            if (doChildren) {
                names.add(child.getName());

                boolean newNode = false;
                JCRNodeWrapper node = null;
                if (destinationNode.hasNode(child.getName())) {
                    node = destinationNode.getNode(child.getName());
                } else if (node == null) {
                    node = destinationNode.addNode(child.getName(), child.getPrimaryNodeTypeName());
                    newNode = true;
                }
                templatesSynchro(child, node, session, references, newNode, doChildren && (!isPageNode || newNode));
            }
        }
        if (doUpdate) {
            List<String> destNames = new ArrayList<String>();
            ni = destinationNode.getNodes();
            while (ni.hasNext()) {
                JCRNodeWrapper oldChild = (JCRNodeWrapper) ni.next();
                destNames.add(oldChild.getName());
            }
            if (destinationNode.getPrimaryNodeType().hasOrderableChildNodes() && !names.equals(destNames)) {
                Collections.reverse(names);
                String previous = null;
                for (String name : names) {
                    destinationNode.orderBefore(name, previous);
                    previous = name;
                }
            }
        }
    }

    public void uninstallModule(final JahiaTemplatesPackage module, final String sitePath,
            final JCRSessionWrapper session) throws RepositoryException {
        uninstallModules(Arrays.asList(module), sitePath, session);
    }

    private boolean uninstallModule(String sitePath, JCRSessionWrapper session, JCRSiteNode siteNode,
            JahiaTemplatesPackage module) throws RepositoryException {
        logger.info("Uninstalling " + module.getName() + " on " + sitePath);
        JCRNodeWrapper moduleNode = null;
        try {
            moduleNode = session.getNode("/modules/" + module.getRootFolder());

            String moduleName = moduleNode.getName();

            if (moduleNode.isNodeType("jnt:module")) {
                moduleNode = moduleNode.getNode(module.getVersion().toString());
            }
            /*
             * synchro(moduleNode, siteNode, session, moduleName, references);
             * 
             * ReferencesHelper.resolveCrossReferences(session, references);
             */

            JCRPropertyWrapper installedModules = siteNode.getProperty("j:installedModules");
            Value toBeRemoved = null;
            Value[] values = installedModules.getValues();
            for (Value value : values) {
                if (value.getString().equals(moduleName)) {
                    toBeRemoved = value;
                    break;
                }
            }
            installedModules.removeValue(toBeRemoved);
            logger.info("Done uninstalling " + module.getName() + " on " + sitePath);
        } catch (PathNotFoundException e) {
            logger.warn("Cannot find module for path {}. Skipping deployment to site {}.", module, sitePath);
            return true;
        }
        return false;
    }

    public void uninstallModule(final String module, final String sitePath, String username,
            final boolean purgeAllContent) throws RepositoryException {
        JCRTemplate.getInstance().doExecuteWithSystemSession(username, new JCRCallback<Object>() {
            public Object doInJCR(JCRSessionWrapper session) throws RepositoryException {
                uninstallModules(Arrays.asList(templatePackageRegistry.lookupByFileName(module)), sitePath, session);
                if (purgeAllContent) {
                    purgeModuleContent(Arrays.asList(templatePackageRegistry.lookupByFileName(module)), sitePath,
                            session);
                }
                session.save();
                return null;
            }
        });
        if (purgeAllContent) {
            JCRTemplate.getInstance().doExecuteWithSystemSession(username, "live", new JCRCallback<Object>() {
                public Object doInJCR(JCRSessionWrapper session) throws RepositoryException {
                    purgeModuleContent(Arrays.asList(templatePackageRegistry.lookupByFileName(module)), sitePath,
                            session);
                    session.save();
                    return null;
                }
            });
        }
    }

    public void uninstallModules(final List<JahiaTemplatesPackage> modules, final String sitePath,
            final JCRSessionWrapper session) throws RepositoryException {
        if (!sitePath.startsWith("/sites/")) {
            return;
        }
        final JCRSiteNode siteNode = (JCRSiteNode) session.getNode(sitePath);

        for (JahiaTemplatesPackage module : modules) {
            if (uninstallModule(sitePath, session, siteNode, module)) {
                return;
            }
        }

        applicationEventPublisher.publishEvent(new JahiaTemplateManagerService.ModuleDeployedOnSiteEvent(sitePath,
                ModuleInstallationHelper.class.getName()));
    }

    public void uninstallModulesFromAllSites(final JahiaTemplatesPackage module, final JCRSessionWrapper session)
            throws RepositoryException {
        uninstallModulesFromAllSites(Arrays.asList(module), session);
    }

    public void uninstallModulesFromAllSites(final List<JahiaTemplatesPackage> modules, final JCRSessionWrapper session)
            throws RepositoryException {
        List<JCRSiteNode> sitesList = siteService.getSitesNodeList(session);
        for (JCRSiteNode jahiaSite : sitesList) {
            for (JahiaTemplatesPackage module : modules) {
                if (uninstallModule(jahiaSite.getName(), session, jahiaSite, module)) {
                    return;
                }
                applicationEventPublisher.publishEvent(new JahiaTemplateManagerService.ModuleDeployedOnSiteEvent(
                        jahiaSite.getName(), ModuleInstallationHelper.class.getName()));
            }
        }
    }

    public void uninstallModulesFromAllSites(final String module, final String username, final boolean purgeAllContent)
            throws RepositoryException {
        JCRTemplate.getInstance().doExecuteWithSystemSession(username, new JCRCallback<Object>() {
            public Object doInJCR(JCRSessionWrapper session) throws RepositoryException {
                uninstallModulesFromAllSites(templatePackageRegistry.lookupByFileName(module), session);
                if (purgeAllContent) {
                    purgeModuleContent(Arrays.asList(templatePackageRegistry.lookupByFileName(module)), "/", session);
                }
                session.save();
                return null;
            }
        });
        if (purgeAllContent) {
            JCRTemplate.getInstance().doExecuteWithSystemSession(username, "live", new JCRCallback<Object>() {
                public Object doInJCR(JCRSessionWrapper session) throws RepositoryException {
                    purgeModuleContent(Arrays.asList(templatePackageRegistry.lookupByFileName(module)), "/", session);
                    session.save();
                    return null;
                }
            });
        }
    }

    public void updateDependencies(JahiaTemplatesPackage pack, List<String> depends) {
        pack.getDepends().clear();
        pack.getDepends().addAll(depends);
        templatePackageRegistry.computeDependencies(pack);

        if (pack.getSourcesFolder() != null) {
            setDependenciesInPom(pack.getSourcesFolder(), depends);
        }

        applicationEventPublisher.publishEvent(new JahiaTemplateManagerService.ModuleDependenciesEvent(pack
                .getRootFolder(), this));
    }

}