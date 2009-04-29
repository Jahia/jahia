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

import org.jahia.content.TreeOperationResult;

/**
 * Created by IntelliJ IDEA.
 * Date: 15 nov. 2005 - 13:09:45
 *
 * @author toto
 * @version $Id$
 */
public class ImportResult extends TreeOperationResult {
    private static final long serialVersionUID = -5987706993483879944L;

    public ImportResult() {
    }

    public ImportResult(int initialStatus) {
        super(initialStatus);
    }
}
/**
 *$Log $
 */