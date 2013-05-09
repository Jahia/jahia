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

package org.jahia.bundles.url.jahiawar.internal;

import org.jahia.utils.osgi.parsers.Parsers;
import org.jahia.utils.osgi.parsers.ParsingContext;
import org.jdom2.Document;
import org.jdom2.JDOMException;
import org.jdom2.Namespace;
import org.jdom2.input.SAXBuilder;
import org.jdom2.output.Format;
import org.jdom2.output.XMLOutputter;
import org.ops4j.io.StreamUtils;
import org.ops4j.lang.NullArgumentException;
import org.ops4j.net.URLUtils;
import org.ops4j.pax.swissbox.bnd.BndUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.*;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLConnection;
import java.util.*;
import java.util.jar.Attributes;
import java.util.jar.JarEntry;
import java.util.jar.JarInputStream;
import java.util.jar.JarOutputStream;
import java.util.jar.Manifest;

/**
 * Url connection for jahiawar protocol handler.
 */
public class Connection extends URLConnection {

    private static Logger logger = LoggerFactory.getLogger(Connection.class);

    private Parser parser;
    private final Configuration configuration;
    private List<String> classPathEntries = new ArrayList<String>();
    private Set<String> exportPackageExcludes = new TreeSet<String>(new Comparator<String>() {
        @Override
        public int compare(String s, String s1) {
            if (s.length() > s1.length()) {
                return -1;
            } else if (s.length() < s1.length()) {
                return 1;
            } else {
                return s.compareTo(s1);
            }
        }
    });

    public Set<String> extensionsToExport = new HashSet<String>();

    public Set<String> importPackages = new TreeSet<String>();
    public Set<String> excludedImportPackages = new TreeSet<String>();

    public Connection(final URL url, final Configuration configuration)
            throws MalformedURLException {
        super(url);

        NullArgumentException.validateNotNull(url, "URL cannot be null");
        NullArgumentException.validateNotNull(configuration, "Service configuration");

        this.configuration = configuration;
        importPackages.addAll(configuration.getImportedPackages());
        excludedImportPackages.addAll(configuration.getExcludedImportPackages());
        extensionsToExport.add(".class");
        extensionsToExport.add(".tld");
        parser = new Parser(url.getPath());
    }

    public InputStream getInputStream()
            throws IOException {
        connect();

        final JarInputStream jarInputStream = new JarInputStream(URLUtils.prepareInputStream(
                parser.getWrappedJarURL(),
                !configuration.getCertificateCheck()
        ));
        final Manifest inputManifest = new Manifest(jarInputStream.getManifest());
        String depends = inputManifest.getMainAttributes().getValue("depends");

        final Set<String> exportPackageIncludes = new HashSet<String>();
        final Set<String> allNonEmptyDirectories = new HashSet<String>();
        final Set<String> directoryEntries = new HashSet<String>();

        ParsingContext parsingContext = new ParsingContext();

        ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
        JarOutputStream jos = null;
        try {
            jos = new JarOutputStream(byteArrayOutputStream, inputManifest);
            JarEntry jarEntry = null;
            Set<String> entryNames = new HashSet<String>();
            long mostRecentTime = Long.MIN_VALUE;
            while ((jarEntry = jarInputStream.getNextJarEntry()) != null) {

                InputStream entryInputStream = jarInputStream;
                if (jarEntry.isDirectory()) {
                    directoryEntries.add(jarEntry.getName());
                }

                String newName = jarEntry.getName();
                if (newName.equals("WEB-INF/web.xml") ||
                        newName.equals("WEB-INF/") ||
                        newName.equals("WEB-INF/classes/") ||
                        newName.equals("WEB-INF/lib/")) {
                    continue;
                }
                if (newName.startsWith("WEB-INF/classes/")) {
                    newName = jarEntry.getName().substring("WEB-INF/classes/".length());
                    if (!jarEntry.isDirectory()) {
                        String directoryName = "";
                        if (newName.lastIndexOf('/') > -1) {
                            directoryName = newName.substring(0, newName.lastIndexOf('/'));
                        }
                        String packageName = directoryName.replaceAll("\\/", "\\.");
                        if (!directoryName.startsWith("META-INF") &&
                            !directoryName.startsWith("OSGI-INF") &&
                            !directoryName.startsWith("WEB-INF") &&
                            !parsingContext.getProjectPackages().contains(packageName)) {
                            parsingContext.getProjectPackages().add(packageName);
                        }
                    }
                } else if (newName.startsWith("WEB-INF/lib/")) {
                    newName = jarEntry.getName().substring("WEB-INF/lib/".length());
                    classPathEntries.add(newName);
                    ByteArrayOutputStream entryOutputStream = new ByteArrayOutputStream();
                    StreamUtils.copyStream(entryInputStream, entryOutputStream, false);
                    ByteArrayInputStream tempEntryInputStream = new ByteArrayInputStream(entryOutputStream.toByteArray());
                    JarInputStream embeddedJarInputStream = new JarInputStream(tempEntryInputStream);
                    Manifest mf = embeddedJarInputStream.getManifest();
                    ByteArrayOutputStream embeddedJarByteOutputStream = new ByteArrayOutputStream();
                    JarOutputStream embeddedJarOutputStream = mf != null ? new JarOutputStream(embeddedJarByteOutputStream, new Manifest(mf)) : new JarOutputStream(embeddedJarByteOutputStream);
                    Set<String> processed = new HashSet<String>();
                    JarEntry embeddedJarEntry = null;
                    while ((embeddedJarEntry = embeddedJarInputStream.getNextJarEntry()) != null) {
                        updateExportTracking(embeddedJarEntry.getName(), exportPackageIncludes, allNonEmptyDirectories);
                        String embeddedJarNewName = embeddedJarEntry.getName();
                        if (processed.contains(embeddedJarNewName)) {
                            continue;
                        }
                        processed.add(embeddedJarNewName);
                        String directoryName = "";
                        if (embeddedJarNewName.lastIndexOf('/') > -1) {
                            directoryName = embeddedJarNewName.substring(0, embeddedJarNewName.lastIndexOf('/'));
                        }
                        if (directoryName.equals("org/jahia/services/workflow")) {
                            String remainingPath = embeddedJarNewName.substring("org/jahia/services/workflow/".length());
                            embeddedJarNewName = "org/jahia/modules/custom/workflow/" + remainingPath;
                        }
                        if (!embeddedJarEntry.isDirectory()) {
                            String packageName = directoryName.replaceAll("\\/", "\\.");
                            if (!directoryName.startsWith("META-INF") &&
                                !directoryName.startsWith("OSGI-INF") &&
                                !directoryName.startsWith("WEB-INF") &&
                                !parsingContext.getProjectPackages().contains(packageName)) {
                                parsingContext.getProjectPackages().add(packageName);
                            }
                        }

                        InputStream embeddedInputStream = parseFile(embeddedJarInputStream, embeddedJarNewName, parsingContext);
                        // create the new entry
                        JarEntry newEmbeddedJarEntry = new JarEntry(embeddedJarNewName);
                        if (embeddedJarEntry.getTime() > mostRecentTime) {
                            mostRecentTime = embeddedJarEntry.getTime();
                        }
                        newEmbeddedJarEntry.setTime(embeddedJarEntry.getTime());
                        embeddedJarOutputStream.putNextEntry(newEmbeddedJarEntry);
                        StreamUtils.copyStream(embeddedInputStream, embeddedJarOutputStream, false);
                        embeddedJarOutputStream.closeEntry();
                    }
                    embeddedJarInputStream.close();
                    tempEntryInputStream.close();
                    tempEntryInputStream = null;
                    embeddedJarOutputStream.close();
                    entryInputStream = new ByteArrayInputStream(embeddedJarByteOutputStream.toByteArray());
                } else if (newName.startsWith("WEB-INF/")) {
                    newName = jarEntry.getName().substring("WEB-INF/".length());
                }
                if (newName.endsWith(".xml")) {
                    ByteArrayOutputStream entryOutputStream = new ByteArrayOutputStream();
                    StreamUtils.copyStream(entryInputStream, entryOutputStream, false);
                    ByteArrayInputStream tempEntryInputStream = new ByteArrayInputStream(entryOutputStream.toByteArray());
                    // let's load the XML and start transforming it.
                    SAXBuilder saxBuilder = new SAXBuilder();
                    try {
                        Document jdomDocument = saxBuilder.build(tempEntryInputStream);
                        List<Namespace> rootElementNamespaces = jdomDocument.getRootElement().getNamespacesInScope();
                        if (hasNamespaceURI(rootElementNamespaces, "http://www.springframework.org/schema/beans")) {
                            Document transformedDocument = SpringFileTransformer.transform(jdomDocument);
                            if (transformedDocument != jdomDocument) {
                                XMLOutputter xmlOutputter = new XMLOutputter(Format.getPrettyFormat());
                                entryOutputStream = new ByteArrayOutputStream();
                                xmlOutputter.output(transformedDocument, entryOutputStream);
                            }
                        } else if (hasNamespaceURI(rootElementNamespaces, "http://jbpm.org/4.3/jpdl")) {
                            // jBPM workflow definition file detected.
                        }
                    } catch (JDOMException e) {
                        e.printStackTrace();
                    }

                    tempEntryInputStream.close();
                    tempEntryInputStream = null;
                    entryInputStream = new ByteArrayInputStream(entryOutputStream.toByteArray());
                }

                entryInputStream = parseFile(entryInputStream, newName, parsingContext);

                JarEntry newJarEntry = new JarEntry(newName);
                if (jarEntry.getTime() > mostRecentTime) {
                    mostRecentTime = jarEntry.getTime();
                }
                newJarEntry.setTime(jarEntry.getTime());
                jos.putNextEntry(newJarEntry);
                StreamUtils.copyStream(entryInputStream, jos, false);
                if (entryInputStream != jarInputStream) {
                    entryInputStream.close();
                    entryInputStream = null;
                }
                jos.closeEntry();
                entryNames.add(newName);
                updateExportTracking(newName, exportPackageIncludes, allNonEmptyDirectories);

            }
            jos.finish();
        } catch (IOException e) {
            throw new RuntimeException("Could not process resources", e);
        } finally {
            try {
                if (jos != null) {
                    jos.close();
                }
            } catch (Exception ignore) {
                //  ignore
            }
        }

        parsingContext.postProcess();
        if (parsingContext.getUnresolvedTaglibUris().size() > 0 ) {
            for (Map.Entry<String,Set<String>> unresolvedUrisForJsp : parsingContext.getUnresolvedTaglibUris().entrySet()) {
                for (String unresolvedUriForJsp : unresolvedUrisForJsp.getValue()) {
                    logger.warn("JSP " + unresolvedUrisForJsp.getKey() + " has a reference to taglib " + unresolvedUriForJsp + " that is not in the project's dependencies !");
                }
            }
        }

        Properties bndProperties = new Properties();
        if (classPathEntries.size() > 0) {
            StringBuffer bundleClassPath = new StringBuffer(".");
            for (String classPathEntry : classPathEntries) {
                bundleClassPath.append(",");
                bundleClassPath.append(classPathEntry);
            }
            bndProperties.put("Bundle-ClassPath", bundleClassPath.toString());
        }
        String versionStr = inputManifest.getMainAttributes().getValue("Implementation-Version");
        if (versionStr != null) {
            int dashPos = versionStr.indexOf("-");
            if (dashPos > -1) {
                versionStr = versionStr.substring(0, dashPos);
            }
            int underScorePos = versionStr.indexOf("_");
            if (underScorePos > -1) {
                versionStr = versionStr.substring(0, underScorePos);
            }
            bndProperties.put("Bundle-Version", versionStr);
        }

        // calculate export package exclusions that are all non-empty directories minus the ones that contain
        // resources to export.
        exportPackageExcludes.addAll(allNonEmptyDirectories);
        exportPackageExcludes.removeAll(exportPackageIncludes);

        String rootFolder = inputManifest.getMainAttributes().getValue("root-folder");
        if (rootFolder != null) {
            String packagePrefix = rootFolder.replaceAll("[ -]", "");

            bndProperties.put("Bundle-SymbolicName", rootFolder);
            StringBuilder exportPackage = new StringBuilder(128);
            if (exportPackageIncludes.size() > 0) {
                for (String exportPackageInclude : exportPackageIncludes) {
                    exportPackage.append(exportPackageInclude);
                    exportPackage.append(",");
                }
            }
            /*
            if (exportPackageExcludes.size() > 0) {
                for (String exportPackageExclude : exportPackageExcludes) {
                    exportPackage.append("!");
                    exportPackage.append(exportPackageExclude);
                    exportPackage.append(".*,");
                }
                //exportPackage.append("*,");
            } else {
                //exportPackage.append("*,");
            }
            */
            String titleAttribute = inputManifest.getMainAttributes().getValue("package-name");
            if (titleAttribute == null) {
                titleAttribute = inputManifest.getMainAttributes().getValue("Implementation-Title");
            }
            bndProperties.put("Bundle-Name", titleAttribute);
            exportPackage.append(titleAttribute.replaceAll("[ -]", ""));
            exportPackage.append(",");
            exportPackage.append(packagePrefix);
            bndProperties.put("Export-Package", exportPackage.toString());

            if (depends == null) {
                depends = "";
            }
            if (!depends.contains("default") && !depends.contains("Default Jahia Templates") && !rootFolder.equals("assets") && !rootFolder.equals("default")) {
                if (!depends.equals("")) {
                    depends += ",";
                }
                depends += "default";
            }


//            String[] dependsArray = depends.split(",");
            StringBuilder importPackage = new StringBuilder("*;resolution:=optional");

            /*
            for (String dep : dependsArray) {
                if (!"".equals(dep)) {
                    importPackage.append(",");
                    dep = dep.replaceAll("[ -]", "");
                    importPackage.append(dep);
                }
            }
            */

            List<String> alreadyImportedPackages = new ArrayList<String>(Arrays.asList(importPackage.toString().split(",")));
            for (String curImportPackage : importPackages) {
                if (!alreadyImportedPackages.contains(curImportPackage) &&
                    !excludedImportPackages.contains(curImportPackage)) {
                    importPackage.append(",");
                    importPackage.append(curImportPackage);
                    alreadyImportedPackages.add(curImportPackage);
                }
            }
            for (String importPackageFromParsing : parsingContext.getPackageImports()) {
                if (!alreadyImportedPackages.contains(importPackageFromParsing) &&
                    !excludedImportPackages.contains(importPackageFromParsing)) {
                    importPackage.append(",");
                    importPackage.append(importPackageFromParsing);
                    alreadyImportedPackages.add(importPackageFromParsing);
                }
            }

            bndProperties.put("Import-Package", importPackage.toString());

            Set<String> rootFolders = new HashSet<String>();
            for (String entry : directoryEntries) {
                rootFolders.add(entry.substring(0,entry.indexOf("/")));
            }
            StringBuilder staticResources = new StringBuilder("");
            for (String folder : rootFolders) {
                if (!folder.equals("META-INF") && !folder.equals("WEB-INF")) {
                    staticResources.append(",/");
                    staticResources.append(folder);
                }
            }
            if (staticResources.length() > 0) {
                bndProperties.put("Jahia-Static-Resources", staticResources.substring(1));
            }
        }

        bndProperties.put("Bundle-Category", "jahia-module");

        convertJahiaManifestAttributes(inputManifest.getMainAttributes(), bndProperties, depends);

        ByteArrayInputStream byteArrayInputStream = new ByteArrayInputStream(byteArrayOutputStream.toByteArray());

        InputStream bndResultInputStream = BndUtils.createBundle(byteArrayInputStream,
                bndProperties,
                url.toExternalForm(),
                parser.getOverwriteMode()
        );

        // we copy the stream to memory once again to avoid pipe issues.

        ByteArrayOutputStream bndByteArrayOutputStream = new ByteArrayOutputStream();
        StreamUtils.copyStream(bndResultInputStream, bndByteArrayOutputStream, false);

        bndResultInputStream.close();
        byteArrayInputStream.close();
        byteArrayInputStream = null;

        ByteArrayInputStream resultInputStream = new ByteArrayInputStream(bndByteArrayOutputStream.toByteArray());
        return resultInputStream;
    }

    private InputStream parseFile(InputStream fileInputStream, String fileName, ParsingContext parsingContext) throws IOException {
        // let's run all the parsers now.
        ByteArrayOutputStream entryOutputStream = new ByteArrayOutputStream();
        StreamUtils.copyStream(fileInputStream, entryOutputStream, false);
        ByteArrayInputStream tempEntryInputStream = new ByteArrayInputStream(entryOutputStream.toByteArray());
        Parsers.getInstance().parse(0, fileName, tempEntryInputStream, parsingContext, false, logger);
        tempEntryInputStream = new ByteArrayInputStream(entryOutputStream.toByteArray());
        Parsers.getInstance().parse(1, fileName, tempEntryInputStream, parsingContext, false, logger);
        tempEntryInputStream.close();
        tempEntryInputStream = null;
        fileInputStream = new ByteArrayInputStream(entryOutputStream.toByteArray());
        return fileInputStream;
    }

    private void convertJahiaManifestAttributes(Attributes attrs, Properties bndProperties, String depends) {
        // prefix non-prefixed Jahia headers with "Jahia-"
        if (depends.length() > 0) {
            bndProperties.put("Jahia-Depends", depends);
        }
        String value = attrs.getValue("module-type");
        if (value != null) {
            bndProperties.put("Jahia-Module-Type", value);
        }
        value = attrs.getValue("root-folder");
        if (value != null) {
            bndProperties.put("Jahia-Root-Folder", value);
        }
        value = attrs.getValue("definitions");
        if (value != null) {
            bndProperties.put("Jahia-Definitions", value);
        }
        value = attrs.getValue("initial-imports");
        if (value != null) {
            bndProperties.put("Jahia-Initial-Imports", value);
        }
        value = attrs.getValue("resource-bundle");
        if (value != null) {
            bndProperties.put("Jahia-Resource-Bundle", value);
        }
        value = attrs.getValue("deploy-on-site");
        if (value != null) {
            bndProperties.put("Jahia-Deploy-On-Site", value);
        }

        // remove legacy headers
        bndProperties.put("-removeheaders", "definitions,depends,deploy-on-site,initial-imports,module-type"
                + ",package-name,resource-bundle,root-folder");
    }

    private boolean hasNamespaceURI(List<Namespace> rootElementNamespaces, String springNSURI) {
        for (Namespace namespace : rootElementNamespaces) {
            if (namespace.getURI().contains(springNSURI)) {
                return true;
            }
        }
        return false;
    }

    private void updateExportTracking(String newName, Set<String> exportPackageIncludes, Set<String> allNonEmptyDirectories) {
        // now let's see if the new entry starts with an existing package import, in which case
        // we will not export it.
        for (String extensionToExport : extensionsToExport) {
            if (newName.endsWith(extensionToExport)) {
                int lastSlashPos = newName.lastIndexOf("/");
                if (lastSlashPos > -1) {
                    String directoryName = newName.substring(0, lastSlashPos).replaceAll("/", ".");
                    if (!directoryName.startsWith("META-INF")) {
                        if (!exportPackageIncludes.contains(directoryName)) {
                            exportPackageIncludes.add(directoryName);
                        }
                        if (!allNonEmptyDirectories.contains(directoryName)) {
                            allNonEmptyDirectories.add(directoryName);
                        }
                    }
                } else {
                    // do nothing this is a top level file.
                }
            } else {
                if (newName.endsWith("/")) {
                    // for a directory we do nothing, we only treat directories that have content.
                } else {
                    int lastSlashPos = newName.lastIndexOf("/");
                    if (lastSlashPos > -1) {
                        String directoryName = newName.substring(0, lastSlashPos).replaceAll("/", ".");
                        if (!directoryName.startsWith("META-INF")) {
                            if (!allNonEmptyDirectories.contains(directoryName)) {
                                allNonEmptyDirectories.add(directoryName);
                            }
                        }
                    } else {
                        // do nothing this is a top level file.
                    }
                }
            }
        }
    }

    @Override
    public void connect() throws IOException {
        // do nothing
    }
}
