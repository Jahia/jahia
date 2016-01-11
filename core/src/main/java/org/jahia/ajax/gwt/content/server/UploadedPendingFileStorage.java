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
