// This script's intention is to detect possible application installation directory relocation,
// and update absolute file paths in configuration/auxiliary files correspondingly if so,
// at the early startup phase.

import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.nio.file.FileSystemNotFoundException;
import java.nio.file.InvalidPathException;
import java.nio.file.LinkOption;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Dictionary;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.Hashtable;

import org.apache.commons.io.FileUtils;
import org.apache.commons.lang.StringUtils;
import org.apache.felix.cm.file.ConfigurationHandler;
import org.jahia.exceptions.JahiaRuntimeException;
import org.jahia.services.SpringContextSingleton;
import org.jahia.settings.SettingsBean;

FileReferenceUpdater FILE_REFERENCE_UPDATER_FSPATH = new FileReferenceUpdaterFsPath();

// Chain of responsibility.
// NOTE: The order of updaters matters.
// Especially, it is critical that the FileReferenceUpdaterFsPath is configured before the FileReferenceUpdaterRootedWindowsFsPath.
FILE_REFERENCE_UPDATERS = [
    new FileReferenceUpdaterUri(),
    FILE_REFERENCE_UPDATER_FSPATH,
    new FileReferenceUpdaterRootedWindowsFsPath(),
    new FileReferenceUpdaterMavenRepositories(FILE_REFERENCE_UPDATER_FSPATH, ".mvn.defaultRepositories", ".mvn.repositories")
] as FileReferenceUpdater[];

updateFileReferencesIfNeeded();

private void updateFileReferencesIfNeeded() throws IOException {

    File varDir = new File(SettingsBean.getInstance().getJahiaVarDiskPath());

    File instancePropertiesFile = getKarafInstancePropertiesFile();
    if (!instancePropertiesFile.exists()) {
        logger.info("'{}' file not found. Assuming the instance is starting for the first time, so no file reference updates needed.", instancePropertiesFile);
        return;
    }

    Properties instanceProperties = new Properties();
    FileInputStream instancePropertiesIn = new FileInputStream(instancePropertiesFile);
    try {
        instanceProperties.load(instancePropertiesIn);
    } finally {
        instancePropertiesIn.close();
    }

    File oldKarafDir = new File(instanceProperties.getProperty("item.0.loc"));
    File oldVarDir = oldKarafDir.getParentFile();

    if (varDir.equals(oldVarDir)) {
        logger.info("The var dir hasn't been changed since last start, so no file path updates needed.");
        return;
    }

    logger.info("The var dir changed from '{}' to '{}', so updating file references correspondingly", oldVarDir, varDir);
    updateFileReferences(oldVarDir, varDir);
}

private void updateFileReferences(File oldVarDir, File varDir) throws IOException {

    FileHandler propertiesFileHandler = new FileHandler() {

        @Override
        public Map<Object, Object> readProperties(File propertiesFile) throws IOException {
            Properties properties = new Properties();
            FileInputStream propertiesFileIn = new FileInputStream(propertiesFile);
            try {
                properties.load(propertiesFileIn);
            } finally {
                propertiesFileIn.close();
            }
            return properties;
        }

        @Override
        public void writeProperties(File propertiesFile, Map<Object, Object> properties) throws IOException {
            Properties props = new Properties();
            props.putAll(properties);
            FileOutputStream propertiesFileOut = new FileOutputStream(propertiesFile);
            try {
                props.store(propertiesFileOut, null);
            } finally {
                propertiesFileOut.close();
            }
        }
    };

    updateFileReferencesInFile(getKarafInstancePropertiesFile(), oldVarDir, varDir, propertiesFileHandler);

    String moduleBundleLocationMapPath = getFileInstallConfig().getProperty("felix.fileinstall.bundleLocationMapFile");
    File moduleBundleLocationMapFile = new File(moduleBundleLocationMapPath);
    if (moduleBundleLocationMapFile.exists()) {
        updateFileReferencesInFile(moduleBundleLocationMapFile, oldVarDir, varDir, propertiesFileHandler);
    }

    String frameworkStoragePath = getOsgiConfig().getProperty("org.osgi.framework.storage");
    File frameworkStorageDir = new File(frameworkStoragePath);

    if (frameworkStorageDir.isDirectory()) {

        updateFileReferencesInConfigFiles(frameworkStorageDir, oldVarDir, varDir, new FileHandler() {

            @Override
            public Map<Object, Object> readProperties(File configFile) throws IOException {
                Dictionary<?, ?> props;
                FileInputStream configFileIn = new FileInputStream(configFile);
                try {
                    props = ConfigurationHandler.read(configFileIn);
                } finally {
                    configFileIn.close();
                }
                HashMap<Object, Object> properties = new HashMap<>(props.size());
                for (Enumeration<?> propertyKeys = props.keys(); propertyKeys.hasMoreElements(); ) {
                    Object propertyKey = propertyKeys.nextElement();
                    Object propertyValue = props.get(propertyKey);
                    properties.put(propertyKey, propertyValue);
                }
                return properties;
            }

            @Override
            public void writeProperties(File configFile, Map<Object, Object> properties) throws IOException {
                Hashtable<Object, Object> props = new Hashtable<>(properties.size());
                props.putAll(properties);
                FileOutputStream configFileOut = new FileOutputStream(configFile);
                try {
                    ConfigurationHandler.write(configFileOut, props);
                } finally {
                    configFileOut.close();
                }
            }
        });
    }
}

private void updateFileReferencesInConfigFiles(File baseDir, File oldVarDir, File newVarDir, FileHandler configFileHandler) throws IOException {
    for (File file : baseDir.listFiles()) {
        if (file.isDirectory()) {
            updateFileReferencesInConfigFiles(file, oldVarDir, newVarDir, configFileHandler);
        } else if (file.getName().endsWith(".config")) {
            updateFileReferencesInFile(file, oldVarDir, newVarDir, configFileHandler);
        }
    }
}

private void updateFileReferencesInFile(File file, File oldVarDir, File newVarDir, FileHandler fileHandler) throws IOException {
    Path oldVarPath = Paths.get(oldVarDir.getAbsolutePath());
    Path newVarPath = Paths.get(newVarDir.getAbsolutePath());
    Map<Object, Object> properties = fileHandler.readProperties(file);
    boolean changed = false;
    for (Map.Entry<Object, Object> entry : properties.entrySet()) {
        Object propertyKey = entry.getKey();
        Object propertyValue = entry.getValue();
        for (FileReferenceUpdater updater : FILE_REFERENCE_UPDATERS) {
            Object newPropertyValue = updater.updateIfFamiliar(propertyKey, propertyValue, oldVarPath, newVarPath);
            if (newPropertyValue != null) {
                properties.put(propertyKey, newPropertyValue);
                changed = true;
                logger.debug("Changed '{}' property value from '{}' to '{}' in '{}'", [propertyKey, propertyValue, newPropertyValue, file] as Object[]);
                break;
            }
        }
    }
    if (changed) {
        fileHandler.writeProperties(file, properties);
        logger.debug("Saved changes to '{}'", file);
    }
}

private static Properties getOsgiConfig() {
    return (Properties) SpringContextSingleton.getBean("combinedOsgiProperties");
}

private static Properties getFileInstallConfig() {
    return (Properties) SpringContextSingleton.getBean("felixFileInstallConfig");
}

private static File getKarafInstancePropertiesFile() {
    String karafInstancesPath = getOsgiConfig().getProperty("karaf.instances");
    return FileUtils.getFile(karafInstancesPath, "instance.properties");
}

interface FileHandler {

    Map<Object, Object> readProperties(File file) throws IOException;
    void writeProperties(File file, Map<Object, Object> properties) throws IOException;
}

interface FileReferenceUpdater {

    Object updateIfFamiliar(Object propertyKey, Object propertyValue, Path oldVarPath, Path newVarPath);
}

abstract class FileReferenceUpdaterSimpleBase implements FileReferenceUpdater {

    @Override
    public Object updateIfFamiliar(Object propertyKey, Object propertyValue, Path oldVarPath, Path newVarPath) {

        if (!(propertyValue instanceof String)) {
            // File reference may only be a String property.
            return null;
        }

        Path fileReference = toPath((String) propertyValue);
        if (fileReference == null) {
            // Not a file reference format the updater is able to handle (maybe not a file reference at all).
            return null;
        }

        // Update the file reference.
        Path relativePath;
        try {
            relativePath = oldVarPath.relativize(fileReference);
        } catch (IllegalArgumentException e) {
            return null;
        }
        fileReference = newVarPath.resolve(relativePath);

        return toString(fileReference);
    }

    protected abstract Path toPath(String fileReferenceString);
    protected abstract String toString(Path fileReference);

    protected static Path canonizeIfPossible(Path path) {
        try {
            return path.toRealPath(LinkOption.NOFOLLOW_LINKS);
        } catch (IOException e) {
            // This path does not exist in reality.
            return path;
        }
    }
}

/*
 * Handles values like
 * file:/C:/Program%20Files/apache-tomcat-8.0.30/digital-factory-data/karaf/etc/jmx.acl.java.lang.Memory.cfg
 * or
 * file:/home/jahia/install/QA-9314/digital-factory-data/karaf/etc/jmx.acl.java.lang.Memory.cfg
 */
class FileReferenceUpdaterUri extends FileReferenceUpdaterSimpleBase {

    @Override
    protected Path toPath(String fileReferenceString) {
        URI uri;
        try {
            uri = new URI(fileReferenceString);
        } catch (URISyntaxException e) {
            // Not a file URI the updater is able to handle.
            return null;
        }
        try {
            return Paths.get(uri);
        } catch (IllegalArgumentException | FileSystemNotFoundException e) {
            // Not a file URI the updater is able to handle.
            return null;
        }
    }

    @Override
    protected String toString(Path fileReference) {
        // Build the URI string manually, because fileReference.toUri().toASCIIString() generates format slightly different from what we see in .config files.
        String fileReferenceString = canonizeIfPossible(fileReference).toString();
        fileReferenceString = StringUtils.replace(fileReferenceString, "\\", "/");
        URI uri;
        try {
            uri = new URI("file", "/" + fileReferenceString, null);
        } catch (URISyntaxException e) {
            throw new JahiaRuntimeException(e);
        }
        return uri.toASCIIString();
    }
}

/*
 * Handles values like
 * C:\\Program Files\\apache-tomcat-8.0.30\\digital-factory-data\\karaf/deploy
 * or
 * /home/jahia/install/QA-9314/digital-factory-data/karaf/deploy
 */
class FileReferenceUpdaterFsPath extends FileReferenceUpdaterSimpleBase {

    @Override
    protected Path toPath(String fileReferenceString) {
        Path path;
        try {
            path = Paths.get(fileReferenceString);
        } catch (InvalidPathException e) {
            // Not a file path the updater is able to handle.
            return null;
        }
        if (!path.isAbsolute()) {
            // We only update absolute paths.
            return null;
        }
        return path;
    }

    @Override
    protected String toString(Path fileReference) {
        return canonizeIfPossible(fileReference).toString();
    }
}

/*
 * Handles specific values where Windows file path is prefixed with slash like
 * /C:/Program Files/apache-tomcat-8.0.30/digital-factory-data/modules/advanced-visibility-7.1.1.jar
 */
class FileReferenceUpdaterRootedWindowsFsPath extends FileReferenceUpdaterFsPath {

    @Override
    protected Path toPath(String fileReferenceString) {
        if (!fileReferenceString.startsWith("/")) {
            return null;
        }
        return super.toPath(fileReferenceString.substring(1));
    }

    @Override
    protected String toString(Path fileReference) {
        return ("/" + super.toString(fileReference));
    }
}

/*
 * Handles Karaf specific values that represent a comma separated Maven repository list like
 * file:C:\\Program Files\\apache-tomcat-8.0.30\\webapps\\ROOT\\WEB-INF\\karaf/system@id\=system.repository@snapshots, file:C:\\Program Files\\apache-tomcat-8.0.30\\digital-factory-data\\karaf\\data/kar@id\=kar.repository@multi@snapshots
 */
class FileReferenceUpdaterMavenRepositories implements FileReferenceUpdater {

    private FileReferenceUpdater fileReferenceUpdaterFsPath;
    private String[] propertyKeyEndings;

    public FileReferenceUpdaterMavenRepositories(FileReferenceUpdater fileReferenceUpdaterFsPath, String... propertyKeyEndings) {
        this.propertyKeyEndings = propertyKeyEndings;
        this.fileReferenceUpdaterFsPath = fileReferenceUpdaterFsPath;
    }

    @Override
    public Object updateIfFamiliar(Object propertyKey, Object propertyValue, Path oldVarPath, Path newVarPath) {

        if (!(propertyKey instanceof String && propertyValue instanceof String)) {
            // File reference may only be a part of a String property.
            return null;
        }
        if (!StringUtils.endsWithAny((String) propertyKey, propertyKeyEndings)) {
            // Not a property this updater is familiar with.
            return null;
        }

        String[] values = StringUtils.split((String) propertyValue, ',');
        String[] newValues = new String[values.length];
        boolean changed = false;

        for (int i = 0; i < values.length; i++) {

            String value = values[i].trim();
            if (!StringUtils.startsWithIgnoreCase(value, "file:")) {
                // Not a file reference.
                newValues[i] = value;
                continue;
            }
            int atIndex = value.indexOf('@');
            if (atIndex < 0) {
                // Not a format this updater is familiar with.
                newValues[i] = value;
                continue;
            }

            // This is not a URI format that can be understood by the java.net.URI class, even though it looks quite similarly.
            // Therefore, we extract its meaningful part manually and consider it a regular file path.
            // And then we compose the new value using the updated file path manually again.
            String path = value.substring("file:".length(), atIndex);
            Object newPath = fileReferenceUpdaterFsPath.updateIfFamiliar(null, path, oldVarPath, newVarPath);
            if (newPath == null) {
                // Not a file path this updater is able to handle.
                newValues[i] = value;
                continue;
            }
            String rest = value.substring(atIndex);
            newValues[i] = "file:" + newPath + rest;
            changed = true;
        }

        if (changed) {
            return StringUtils.join(newValues, ',');
        } else {
            return null;
        }
    }
}
