package org.jahia.bundles.url.jahiawar.internal;

import org.ops4j.io.StreamUtils;
import org.ops4j.lang.NullArgumentException;
import org.ops4j.monitors.stream.StreamMonitor;
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
import java.util.zip.ZipEntry;

/**
 * Url connection for jahiawar protocol handler.
 */
public class Connection extends URLConnection {

    private Parser parser;
    private final Configuration configuration;
    private List<String> classPathEntries = new ArrayList<String>();
    private Set<String> exportPackageExcludes = new HashSet<String>();

    // @todo this list should be configurable
    private String[] hardcodedImports = new String[]{
            "org.jahia.bundles.extender.jahiamodules",
            "org.jahia.data.viewhelper.principal",
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
            "org.jahia.utils",
            "org.osgi.framework",
            "org.osgi.service.http",
            "org.osgi.util.tracker",
            "org.springframework.beans.factory.xml",
            "org.apache.commons.lang",
            "org.apache.commons.lang.math",
            "org.apache.commons.id"
    };

    public Set<String> importPackages = new HashSet<String>();

    public Connection(final URL url, final Configuration configuration)
            throws MalformedURLException {
        super(url);

        NullArgumentException.validateNotNull(url, "URL cannot be null");
        NullArgumentException.validateNotNull(configuration, "Service configuration");

        this.configuration = configuration;
        importPackages.addAll(Arrays.asList(hardcodedImports));
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

        final PipedOutputStream pos = new PipedOutputStream();
        final PipedInputStream pis = new PipedInputStream(pos);
        new Thread() {

            public void run() {
                JarOutputStream jos = null;
                try {
                    jos = new JarOutputStream(pos, inputManifest);
                    JarEntry jarEntry = null;
                    Set<String> entryNames = new HashSet<String>();
                    long mostRecentTime = Long.MIN_VALUE;
//                    String rootFolderPrefix = inputManifest.getMainAttributes().getValue("root-folder").replaceAll("[ -]", "") + "/";
//                    entryNames.add(rootFolderPrefix);
                    while ((jarEntry = jarInputStream.getNextJarEntry()) != null) {
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
                        } else if (newName.startsWith("WEB-INF/")) {
                            newName = jarEntry.getName().substring("WEB-INF/".length());
                        }
                        JarEntry newJarEntry = new JarEntry(newName);
                        if (jarEntry.getTime() > mostRecentTime) {
                            mostRecentTime = jarEntry.getTime();
                        }
                        newJarEntry.setTime(jarEntry.getTime());
                        jos.putNextEntry(newJarEntry);
                        StreamUtils.copyStream(jarInputStream, jos, false);
                        jos.closeEntry();
                        entryNames.add(newName);
                        // now let's see if the new entry starts with an existing package import, in which case
                        // we will not export it.
                        int lastSlashPos = newName.lastIndexOf("/");
                        if (lastSlashPos > -1) {
                            String directoryName = newName.substring(0, lastSlashPos);
                            if (importPackages.contains(directoryName)) {
                                exportPackageExcludes.add(directoryName);
                            }
                        } else {
                            // do nothing this is not a directory.
                        }
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
            }
        }.start();

        final JarInputStream jarInputStream2 = new JarInputStream(URLUtils.prepareInputStream(
                parser.getWrappedJarURL(),
                !configuration.getCertificateCheck()
        ));
        Set<String> entries = new HashSet<String>();
        ZipEntry zipEntry;
        while ((zipEntry = jarInputStream2.getNextEntry()) != null) {
            if (zipEntry.isDirectory()) {
                entries.add(zipEntry.getName());
            }
        }

        // now we read the full input stream into memory to make sure we collect all dependencies.
        ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
        StreamUtils.copyStream(new StreamMonitor() {

            @Override
            public void notifyUpdate(URL url, int expected, int count) {
            }

            @Override
            public void notifyCompletion(URL url) {
            }

            @Override
            public void notifyError(URL url, String message) {
            }

        }, parser.getWrappedJarURL(), 0, pis, byteArrayOutputStream, false);
        byteArrayOutputStream.close();

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

        String rootFolder = inputManifest.getMainAttributes().getValue("root-folder");
        if (rootFolder != null) {
            String packagePrefix = rootFolder.replaceAll("[ -]", "");

            bndProperties.put("Bundle-SymbolicName", rootFolder);
            StringBuilder exportPackage = new StringBuilder("");
            if (exportPackageExcludes.size() > 0) {
                for (String exportPackageExclude : exportPackageExcludes) {
                    exportPackage.append("!");
                    exportPackage.append(exportPackageExclude);
                    exportPackage.append(",");
                }
                exportPackage.append(",*,");
            } else {
                exportPackage.append("*,");
            }
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

            for (String curImportPackage : importPackages) {
                if (!importPackage.toString().contains(curImportPackage)) {
                    importPackage.append(",");
                    importPackage.append(curImportPackage);
                }
            }

            bndProperties.put("Import-Package", importPackage.toString());

            Set<String> rootFolders = new HashSet<String>();
            for (String entry : entries) {
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

        return BndUtils.createBundle(byteArrayInputStream,
                bndProperties,
                url.toExternalForm(),
                parser.getOverwriteMode()
        );
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
