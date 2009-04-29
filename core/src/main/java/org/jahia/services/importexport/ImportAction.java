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
package org.jahia.services.importexport;

import java.util.Collections;

import org.jahia.content.ContentObjectKey;
import org.jahia.services.scheduler.ProcessAction;

/**
 * Created by IntelliJ IDEA.
 * User: toto
 * Date: Nov 24, 2008
 * Time: 6:46:11 PM
 * To change this template use File | Settings | File Templates.
 */
public class ImportAction extends ProcessAction {

    public ImportAction(ContentObjectKey key, String lang, String action) {
        super(key, Collections.singleton(lang), action);
    }

}
