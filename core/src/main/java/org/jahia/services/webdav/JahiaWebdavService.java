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
package org.jahia.services.webdav;

import org.jahia.params.ProcessingContext;
import org.jahia.services.usermanager.JahiaUser;
import org.jahia.services.content.JCRNodeWrapper;

/**
 * Created by IntelliJ IDEA.
 * User: toto
 * Date: Jul 7, 2003
 * Time: 12:49:15 PM
 * To change this template use Options | File Templates.
 * @deprecated Use JCRStoreService instead
 */
public interface JahiaWebdavService {
    JCRNodeWrapper getDAVFileAccess(String path, JahiaUser user);
}
