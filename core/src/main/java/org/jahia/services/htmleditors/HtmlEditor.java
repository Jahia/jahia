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
     * <jahia-home>/htmleditors
     *
     * @return
     */
    public abstract String getBaseDirectory();

    /**
     * Returns the path to the file containing the HTML Editor.
     * This file will be included in Content Editor View ( Jahia Update Engine ).
     * The path must be relative to Jahia's HtmlEditor root path:
     *
     * <jahia-home>/htmleditors
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