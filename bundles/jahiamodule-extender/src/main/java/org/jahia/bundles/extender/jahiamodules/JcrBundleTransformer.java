/**
 * ==========================================================================================
 * =                   JAHIA'S DUAL LICENSING - IMPORTANT INFORMATION                       =
 * ==========================================================================================
 *
 *                                 http://www.jahia.com
 *
 *     Copyright (C) 2002-2016 Jahia Solutions Group SA. All rights reserved.
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
package org.jahia.bundles.extender.jahiamodules;

import org.jahia.services.content.JCRCallback;
import org.jahia.services.content.JCRNodeWrapper;
import org.jahia.services.content.JCRSessionWrapper;
import org.jahia.services.content.JCRTemplate;
import org.osgi.service.url.AbstractURLStreamHandlerService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.jcr.RepositoryException;
import java.io.*;
import java.net.URL;
import java.net.URLConnection;

/**
 * JCR bundle protocol transformer
 */
public class JcrBundleTransformer extends AbstractURLStreamHandlerService{

    /**
     * A wrapper class for the underlying {@link URLConnection} to perform Jcr protocol bundle file path resolution
     */
    private static class TransformedURLConnection extends URLConnection {

        static Logger logger = LoggerFactory.getLogger(Activator.class);

        /**
         * Initializes an instance of this class.
         * 
         * @param url
         *            the URL to be transformed
         */
        protected TransformedURLConnection(URL url) {
            super(url);
        }

        @Override
        public void connect() throws IOException {
            // Do nothing
        }

        @Override
        public InputStream getInputStream() throws IOException {
            String file = url.getFile();
            final String jcrPath = getJcrPath(file);
            try {
                final InputStream result = (InputStream) JCRTemplate.getInstance().doExecuteWithSystemSession(new JCRCallback<Object>() {
                    @Override
                    public Object doInJCR(JCRSessionWrapper session) throws RepositoryException {
                        JCRNodeWrapper pathNode = session.getNode(jcrPath);
                        return pathNode.getFileContent().downloadFile();
                    }
                });
                return result;
            } catch (RepositoryException e) {
                logger.warn("Couldn't resolve the jcr: protocol path for : "+ url.getFile() );
                return  null;
            }
        }

        private String getJcrPath(String file) {
            String groupId = file.substring(0,file.indexOf("/"));
            String bundleDefinition = file.substring(file.indexOf("/")+1,file.length());
            String[] packageFolders = groupId.split("\\.");
            String[] bundleDefinitions = bundleDefinition.split("/");
            String bundleName = bundleDefinitions[0];
            String bundleVersion = bundleDefinitions[1];
            StringBuilder jcrPathBuilder = new StringBuilder("/module-management/bundles/");
            for (int i = 0; i < packageFolders.length; i++) {
                jcrPathBuilder.append(packageFolders[i]).append("/");
            }
            jcrPathBuilder.append(bundleName).append("/").append(bundleVersion).append("/").append
                    (bundleName).append("-").append(bundleVersion).append(".jar");
            jcrPathBuilder.toString();
            return  jcrPathBuilder.toString();
        }
    }


    @Override
    public URLConnection openConnection(URL url) throws IOException {
        return new TransformedURLConnection(url);
    }
}
