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
 package org.jahia.services.search.compass;

import org.compass.core.Resource;
import org.jahia.services.search.IndexableDocument;

/**
 * Created by IntelliJ IDEA.
 *
 * Build a Compass Resource for a given IndexableDocument
 *
 * User: hollis
 * Date: 18 janv. 2006
 * Time: 16:00:47
 * To change this template use File | Settings | File Templates.
 */
public interface CompassResourceConverter {

    /**
     * Returns a Compass Resource for a given IndexableDocument
     *
     * @param doc
     * @return
     */
    public Resource getResourceFromIndexableDocument(IndexableDocument doc);

}
