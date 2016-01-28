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
package org.jahia.ajax.gwt.content.server;

import java.io.InputStream;

/**
 * A storage where files temporarily live during multiple steps upload actions when a file is uploaded first and then waits for some processing to start
 * (typically by the end user).
 * <p>
 * Files belonging to different HTTP sessions must be isolated, so that specifically equally named files uploaded by different sessions could co-exist in
 * the storage. However, file name must be unique within a single session: we assume a single user won't upload multiple equally named files simultaneously.
 * <p>
 * In a cluster setup, the storage must be shared between cluster nodes, so that different steps of the upload action could be performed by different
 * cluster nodes even with non-sticky sessions used.
 */
public interface UploadedPendingFileStorage {

    /**
     * Put file uploaded by specific HTTP session; overwrite in case a file with the same name already exists in the storage
     * @param sessionID HTTP session ID
     * @param name File name
     * @param contentType Content type
     * @param contentStream File bytes stream
     */
    void put(String sessionID, String name, String contentType, InputStream contentStream);

    /**
     * Get file previously uploaded by specific HTTP session, by name
     * @param sessionID HTTP session ID
     * @param name File name
     * @return Given file presentation
     */
    PendingFile get(String sessionID, String name);

    /**
     * Remove file previously uploaded by specific HTTP session, by name
     * @param sessionID HTTP session ID
     * @param name File name
     */
    void remove(String sessionID, String name);

    /**
     * Remove all files previously uploaded by specific HTTP session, if any.
     * @param sessionID HTTP session ID
     */
    void removeIfExists(String sessionID);

    /**
     * A file pending further processing in the storage
     */
    interface PendingFile {

        /**
         * @return ID of the HTTP session that uploaded the file
         */
        String getSessionID();

        /**
         * @return File name
         */
        String getName();

        /**
         * @return File content type
         */
        String getContentType();

        /**
         * @return File bytes stream
         */
        InputStream getContentStream();
    }
}
