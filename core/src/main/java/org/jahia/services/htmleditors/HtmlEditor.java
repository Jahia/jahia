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

//

package org.jahia.services.htmleditors;

/**
 * <p>Title: HtmlEditor Interface</p>
 * <p>Description: Interface for Html Editor</p>
 * <p>Copyright: Copyright (c) 2002</p>
 * <p>Company: Jahia S.A.R.L</p>
 * @author Khue Nguyen
 * @version 1.0
 */

public interface HtmlEditor {

    /**
     * Returns the unique identifier.
     *
     * @return the unique identifier
     */
    public abstract String getId();

    /**
     * Returns the visual Display Name
     *
     * @return the visual Display name
     */
    public abstract String getDisplayName();

    /**
     * Returns the base directory relative to
     *
     * <jahia-home>/jsp/jahia/htmleditors
     *
     * @return
     */
    public abstract String getBaseDirectory();

    /**
     * Returns the path to the file containing the HTML Editor.
     * This file will be included in Content Editor View ( Jahia Update Engine ).
     * The path must be relative to Jahia's HtmlEditor root path:
     *
     * <jahia-home>/jsp/jahia/htmleditors
     *
     * @return
     */
    public abstract String getIncludeFile();

    /**
     * Returns true if this Editor can run given Client Capabilities.
     *
     * @param clientCapabilities
     * @return true if this Editor can run for the given ClientCapabilities
     */
    public abstract boolean isClientCapable(ClientCapabilities clientCapabilities);

    /**
     * Returns true if this Editor is authorized for a given site.
     *
     * @param siteID
     * @return true if the given site has autorization to use this Editor
     */
    public abstract boolean isSiteAuthorized(int siteID);

    /**
     * Returns true if this Editor support CSS.
     *
     * @return true if CSS selection should be displayed for this CSS
     */
    public abstract boolean enableCSS();

}