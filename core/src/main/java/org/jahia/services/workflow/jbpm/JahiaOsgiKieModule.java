package org.jahia.services.workflow.jbpm;

import org.drools.compiler.kie.builder.impl.AbstractKieModule;
import org.drools.compiler.kproject.ReleaseIdImpl;
import org.drools.compiler.kproject.models.KieModuleModelImpl;
import org.kie.api.builder.ReleaseId;
import org.kie.api.builder.model.KieModuleModel;
import org.osgi.framework.Bundle;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Enumeration;

import static org.drools.core.util.IoUtils.readBytesFromInputStream;

/**
 * An re-implementation of the org.drools.osgi.compiler.OsgiKieModule to make it possible to pass the bundle
 * reference directly, instead of it being resolved by the URL.
 */
public class JahiaOsgiKieModule extends AbstractKieModule {

    private final Bundle bundle;
    private final int bundleUrlPrefixLength;

    private Collection<String> fileNames;

    public JahiaOsgiKieModule(ReleaseId releaseId, KieModuleModel kModuleModel, Bundle bundle, int bundleUrlPrefixLength) {
        super(releaseId, kModuleModel);
        this.bundle = bundle;
        this.bundleUrlPrefixLength = bundleUrlPrefixLength;
    }

    @Override
    public byte[] getBytes() {
        throw new UnsupportedOperationException();
    }

    @Override
    public boolean isAvailable(String pResourceName) {
        return fileNames.contains(pResourceName);
    }

    @Override
    public byte[] getBytes(String pResourceName) {
        URL url = bundle.getEntry(pResourceName);
        return url == null ? null : readUrlAsBytes(url);
    }

    @Override
    public Collection<String> getFileNames() {
        if (fileNames != null) {
            return fileNames;
        }
        fileNames = new ArrayList<String>();
        Enumeration<URL> e = bundle.findEntries("", "*", true);
        while (e.hasMoreElements()) {
            URL url = e.nextElement();
            String urlString = url.toString();
            if (urlString.endsWith("/")) {
                continue;
            }
            fileNames.add(urlString.substring(bundleUrlPrefixLength));
        }
        return fileNames;
    }

    @Override
    public File getFile() {
        throw new UnsupportedOperationException();
    }

    private static byte[] readUrlAsBytes(URL url) {
        InputStream is = null;
        try {
            is = url.openStream();
            return readBytesFromInputStream(is);
        } catch (IOException e) {
            throw new RuntimeException(e);
        } finally {
            if (is != null) {
                try {
                    is.close();
                } catch (IOException e) {
                }
            }
        }
    }

    private static String readUrlAsString(URL url) {
        return new String(readUrlAsBytes(url));
    }

    private static String getPomProperties(Bundle bundle) {
        Enumeration<URL> e = bundle.findEntries("META-INF/maven", "pom.properties", true);
        if (!e.hasMoreElements()) {
            throw new RuntimeException("Cannot find pom.properties file in bundle " + bundle);
        }
        return readUrlAsString(e.nextElement());
    }

    public static JahiaOsgiKieModule create(URL url, Bundle bundle) {
        KieModuleModel kieProject = KieModuleModelImpl.fromXML(url);
        String urlString = url.toString();

        String pomProperties = getPomProperties(bundle);
        ReleaseId releaseId = ReleaseIdImpl.fromPropertiesString(pomProperties);
        return new JahiaOsgiKieModule(releaseId, kieProject, bundle, urlString.indexOf("META-INF"));
    }

    @Override
    public String toString() {
        return "JahiaOsgiKieModule{" +
                "bundle=" + bundle + "," +
                "releaseId=" + getReleaseId() +
                '}';
    }
}
