/**
 * This file is part of Jahia, next-generation open source CMS:
 * Jahia's next-generation, open source CMS stems from a widely acknowledged vision
 * of enterprise application convergence - web, search, document, social and portal -
 * unified by the simplicity of web content management.
 *
 * For more information, please visit http://www.jahia.com.
 *
 * Copyright (C) 2002-2012 Jahia Solutions Group SA. All rights reserved.
 *
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the GNU General Public License
 * as published by the Free Software Foundation; either version 2
 * of the License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program; if not, write to the Free Software
 * Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston, MA 02110-1301, USA.
 *
 * As a special exception to the terms and conditions of version 2.0 of
 * the GPL (or any later version), you may redistribute this Program in connection
 * with Free/Libre and Open Source Software ("FLOSS") applications as described
 * in Jahia's FLOSS exception. You should have received a copy of the text
 * describing the FLOSS exception, and it is also available here:
 * http://www.jahia.com/license
 *
 * Commercial and Supported Versions of the program (dual licensing):
 * alternatively, commercial and supported versions of the program may be used
 * in accordance with the terms and conditions contained in a separate
 * written agreement between you and Jahia Solutions Group SA.
 *
 * If you are unsure which license is appropriate for your use,
 * please contact the sales department at sales@jahia.com.
 */

package org.jahia.services.content.impl.external;

import javax.jcr.ItemNotFoundException;
import javax.jcr.PathNotFoundException;
import java.util.List;
import java.util.Map;

/**
 * Interface to define an external DataSource to handle (@link org.jahia.services.content.impl.external.ExternalData}
 * a DataSource is used to declare a {@link org.jahia.services.content.JCRStoreProvider}
 * This is a simple way to create a new JCR Provider
 */

public interface ExternalDataSource {
    boolean isSupportsUuid();

    /**
     * @return list of supported nodetypes
     */
    List<String> getSupportedNodeTypes();

    /**
     * identifier is unique for an ExternalData
     * @param identifier
     * @return ExternalData defined by the identifier
     * @throws ItemNotFoundException
     */
    ExternalData getItemByIdentifier(String identifier) throws ItemNotFoundException;

    /**
     * As getItemByIdentifier, get an ExternalData by its path
     * @param path
     * @return ExternalData
     * @throws PathNotFoundException
     */
    ExternalData getItemByPath(String path) throws PathNotFoundException;

    /**
     * @param path path where to get children
     * @return list of paths as String
     */
    List<String> getChildren(String path);

    public interface LazyProperty {
        String[] getPropertyValues(String path, String propertyName) throws PathNotFoundException;
    }

    /**
     * If implemented, this interface allow and defines writing
     */
    public interface Writable {
        /**
         * saves the data
         * @param data ExternalData to save
         * @throws PathNotFoundException
         */
        void saveItem(ExternalData data) throws PathNotFoundException;

        /**
         * moves ExternalData from oldPath to newPath
         * @param oldPath  source path
         * @param newPath  destination path
         * @throws PathNotFoundException
         */
        void move(String oldPath, String newPath) throws PathNotFoundException;

        /**
         * Delete an item
         * @param path path of the item to delete
         * @throws PathNotFoundException
         */
        void removeItemByPath(String path) throws PathNotFoundException;

        void order(String path, List<String> children) throws PathNotFoundException;
    }

    /**
     * If implemented, this interface allow and defines search
     */
    public interface Searchable {
        List<String> search(String basePath, String type, Map<String, String> constraints, String orderBy, int limit);
    }


}
