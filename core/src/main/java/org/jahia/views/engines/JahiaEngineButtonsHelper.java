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

package org.jahia.views.engines;

import java.util.ArrayList;
import java.util.List;


/**
 *
 * <p>Title: Helper object for Jahia Engine Control Buttons </p>
 * <p>Description: </p>
 * <p>Copyright: Copyright (c) 2002</p>
 * <p>Company: </p>
 * @author Khue Nguyen
 * @version 1.0
 */
public class JahiaEngineButtonsHelper {

	public static final String JAHIA_ENGINE_BUTTONS_HELPER = "jahiaEngineButtonsHelper";

    private List buttons = new ArrayList();

    public static final String OK_BUTTON = "OK_BUTTON";
    public static final String SAVE_ADD_NEW_BUTTON = "SAVE_ADD_NEW_BUTTON";
    public static final String APPLY_BUTTON = "APPLY_BUTTON";
    public static final String CANCEL_BUTTON = "CANCEL_BUTTON";
    public static final String CLOSE_BUTTON = "CLOSE_BUTTON";
    public static final String REFRESH_MAIN_PAGE_BUTTON = "REFRESH_MAIN_PAGE_BUTTON";

    public static final String AUTHORING_BUTTON = "AUTHORING_BUTTON";
    public static final String RIGHTS_BUTTON = "RIGHTS_BUTTON";
    public static final String FIELDS_RIGHTS_BUTTON = "FIELDS_RIGHTS_BUTTON";
    public static final String VERSIONING_BUTTON = "VERSIONING_BUTTON";
    public static final String LOGS_BUTTON = "LOGS_BUTTON";
    public static final String CONTENT_DEFINITION_BUTTON = "CONTENT_DEFINITION_BUTTON";

    /**
     *
     */
    public JahiaEngineButtonsHelper(){
        // Empty constructor enabling tag lib Java Bean access
    }

    /**
     * Button sould be OK_BUTTON, SAVE_ADD_NEW_BUTTON, APPLY_BUTTON,
     * CANCEL_BUTTON
     *
     * @param button
     */
    public void addButton(String button){
        buttons.add(button);
    }

    public void addOkButton(){
        addButton(OK_BUTTON);
    }

    public void addSaveAddNewButton(){
        addButton(SAVE_ADD_NEW_BUTTON);
    }

    public void addApplyButton(){
        addButton(APPLY_BUTTON);
    }

    public void addCancelButton(){
        addButton(CANCEL_BUTTON);
    }

    public void addCloseButton(){
        addButton(CLOSE_BUTTON);
    }

    // tab buttons
    public void addAuthoringButton(){
        addButton(AUTHORING_BUTTON);
    }

    public void addRightsButton(){
        addButton(RIGHTS_BUTTON);
    }

    public void addFieldsRightsButton(){
        addButton(FIELDS_RIGHTS_BUTTON);
    }

    public void addVersioningButton(){
        addButton(VERSIONING_BUTTON);
    }

    public void addContentDefinitionButton(){
        addButton(CONTENT_DEFINITION_BUTTON);
    }

    public void addLogsButton(){
        addButton(LOGS_BUTTON);
    }

    public List getButtons(){
        return this.buttons;
    }


}

