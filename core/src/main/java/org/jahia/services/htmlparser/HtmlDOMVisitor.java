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
 package org.jahia.services.htmlparser;

import org.w3c.dom.Document;

/**
 *
 * <p>Title: Html DOM Visitor</p>
 * <p>Description: </p>
 * <p>Copyright: Copyright (c) 2002</p>
 * <p>Company: </p>
 * @author Khue Nguyen
 * @version 1.0
 */
public interface HtmlDOMVisitor {

    /**
     * let the visitor initiate itself
     *
     * @param siteId , current site
     * @return
     */
    public abstract void init(int siteId);

    /**
     * process the Document build by the HtmlParser from parsing the Html content.
     *
     * @param doc
     * @return
     */
    public abstract Document parseDOM(Document doc);

}