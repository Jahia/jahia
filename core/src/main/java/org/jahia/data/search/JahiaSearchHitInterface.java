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
 package org.jahia.data.search;

import java.util.List;

import org.jahia.content.ObjectKey;
import org.jahia.services.pages.JahiaPage;
import org.jahia.services.search.ParsedObject;

/**
 * Define the search hit interface
 *
 * <p>Title: </p>
 * <p>Description: </p>
 * <p>Copyright: Copyright (c) 2002</p>
 * <p>Company: </p>
 * @author Khue Nguyen
 * @version 1.0
 */
public interface JahiaSearchHitInterface extends Comparable<JahiaSearchHitInterface> {

    public final int UNDEFINED_TYPE = 0;

    public final int PAGE_TYPE = 1;

    public final int FILE_TYPE = 2;

    public final int CONTAINER_TYPE = 3;

    public final int CONTAINERLIST_TYPE = 4;

    public final int FIELD_TYPE = 5;

    public final int WEBDAVFILE_TYPE = 6;

    public abstract String getId();

    public abstract void setId(String id);

    /**
     * Returns the object, which can be a JahiaPage, JahiaContainer ,JahiaFileField or other resource.
     */
    public abstract Object getObject();

    public abstract void setObject(Object object);

    public abstract int getPageId();

    public abstract void setPageId(int pageId);

    public abstract JahiaPage getPage();

    public abstract void setPage(JahiaPage page);

    /**
     * Returns the URL of the object
     */
    public abstract String getURL();

    public abstract void setURL(String URL);

    /**
     * Returns the type of the object
     */
    public abstract int getType();

    public abstract void setType(int type);

    /**
     * Returns the list of locales available for this hit.
     * @return ArrayList
     */
    public abstract List<String> getLanguageCodes();

    public abstract void setLanguageCodes(List<String> languageCodes);

    /**
     * Returns the hit score
     */
    public abstract float getScore();

    public abstract void setScore(float score);

    /**
     * Returns a small teaser about this hit
     */
    public abstract String getTeaser();

    public abstract void setTeaser(String teaser);

    /**
     * Returns a ParsedObject instance of this hit
     * @return ParsedObject
     */
    public abstract ParsedObject getParsedObject();

    public abstract void setParsedObject(ParsedObject parsed);

    public abstract int hashCode();

    public abstract ObjectKey getSearchHitObjectKey();

    public abstract int getSortOrder();

    public void setSortOrder(int sortOrder);
}
