package org.jahia.bundles.url.jahiawar.internal;

import org.jdom2.Document;
import org.jdom2.Element;
import org.jdom2.JDOMException;
import org.jdom2.Namespace;
import org.jdom2.input.SAXBuilder;
import org.jdom2.output.Format;
import org.jdom2.output.XMLOutputter;
import org.jdom2.xpath.XPath;
import org.ops4j.io.StreamUtils;
import org.ops4j.lang.NullArgumentException;
import org.ops4j.net.URLUtils;
import org.ops4j.pax.swissbox.bnd.BndUtils;

import java.io.*;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLConnection;
import java.util.*;
import java.util.jar.JarEntry;
import java.util.jar.JarInputStream;
import java.util.jar.JarOutputStream;
import java.util.jar.Manifest;

/**
 * Url connection for jahiawar protocol handler.
 */
public class Connection extends URLConnection {

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

    // @todo this list should be configurable
    private String[] hardcodedImports = new String[]{
            "javax.servlet.jsp.tagext",
            "org.jahia.api",
            "org.jahia.bundles.extender.jahiamodules",
            "org.jahia.data.viewhelper.principal",
            "org.jahia.exceptions",
            "org.jahia.params.valves",
            "org.jahia.taglibs",
            "org.jahia.taglibs.facet",
            "org.jahia.taglibs.functions",
            "org.jahia.taglibs.jcr",
            "org.jahia.taglibs.jcr.node",
            "org.jahia.taglibs.jcr.query",
            "org.jahia.taglibs.query",
            "org.jahia.taglibs.search",
            "org.jahia.taglibs.template",
            "org.jahia.taglibs.template.gwt",
            "org.jahia.taglibs.template.include",
            "org.jahia.taglibs.template.layoutmanager",
            "org.jahia.taglibs.template.pager",
            "org.jahia.taglibs.uicomponents",
            "org.jahia.taglibs.uicomponents.i18n",
            "org.jahia.taglibs.uicomponents.image",
            "org.jahia.taglibs.uicomponents.loginform",
            "org.jahia.taglibs.uicomponents.portlets",
            "org.jahia.taglibs.uicomponents.user",
            "org.jahia.taglibs.user",
            "org.jahia.taglibs.utility",
            "org.jahia.taglibs.utility.constants",
            "org.jahia.taglibs.utility.i18n",
            "org.jahia.taglibs.utility.session",
            "org.jahia.taglibs.utility.siteproperties",
            "org.jahia.taglibs.workflow",
            "org.jahia.services.content",
            "org.jahia.services.usermanager",
            "org.jahia.services.render",
            "org.jahia.services.search",
            "org.jahia.services.sites",
            "org.jahia.utils",
            "org.osgi.framework",
            "org.osgi.service.http",
            "org.osgi.util.tracker",
            "org.springframework.beans.factory.xml",
            "org.apache.commons.lang",
            "org.apache.commons.lang.math",
            "org.apache.commons.id",
            "org.apache.taglibs.unstandard"
    };

    public Set<String> extensionsToExport = new HashSet<String>();

    public Set<String> importPackages = new TreeSet<String>();

    public Connection(final URL url, final Configuration configuration)
            throws MalformedURLException {
        super(url);

        NullArgumentException.validateNotNull(url, "URL cannot be null");
        NullArgumentException.validateNotNull(configuration, "Service configuration");

        this.configuration = configuration;
        importPackages.addAll(Arrays.asList(hardcodedImports));
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
                } else if (newName.startsWith("WEB-INF/lib/")) {
                    newName = jarEntry.getName().substring("WEB-INF/lib/".length());
                    classPathEntries.add(newName);
                    ByteArrayOutputStream entryOutputStream = new ByteArrayOutputStream();
                    StreamUtils.copyStream(entryInputStream, entryOutputStream, false);
                    ByteArrayInputStream tempEntryInputStream = new ByteArrayInputStream(entryOutputStream.toByteArray());
                    JarInputStream embeddedJar = new JarInputStream(tempEntryInputStream);
                    JarEntry embeddedJarEntry = null;
                    while ((embeddedJarEntry = embeddedJar.getNextJarEntry()) != null) {
                        updateExportTracking(embeddedJarEntry.getName(), exportPackageIncludes, allNonEmptyDirectories);
                    }
                    embeddedJar.close();
                    tempEntryInputStream.close();
                    tempEntryInputStream = null;
                    entryInputStream = new ByteArrayInputStream(entryOutputStream.toByteArray());
                } else if (newName.startsWith("WEB-INF/")) {
                    newName = jarEntry.getName().substring("WEB-INF/".length());
                }
                if (newName.endsWith(".xml")) {
                    ByteArrayOutputStream entryOutputStream = new ByteArrayOutputStream();
                    StreamUtils.copyStream(entryInputStream, entryOutputStream, false);
                    ByteArrayInputStream tempEntryInputStream = new ByteArrayInputStream(entryOutputStream.toByteArray());
                    // let's load the XML and start transforming it.
                    SAXBuilder saxBuilder = new SAXBuilder();
                    String prefix = "";
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
                        e.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.
                    }

                    tempEntryInputStream.close();
                    tempEntryInputStream = null;
                    entryInputStream = new ByteArrayInputStream(entryOutputStream.toByteArray());
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
                updateExportTracking(newName, exportPackageIncludes, allNonEmptyDirectories);

            }
            if (inputManifest.getMainAttributes().getValue("Implementation-Title") != null) {
                String newEntryName = inputManifest.getMainAttributes().getValue("Implementation-Title").replaceAll("[ -]", "");
                addFakeEntries(jos, entryNames, mostRecentTime, newEntryName);
            }
            if (inputManifest.getMainAttributes().getValue("root-folder") != null) {
                String newEntryName = inputManifest.getMainAttributes().getValue("root-folder").replaceAll("[ -]", "");
                addFakeEntries(jos, entryNames, mostRecentTime, newEntryName);
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
            StringBuilder exportPackage = new StringBuilder("");
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
            exportPackage.append(inputManifest.getMainAttributes().getValue("Implementation-Title").replaceAll("[ -]", ""));
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


            String[] dependsArray = depends.split(",");
            StringBuilder importPackage = new StringBuilder("*");

            for (String dep : dependsArray) {
                if (!"".equals(dep)) {
                    importPackage.append(",");
                    dep = dep.replaceAll("[ -]", "");
                    importPackage.append(dep);
                }
            }

            List<String> alreadyImportedPackages = new ArrayList<String>(Arrays.asList(importPackage.toString().split(",")));
            for (String curImportPackage : importPackages) {
                if (!alreadyImportedPackages.contains(curImportPackage)) {
                    importPackage.append(",");
                    importPackage.append(curImportPackage);
                    alreadyImportedPackages.add(curImportPackage);
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

    private void addFakeEntries(JarOutputStream jos, Set<String> entryNames, long mostRecentTime, String newEntryName) throws IOException {
        if (!entryNames.contains(newEntryName + "/")) {
            JarEntry newJarEntry = new JarEntry(newEntryName + "/");
            newJarEntry.setTime(mostRecentTime);
            jos.putNextEntry(newJarEntry);
            jos.closeEntry();
            entryNames.add(newJarEntry.getName());
        }
        if (!entryNames.contains(newEntryName + "/index.html")) {
            JarEntry newJarEntry = new JarEntry(newEntryName + "/index.html");
            newJarEntry.setTime(mostRecentTime);
            jos.putNextEntry(newJarEntry);
            jos.closeEntry();
            entryNames.add(newJarEntry.getName());
        }
    }

    @Override
    public void connect() throws IOException {
    }
}
