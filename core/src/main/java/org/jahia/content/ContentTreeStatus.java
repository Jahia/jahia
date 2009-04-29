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
 package org.jahia.content;


/**
 * <p>Title: Class used to keep some information status when traversing the Tree.</p>
 * <p>Copyright: Copyright (c) 2002</p>
 * <p>Company: </p>
 * @author Khue Nguyen
 * @version 1.0
 */

public class ContentTreeStatus implements ContentTreeStatusInterface{

    private boolean continueWithNextContentObject = true;
    private boolean continueWithChilds = true;
    private boolean continueAfterChilds = true;

    public ContentTreeStatus(){
    }

    /**
     * Returns true if we should continue processing the next content object
     * @return
     */
    public boolean continueWithNextContentObject(){
        return this.continueWithNextContentObject;
    }

    /**
     * Change the continue with next content object status
     * @return
     */
    public void setContinueWithNextContentObject(boolean value){
        this.continueWithNextContentObject = value;
    }

    /**
     * Returns true if we should continue after processing all childs
     * @return
     */
    public boolean continueAfterChilds(){
        return this.continueAfterChilds;
    }

    /**
     * Change the continue continueAfterChilds status
     * @return
     */
    public void setContinueAfterChilds(boolean value){
        this.continueAfterChilds = value;
    }

    /**
     * Returns true if we should process the child of the last ContentObject
     * @return
     */
    public boolean continueWithChilds(){
        return this.continueWithChilds;
    }

    public void setContinueWithChilds(boolean value){
        this.continueWithChilds = value;
    }
}