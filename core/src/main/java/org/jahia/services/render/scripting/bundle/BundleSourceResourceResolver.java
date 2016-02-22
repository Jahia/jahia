package org.jahia.services.render.scripting.bundle;

import org.apache.commons.lang.StringUtils;
import org.apache.maven.model.Model;
import org.jahia.utils.PomUtils;
import org.osgi.framework.Bundle;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLConnection;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by loom on 08.10.15.
 */
public class BundleSourceResourceResolver {

    private static final URL[] EMPTY_URL_ARRAY = new URL[0];

    private static Logger logger = LoggerFactory.getLogger(BundleSourceResourceResolver.class);

    private final URL[] sourceURLs;

    public BundleSourceResourceResolver(Bundle bundle) {
        URL[] sourceURLs = getSourceURLs(bundle);
        this.sourceURLs = sourceURLs != null && sourceURLs.length == 0 ? null : sourceURLs;
    }

    public URL[] getSourceURLs() {
        return sourceURLs;
    }

    public boolean hasSourceURLs() {
        return sourceURLs != null && sourceURLs.length > 0;
    }

    private static URL[] getSourceURLs(Bundle bundle) {
        URL[] urls = EMPTY_URL_ARRAY;
        String sourceFolder = bundle.getHeaders().get("Jahia-Source-Folders");
        if (StringUtils.isNotEmpty(sourceFolder)) {
            File pomFile = new File(sourceFolder, "pom.xml");
            if (!pomFile.exists()) {
                return null;
            }
            try {
                final Model model = PomUtils.read(pomFile);
                String s = model.getVersion();
                if (s == null) {
                    s = model.getParent().getVersion();
                }
                if (!s.equals(bundle.getHeaders().get("Implementation-Version"))) {
                    return null;
                }
            } catch (Exception e) {
                logger.warn("Invalid source folder " + sourceFolder + ", cannot read pom file", e.getMessage());
                return null;
            }

            List<URL> sourceURLs = new ArrayList<>();
            File resourceFolderFile = new File(sourceFolder, "src/main/resources");
            if (resourceFolderFile.exists()) {
                try {
                    sourceURLs.add(resourceFolderFile.toURI().toURL());
                } catch (MalformedURLException e) {
                    logger.warn("Invalid source folder " + sourceFolder + ", cannot convert to URL", e);
                }
            }
            // Legacy sources
            File webappFolderFile = new File(sourceFolder, "src/main/webapp");
            if (webappFolderFile.exists()) {
                try {
                    sourceURLs.add(webappFolderFile.toURI().toURL());
                } catch (MalformedURLException e) {
                    logger.warn("Invalid source folder " + sourceFolder + ", cannot convert to URL", e);
                }
            }

            urls = sourceURLs.toArray(new URL[sourceURLs.size()]);
            logger.debug("Detected {} source folders for bundle {}", sourceURLs.size(), bundle.getSymbolicName());
        }
        return urls;
    }

    public URL getResource(String name) {
        if (name != null && name.charAt(0) == '/' && name.length() > 1) {
            name = name.substring(1);
        } 
        for (URL sourceURL : sourceURLs) {
            URL resourceURL;
            try {
                resourceURL = new URL(sourceURL, name);
                if (urlExists(resourceURL)) {
                    // @todo we could add dynamic registration here if the resource has not yet been registered in the activator
                    return resourceURL;
                }
            } catch (MalformedURLException e) {
                logger.error("Error in resource URL " + name, e);
            }
        }
        return null;
    }

    private boolean urlExists(URL url) {
        // Try a URL connection content-length header...
        try {
            URLConnection con = null;
            con = url.openConnection();
            con.setUseCaches(false);
            HttpURLConnection httpCon =
                    (con instanceof HttpURLConnection ? (HttpURLConnection) con : null);
            if (httpCon != null) {
                httpCon.setRequestMethod("HEAD");
                if (httpCon.getResponseCode() == HttpURLConnection.HTTP_OK) {
                    return true;
                }
            }
            if (con.getContentLength() > 0) {
                return true;
            }
            if (httpCon != null) {
                // no HTTP OK status, and no content-length header: give up
                httpCon.disconnect();
                return false;
            } else {
                // Fall back to stream existence: can we open the stream?
                InputStream is = getInputStream(url);
                is.close();
                return true;
            }
        } catch (IOException e) {
            logger.debug("Testing existence of resource " + url, e);
            return false;
        }
    }

    private InputStream getInputStream(URL url) throws IOException {
        URLConnection con = url.openConnection();
        con.setUseCaches(false);
        try {
            return con.getInputStream();
        } catch (IOException ex) {
            // Close the HTTP connection (if applicable).
            if (con instanceof HttpURLConnection) {
                ((HttpURLConnection) con).disconnect();
            }
            throw ex;
        }
    }

}
