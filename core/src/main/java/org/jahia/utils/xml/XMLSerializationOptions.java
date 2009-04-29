/**
 * Jahia Enterprise Edition v6
 *
 * Copyright (C) 2002-2009 Jahia Solutions Group. All rights reserved.
 *
 * Jahia delivers the first Open Source Web Content Integration Software by combining Enterprise Web Content Management
 * with Document Management and Portal features.
 *
 * The Jahia Enterprise Edition is delivered ON AN "AS IS" BASIS, WITHOUT WARRANTY OF ANY KIND, EITHER EXPRESSED OR
 * IMPLIED.
 *
 * Jahia Enterprise Edition must be used in accordance with the terms contained in a separate license agreement between
 * you and Jahia (Jahia Sustainable Enterprise License - JSEL).
 *
 * If you are unsure which license is appropriate for your use, please contact the sales department at sales@jahia.com.
 */
 package org.jahia.utils.xml;

/**
 * <p>Title: Options to modify XML serialization behavior </p>
 * <p>Description: This class regroups options that allow to change the
 * behavior of the XML export system. Examples include how binary data is
 * handled, if ACLs should be exported along, if sub pages should be processed,
 * etc... </p>
 * <p>Copyright: Copyright (c) 2002</p>
 * <p>Company: </p>
 * @author unascribed
 * @version 1.0
 */

public class XMLSerializationOptions {

    private boolean includingSubPages = true;
    private boolean includingRights = false;
    private boolean embeddingBinary = false;
    private boolean includingAllLanguages = true;
    private boolean includingAllStates = false;
    private boolean includingAllVersions = false;

    public XMLSerializationOptions() {
    }

    /**
     * Specifies whether we should recursively go into all the sub pages to
     * export data. Default is true.
     * @param includingSubPages
     */
    public void setIncludingSubPages(boolean includingSubPages) {
        this.includingSubPages = includingSubPages;
    }

    public boolean isIncludingSubPages() {
        return includingSubPages;
    }

    /**
     * Specifies whether we should include ACL rights in the XML export stream.
     * Default is false.
     * @param includingRights
     */
    public void setIncludingRights(boolean includingRights) {
        this.includingRights = includingRights;
    }

    public boolean isIncludingRights() {
        return includingRights;
    }

    /**
     * This option specifies whether the XML file generated should include
     * binary data (application WARs, file field data) directly into the XML
     * file itself or if it should include URL links to the actual data.
     * Default is false.
     * @param embeddingBinary a boolean that specifies whether we should
     * embed the binary data into the XML stream.
     */
    public void setEmbeddingBinary(boolean embeddingBinary) {
        this.embeddingBinary = embeddingBinary;
    }

    public boolean isEmbeddingBinary() {
        return embeddingBinary;
    }

    /**
     * Specifies whether we should include all languages when exporting XML
     * data
     * @param includingAllLanguages
     */
    public void setIncludingAllLanguages(boolean includingAllLanguages) {
        this.includingAllLanguages = includingAllLanguages;
    }
    public boolean isIncludingAllLanguages() {
        return includingAllLanguages;
    }

    /**
     * Specifies whether we should include all the workflow states (only
     * active and staging) when outputting the values. Default is false and
     * will only export the active state.
     * @param includingAllStates
     */
    public void setIncludingAllStates(boolean includingAllStates) {
        this.includingAllStates = includingAllStates;
    }
    public boolean isIncludingAllStates() {
        return includingAllStates;
    }

    /**
     * Specifies whether we should include all the versioned values in the
     * XML export stream. Default is false.
     * @param includingAllVersions
     */
    public void setIncludingAllVersions(boolean includingAllVersions) {
        this.includingAllVersions = includingAllVersions;
    }
    public boolean isIncludingAllVersions() {
        return includingAllVersions;
    }
}