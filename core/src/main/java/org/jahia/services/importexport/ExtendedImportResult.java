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

import java.util.Map;
import java.util.HashMap;

/**
 * Created by IntelliJ IDEA.
 * User: toto
 * Date: 5 juil. 2007
 * Time: 17:54:17
 * To change this template use File | Settings | File Templates.
 */
public class ExtendedImportResult extends ImportResult {
    private static final long serialVersionUID = 1L;

    private Map pidMapping = new HashMap();

    public ExtendedImportResult() {
    }

    public ExtendedImportResult(int initialStatus) {
        super(initialStatus);
    }

    public void addPidMapping(int oldPid, int newPid) {
        pidMapping.put(new Integer(oldPid), new Integer(newPid));
    }

    public Map getPidMapping() {
        return pidMapping;
    }
}
