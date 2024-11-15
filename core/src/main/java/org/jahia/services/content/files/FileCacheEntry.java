/*
 * ==========================================================================================
 * =                   JAHIA'S DUAL LICENSING - IMPORTANT INFORMATION                       =
 * ==========================================================================================
 *
 *                                 http://www.jahia.com
 *
 *     Copyright (C) 2002-2024 Jahia Solutions Group SA. All rights reserved.
 *
 *     THIS FILE IS AVAILABLE UNDER TWO DIFFERENT LICENSES:
 *     1/Apache2 OR 2/JSEL
 *
 *     1/ Apache2
 *     ==================================================================================
 *
 *     Copyright (C) 2002-2023 Jahia Solutions Group SA. All rights reserved.
 *
 *     Licensed under the Apache License, Version 2.0 (the "License");
 *     you may not use this file except in compliance with the License.
 *     You may obtain a copy of the License at
 *
 *       http://www.apache.org/licenses/LICENSE-2.0
 *
 *     Unless required by applicable law or agreed to in writing, software
 *     distributed under the License is distributed on an "AS IS" BASIS,
 *     WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *     See the License for the specific language governing permissions and
 *     limitations under the License.
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
package org.jahia.services.content.files;

import java.io.Serializable;
import java.util.Collections;
import java.util.List;

import javax.jcr.Binary;

/**
 * Represents an entry in the file cache.
 *
 * @author Sergiy Shyrkov
 */
public class FileCacheEntry implements Serializable {

    private static final long serialVersionUID = 1233428918424160871L;

    private transient Binary binary;

    private long contentLength;

    private byte[] data;

    private String eTag;

    private long lastModified;

    private String mimeType;

    private List<String> nodeTypes = Collections.emptyList();

    private String identifier;

    private String fileName;

    /**
     * Initializes an instance of this class.
     *
     * @param eTag
     * @param mimeType
     * @param contentLength
     * @param lastModified
     */
    public FileCacheEntry(String eTag, String mimeType, long contentLength, long lastModified) {
        super();
        this.eTag = eTag;
        this.mimeType = mimeType;
        this.contentLength = contentLength;
        this.lastModified = lastModified;
    }
    /**
     * Initializes an instance of this class.
     *
     * @param eTag
     * @param mimeType
     * @param contentLength
     * @param lastModified
     * @param nodeTypes
     */
    public FileCacheEntry(String eTag, String mimeType, long contentLength, long lastModified, String identifier, List<String> nodeTypes) {
        this(eTag, mimeType, contentLength, lastModified);
        this.identifier = identifier;
        this.nodeTypes = nodeTypes;
    }

    public Binary getBinary() {
        return binary;
    }

    public long getContentLength() {
        return contentLength;
    }

    public byte[] getData() {
        return data;
    }

    public String getETag() {
        return eTag;
    }

    public long getLastModified() {
        return lastModified;
    }

    public String getMimeType() {
        return mimeType;
    }

    public void setBinary(Binary binary) {
        this.binary = binary;
    }

    public void setData(byte[] data) {
        this.data = data;
    }

    public List<String> getNodeTypes() {
        return nodeTypes;
    }

    public void setNodeTypes(List<String> nodeTypes) {
        this.nodeTypes = nodeTypes;
    }

    public String getIdentifier() {
        return identifier;
    }

    public void setFileName(String fileName) {
        this.fileName = fileName;
    }

    public String getFileName() {
        return fileName;
    }
}
