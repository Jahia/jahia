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

package org.jahia.services.fields;

import org.jahia.exceptions.JahiaException;
import org.jahia.params.ProcessingContext;
import org.jahia.data.ConnectionTypes;
import org.jahia.services.version.ContentObjectEntryState;
import org.jahia.services.version.EntrySaveRequest;

import java.util.List;
import java.util.Map;

/**
 * Created by IntelliJ IDEA.
 * User: Xavier Lawrence
 * Date: 10 oct. 2006
 * Time: 16:27:55
 * To change this template use File | Settings | File Templates.
 */
public class ContentColorField extends ContentSmallTextField {

    protected ContentColorField(Integer ID,
                                Integer jahiaID,
                                Integer pageID,
                                Integer ctnid,
                                Integer fieldDefID,
                                Integer fieldType,
                                Integer connectType,
                                Integer aclID,
                                List<ContentObjectEntryState> activeAndStagingEntryStates,
                                Map<ContentObjectEntryState, String> activeAndStagedDBValues) throws JahiaException {
        super(ID, jahiaID, pageID, ctnid, fieldDefID, fieldType, connectType, aclID, activeAndStagingEntryStates,
                activeAndStagedDBValues);
    }

    public static ContentColorField createColorField(int siteID,
                                                     int pageID,
                                                     int containerID,
                                                     int fieldDefID,
                                                     int parentAclID,
                                                     int aclID,
                                                     String text,
                                                     ProcessingContext jParams)
            throws JahiaException {
        ContentColorField result = (ContentColorField) ContentField.createField(siteID,
                pageID,
                containerID, fieldDefID,
                ContentFieldTypes.COLOR,
                ConnectionTypes.LOCAL,
                parentAclID, aclID);
        EntrySaveRequest saveRequest = new EntrySaveRequest(jParams.getUser(),
                jParams.getLocale().toString(), true);

        result.setText(text, saveRequest);
        return result;
    }

    /**
     * Is this kind of field shared (i.e. not one version for each language, but one version for every language)
     */
    public boolean isShared() {
        return true;
    }
}
