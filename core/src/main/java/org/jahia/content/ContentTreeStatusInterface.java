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

public interface ContentTreeStatusInterface {

    /**
     * Continue processing the next Content object or not
     * @return
     */
    public abstract boolean continueWithNextContentObject();

    /**
     * Change the continue processing the next Content object or not status
     */
    public abstract void setContinueWithNextContentObject(boolean value);

    /**
     * Returns true if we should continue after processing all childs
     * @return
     */
    public abstract boolean continueAfterChilds();

    /**
     * Change the continue continueAfterChilds status
     * @return
     */
    public abstract void setContinueAfterChilds(boolean value);

    /**
     * Returns true if we should process the child of the last ContentObject
     * @return
     */
    public abstract boolean continueWithChilds();

    /**
     * Change the continueWithChilds status
     * @return
     */
    public abstract void setContinueWithChilds(boolean value);

}