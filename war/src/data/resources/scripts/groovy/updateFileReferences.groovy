/*
 * Detects possible application installation directory relocation
 * and update absolute file paths in configuration/auxiliary files correspondingly at the early startup phase if needed.
 */

import java.io.File
import java.nio.file.FileSystems
import java.nio.file.Paths
import java.util.Iterator
import java.util.Properties;

import org.apache.commons.collections.IteratorUtils
import org.apache.commons.io.FileUtils
import org.apache.commons.lang.StringUtils
import org.codehaus.plexus.util.PropertyUtils
import org.jahia.services.SpringContextSingleton;
import org.jahia.settings.SettingsBean

private static Map<String, String> calculateReplacements(String targetPath, String... sourcePaths) {
    Map<String, String> replacements = new LinkedHashMap<String, String>();
    for (String sourcePath : sourcePaths) {
        if (sourcePath == null) {
            continue;
        }
        if (sourcePath.indexOf('\\') != -1) {
            // source system was Windows
            String sourcePathBackSlash = StringUtils.replace(sourcePath, "\\", "\\\\");
            String sourcePathForwardSlash = sourcePath.replace('\\', '/');
            if (targetPath.indexOf('\\') != -1) {
                // current system is Windows
                String targetPathBackSlash = StringUtils.replace(targetPath, "\\", "\\\\");
                String targetPathForwardSlash = targetPath.replace('\\', '/');
                replacements.put(sourcePathBackSlash, targetPathBackSlash);
                replacements.put(sourcePathForwardSlash, targetPathForwardSlash);
                replacements.put(StringUtils.replace(sourcePathBackSlash, ":", "\\:"), StringUtils.replace(targetPathBackSlash, ":", "\\:"));
                replacements.put(StringUtils.replace(sourcePathForwardSlash, ":", "\\:"), StringUtils.replace(targetPathForwardSlash, ":", "\\:"));
            } else {
                // current system is Linux
                replacements.put(sourcePathBackSlash, targetPath);
                replacements.put(sourcePathForwardSlash, targetPath);
                replacements.put(StringUtils.replace(sourcePathBackSlash, ":", "\\:"), targetPath);
                replacements.put(StringUtils.replace(sourcePathForwardSlash, ":", "\\:"), targetPath);
                replacements.put("\\\\karaf\\\\data", "/karaf/data");
                replacements.put("\\\\karaf", "/karaf");
            }
        } else {
            // source system was Linux
            if (targetPath.indexOf('\\') != -1) {
                // current system is Windows
                replacements.put(sourcePath, targetPath.replace('\\', '/'));
            } else {
                // current system is Linux
                replacements.put(sourcePath, targetPath);
            }
        }
    }

    return replacements;
}

private static File getBundleLocationMapFile() {
    return new File(getFileInstallConfig().getProperty("felix.fileinstall.bundleLocationMapFile"));
}

private static String getDataDirLocationFromKarafInstanceProperties() {
    Properties props = PropertyUtils.loadProperties(getKarafInstancePropertiesFile());

    String path = props != null ? props.getProperty("item.0.loc") : null;
    return path != null ? getParent(normalize(path)) : null;
}

private static String getDataDirLocationFromLocationMapProperties() {
    Properties props = PropertyUtils.loadProperties(getBundleLocationMapFile());

    if (props == null) {
        return null;
    }
    for (Object key : props.keySet()) {
        if (key.toString().startsWith("dx:org.jahia.modules/default/")) {
            String path = normalize(props.getProperty(key));
            return getParent(getParent(path));
        }
    }
    return null;
}

private static File getDeployedBundlesDir() {
    return new File(getOsgiConfig().getProperty("org.osgi.framework.storage"));
}

private static Properties getFileInstallConfig() {
    return (Properties) SpringContextSingleton.getBean("felixFileInstallConfig");
}

private static File getKarafInstancePropertiesFile() {
    return new File(getOsgiConfig().getProperty("karaf.instances"), "instance.properties");
}

private static Properties getOsgiConfig() {
    return (Properties) SpringContextSingleton.getBean("combinedOsgiProperties");
}

private static String getParent(String path) {
    int pos = path.lastIndexOf('/');
    if (pos == -1) {
        pos = path.lastIndexOf('\\');
    }
    return pos != -1 ? path.substring(0, pos) : path;
}

private static Iterator<File> iterateOnConfigFiles() {
    String[] ext = ["config"];
    return FileUtils.iterateFiles(getDeployedBundlesDir(), ext, true);
}

private static String normalize(String path) {
    if (path.indexOf(':') != -1) {
        // we are on Windows
        if (path.charAt(0) == '/') {
            path = path.substring(1);
        }
        path = path.replace('/', '\\');
    }
    
    return path;
}

private static boolean postProcessBundleLocationMapFile(File target) {
    if (!target.isFile()) {
        return false;
    }
    
    String content = FileUtils.readFileToString(target, "UTF-8");
    String adjustedContent = content;
    adjustedContent = StringUtils.replace(adjustedContent, "//", "/");
    adjustedContent = StringUtils.replace(adjustedContent, "=", "=/");
    adjustedContent = StringUtils.replace(adjustedContent, "//", "/");
    
    boolean updated = !adjustedContent.equals(content);     
    if (updated) {
        FileUtils.writeStringToFile(target, adjustedContent, "UTF-8");
    }

    return updated;
}

private static boolean updatePaths(File target, Map<String, String> replacements) {
    if (!target.isFile()) {
        return false;
    }
    
    String content = FileUtils.readFileToString(target, "UTF-8");
    String adjustedContent = content;
    for (Map.Entry<String, String> r : replacements.entrySet()) {
        adjustedContent = StringUtils.replace(adjustedContent, r.getKey(), r.getValue());
    }

    boolean updated = !adjustedContent.equals(content);     
    if (updated) {
        FileUtils.writeStringToFile(target, adjustedContent, "UTF-8");
    }
    
    return updated;
}

String dataDir = SettingsBean.getInstance().getJahiaVarDiskPath();

String dataDirLocationFromKarafInstanceProperties = getDataDirLocationFromKarafInstanceProperties();

String dataDirLocationFromLocationMapProperties = getDataDirLocationFromLocationMapProperties();

if ((dataDirLocationFromKarafInstanceProperties == null || dataDirLocationFromKarafInstanceProperties.equals(dataDir)) &&
(dataDirLocationFromLocationMapProperties == null || dataDirLocationFromLocationMapProperties.equals(dataDir))) {
    log.info("No rewrite of paths needed");
    return;
}

log.info("Will perform rewrite of paths in files");

log.info("dataDir: " + dataDir);
log.info("dataDirLocationFromKarafInstanceProperties: " + dataDirLocationFromKarafInstanceProperties);
log.info("dataDirLocationFromLocationMapProperties: " + dataDirLocationFromLocationMapProperties);

Map<String, String> replacements = calculateReplacements(dataDir, dataDirLocationFromKarafInstanceProperties, dataDirLocationFromLocationMapProperties);
log.info("Will use the following replacesments");
for (Map.Entry<String, String> r : replacements.entrySet()) {
    log.info(r.getKey() + " -> " + r.getValue());
}

File bundleLocationMapFile = getBundleLocationMapFile();

List<File> filesToModify = new LinkedList<File>();
filesToModify.add(getKarafInstancePropertiesFile());
filesToModify.add(bundleLocationMapFile);
filesToModify.addAll(IteratorUtils.toList(iterateOnConfigFiles()));

log.info("Collected " + filesToModify.size() + " files to be checked");

for (File f : filesToModify) {
    if (updatePaths(f, replacements)) {
        log.info("\tpaths updaded in " + f);
    }
}

if (postProcessBundleLocationMapFile(bundleLocationMapFile)) {
    log.info("Post-precessed " + bundleLocationMapFile);
}

log.info("Done checking paths in configuration files");