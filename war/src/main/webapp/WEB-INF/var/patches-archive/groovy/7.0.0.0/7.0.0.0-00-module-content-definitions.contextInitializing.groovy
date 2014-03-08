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
import java.sql.SQLException
import java.util.jar.JarEntry
import java.util.jar.JarFile
import java.util.jar.Manifest

final Logger log = Logger.getLogger("org.jahia.tools.groovyConsole");

DataSource dataSource = DatabaseUtils.getDatasource();

ServletContext servletContext = SettingsBean.getInstance().getServletContext();
String modulesPath = servletContext.getRealPath("WEB-INF/var/modules");

Collection<File> moduleFiles = FileUtils.listFiles(new File(modulesPath), TrueFileFilter.INSTANCE, TrueFileFilter.INSTANCE);
for (File moduleFile : moduleFiles) {
    if (moduleFile.getName().endsWith(".jar") || moduleFile.getName().endsWith(".war")) {
        processJarFile(moduleFile, dataSource);
    }
}

public void processJarFile(File file, DataSource dataSource) {
    JarFile jarFile = new JarFile(file);
    Manifest manifest = jarFile.getManifest();
    String systemId = manifest.getMainAttributes().getValue("Bundle-SymbolicName");
    if (systemId == null) {
        systemId = manifest.getMainAttributes().getValue("Bundle-SymbolicName")
    }

    Enumeration<JarEntry> jarEntryEnumeration = jarFile.entries();
    while (jarEntryEnumeration.hasMoreElements()) {
        JarEntry jarEntry = jarEntryEnumeration.nextElement();
        if (jarEntry.getName().endsWith(".cnd")) {
            InputStream entryInputStream = jarFile.getInputStream(jarEntry);
            ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
            IOUtils.copy(entryInputStream, byteArrayOutputStream);
            String fileContents = new String(byteArrayOutputStream.toByteArray(), "UTF-8");
            String fileName = systemId + ".cnd";
            saveCndFile(fileName, fileContents, dataSource);
            log.info("Copied CND file " + jarEntry.getName() + " from module " + file + " into database under file name=" + fileName);
            entryInputStream.close();
            byteArrayOutputStream.close();
        }
    }
}

public void saveCndFile(String filename,String content, DataSource dataSource) throws RepositoryException {
    Connection conn = null;
    PreparedStatement stmt = null;
    try {
        conn = dataSource.getConnection();
        stmt = conn.prepareStatement('insert into jahia_nodetypes_provider(cndFile, filename) values (?,?)');
        stmt.setString(1, content);
        stmt.setString(2, filename);
        int result = stmt.executeUpdate();
        if (!conn.getAutoCommit()) {
            conn.commit();
        }
    } catch (SQLException e) {
        if (conn != null && !conn.getAutoCommit()) {
            conn.rollback();
        }
    } finally {
        if (stmt != null) {
            try {
                stmt.close();
            } catch (Exception e) {
                // ignore
            }
        }
        if (conn != null) {
            try {
                conn.close();
            } catch (Exception e) {
                // ignore
            }
        }
    }

}
