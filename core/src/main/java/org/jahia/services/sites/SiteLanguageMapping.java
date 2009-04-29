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
package org.jahia.services.sites;

import java.io.Serializable;

/**
 * <p>Title: Site language mapping bean</p>
 * <p>Description: This bean represents an entry for a language mapping in a
 * site. A language mapping allows to define "routes" to resolve languages.
 * A typical example of a language mapping could be :</p>
 * <p> fr -> fr_FR </p>
 * <p>This is particularly interesting for Jahia since the needs of configuraiton
 * for the languages might evolve over time, and there is no need to redefine
 * all the content when needed to do simple mappings such as this one.
 * </p>
 * <p>Copyright: Copyright (c) 2002</p>
 * <p>Company: </p>
 *
 * @author Serge Huber
 * @version 1.0
 */

public class SiteLanguageMapping implements Serializable {
    private int id = -1;
    private String fromLanguageCode;
    private String toLanguageCode;
    private int siteID;

    /**
     * Internal constructor used only for persistance purposes. For new objects
     * to be serialized, check the constructor that doesn't take an ID.
     *
     * @param id               the identifier from the database
     * @param siteID           the identifier for the site in which this mapping
     *                         resides.
     * @param fromLanguageCode the language code from which we want to resolve
     * @param toLanguageCode   the language code to which we want the
     *                         fromLanguageCode to point.
     */
    public SiteLanguageMapping (int id, int siteID, String fromLanguageCode,
                                   String toLanguageCode) {
        this.id = id;
        this.siteID = siteID;
        this.fromLanguageCode = fromLanguageCode;
        this.toLanguageCode = toLanguageCode;
    }

    /**
     * Public constructor destined for building this bean first in memory to
     * be saved in the database later.
     *
     * @param siteID           the identifier for the site in which this mapping
     *                         resides.
     * @param fromLanguageCode the language code from which we want to resolve
     * @param toLanguageCode   the language code to which we want the
     *                         fromLanguageCode to point.
     */
    public SiteLanguageMapping (int siteID, String fromLanguageCode, String toLanguageCode) {
        this.siteID = siteID;
        this.fromLanguageCode = fromLanguageCode;
        this.toLanguageCode = toLanguageCode;
    }

    /**
     * Returns the value of the database identifier for this bean.
     *
     * @return the integer corresponding to the ID in the database, OR -1 if
     *         this bean exists only in memory.
     */
    public int getId () {
        return id;
    }

    /**
     * Used by the persistance manager to set an ID for a bean that was
     * previously created in memory. From this call on this bean exists in
     * the persistant store
     *
     * @param id the integer corresponding to the database identifier
     */
    public void setId (int id) {
        this.id = id;
    }

    /**
     * Set the site identifier this mapping belongs to.
     *
     * @param siteID an integer containing the site identifier for this
     *               mapping.
     */
    public void setSiteID (int siteID) {
        this.siteID = siteID;
    }

    /**
     * Returns the site identifier this mapping belongs to.
     *
     * @return an integer representing the unique site identifier this mapping
     *         belongs to.
     */
    public int getSiteID () {
        return siteID;
    }

    /**
     * Sets the languageCode to map to another languageCode. Use the
     * setToLanguageCode to set the target corresponding to this source
     * language code.
     *
     * @param fromLanguageCode the language code (corresponding to a
     *                         Locale.toString() compliant output) that we want to map for
     */
    public void setFromLanguageCode (String fromLanguageCode) {
        this.fromLanguageCode = fromLanguageCode;
    }

    /**
     * Returns the current "source" language code in this mapping. The format
     * of this String is compliant to the Locale.toString() output.
     *
     * @return a String in the Locale.toString() format corresponding to the
     *         source of the mapping.
     */
    public String getFromLanguageCode () {
        return fromLanguageCode;
    }

    /**
     * Sets the target languageCode for this mapping. This strings format
     * must compliant to the output of Locale.toString().
     *
     * @param toLanguageCode a String containing a Locale.toString() formatted
     *                       language code that will correspond to the target language for the
     *                       from language code defined also in this bean.
     */
    public void setToLanguageCode (String toLanguageCode) {
        this.toLanguageCode = toLanguageCode;
    }

    /**
     * Returns the value of the "target" language code in this mapping
     *
     * @return a String in a Locale.toString() compliant format that represents
     *         the target language code in this mapping.
     */
    public String getToLanguageCode () {
        return toLanguageCode;
    }
}