package org.jahia.bundles.extender.jahiamodules;

import org.osgi.framework.Bundle;
import org.osgi.service.http.HttpContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
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
 * HttpContext that can look up resources in files as well as the default OSGi HttpContext
 */
public class FileHttpContext implements HttpContext {

    private static Logger logger = LoggerFactory.getLogger(FileHttpContext.class);

    HttpContext parentHttpContext;
    URL[] sourceURLs;

    public FileHttpContext(URL[] sourceURLs, HttpContext parentHttpContext) {
        this.sourceURLs = sourceURLs;
        this.parentHttpContext = parentHttpContext;
    }

    public static URL[] getSourceURLs(Bundle bundle) {
        String sourceFolderHeader = (String) bundle.getHeaders().get("Source-Folders");
        List<URL> sourceURLs = new ArrayList<URL>();
        if (sourceFolderHeader != null) {
            String[] sourceFolders = sourceFolderHeader.split(",");

            for (String sourceFolder : sourceFolders) {
                File resourceFolderFile = new File(sourceFolder + "/src/main/resources");
                if (resourceFolderFile.exists()) {
                    try {
                        sourceURLs.add(resourceFolderFile.toURI().toURL());
                    } catch (MalformedURLException e) {
                        logger.warn("Invalid source folder " + sourceFolder + ", cannot convert to URL", e);
                    }
                }
            }
        }
        return sourceURLs.toArray(new URL[sourceURLs.size()]);
    }

    @Override
    public boolean handleSecurity(HttpServletRequest request, HttpServletResponse response) throws IOException {
        return parentHttpContext.handleSecurity(request, response);
    }

    @Override
    public URL getResource(String name) {
        if (sourceURLs != null) {
            for (URL sourceURL : sourceURLs) {
                URL resourceURL = null;
                try {
                    resourceURL = new URL(sourceURL, name);
                } catch (MalformedURLException e) {
                    logger.error("Error in resource URL " + name, e);
                }
                if (urlExists(resourceURL)) {
                    // @todo we could add dynamic registration here if the resource has not yet been registered in the activator
                    return resourceURL;
                }
            }
        }
        return parentHttpContext.getResource(name);
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

    public InputStream getInputStream(URL url) throws IOException {
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


    @Override
    public String getMimeType(String name) {
        return parentHttpContext.getMimeType(name);
    }
}
