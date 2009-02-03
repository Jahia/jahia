/**
 * 
 * This file is part of Jahia: An integrated WCM, DMS and Portal Solution
 * Copyright (C) 2002-2009 Jahia Limited. All rights reserved.
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
 * in Jahia's FLOSS exception. You should have recieved a copy of the text
 * describing the FLOSS exception, and it is also available here:
 * http://www.jahia.com/license"
 * 
 * Commercial and Supported Versions of the program
 * Alternatively, commercial and supported versions of the program may be used
 * in accordance with the terms contained in a separate written agreement
 * between you and Jahia Limited. If you are unsure which license is appropriate
 * for your use, please contact the sales department at sales@jahia.com.
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
