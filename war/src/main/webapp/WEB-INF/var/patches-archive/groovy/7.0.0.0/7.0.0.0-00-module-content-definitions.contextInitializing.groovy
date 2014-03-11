import org.apache.commons.io.FileUtils
import org.apache.commons.io.filefilter.TrueFileFilter
import org.apache.log4j.Logger
import org.apache.poi.util.IOUtils
import org.jahia.settings.SettingsBean
import org.jahia.utils.DatabaseUtils

import javax.jcr.RepositoryException
import javax.servlet.ServletContext
import javax.sql.DataSource
import java.sql.Connection
import java.sql.PreparedStatement
import java.sql.ResultSet
import java.sql.SQLException
import java.util.jar.JarEntry
import java.util.jar.JarFile
import java.util.jar.Manifest
import java.util.regex.Matcher
import java.util.regex.Pattern

final Logger log = Logger.getLogger("org.jahia.tools.groovyConsole");

Connection connection = null;

try {
    DataSource dataSource = DatabaseUtils.getDatasource();
    connection = dataSource.getConnection();

    ServletContext servletContext = SettingsBean.getInstance().getServletContext();
    String modulesPath = servletContext.getRealPath("WEB-INF/var/modules");

    Collection<File> moduleFiles = FileUtils.listFiles(new File(modulesPath), TrueFileFilter.INSTANCE, TrueFileFilter.INSTANCE);
    for (File moduleFile : moduleFiles) {
        if (moduleFile.getName().endsWith(".jar") || moduleFile.getName().endsWith(".war")) {
            processJarFile(moduleFile, connection);
        }
    }

    if (!connection.getAutoCommit()) {
        connection.commit();
    }

} catch (SQLException sqle) {
    if (connection != null && !connection.getAutoCommit()) {
        connection.rollback();
    }

} finally {
    if (connection != null) {
        connection.close();
    }
}

public void processJarFile(File file, Connection connection) throws SQLException {
    JarFile jarFile = new JarFile(file);
    Manifest manifest = jarFile.getManifest();
    String systemId = manifest.getMainAttributes().getValue("Bundle-SymbolicName");
    if (systemId == null) {
        systemId = manifest.getMainAttributes().getValue("Bundle-SymbolicName")
    }
    String fileName = systemId + ".cnd";

    Map<String, String> definitionContents = new LinkedHashMap<String, String>();
    Map<String, String> namespaceDeclarations = new LinkedHashMap<String, String>();

    Enumeration<JarEntry> jarEntryEnumeration = jarFile.entries();
    while (jarEntryEnumeration.hasMoreElements()) {
        JarEntry jarEntry = jarEntryEnumeration.nextElement();
        if (jarEntry.getName().endsWith(".cnd")) {
            InputStream entryInputStream = jarFile.getInputStream(jarEntry);
            ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
            IOUtils.copy(entryInputStream, byteArrayOutputStream);
            String fileContents = new String(byteArrayOutputStream.toByteArray(), "UTF-8");
            fileContents = processNamespaces(fileContents, namespaceDeclarations);
            definitionContents.put(jarEntry.getName(), fileContents);
            entryInputStream.close();
            byteArrayOutputStream.close();
        }
    }

    StringBuffer concatenatedDefinitions = new StringBuffer();
    if (definitionContents.size() > 0) {
        for (Map.Entry<String, String> namespaceDeclaration : namespaceDeclarations.entrySet()) {
            concatenatedDefinitions.append("<");
            concatenatedDefinitions.append(namespaceDeclaration.getKey());
            concatenatedDefinitions.append(" = ");
            concatenatedDefinitions.append(namespaceDeclaration.getValue())
            concatenatedDefinitions.append(">\n");
        }
        for (Map.Entry<String, String> definitionContent : definitionContents.entrySet()) {
            String definitionContentValue = definitionContent.getValue();
            concatenatedDefinitions.append(definitionContentValue);
            concatenatedDefinitions.append("\n");
        }

        saveCndFile(fileName, concatenatedDefinitions.toString(), connection);
        log.info("Copied CND files from module " + file + " into database under file name=" + fileName);
    }

}

public String processNamespaces(String fileContents, Map<String, String> namespaceDeclarations) {

    final Pattern namespacePattern = Pattern.compile("<\\s*([A-Za-z0-9:_']*)\\s*=\\s*([A-Za-z0-9:_'\\.\\/]*)\\s*>");
    final Matcher namespaceMatcher = namespacePattern.matcher(fileContents);

    int lastFoundPos = 0;
    while (namespaceMatcher.find()) {
        log.info("Found namespace prefix=" + namespaceMatcher.group(1) + " uri=" + namespaceMatcher.group(2));
        namespaceDeclarations.put(namespaceMatcher.group(1), namespaceMatcher.group(2));
        lastFoundPos = namespaceMatcher.end();
    }
    return fileContents.substring(lastFoundPos);
}

public void saveCndFile(String filename, String content, Connection connection) throws RepositoryException, SQLException {
    if (cndFileExists(filename, connection)) {
        updateExistingCndFile(filename, content, connection);
        log.info("Updated existing file " + filename);
    } else {
        saveNewCndFile(filename, content, connection);
        log.info("Save new CND file under file name " + filename);
    }
}

public int saveNewCndFile(String filename, String content, Connection connection) throws RepositoryException, SQLException {
    PreparedStatement stmt = null;
    try {
        stmt = connection.prepareStatement('insert into jahia_nodetypes_provider(cndFile, filename) values (?,?)');
        stmt.setString(1, content);
        stmt.setString(2, filename);
        return stmt.executeUpdate();
    } finally {
        if (stmt != null) {
            try {
                stmt.close();
            } catch (Exception e) {
                log.error("Error closing statement", e);
            }
        }
    }
    return -1;
}

public int updateExistingCndFile(String filename, String content, Connection connection) throws RepositoryException, SQLException {
    PreparedStatement stmt = null;
    try {
        stmt = connection.prepareStatement('update jahia_nodetypes_provider set cndFile=? where filename=?');
        stmt.setString(1, content);
        stmt.setString(2, filename);
        return stmt.executeUpdate();
    } finally {
        if (stmt != null) {
            try {
                stmt.close();
            } catch (Exception e) {
                log.error("Error closing statement", e);
            }
        }
    }
    return -1;
}

public boolean cndFileExists(String filename, Connection connection) {
    PreparedStatement stmt = null;
    ResultSet resultSet = null;
    try {
        stmt = connection.prepareStatement('select * from jahia_nodetypes_provider where filename=?');
        stmt.setString(1, filename);
        resultSet = stmt.executeQuery();
        return resultSet.next();
    } finally {
        if (resultSet != null) {
            try {
                resultSet.close();
            } catch (Exception e) {
                log.error("Error closing result set ", e);
            }
        }
        if (stmt != null) {
            try {
                stmt.close();
            } catch (Exception e) {
                log.error("Error closing statement", e);
            }
        }
    }
    return false;
}
