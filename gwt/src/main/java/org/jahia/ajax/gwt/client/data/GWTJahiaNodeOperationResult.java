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
package org.jahia.ajax.gwt.client.data;

import org.jahia.ajax.gwt.client.data.GWTJahiaNodeOperationResultItem;

import java.io.Serializable;
import java.util.List;
import java.util.ArrayList;

/**
 * This contains all the validation errors for a given content object in a given language.
 *
 * @author rfelden
 * @version 12 ao�t 2008 - 09:46:42
 */
public class GWTJahiaNodeOperationResult implements Serializable {

    private List<GWTJahiaNodeOperationResultItem> errorsAndWarnings = new ArrayList<GWTJahiaNodeOperationResultItem>() ;

    /**
     * Default constructor
     */
    public GWTJahiaNodeOperationResult() {}

    public boolean hasErrorsOrWarning() {
        return !errorsAndWarnings.isEmpty();
    }

    public void addErrorOrWarning(GWTJahiaNodeOperationResultItem warning) {
        errorsAndWarnings.add(warning);
    }

    public List<GWTJahiaNodeOperationResultItem> getErrorsAndWarnings() {
        return errorsAndWarnings;
    }


}
