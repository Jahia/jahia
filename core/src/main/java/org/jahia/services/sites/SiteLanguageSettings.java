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
 * <p>Title: Site language settings</p>
 * <p>Description: contains the information about a language configured on
 * a site. It contains information such as wether the language is activated on
 * a site, the position in the order of preferences for this language, wether
 * it is a mandatory language and finally it's language code, which is either
 * an RFC 3066 language or a language code extension defined in Jahia's XML
 * language definition file.
 * This is usally part of a collection within a JahiaSite object.</p>
 * <p>Copyright: Copyright (c) 2002</p>
 * <p>Company: </p>
 *
 * @author Serge Huber
 * @version 1.0
 */

public class SiteLanguageSettings implements Serializable {

    private int ID;
    private int siteID;
    private boolean activated;
    private int rank;
    private boolean mandatory;
    private String code;
    private boolean inPersistantStorage = false;
    private boolean dataModified = false;

    /**
     * General bean constructor, with most of the values represented except
     * the internal dataModified boolean. This is used notably by the
     * persistance manager for these objects.
     *
     * @param ID                        identifier of the language setting in the database. If we
     *                                  are creating new objects not yet existing the in the database, this
     *                                  value will be ignored.
     * @param siteID                    the site identifier this object is attached to.
     * @param code                      the language code identifier for which to store the settings
     * @param activated                 a boolean that specifies whether this language is
     *                                  active for the specified site
     * @param rank                      the rank importance of this language. The lower the value
     *                                  the higher the rank
     * @param mandatory                 specifies whether this language is mandatory for the
     *                                  specified site (influences the validation process of a page)
     * @param existsInPersistantStorage a boolean that allows to indicate
     *                                  whether this object comes from the persistance manager. If you only
     *                                  need to create objects that are not yet existing in the storage space,
     *                                  we suggest the usage of the other constructor.
     */
    public SiteLanguageSettings (int ID, int siteID, String code,
                                 boolean activated, int rank,
                                 boolean mandatory,
                                 boolean existsInPersistantStorage) {
        this.ID = ID;
        this.siteID = siteID;
        this.code = code;
        this.activated = activated;
        this.rank = rank;
        this.mandatory = mandatory;
        this.inPersistantStorage = existsInPersistantStorage;
    }

    /**
     * See the general constructor for details. Basically this alternative
     * constructor is used when needed to create objects that don't yet exist
     * in the database and that will be stored there later. This is why it has
     * no ID or existsInPersistantStorage value
     *
     * @param siteID    the site identifier this object is attached to.
     * @param code      the language code identifier for which to store the settings
     * @param activated a boolean that specifies whether this language is
     *                  active for the specified site
     * @param rank      the rank importance of this language. The lower the value
     *                  the higher the rank
     * @param mandatory specifies whether this language is mandatory for the
     *                  specified site (influences the validation process of a page)
     */
    public SiteLanguageSettings (int siteID, String code,
                                 boolean activated, int rank,
                                 boolean mandatory) {
        this (-1, siteID, code, activated, rank, mandatory, false);
    }


    /**
     * Sets the database ID for the entry. This is used to when working with
     * objects that don't exist yet in the persistant store. Basically we can
     * create the object with a fake ID (ie -1) and then the backend system
     * calls this method to set the ID to it's real value once it is stored.
     *
     * @param ID an integer representing the database id.
     */
    public void setID (int ID) {
        // this.dataModified = true;
        this.ID = ID;
    }

    /**
     * Retrieve the language setting database ID.
     *
     * @return an integer containing the database ID for this language setting
     */
    public int getID () {
        return ID;
    }

    /**
     * Retrieve the site identifier for this language setting.
     *
     * @return an integer containing the site identifier for this language
     *         setting entry.
     */
    public int getSiteID () {
        return siteID;
    }

    /**
     * Returns the language code for this language setting entry.
     *
     * @return a String object containing a language code (by default RFC 3066
     *         language codes but could be a Jahia extension) representing the language
     *         we are configuring
     */
    public String getCode () {
        return code;
    }

    /**
     * Returns true if the language is activated
     *
     * @return true if language is activated
     */
    public boolean isActivated () {
        return activated;
    }

    /**
     * Returns the rank for this language setting. The rank setting allows
     * to specify the order of preference for site languages, and therefore
     * allowing to fall back to other languages if content doesn't exist in a
     * language, and also specifies the default language. The lower the rank,
     * the more important the language (0=most important, ....)
     *
     * @return an integer (0-based) representing the rank of the language
     */
    public int getRank () {
        return rank;
    }

    /**
     * Sets the rank for this language entry. The rank setting allows
     * to specify the order of preference for site languages, and therefore
     * allowing to fall back to other languages if content doesn't exist in a
     * language, and also specifies the default language. The lower the rank,
     * the more important the language (0=most important, ....)
     *
     * @param rank an integer (0-based) representing the rank we set for this
     *             language. No verification on duplicate rank values is made here so make
     *             sure it is unique before setting this value.
     */
    public void setRank (int rank) {
        this.dataModified = true;
        this.rank = rank;
    }

    /**
     * Returns true if the language is mandatory, meaning that before the page
     * may be validated that all the field and containers must have content
     * defined for this language.
     *
     * @return true if the language is mandatory on this site.
     */
    public boolean isMandatory () {
        return mandatory;
    }

    /**
     * Returns true if the data contained in this bean comes from the persistant
     * storage values. It is not changed if the data is modified. For this
     * information check out the isDataModified method.
     *
     * @return true if the bean comes from the persistant storage, false
     *         otherwise.
     */
    public boolean isInPersistantStorage () {
        return inPersistantStorage;
    }

    /**
     * Returns true if the data has been modified since the initial call to
     * the constructor.
     *
     * @return true if the data has been modified.
     */
    public boolean isDataModified () {
        return dataModified;
    }

    /**
     * Allows to set the internal bean status to note that it has now been
     * updated in the persistant storage and therefore is synchronized.
     *
     * @param dataModified set the value to true if the data has been
     *                     synchronized with the persistant storage.
     */
    public void setDataModified (boolean dataModified) {
        this.dataModified = dataModified;
    }

    /**
     * Sets the activation property of a language. If a language is active
     * it means that users can browse, add content, etc in that language.
     *
     * @param activated a boolean true if the language is active
     */
    public void setActivated (boolean activated) {
        this.dataModified = true;
        this.activated = activated;
    }

    /**
     * Sets the mandatory property for a language. A mandatory language means
     * that all the content on a page must exist in a mandatory language before
     * it can be published
     *
     * @param mandatory a boolean. true means the language is mandatory.
     */
    public void setMandatory (boolean mandatory) {
        this.dataModified = true;
        this.mandatory = mandatory;
    }

    public String toString() {
        final StringBuffer buff = new StringBuffer();
        buff.append("SiteLanguageSettings: code ");
        buff.append(code);
        buff.append(", mandatory " );
        buff.append(mandatory);
        buff.append(", active ");
        buff.append(activated);
        return buff.toString();
    }

}