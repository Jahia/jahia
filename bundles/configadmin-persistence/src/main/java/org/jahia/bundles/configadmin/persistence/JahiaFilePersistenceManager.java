/**
 * ==========================================================================================
 * =                   JAHIA'S DUAL LICENSING - IMPORTANT INFORMATION                       =
 * ==========================================================================================
 *
 *                                 http://www.jahia.com
 *
 *     Copyright (C) 2002-2017 Jahia Solutions Group SA. All rights reserved.
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
package org.jahia.bundles.configadmin.persistence;

import java.io.File;
import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.Dictionary;

import org.apache.felix.cm.PersistenceManager;
import org.apache.felix.cm.file.FilePersistenceManager;
import org.osgi.framework.BundleContext;

/**
 * Adjusted {@link PersistenceManager} service for ConfigurationAdmin service.
 * 
 * @author Sergiy Shyrkov
 */
public class JahiaFilePersistenceManager extends FilePersistenceManager {

    public JahiaFilePersistenceManager(BundleContext bundleContext, String location) {
        super(bundleContext, location);
    }

    @SuppressWarnings({ "unchecked", "rawtypes" })
    @Override
    public void store(String pid, Dictionary props) throws IOException {
        long timerstamp = getTimerstamp(props);
        if (timerstamp > 0) {
            props.put("felix.fileinstall.source.timestamp", ""+ timerstamp);
        }
        super.store(pid, props);
    }

    @SuppressWarnings("rawtypes")
    private long getTimerstamp(Dictionary props) {
        long timestamp = 0;
        String fileLocation = (String) props.get("felix.fileinstall.filename");
        if (fileLocation != null && fileLocation.startsWith("file:")) {
            try {
                File source = new File(new URI(fileLocation));
                if (source.exists()) {
                    timestamp = source.lastModified();
                }
            } catch (URISyntaxException e) {
                throw new RuntimeException("Improper file location for property felix.fileinstall.filename: " + fileLocation, e);
            }
        }

        return timestamp;
    }
}
