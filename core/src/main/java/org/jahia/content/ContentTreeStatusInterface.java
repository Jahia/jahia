/**
 * This file is part of Jahia: An integrated WCM, DMS and Portal Solution
 * Copyright (C) 2002-2009 Jahia Solutions Group SA. All rights reserved.
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
 * in Jahia's FLOSS exception. You should have received a copy of the text
 * describing the FLOSS exception, and it is also available here:
 * http://www.jahia.com/license
 *
 * Commercial and Supported Versions of the program
 * Alternatively, commercial and supported versions of the program may be used
 * in accordance with the terms contained in a separate written agreement
 * between you and Jahia Solutions Group SA. If you are unsure which license is appropriate
 * for your use, please contact the sales department at sales@jahia.com.
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