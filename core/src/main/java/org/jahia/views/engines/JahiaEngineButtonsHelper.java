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

