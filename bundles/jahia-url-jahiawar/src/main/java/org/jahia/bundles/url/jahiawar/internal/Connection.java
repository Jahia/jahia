/**
 * ==========================================================================================
 * =                   JAHIA'S DUAL LICENSING - IMPORTANT INFORMATION                       =
 * ==========================================================================================
 *
 *                                 http://www.jahia.com
 *
 *     Copyright (C) 2002-2018 Jahia Solutions Group SA. All rights reserved.
 *
 *     THIS FILE IS AVAILABLE UNDER TWO DIFFERENT LICENSES:
 *     1/GPL OR 2/JSEL
 *
 *     1/ GPL
 *     ==================================================================================
 *
 *     IF YOU DECIDE TO CHOOSE THE GPL LICENSE, YOU MUST COMPLY WITH THE FOLLOWING TERMS:
 *
 *     This program is free software: you can redistribute it and/or modify
 *     it under the terms of the GNU General Public License as published by
 *     the Free Software Foundation, either version 3 of the License, or
 *     (at your option) any later version.
 *
 *     This program is distributed in the hope that it will be useful,
 *     but WITHOUT ANY WARRANTY; without even the implied warranty of
 *     MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 *     GNU General Public License for more details.
 *
 *     You should have received a copy of the GNU General Public License
 *     along with this program. If not, see <http://www.gnu.org/licenses/>.
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
package org.jahia.bundles.url.jahiawar.internal;

import org.apache.commons.io.IOUtils;
import org.apache.commons.lang.StringUtils;
import org.jahia.commons.Version;
import org.jahia.data.templates.JahiaTemplatesPackage;
import org.jahia.registries.ServicesRegistry;
import org.jahia.services.SpringContextSingleton;
import org.jahia.services.templates.JahiaTemplateManagerService;
import org.jahia.utils.migration.Migrators;
import org.jahia.utils.osgi.parsers.PackageInfo;
import org.jahia.utils.osgi.parsers.Parsers;
import org.jahia.utils.osgi.parsers.ParsingContext;
import org.jahia.utils.osgi.parsers.TldXmlFileParser;
import org.jdom2.Document;
import org.jdom2.Element;
import org.jdom2.JDOMException;
import org.jdom2.Namespace;
import org.jdom2.input.SAXBuilder;
import org.jdom2.input.sax.XMLReaders;
import org.jdom2.output.Format;
import org.jdom2.output.XMLOutputter;
import org.ops4j.io.StreamUtils;
import org.ops4j.lang.NullArgumentException;
import org.ops4j.net.URLUtils;
import org.ops4j.pax.swissbox.bnd.BndUtils;
import org.osgi.framework.Bundle;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.io.Resource;
import org.springframework.core.io.UrlResource;

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
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Url connection for jahiawar protocol handler.
 */
public class Connection extends URLConnection {

    private static Logger logger = LoggerFactory.getLogger(Connection.class);

    private Parser parser;
    private final Configuration configuration;
    private List<String> classPathEntries = new ArrayList<String>();

    private Set<String> extensionsToExport = new HashSet<String>();

    private Map<String,Set<String>> importPackages = null;
    private Map<String,Set<String>> excludedImportPackages = null;
    private Set<String> forbiddenJars = new LinkedHashSet<String>();
    private List<Pattern> forbiddenJarPatterns = new ArrayList<Pattern>();
    private Map<String,byte[]> addedFiles = new LinkedHashMap<String,byte[]>();

    public Connection(final URL url, final Configuration configuration)
            throws MalformedURLException {
        super(url);

        NullArgumentException.validateNotNull(url, "URL cannot be null");
        NullArgumentException.validateNotNull(configuration, "Service configuration");

        this.configuration = configuration;
        importPackages = configuration.getImportedPackages();
        excludedImportPackages = configuration.getExcludedImportPackages();
        forbiddenJars = configuration.getForbiddenJars();
        for (String forbiddenJar : forbiddenJars) {
            Pattern forbiddenJarPattern = Pattern.compile(forbiddenJar.replaceAll("\\*", "\\.*"));
            forbiddenJarPatterns.add(forbiddenJarPattern);
        }
        parser = new Parser(url.getPath());
    }

    public InputStream getInputStream()
            throws IOException {
        logger.info("Started tranforming WAR module {}", parser.getWrappedJarURL());
        long timer = System.currentTimeMillis();
        
        connect();

        final JarInputStream jarInputStream = new JarInputStream(URLUtils.prepareInputStream(
                parser.getWrappedJarURL(),
                !configuration.getCertificateCheck()
        ));
        final Manifest inputManifest = new Manifest(jarInputStream.getManifest());
        String depends = inputManifest.getMainAttributes().getValue("depends");

        final Set<String> directoryEntries = new HashSet<String>();

        ParsingContext parsingContext = new ParsingContext();
        String resolvedGroupId = null;

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
                            !parsingContext.getLocalPackages().contains(new PackageInfo(packageName, parser.getWrappedJarURL().toString(), parsingContext))) {
                            parsingContext.getLocalPackages().add(new PackageInfo(packageName, parser.getWrappedJarURL().toString(), parsingContext));
                        }
                    }
                } else if (newName.startsWith("WEB-INF/lib/")) {
                    // newName = jarEntry.getName().substring("WEB-INF/lib/".length());
                    boolean forbiddenJar = false;
                    for (Pattern forbiddenJarPattern : forbiddenJarPatterns) {
                        Matcher forbiddenJarMatcher = forbiddenJarPattern.matcher(newName);
                        if (forbiddenJarMatcher.matches()) {
                            forbiddenJar = true;
                            break;
                        }
                    }
                    if (forbiddenJar) {
                        continue;
                    }
                    classPathEntries.add(newName);
                    // first we copy the JAR input stream into a byte array
                    ByteArrayOutputStream entryOutputStream = new ByteArrayOutputStream();
                    StreamUtils.copyStream(entryInputStream, entryOutputStream, false);
                    // then we read the byte array as a JAR again to process it
                    ByteArrayInputStream tempEntryInputStream = new ByteArrayInputStream(entryOutputStream.toByteArray());
                    JarInputStream embeddedJarInputStream = new JarInputStream(tempEntryInputStream);
                    Manifest mf = embeddedJarInputStream.getManifest();
                    ByteArrayOutputStream embeddedJarByteOutputStream = new ByteArrayOutputStream();
                    JarOutputStream embeddedJarOutputStream = mf != null ? new JarOutputStream(embeddedJarByteOutputStream, new Manifest(mf)) : new JarOutputStream(embeddedJarByteOutputStream);
                    Set<String> processed = new HashSet<String>();
                    JarEntry embeddedJarEntry = null;
                    while ((embeddedJarEntry = embeddedJarInputStream.getNextJarEntry()) != null) {
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
                                !parsingContext.getLocalPackages().contains(new PackageInfo(packageName, parser.getWrappedJarURL() + "/" + newName, parsingContext))) {
                                parsingContext.getLocalPackages().add(new PackageInfo(packageName, parser.getWrappedJarURL() + "/" + newName, parsingContext));
                            }
                        }

                        InputStream embeddedInputStream = parseFile(embeddedJarInputStream, embeddedJarNewName, parser.getWrappedJarURL() + "/" + newName, false, parsingContext);
                        if (!embeddedJarEntry.isDirectory() && embeddedJarNewName.endsWith(".xml") &&
                                (embeddedJarNewName.startsWith("org/jahia/config/") ||
                                        embeddedJarNewName.startsWith("org/jahia/defaults/config/"))) {
                            // we have detected old Spring configuration files for the top-level application context,
                            // we will now move them into the bundle's META-INF/spring directory.
                            ByteArrayOutputStream destinationByteArrayStream = new ByteArrayOutputStream();
                            StreamUtils.copyStream(embeddedInputStream, destinationByteArrayStream, false);
                            String destinationName = null;
                            if (embeddedJarNewName.startsWith("org/jahia/config")) {
                                destinationName = "META-INF/spring/" + embeddedJarNewName.substring("org/jahia/config/".length());
                            } else if (embeddedJarNewName.startsWith("org/jahia/defaults/config/")) {
                                destinationName = "META-INF/spring/" + embeddedJarNewName.substring("org/jahia/defaults/config/".length());
                            }
                            addedFiles.put(destinationName, destinationByteArrayStream.toByteArray());
                            destinationByteArrayStream.close();
                            // set to null to deallocate as quickly as possible.
                            destinationByteArrayStream = null;
                        } else {
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
                    }
                    embeddedJarInputStream.close();
                    tempEntryInputStream.close();
                    tempEntryInputStream = null;
                    embeddedJarOutputStream.close();
                    entryInputStream = new ByteArrayInputStream(embeddedJarByteOutputStream.toByteArray());
                } else if (newName.startsWith("WEB-INF/")) {
                    // newName = jarEntry.getName().substring("WEB-INF/".length());
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
                        } else if (hasNamespaceURI(rootElementNamespaces, "http://maven.apache.org/POM/4.0.0")) {
                            Namespace mavenNamespace = getNamespaceByURI(rootElementNamespaces, "http://maven.apache.org/POM/4.0.0");
                            // Maven POM.xml file detected
                            if (newName.startsWith("META-INF/maven/")) {
                                // we must check if it matches with the module's name
                                Element projectElement = jdomDocument.getRootElement();
                                Element artifactIdElement = projectElement.getChild("artifactId", mavenNamespace);
                                String rootFolder = inputManifest.getMainAttributes().getValue("root-folder");
                                if (rootFolder != null) {
                                    if (artifactIdElement != null && artifactIdElement.getTextTrim().equals(rootFolder)) {
                                        Element groupIdElement = projectElement.getChild("groupId", mavenNamespace);
                                        if (groupIdElement != null && groupIdElement.getTextTrim() != null && groupIdElement.getTextTrim().length() > 0) {
                                            resolvedGroupId = groupIdElement.getTextTrim();
                                        }
                                    }
                                }
                            }
                        }
                    } catch (JDOMException e) {
                        logger.error(e.getMessage(),e);
                    }

                    tempEntryInputStream.close();
                    tempEntryInputStream = null;
                    entryInputStream = new ByteArrayInputStream(entryOutputStream.toByteArray());
                }

                entryInputStream = parseFile(entryInputStream, newName, parser.getWrappedJarURL().toExternalForm(), false, parsingContext);

                ByteArrayOutputStream tempByteArrayOutputStream = new ByteArrayOutputStream();
                IOUtils.copyLarge(entryInputStream, tempByteArrayOutputStream);
                byte[] tempByteArray = tempByteArrayOutputStream.toByteArray();
                ByteArrayInputStream tempByteArrayInputStream = new ByteArrayInputStream(tempByteArray);
                if (Migrators.getInstance().willMigrate(tempByteArrayInputStream, newName, new Version("6.6"), new Version("7.0"))) {
                    entryInputStream = new ByteArrayInputStream(tempByteArray);
                    ByteArrayOutputStream entryOutputStream = new ByteArrayOutputStream();
                    List<String> messages = Migrators.getInstance().migrate(entryInputStream, entryOutputStream, newName, new Version("6.6"), new Version("7.0"), true);
                    if (messages.size() > 0) {
                        for (String message : messages) {
                            logger.warn(message);
                        }
                    }
                    entryInputStream = new ByteArrayInputStream(entryOutputStream.toByteArray());
                    entryOutputStream.close();
                    entryOutputStream = null;
                } else {
                    entryInputStream = new ByteArrayInputStream(tempByteArray);
                }

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
            }

            for (Map.Entry<String,byte[]> addedFileEntry : addedFiles.entrySet()) {
                JarEntry newJarEntry = new JarEntry(addedFileEntry.getKey());
                if (mostRecentTime > 0) {
                    newJarEntry.setTime(mostRecentTime);
                }
                jos.putNextEntry(newJarEntry);
                ByteArrayInputStream entryInputStream = new ByteArrayInputStream(addedFileEntry.getValue());
                StreamUtils.copyStream(entryInputStream, jos, false);
                entryInputStream.close();
                entryInputStream = null;
                jos.closeEntry();
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

        if (parsingContext.getUnresolvedTaglibUris().size() > 0) {
            logger.info("Started resolving tag libraries for {}: {}", parser.getWrappedJarURL(), parsingContext.getUnresolvedTaglibUris());
            long timer2 = System.currentTimeMillis();
            
            Set<String> uris = new HashSet<String>();
            for (Set<String> u : parsingContext.getUnresolvedTaglibUris().values()) {
                uris.addAll(u);
            }
            resolveTaglibs(depends, parsingContext, uris);
            
            logger.info("Taglib resolution took {} ms", System.currentTimeMillis() - timer2);
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
            StringBuilder bundleClassPath = new StringBuilder(".");
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

        String rootFolder = inputManifest.getMainAttributes().getValue("root-folder");
        if (rootFolder != null) {
            bndProperties.put("Bundle-SymbolicName", rootFolder);
            String titleAttribute = inputManifest.getMainAttributes().getValue("package-name");
            if (titleAttribute == null) {
                titleAttribute = inputManifest.getMainAttributes().getValue("Implementation-Title");
            }
            bndProperties.put("Bundle-Name", titleAttribute);

            if (depends == null) {
                depends = "";
            }
            if (!depends.contains("default") && !depends.contains("Default Jahia Templates") && !rootFolder.equals("assets") && !rootFolder.equals("default") && !rootFolder.equals("jquery")) {
                if (!depends.equals("")) {
                    depends += ",";
                }
                depends += "default";
            }

            StringBuilder importPackage = new StringBuilder("*;resolution:=optional");

            List<String> alreadyImportedPackages = new ArrayList<String>(Arrays.asList(importPackage.toString().split(",")));
            for (String curImportPackage : getBundlePackages(rootFolder, importPackages)) {
                if (!alreadyImportedPackages.contains(curImportPackage) &&
                    !getBundlePackages(rootFolder, excludedImportPackages).contains(curImportPackage)) {
                    importPackage.append(",");
                    importPackage.append(curImportPackage);
                    importPackage.append(";resolution:=optional");
                    alreadyImportedPackages.add(curImportPackage);
                }
            }
            for (PackageInfo importPackageFromParsing : parsingContext.getPackageImports()) {
                if (!alreadyImportedPackages.contains(importPackageFromParsing.getName()) &&
                    !getBundlePackages(rootFolder, excludedImportPackages).contains(importPackageFromParsing.getName())) {
                    importPackage.append(",");
                    importPackage.append(importPackageFromParsing.getName());
                    importPackage.append(";resolution:=optional");
                    alreadyImportedPackages.add(importPackageFromParsing.getName());
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

        if (resolvedGroupId != null) {
            bndProperties.put("Jahia-GroupId", resolvedGroupId);
        } else {
            bndProperties.put("Jahia-GroupId", "org.jahia.modules"); // use default Jahia groupdId if none could be resolved.
        }

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
        
        logger.info("Transformation took {} ms", System.currentTimeMillis() - timer);
        
        return resultInputStream;
    }

    private void resolveTaglibs(String depends, ParsingContext parsingContext, Set<String> unresolvedTaglibUris) {
        try {
            List<Resource> allTlds = new LinkedList<Resource>();
            // first detect available TLDs in Web application class loader, if it is available (not available in unit tests)
            if (SpringContextSingleton.getInstance().isInitialized()) {
                Resource[] tldResources = SpringContextSingleton.getInstance().getResources("classpath*:/META-INF/*.tld",
                        false);
                if (tldResources != null) {
                    allTlds.addAll(Arrays.asList(tldResources));
                }
            }

            // check dependencies
            if (StringUtils.isNotEmpty(depends)) {
                // we still have unresolved taglibs -> check dependencies
                JahiaTemplateManagerService templateService = ServicesRegistry.getInstance()
                        .getJahiaTemplateManagerService();
                for (String dependency : StringUtils.split(depends, ", ")) {
                    JahiaTemplatesPackage pkg = templateService.getTemplatePackageById(dependency);
                    if (pkg == null) {
                        pkg = templateService.getTemplatePackage(dependency);
                    }
                    Bundle b = pkg != null ? pkg.getBundle() : null;
                    if (b != null) {
                        Enumeration<URL> tldEntries = b.findEntries("/META-INF", "*.tld", false);
                        if (tldEntries != null) {
                            while (tldEntries.hasMoreElements()) {
                                URL url = (URL) tldEntries.nextElement();
                                allTlds.add(new UrlResource(url));
                            }
                        }
                    }
                }
            }

            for (Resource tld : allTlds) {
                Document doc = readDocument(tld);
                String taglibUri = TldXmlFileParser.getTaglibUri(doc.getRootElement());
                if (StringUtils.isEmpty(taglibUri) || !unresolvedTaglibUris.contains(taglibUri)) {
                    // skip
                    continue;
                }
                logger.info("Analysing taglib {} from URI {}", taglibUri, tld);
                TldXmlFileParser parser = new TldXmlFileParser();
                parser.setLogger(logger);
                parser.parse(tld.getFilename(), doc.getRootElement(), tld.getURL().toExternalForm(), true, false, null, parsingContext);
                if (parsingContext.getUnresolvedTaglibUris().size() == 0) {
                    // we have resolved everything -> stop
                    break;
                }
            }

        } catch (Exception e) {
            logger.error(e.getMessage(), e);
        }
    }

    private Document readDocument(Resource tld) throws JDOMException, IOException {
        SAXBuilder saxBuilder = new SAXBuilder(XMLReaders.NONVALIDATING);
        saxBuilder.setFeature("http://xml.org/sax/features/validation", false);
        saxBuilder.setFeature("http://apache.org/xml/features/nonvalidating/load-dtd-grammar", false);
        saxBuilder.setFeature("http://apache.org/xml/features/nonvalidating/load-external-dtd", false);
        InputStream inputStream = tld.getInputStream();
        try {
            return saxBuilder.build(inputStream);
        } finally {
            IOUtils.closeQuietly(inputStream);
        }
    }
    
    private InputStream parseFile(InputStream fileInputStream, String fileName, String fileParent, boolean optional, ParsingContext parsingContext) throws IOException {
        // let's run all the parsers now.
        ByteArrayOutputStream entryOutputStream = new ByteArrayOutputStream();
        StreamUtils.copyStream(fileInputStream, entryOutputStream, false);
        ByteArrayInputStream tempEntryInputStream = new ByteArrayInputStream(entryOutputStream.toByteArray());
        Parsers.getInstance().parse(0, fileName, tempEntryInputStream, fileParent, false, optional, null, logger, parsingContext);
        tempEntryInputStream = new ByteArrayInputStream(entryOutputStream.toByteArray());
        Parsers.getInstance().parse(1, fileName, tempEntryInputStream, fileParent, false, optional, null, logger, parsingContext);
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
        bndProperties.put("Jahia-Module-Type", value != null ? value : "system");
        
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

    private boolean hasNamespaceURI(List<Namespace> rootElementNamespaces, String namespaceURI) {
        for (Namespace namespace : rootElementNamespaces) {
            if (namespace.getURI().contains(namespaceURI)) {
                return true;
            }
        }
        return false;
    }

    private Namespace getNamespaceByURI(List<Namespace> rootElementNamespaces, String namespaceURI) {
        for (Namespace namespace : rootElementNamespaces) {
            if (namespace.getURI().contains(namespaceURI)) {
                return namespace;
            }
        }
        return null;
    }

    public Set<String> getBundlePackages(String bundleName, Map<String,Set<String>> bundlePackages) {
        Set<String> packages = new TreeSet<String>();
        if (bundlePackages.get("*") != null) {
            packages.addAll(bundlePackages.get("*"));
        }
        if (bundlePackages.get(bundleName) != null) {
            packages.addAll(bundlePackages.get(bundleName));
        }
        return packages;
    }

    public boolean isPackageInBundlePackages(String packageName, String bundleName, Map<String,Set<String>> bundlePackages) {
        return getBundlePackages(bundleName, bundlePackages).contains(packageName);
    }

    @Override
    public void connect() throws IOException {
        // do nothing
    }
}
