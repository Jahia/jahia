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
// DJ       03.01.2001

package org.jahia.data.search;

import org.jahia.services.search.ParsedObject;

/**
 * This class defines what does a search "hit" looks like.
 * One hit is one page found by jahia containing at least one time the searchstring
 * in one of its fields.
 *
 * @author DJ
 * @see JahiaSearchResult
 * @see org.jahia.engines.search.Search_Engine
 */
public class JahiaContainerSearchHit extends JahiaSearchHit {

    /**
     * @param parsedObject ParsedObject
     */
    public JahiaContainerSearchHit(ParsedObject parsedObject) {
        super(parsedObject);
    }

    public int hashCode() {
        return this.getId().hashCode();
    }

}
